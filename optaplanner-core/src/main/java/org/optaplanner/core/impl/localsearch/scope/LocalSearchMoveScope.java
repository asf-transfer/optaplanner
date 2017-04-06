/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.localsearch.scope;

import java.util.Random;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.score.director.ScoreDirector;

/**
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public class LocalSearchMoveScope<Solution_> {

    private final LocalSearchStepScope<Solution_> stepScope;

    private int moveIndex;
    private Move<Solution_> move = null;
    private Move<Solution_> undoMove = null;
    private Score score = null;
    private Boolean accepted = null;

    public LocalSearchMoveScope(LocalSearchStepScope<Solution_> stepScope) {
        this.stepScope = stepScope;
    }

    public LocalSearchStepScope<Solution_> getStepScope() {
        return stepScope;
    }

    public int getMoveIndex() {
        return moveIndex;
    }

    public void setMoveIndex(int moveIndex) {
        this.moveIndex = moveIndex;
    }

    public Move<Solution_> getMove() {
        return move;
    }

    public void setMove(Move<Solution_> move) {
        this.move = move;
    }

    public Move<Solution_> getUndoMove() {
        return undoMove;
    }

    public void setUndoMove(Move<Solution_> undoMove) {
        this.undoMove = undoMove;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    public Boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }

    // ************************************************************************
    // Calculated methods
    // ************************************************************************

    public ScoreDirector<Solution_> getScoreDirector() {
        return stepScope.getScoreDirector();
    }

    public Solution_ getWorkingSolution() {
        return stepScope.getWorkingSolution();
    }

    public Random getWorkingRandom() {
        return stepScope.getWorkingRandom();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + stepScope.getStepIndex() + "/" + moveIndex + ")";
    }

}
