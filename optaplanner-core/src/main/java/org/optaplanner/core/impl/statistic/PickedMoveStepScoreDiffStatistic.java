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

package org.optaplanner.core.impl.statistic;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.config.solver.metric.SolverMetric;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchPhaseScope;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchStepScope;
import org.optaplanner.core.impl.phase.event.PhaseLifecycleListenerAdapter;
import org.optaplanner.core.impl.phase.scope.AbstractPhaseScope;
import org.optaplanner.core.impl.phase.scope.AbstractStepScope;
import org.optaplanner.core.impl.score.definition.ScoreDefinition;
import org.optaplanner.core.impl.score.director.InnerScoreDirectorFactory;
import org.optaplanner.core.impl.solver.DefaultSolver;

import io.micrometer.core.instrument.Tags;

public class PickedMoveStepScoreDiffStatistic implements SolverStatistic {
    @Override
    @SuppressWarnings("unchecked")
    public void register(Solver<?> solver) {
        DefaultSolver<?> defaultSolver = (DefaultSolver<?>) solver;
        InnerScoreDirectorFactory<?, ?> innerScoreDirectorFactory = defaultSolver.getScoreDirectorFactory();
        SolutionDescriptor<?> solutionDescriptor = innerScoreDirectorFactory.getSolutionDescriptor();
        defaultSolver.addPhaseLifecycleListener(
                new PickedMoveStepScoreDiffStatisticListener(solutionDescriptor.getScoreDefinition()));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static class PickedMoveStepScoreDiffStatisticListener extends PhaseLifecycleListenerAdapter {
        private Score<?> oldStepScore = null;
        private final ScoreDefinition<?> scoreDefinition;
        private final Map<Tags, List<AtomicReference<Number>>> tagsToMoveScoreMap = new ConcurrentHashMap<>();

        public PickedMoveStepScoreDiffStatisticListener(ScoreDefinition scoreDefinition) {
            this.scoreDefinition = scoreDefinition;
        }

        @Override
        public void phaseStarted(AbstractPhaseScope phaseScope) {
            if (phaseScope instanceof LocalSearchPhaseScope) {
                oldStepScore = phaseScope.getStartingScore();
            }
        }

        @Override
        public void phaseEnded(AbstractPhaseScope phaseScope) {
            if (phaseScope instanceof LocalSearchPhaseScope) {
                oldStepScore = null;
            }
        }

        @Override
        public void stepEnded(AbstractStepScope stepScope) {
            if (stepScope instanceof LocalSearchStepScope) {
                localSearchStepEnded((LocalSearchStepScope) stepScope);
            }
        }

        private void localSearchStepEnded(LocalSearchStepScope stepScope) {
            String moveType = stepScope.getStep().getSimpleMoveTypeDescription();
            Score newStepScore = stepScope.getScore();
            Score stepScoreDiff = newStepScore.subtract(oldStepScore);
            oldStepScore = newStepScore;

            SolverMetric.registerScoreMetrics(SolverMetric.PICKED_MOVE_TYPE_STEP_SCORE_DIFF,
                    stepScope.getPhaseScope().getSolverScope().getMetricTags()
                            .and("move.type", moveType),
                    scoreDefinition,
                    tagsToMoveScoreMap,
                    stepScoreDiff);
        }
    }
}
