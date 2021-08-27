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

package org.optaplanner.persistence.minizinc.model;

import java.util.List;
import java.util.Objects;

public class FlatZincSolve {
    private final List<FlatZincAnnotation> annotationList;
    private final FlatZincGoal goal;
    private final FlatZincExpr goalExpression;

    public FlatZincSolve(List<FlatZincAnnotation> annotationList, FlatZincGoal goal, FlatZincExpr goalExpression) {
        this.annotationList = annotationList;
        this.goal = goal;
        this.goalExpression = goalExpression;
    }

    public List<FlatZincAnnotation> getAnnotationList() {
        return annotationList;
    }

    public FlatZincGoal getGoal() {
        return goal;
    }

    public FlatZincExpr getGoalExpression() {
        return goalExpression;
    }

    @Override
    public String toString() {
        return "FlatZincSolve{" +
                "annotationList=" + annotationList +
                ", goal=" + goal +
                ", goalExpression=" + goalExpression +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FlatZincSolve that = (FlatZincSolve) o;
        return annotationList.equals(that.annotationList) && goal == that.goal
                && Objects.equals(goalExpression, that.goalExpression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(annotationList, goal, goalExpression);
    }
}
