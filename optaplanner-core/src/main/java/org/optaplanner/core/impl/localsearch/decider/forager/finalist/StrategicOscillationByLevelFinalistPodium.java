/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.localsearch.decider.forager.finalist;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchMoveScope;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchPhaseScope;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchStepScope;

/**
 * Strategic oscillation, works well with Tabu search.
 * @see FinalistPodium
 */
public class StrategicOscillationByLevelFinalistPodium extends AbstractFinalistPodium {

    protected final boolean referenceBestScoreInsteadOfLastStepScore;

    protected Score referenceScore;
    protected Number[] referenceLevelNumbers;

    protected Score finalistScore;
    protected Number[] finalistLevelNumbers;
    protected boolean finalistImprovesUponReference;

    public StrategicOscillationByLevelFinalistPodium(boolean referenceBestScoreInsteadOfLastStepScore) {
        this.referenceBestScoreInsteadOfLastStepScore = referenceBestScoreInsteadOfLastStepScore;
    }

    @Override
    public void stepStarted(LocalSearchStepScope stepScope) {
        super.stepStarted(stepScope);
        referenceScore = referenceBestScoreInsteadOfLastStepScore
                ? stepScope.getPhaseScope().getBestScore()
                : stepScope.getPhaseScope().getLastCompletedStepScope().getScore();
        referenceLevelNumbers = referenceBestScoreInsteadOfLastStepScore
                ? stepScope.getPhaseScope().getBestScore().toLevelNumbers()
                : stepScope.getPhaseScope().getLastCompletedStepScope().getScore().toLevelNumbers();
        finalistScore = null;
        finalistLevelNumbers = null;
        finalistImprovesUponReference = false;
    }

    @Override
    public void addMove(LocalSearchMoveScope moveScope) {
        boolean accepted = moveScope.getAccepted();
        if (finalistIsAccepted && !accepted) {
            return;
        }
        if (accepted && !finalistIsAccepted) {
            finalistIsAccepted = true;
            finalistScore = null;
            finalistLevelNumbers = null;
        }
        Score moveScore = moveScope.getScore();
        Number[] moveLevelNumbers = moveScore.toLevelNumbers();
        int comparison = doComparison(moveScore, moveLevelNumbers);
        if (comparison > 0) {
            finalistScore = moveScore;
            finalistLevelNumbers = moveLevelNumbers;
            finalistImprovesUponReference = (moveScore.compareTo(referenceScore) > 0);
            finalistList.clear();
            finalistList.add(moveScope);
        } else if (comparison == 0) {
            finalistList.add(moveScope);
        }
    }

    private int doComparison(Score moveScore, Number[] moveLevelNumbers) {
        if (finalistScore == null) {
            return 1;
        }
        // If there is an improving move, do not oscillate
        if (!finalistImprovesUponReference && moveScore.compareTo(referenceScore) < 0) {
            for (int i = 0; i < referenceLevelNumbers.length; i++) {
                boolean moveIsHigher = ((Comparable) moveLevelNumbers[i]).compareTo(referenceLevelNumbers[i]) > 0;
                boolean finalistIsHigher = ((Comparable) finalistLevelNumbers[i]).compareTo(referenceLevelNumbers[i]) > 0;
                if (moveIsHigher) {
                    if (finalistIsHigher) {
                        // Both are higher, take the best one but do not ignore higher levels
                        break;
                    } else {
                        // The move has the first level which is higher while the finalist is lower than the reference
                        return 1;
                    }
                } else {
                    if (finalistIsHigher) {
                        // The finalist has the first level which is higher while the move is lower than the reference
                        return -1;
                    } else {
                        // Both are lower, ignore this level
                    }
                }
            }
        }
        return moveScore.compareTo(finalistScore);
    }

    @Override
    public void phaseEnded(LocalSearchPhaseScope phaseScope) {
        super.phaseEnded(phaseScope);
        referenceScore = null;
        referenceLevelNumbers = null;
        finalistScore = null;
        finalistLevelNumbers = null;
    }

}
