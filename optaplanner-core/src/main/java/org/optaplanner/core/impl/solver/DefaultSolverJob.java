/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.impl.solver;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverJob;
import org.optaplanner.core.api.solver.SolverStatus;
import org.optaplanner.core.impl.phase.event.PhaseLifecycleListener;
import org.optaplanner.core.impl.phase.scope.AbstractPhaseScope;
import org.optaplanner.core.impl.phase.scope.AbstractStepScope;
import org.optaplanner.core.impl.solver.scope.SolverScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 * @param <ProblemId_> the ID type of a submitted problem, such as {@link Long} or {@link UUID}.
 */
public final class DefaultSolverJob<Solution_, ProblemId_> implements SolverJob<Solution_, ProblemId_>, Callable<Solution_> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSolverJob.class);

    private final DefaultSolverManager<Solution_, ProblemId_> solverManager;
    private final DefaultSolver<Solution_> solver;
    private final ProblemId_ problemId;
    private final Function<? super ProblemId_, ? extends Solution_> problemFinder;
    private final Consumer<? super Solution_> finalBestSolutionConsumer;
    private final BiConsumer<? super ProblemId_, ? super Throwable> exceptionHandler;

    private volatile SolverStatus solverStatus;
    private final CountDownLatch terminatedLatch;
    private final ReentrantLock solverStatusModifyingLock;

    private Future<Solution_> future;

    public DefaultSolverJob(
            DefaultSolverManager<Solution_, ProblemId_> solverManager,
            Solver<Solution_> solver, ProblemId_ problemId,
            Function<? super ProblemId_, ? extends Solution_> problemFinder,
            Consumer<? super Solution_> finalBestSolutionConsumer,
            BiConsumer<? super ProblemId_, ? super Throwable> exceptionHandler) {
        this.solverManager = solverManager;
        this.problemId = problemId;
        if (!(solver instanceof DefaultSolver)) {
            throw new IllegalStateException("Impossible state: solver is not instance of " +
                    DefaultSolver.class.getSimpleName() + ".");
        }
        this.solver = (DefaultSolver<Solution_>) solver;
        this.problemFinder = problemFinder;
        this.finalBestSolutionConsumer = finalBestSolutionConsumer;
        this.exceptionHandler = exceptionHandler;
        solverStatus = SolverStatus.SOLVING_SCHEDULED;
        terminatedLatch = new CountDownLatch(1);
        solverStatusModifyingLock = new ReentrantLock();
    }

    public void setFuture(Future<Solution_> future) {
        this.future = future;
    }

    @Override
    public ProblemId_ getProblemId() {
        return problemId;
    }

    @Override
    public SolverStatus getSolverStatus() {
        return solverStatus;
    }

    @Override
    public Solution_ call() {
        solverStatusModifyingLock.lock();
        if (solverStatus != SolverStatus.SOLVING_SCHEDULED) {
            // This job has been canceled before it started,
            // or it is already solving
            solverStatusModifyingLock.unlock();
            return problemFinder.apply(problemId);
        }
        try {
            solverStatus = SolverStatus.SOLVING_ACTIVE;
            Solution_ problem = problemFinder.apply(problemId);
            // add a phase lifecycle listener that unlock the solver status lock when solving started
            solver.addPhaseLifecycleListener(new UnlockLockPhaseLifecycleListener());
            final Solution_ finalBestSolution = solver.solve(problem);
            if (finalBestSolutionConsumer != null) {
                // TODO consumption should happen on different thread than solver thread
                finalBestSolutionConsumer.accept(finalBestSolution);
            }
            return finalBestSolution;
        } catch (Exception e) {
            exceptionHandler.accept(problemId, e);
            throw new IllegalStateException("Solving failed for problemId (" + problemId + ").", e);
        } finally {
            if (solverStatusModifyingLock.isHeldByCurrentThread()) {
                // release the lock if we have it (due to solver raising an exception before solving starts);
                // This does not make it possible to do a double terminate in terminateEarly because:
                // 1. The case SOLVING_SCHEDULED is impossible (only set to SOLVING_SCHEDULED in constructor,
                //    and it was set it to SolverStatus.SOLVING_ACTIVE in the method)
                // 2. The case SOLVING_ACTIVE only calls solver.terminateEarly, so it effectively does nothing
                // 3. The case NOT_SOLVING does nothing
                solverStatusModifyingLock.unlock();
            }
            solvingTerminated();
        }
    }

    private void solvingTerminated() {
        solverStatus = SolverStatus.NOT_SOLVING;
        solverManager.unregisterSolverJob(problemId);
        terminatedLatch.countDown();
    }

    // TODO Future features
    //    @Override
    //    public void reloadProblem(Function<? super ProblemId_, Solution_> problemFinder) {
    //        throw new UnsupportedOperationException("The solver is still solving and reloadProblem() is not yet supported.");
    //    }

    // TODO Future features
    //    @Override
    //    public void addProblemFactChange(ProblemFactChange<Solution_> problemFactChange) {
    //        solver.addProblemFactChange(problemFactChange);
    //    }

    @Override
    public void terminateEarly() {
        try {
            solverStatusModifyingLock.lock();
            future.cancel(false);
            switch (solverStatus) {
                case SOLVING_SCHEDULED:
                    solvingTerminated();
                    break;
                case SOLVING_ACTIVE:
                    // Indirectly triggers solvingTerminated()
                    solver.terminateEarly();
                    break;
                case NOT_SOLVING:
                    // Do nothing, solvingTerminated() already called
                    break;
                default:
                    throw new IllegalStateException("Unsupported solverStatus (" + solverStatus + ").");
            }
            try {
                // Don't return until bestSolutionConsumer won't be called any more
                terminatedLatch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.warn("The terminateEarly() call is interrupted.", e);
            }
        } finally {
            solverStatusModifyingLock.unlock();
        }
    }

    @Override
    public Solution_ getFinalBestSolution() throws InterruptedException, ExecutionException {
        return future.get();
    }

    @Override
    public Duration getSolvingDuration() {
        SolverScope<Solution_> solverScope = solver.getSolverScope();
        Long startingSystemTimeMillis = solverScope.getStartingSystemTimeMillis();
        if (startingSystemTimeMillis == null) {
            // The solver hasn't started yet
            return Duration.ZERO;
        }
        Long endingSystemTimeMillis = solverScope.getEndingSystemTimeMillis();
        if (endingSystemTimeMillis == null) {
            // The solver hasn't ended yet
            endingSystemTimeMillis = System.currentTimeMillis();
        }
        return Duration.ofMillis(endingSystemTimeMillis - startingSystemTimeMillis);
    }

    /**
     * A listener that unlocks the solverStatusModifyingLock when Solving has started.
     *
     * It to prevent the following scenario caused by unlocking before Solving started:
     *
     * Thread 1:
     * solverStatusModifyingLock.unlock()
     * >solver.solve(...) // executes second
     *
     * Thread 2:
     * case SOLVING_ACTIVE:
     * >solver.terminateEarly(); // executes first
     *
     * The solver.solve() call resets the terminateEarly flag, and thus the solver will not be terminated
     * by the call, which means terminatedLatch will not be decremented, causing Thread 2 to wait forever
     * (at least until another Thread calls terminateEarly again).
     *
     * To prevent Thread 2 from potentially waiting forever, we only unlock the lock after the
     * solvingStarted phase lifecycle event is fired, meaning the terminateEarly flag will not be
     * reset and thus the solver will actually terminate.
     */
    private final class UnlockLockPhaseLifecycleListener implements PhaseLifecycleListener<Solution_> {

        @Override
        public void solvingStarted(SolverScope<Solution_> solverScope) {
            solverStatusModifyingLock.unlock();
        }

        // Do nothing for everything else
        @Override
        public void solvingEnded(SolverScope<Solution_> solverScope) {
            // Do nothing
        }

        @Override
        public void phaseStarted(AbstractPhaseScope<Solution_> phaseScope) {
            // Do nothing
        }

        @Override
        public void stepStarted(AbstractStepScope<Solution_> stepScope) {
            // Do nothing
        }

        @Override
        public void stepEnded(AbstractStepScope<Solution_> stepScope) {
            // Do nothing
        }

        @Override
        public void phaseEnded(AbstractPhaseScope<Solution_> phaseScope) {
            // Do nothing
        }
    }
}
