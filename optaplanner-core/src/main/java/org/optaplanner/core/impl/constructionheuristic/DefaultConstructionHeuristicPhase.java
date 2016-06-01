/*
 * Copyright 2011 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.impl.constructionheuristic;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.impl.constructionheuristic.decider.ConstructionHeuristicDecider;
import org.optaplanner.core.impl.constructionheuristic.placer.EntityPlacer;
import org.optaplanner.core.impl.constructionheuristic.placer.Placement;
import org.optaplanner.core.impl.constructionheuristic.scope.ConstructionHeuristicPhaseScope;
import org.optaplanner.core.impl.constructionheuristic.scope.ConstructionHeuristicStepScope;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.phase.AbstractPhase;
import org.optaplanner.core.impl.solver.scope.DefaultSolverScope;

/**
 * Default implementation of {@link ConstructionHeuristicPhase}.
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public class DefaultConstructionHeuristicPhase<Solution_> extends AbstractPhase<Solution_>
        implements ConstructionHeuristicPhase<Solution_> {

    protected EntityPlacer entityPlacer;
    protected ConstructionHeuristicDecider decider;

    public void setEntityPlacer(EntityPlacer entityPlacer) {
        this.entityPlacer = entityPlacer;
    }

    public void setDecider(ConstructionHeuristicDecider decider) {
        this.decider = decider;
    }

    @Override
    public String getPhaseTypeString() {
        return "Construction Heuristics";
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void solve(DefaultSolverScope<Solution_> solverScope) {
        ConstructionHeuristicPhaseScope<Solution_> phaseScope = new ConstructionHeuristicPhaseScope<>(solverScope);
        phaseStarted(phaseScope);

        for (Placement placement : entityPlacer) {
            ConstructionHeuristicStepScope<Solution_> stepScope = new ConstructionHeuristicStepScope<>(phaseScope);
            stepStarted(stepScope);
            decider.decideNextStep(stepScope, placement);
            if (stepScope.getStep() == null) {
                if (termination.isPhaseTerminated(phaseScope)) {
                    logger.trace("    Step index ({}), time spent ({}) terminated without picking a nextStep.",
                            stepScope.getStepIndex(),
                            stepScope.getPhaseScope().calculateSolverTimeMillisSpentUpToNow());
                } else if (stepScope.getSelectedMoveCount() == 0L) {
                    logger.warn("    No doable selected move at step index ({}), time spent ({})."
                            + " Terminating phase early.",
                            stepScope.getStepIndex(),
                            stepScope.getPhaseScope().calculateSolverTimeMillisSpentUpToNow());
                } else {
                    throw new IllegalStateException("The step index (" + stepScope.getStepIndex()
                            + ") has selected move count (" + stepScope.getSelectedMoveCount()
                            + ") but failed to pick a nextStep (" + stepScope.getStep() + ").");
                }
                // Although stepStarted has been called, stepEnded is not called for this step
                break;
            }
            doStep(stepScope);
            stepEnded(stepScope);
            phaseScope.setLastCompletedStepScope(stepScope);
            if (termination.isPhaseTerminated(phaseScope)) {
                break;
            }
        }
        phaseEnded(phaseScope);
    }

    private void doStep(ConstructionHeuristicStepScope<Solution_> stepScope) {
        Move nextStep = stepScope.getStep();
        nextStep.doMove(stepScope.getScoreDirector());
        predictWorkingStepScore(stepScope, nextStep);
    }

    @Override
    public void solvingStarted(DefaultSolverScope<Solution_> solverScope) {
        super.solvingStarted(solverScope);
        entityPlacer.solvingStarted(solverScope);
        decider.solvingStarted(solverScope);
    }

    public void phaseStarted(ConstructionHeuristicPhaseScope<Solution_> phaseScope) {
        super.phaseStarted(phaseScope);
        entityPlacer.phaseStarted(phaseScope);
        decider.phaseStarted(phaseScope);
    }

    public void stepStarted(ConstructionHeuristicStepScope<Solution_> stepScope) {
        super.stepStarted(stepScope);
        entityPlacer.stepStarted(stepScope);
        decider.stepStarted(stepScope);
    }

    public void stepEnded(ConstructionHeuristicStepScope<Solution_> stepScope) {
        super.stepEnded(stepScope);
        entityPlacer.stepEnded(stepScope);
        decider.stepEnded(stepScope);
        if (logger.isDebugEnabled()) {
            long timeMillisSpent = stepScope.getPhaseScope().calculateSolverTimeMillisSpentUpToNow();
            logger.debug("    CH step ({}), time spent ({}), score ({}), selected move count ({}),"
                    + " picked move ({}).",
                    stepScope.getStepIndex(), timeMillisSpent,
                    stepScope.getScore(),
                    stepScope.getSelectedMoveCount(),
                    stepScope.getStepString());
        }
    }

    public void phaseEnded(ConstructionHeuristicPhaseScope<Solution_> phaseScope) {
        super.phaseEnded(phaseScope);
        Solution_ newBestSolution = phaseScope.getScoreDirector().cloneWorkingSolution();
        int newBestUninitializedVariableCount = phaseScope.getSolutionDescriptor()
                .countUninitializedVariables(newBestSolution);
        bestSolutionRecaller.updateBestSolution(phaseScope.getSolverScope(),
                newBestSolution, newBestUninitializedVariableCount);
        entityPlacer.phaseEnded(phaseScope);
        decider.phaseEnded(phaseScope);
        phaseScope.endingNow();
        logger.info("Construction Heuristic phase ({}) ended: time spent ({}), best score ({}),"
                        + " score calculation speed ({}/sec), step total ({}).",
                phaseIndex,
                phaseScope.calculateSolverTimeMillisSpentUpToNow(),
                phaseScope.getBestScore(),
                phaseScope.getPhaseScoreCalculationSpeed(),
                phaseScope.getNextStepIndex());
    }

    @Override
    public void solvingEnded(DefaultSolverScope<Solution_> solverScope) {
        super.solvingEnded(solverScope);
        entityPlacer.solvingEnded(solverScope);
        decider.solvingEnded(solverScope);
    }

}
