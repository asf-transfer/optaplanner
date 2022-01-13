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

package org.optaplanner.core.impl.solver.change;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.solver.ProblemFactChange;
import org.optaplanner.core.impl.solver.scope.SolverScope;

public final class ProblemFactChangeAdapterImpl<Solution_> implements ProblemChangeAdapter<Solution_> {

    private final ProblemFactChange<Solution_> problemFactChange;

    public ProblemFactChangeAdapterImpl(ProblemFactChange<Solution_> problemFactChange) {
        this.problemFactChange = problemFactChange;
    }

    public Score<?> doProblemChange(SolverScope<Solution_> solverScope) {
        problemFactChange.doChange(solverScope.getScoreDirector());
        return solverScope.calculateScore();
    }
}
