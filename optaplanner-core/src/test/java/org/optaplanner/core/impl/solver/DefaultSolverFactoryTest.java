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

package org.optaplanner.core.impl.solver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.config.solver.SolverConfig;

class DefaultSolverFactoryTest {

    @Test
    void moveThreadCountAutoIsCorrectlyResolvedWhenCpuCountIsPositive() {
        final int cpuCount = 16;
        assertThat(mockMoveThreadCountResolverAuto(cpuCount)).isEqualTo(Integer.valueOf(cpuCount - 2));
    }

    @Test
    void moveThreadCountAutoIsResolvedToNullWhenCpuCountIsNegative() {
        assertThat(mockMoveThreadCountResolverAuto(-1)).isNull();
    }

    private Integer mockMoveThreadCountResolverAuto(int mockCpuCount) {
        DefaultSolverFactory.MoveThreadCountResolver moveThreadCountResolver =
                spy(new DefaultSolverFactory.MoveThreadCountResolver());
        when(moveThreadCountResolver.getAvailableProcessors()).thenReturn(mockCpuCount);

        return moveThreadCountResolver.resolveMoveThreadCount(SolverConfig.MOVE_THREAD_COUNT_AUTO);
    }

    @Test
    void moveThreadCountIsCorrectlyResolvedWhenValueIsPositive() {
        assertThat(resolveMoveThreadCount("2")).isEqualTo(Integer.valueOf(2));
    }

    @Test
    void moveThreadCountThrowsExceptionWhenValueIsNegative() {
        assertThatIllegalArgumentException().isThrownBy(() -> resolveMoveThreadCount("-1"));
    }

    @Test
    void moveThreadCountIsResolvedToNullWhenValueIsNone() {
        assertThat(resolveMoveThreadCount(SolverConfig.MOVE_THREAD_COUNT_NONE)).isNull();
    }

    private Integer resolveMoveThreadCount(String moveThreadCountString) {
        DefaultSolverFactory.MoveThreadCountResolver moveThreadCountResolver =
                new DefaultSolverFactory.MoveThreadCountResolver();
        return moveThreadCountResolver.resolveMoveThreadCount(moveThreadCountString);
    }
}
