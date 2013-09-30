/*
 * Copyright 2013 JBoss Inc
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

package org.optaplanner.benchmark.impl.statistic.stepscore;

import java.util.ArrayList;
import java.util.List;

import org.optaplanner.benchmark.impl.statistic.AbstractSingleStatistic;
import org.optaplanner.benchmark.impl.statistic.SingleStatisticState;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.impl.phase.event.SolverPhaseLifecycleListenerAdapter;
import org.optaplanner.core.impl.phase.step.AbstractStepScope;
import org.optaplanner.core.impl.solver.DefaultSolver;

public class StepScoreSingleStatistic extends AbstractSingleStatistic {

    private final StepScoreSingleStatisticListener listener = new StepScoreSingleStatisticListener();

//    private List<StepScoreSingleStatisticPoint> pointList = new ArrayList<StepScoreSingleStatisticPoint>();
    
    private StepScoreSingleStatisticState state;

    public StepScoreSingleStatistic() {
        this.state = new StepScoreSingleStatisticState();
    }

    public StepScoreSingleStatistic(StepScoreSingleStatisticState state) {
        this.state = state;
    }

    public List<StepScoreSingleStatisticPoint> getPointList() {
        return state.getPointList();
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public void open(Solver solver) {
        ((DefaultSolver) solver).addSolverPhaseLifecycleListener(listener);
    }

    public void close(Solver solver) {
        ((DefaultSolver) solver).removeSolverPhaseLifecycleListener(listener);
    }

    @Override
    public SingleStatisticState getSingleStatisticState() {
        return state;
    }
    
    private class StepScoreSingleStatisticListener extends SolverPhaseLifecycleListenerAdapter {

        @Override
        public void stepEnded(AbstractStepScope stepScope) {
            if (stepScope.hasNoUninitializedVariables()) {
                long timeMillisSpend = stepScope.getPhaseScope().calculateSolverTimeMillisSpend();
                getPointList().add(new StepScoreSingleStatisticPoint(timeMillisSpend, stepScope.getScore()));
            }
        }

    }

}
