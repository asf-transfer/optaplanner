/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.solver.termination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.solver.scope.SolverScope;
import org.optaplanner.core.impl.testdata.domain.TestdataSolution;

public class BasicPlumbingTerminationTest {

    @Test
    public void addProblemFactChangeWithoutDaemon() {
        AtomicInteger count = new AtomicInteger(0);
        BasicPlumbingTermination<TestdataSolution> basicPlumbingTermination = new BasicPlumbingTermination<>(false);
        assertThat(basicPlumbingTermination.waitForRestartSolverDecision()).isFalse();
        basicPlumbingTermination.addProblemChange((workingSolution, problemChangeDirector) -> count.getAndIncrement());
        assertThat(basicPlumbingTermination.waitForRestartSolverDecision()).isTrue();
        assertThat(count).hasValue(0);

        SolverScope<TestdataSolution> solverScopeMock = mockSolverScope();
        basicPlumbingTermination.startProblemFactChangesProcessing().removeIf(problemChangeAdapter -> {
            problemChangeAdapter.doProblemChange(solverScopeMock);
            return true;
        });
        assertThat(basicPlumbingTermination.waitForRestartSolverDecision()).isFalse();
        assertThat(count).hasValue(1);
    }

    @Test
    public void addProblemFactChangesWithoutDaemon() {
        AtomicInteger count = new AtomicInteger(0);
        BasicPlumbingTermination<TestdataSolution> basicPlumbingTermination = new BasicPlumbingTermination<>(false);
        assertThat(basicPlumbingTermination.waitForRestartSolverDecision()).isFalse();
        basicPlumbingTermination.addProblemChanges(Arrays.asList(
                (workingSolution, problemChangeDirector) -> count.getAndIncrement(),
                (workingSolution, problemChangeDirector) -> count.getAndAdd(20)));
        assertThat(basicPlumbingTermination.waitForRestartSolverDecision()).isTrue();
        assertThat(count).hasValue(0);
        SolverScope<TestdataSolution> solverScopeMock = mockSolverScope();
        basicPlumbingTermination.startProblemFactChangesProcessing().removeIf(problemChangeAdapter -> {
            problemChangeAdapter.doProblemChange(solverScopeMock);
            return true;
        });
        assertThat(basicPlumbingTermination.waitForRestartSolverDecision()).isFalse();
        assertThat(count).hasValue(21);
    }

    private SolverScope<TestdataSolution> mockSolverScope() {
        InnerScoreDirector<TestdataSolution, ?> scoreDirectorMock = mock(InnerScoreDirector.class);
        SolverScope<TestdataSolution> solverScopeMock = mock(SolverScope.class);
        doReturn(scoreDirectorMock).when(solverScopeMock).getScoreDirector();
        return solverScopeMock;
    }
}
