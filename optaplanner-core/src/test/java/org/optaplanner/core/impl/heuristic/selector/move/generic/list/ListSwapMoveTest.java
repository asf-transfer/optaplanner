/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListEntity;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListSolution;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListValue;

class ListSwapMoveTest {

    @Test
    void doMove() {
        TestdataListValue v1 = new TestdataListValue("1");
        TestdataListValue v2 = new TestdataListValue("2");
        TestdataListValue v3 = new TestdataListValue("3");
        TestdataListEntity e1 = new TestdataListEntity("e1", v1, v2);
        TestdataListEntity e2 = new TestdataListEntity("e2", v3);

        InnerScoreDirector<TestdataListSolution, SimpleScore> scoreDirector = mock(InnerScoreDirector.class);
        ListVariableDescriptor<TestdataListSolution> variableDescriptor =
                TestdataListEntity.buildVariableDescriptorForValueList();

        // Swap Move 1: between two entities
        ListSwapMove<TestdataListSolution> move1 = new ListSwapMove<>(variableDescriptor, e1, 0, e2, 0);

        AbstractMove<TestdataListSolution> undoMove1 = move1.doMove(scoreDirector);
        assertThat(e1.getValueList()).containsExactly(v3, v2);
        assertThat(e2.getValueList()).containsExactly(v1);

        // undo
        undoMove1.doMove(scoreDirector);
        assertThat(e1.getValueList()).containsExactly(v1, v2);
        assertThat(e2.getValueList()).containsExactly(v3);

        // Swap Move 2: same entity
        ListSwapMove<TestdataListSolution> move2 = new ListSwapMove<>(variableDescriptor, e1, 0, e1, 1);

        AbstractMove<TestdataListSolution> undoMove2 = move2.doMove(scoreDirector);
        assertThat(e1.getValueList()).containsExactly(v2, v1);

        // undo
        undoMove2.doMove(scoreDirector);
        assertThat(e1.getValueList()).containsExactly(v1, v2);
    }

    @Test
    void isMoveDoable() {
        TestdataListValue v1 = new TestdataListValue("1");
        TestdataListValue v2 = new TestdataListValue("2");
        TestdataListValue v3 = new TestdataListValue("3");
        TestdataListEntity e1 = new TestdataListEntity("e1", v1, v2);
        TestdataListEntity e2 = new TestdataListEntity("e2", v3);

        ScoreDirector<TestdataListSolution> scoreDirector = mock(ScoreDirector.class);
        ListVariableDescriptor<TestdataListSolution> variableDescriptor =
                TestdataListEntity.buildVariableDescriptorForValueList();

        // same entity, same index => not doable because the move doesn't change anything
        assertThat(new ListSwapMove<>(variableDescriptor, e1, 1, e1, 1).isMoveDoable(scoreDirector)).isFalse();
        // same entity, different index => doable
        assertThat(new ListSwapMove<>(variableDescriptor, e1, 0, e1, 1).isMoveDoable(scoreDirector)).isTrue();
        // different entity => doable
        assertThat(new ListSwapMove<>(variableDescriptor, e1, 0, e2, 0).isMoveDoable(scoreDirector)).isTrue();
    }

    @Test
    void toStringTest() {
        TestdataListValue v1 = new TestdataListValue("1");
        TestdataListValue v2 = new TestdataListValue("2");
        TestdataListValue v3 = new TestdataListValue("3");
        TestdataListEntity e1 = new TestdataListEntity("e1", v1, v2);
        TestdataListEntity e2 = new TestdataListEntity("e2", v3);

        ListVariableDescriptor<TestdataListSolution> variableDescriptor =
                TestdataListEntity.buildVariableDescriptorForValueList();

        assertThat(new ListSwapMove<>(variableDescriptor, e1, 0, e1, 1)).hasToString("1 {e1[0]} <-> 2 {e1[1]}");
        assertThat(new ListSwapMove<>(variableDescriptor, e1, 1, e2, 0)).hasToString("2 {e1[1]} <-> 3 {e2[0]}");
    }
}
