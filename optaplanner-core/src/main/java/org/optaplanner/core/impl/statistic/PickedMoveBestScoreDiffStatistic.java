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

public class PickedMoveBestScoreDiffStatistic<Solution_, Score_ extends Score<Score_>> implements SolverStatistic<Solution_> {
    @Override
    @SuppressWarnings("unchecked")
    public void register(Solver<Solution_> solver) {
        DefaultSolver<Solution_> defaultSolver = (DefaultSolver<Solution_>) solver;
        InnerScoreDirectorFactory<Solution_, ?> innerScoreDirectorFactory = defaultSolver.getScoreDirectorFactory();
        SolutionDescriptor<Solution_> solutionDescriptor = innerScoreDirectorFactory.getSolutionDescriptor();
        defaultSolver.addPhaseLifecycleListener(
                new PickedMoveBestScoreDiffStatisticListener<Solution_, Score_>(solutionDescriptor.getScoreDefinition()));
    }

    private static class PickedMoveBestScoreDiffStatisticListener<Solution_, Score_ extends Score<Score_>>
            extends PhaseLifecycleListenerAdapter<Solution_> {

        private Score_ oldBestScore = null;
        private final ScoreDefinition<Score_> scoreDefinition;
        private final Map<Tags, List<AtomicReference<Number>>> tagsToMoveScoreMap = new ConcurrentHashMap<>();

        public PickedMoveBestScoreDiffStatisticListener(ScoreDefinition<Score_> scoreDefinition) {
            this.scoreDefinition = scoreDefinition;
        }

        @Override
        public void phaseStarted(AbstractPhaseScope<Solution_> phaseScope) {
            if (phaseScope instanceof LocalSearchPhaseScope) {
                oldBestScore = phaseScope.getBestScore();
            }
        }

        @Override
        public void phaseEnded(AbstractPhaseScope<Solution_> phaseScope) {
            if (phaseScope instanceof LocalSearchPhaseScope) {
                oldBestScore = null;
            }
        }

        @Override
        public void stepEnded(AbstractStepScope<Solution_> stepScope) {
            if (stepScope instanceof LocalSearchStepScope) {
                localSearchStepEnded((LocalSearchStepScope<Solution_>) stepScope);
            }
        }

        @SuppressWarnings("unchecked")
        private void localSearchStepEnded(LocalSearchStepScope<Solution_> stepScope) {
            if (stepScope.getBestScoreImproved()) {
                String moveType = stepScope.getStep().getSimpleMoveTypeDescription();
                Score_ newBestScore = (Score_) stepScope.getScore();
                Score_ bestScoreDiff = newBestScore.subtract(oldBestScore);
                oldBestScore = newBestScore;
                SolverMetric.registerScoreMetrics(SolverMetric.PICKED_MOVE_TYPE_BEST_SCORE_DIFF,
                        stepScope.getPhaseScope().getSolverScope().getMetricTags()
                                .and("move.type", moveType),
                        scoreDefinition,
                        tagsToMoveScoreMap,
                        bestScoreDiff);
            }
        }
    }
}
