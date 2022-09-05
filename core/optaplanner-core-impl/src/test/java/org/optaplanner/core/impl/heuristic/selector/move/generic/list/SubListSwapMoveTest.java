package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.heuristic.move.AbstractMove;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListEntity;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListSolution;
import org.optaplanner.core.impl.testdata.domain.list.TestdataListValue;

class SubListSwapMoveTest {

    @Test
    void isMoveDoable() {
        TestdataListValue v1 = new TestdataListValue("1");
        TestdataListValue v2 = new TestdataListValue("2");
        TestdataListValue v3 = new TestdataListValue("3");
        TestdataListValue v4 = new TestdataListValue("4");
        TestdataListValue v5 = new TestdataListValue("5");
        TestdataListEntity e1 = new TestdataListEntity("e1", v1, v2, v3, v4);
        TestdataListEntity e2 = new TestdataListEntity("e2", v5);

        ScoreDirector<TestdataListSolution> scoreDirector = mock(ScoreDirector.class);
        ListVariableDescriptor<TestdataListSolution> variableDescriptor =
                TestdataListEntity.buildVariableDescriptorForValueList();

        // same entity, overlap => not doable
        assertThat(new SubListSwapMove<>(variableDescriptor, sub(e1, 0, 3), sub(e1, 2, 3), false).isMoveDoable(scoreDirector))
                .isFalse();
        // same entity, overlap => not doable
        assertThat(new SubListSwapMove<>(variableDescriptor, sub(e1, 0, 5), sub(e1, 1, 1), false).isMoveDoable(scoreDirector))
                .isFalse();
        // same entity, no overlap (with gap) => doable
        assertThat(new SubListSwapMove<>(variableDescriptor, sub(e1, 0, 1), sub(e1, 4, 1), false).isMoveDoable(scoreDirector))
                .isTrue();
        // same entity, no overlap (with touch) => doable
        assertThat(new SubListSwapMove<>(variableDescriptor, sub(e1, 0, 3), sub(e1, 3, 2), false).isMoveDoable(scoreDirector))
                .isTrue();
        // same entity, no overlap (with touch, right below left) => doable
        assertThat(new SubListSwapMove<>(variableDescriptor, sub(e1, 2, 3), sub(e1, 0, 2), false).isMoveDoable(scoreDirector))
                .isTrue();
        // different entities => doable
        assertThat(new SubListSwapMove<>(variableDescriptor, sub(e1, 0, 5), sub(e2, 0, 1), false).isMoveDoable(scoreDirector))
                .isTrue();
    }

    @Test
    void doMove() {
        TestdataListValue v1 = new TestdataListValue("1");
        TestdataListValue v2 = new TestdataListValue("2");
        TestdataListValue v3 = new TestdataListValue("3");
        TestdataListValue v4 = new TestdataListValue("4");
        TestdataListValue v5 = new TestdataListValue("5");
        TestdataListEntity e1 = new TestdataListEntity("e1", v1, v2, v3, v4);
        TestdataListEntity e2 = new TestdataListEntity("e2", v5);

        InnerScoreDirector<TestdataListSolution, SimpleScore> scoreDirector = mock(InnerScoreDirector.class);
        ListVariableDescriptor<TestdataListSolution> variableDescriptor =
                TestdataListEntity.buildVariableDescriptorForValueList();

        SubListSwapMove<TestdataListSolution> move =
                new SubListSwapMove<>(variableDescriptor, sub(e1, 1, 2), sub(e2, 0, 1), false);

        AbstractMove<TestdataListSolution> undoMove = move.doMove(scoreDirector);

        assertThat(e1.getValueList()).containsExactly(v1, v5, v4);
        assertThat(e2.getValueList()).containsExactly(v2, v3);

        verify(scoreDirector).beforeSubListChanged(variableDescriptor, e1, 1, 3);
        verify(scoreDirector).afterSubListChanged(variableDescriptor, e1, 1, 2);
        verify(scoreDirector).beforeSubListChanged(variableDescriptor, e2, 0, 1);
        verify(scoreDirector).afterSubListChanged(variableDescriptor, e2, 0, 2);
        verify(scoreDirector).triggerVariableListeners();
        verifyNoMoreInteractions(scoreDirector);

        undoMove.doMove(scoreDirector);

        assertThat(e1.getValueList()).containsExactly(v1, v2, v3, v4);
        assertThat(e2.getValueList()).containsExactly(v5);
    }

    @Test
    void doReversingMove() {
        TestdataListValue v1 = new TestdataListValue("1");
        TestdataListValue v2 = new TestdataListValue("2");
        TestdataListValue v3 = new TestdataListValue("3");
        TestdataListValue v4 = new TestdataListValue("4");
        TestdataListValue v5 = new TestdataListValue("5");
        TestdataListValue v6 = new TestdataListValue("6");
        TestdataListEntity e1 = new TestdataListEntity("e1", v1, v2, v3, v4);
        TestdataListEntity e2 = new TestdataListEntity("e2", v5, v6);

        InnerScoreDirector<TestdataListSolution, SimpleScore> scoreDirector = mock(InnerScoreDirector.class);
        ListVariableDescriptor<TestdataListSolution> variableDescriptor =
                TestdataListEntity.buildVariableDescriptorForValueList();

        SubListSwapMove<TestdataListSolution> move =
                new SubListSwapMove<>(variableDescriptor, sub(e1, 0, 3), sub(e2, 0, 2), true);

        AbstractMove<TestdataListSolution> undoMove = move.doMove(scoreDirector);

        assertThat(e1.getValueList()).containsExactly(v6, v5, v4);
        assertThat(e2.getValueList()).containsExactly(v3, v2, v1);

        verify(scoreDirector).beforeSubListChanged(variableDescriptor, e1, 0, 3);
        verify(scoreDirector).afterSubListChanged(variableDescriptor, e1, 0, 2);
        verify(scoreDirector).beforeSubListChanged(variableDescriptor, e2, 0, 2);
        verify(scoreDirector).afterSubListChanged(variableDescriptor, e2, 0, 3);
        verify(scoreDirector).triggerVariableListeners();
        verifyNoMoreInteractions(scoreDirector);

        undoMove.doMove(scoreDirector);

        assertThat(e1.getValueList()).containsExactly(v1, v2, v3, v4);
        assertThat(e2.getValueList()).containsExactly(v5, v6);
    }

    @Test
    void doMoveOnSameEntity() {
        TestdataListValue v1 = new TestdataListValue("1");
        TestdataListValue v2 = new TestdataListValue("2");
        TestdataListValue v3 = new TestdataListValue("3");
        TestdataListValue v4 = new TestdataListValue("4");
        TestdataListValue v5 = new TestdataListValue("5");
        TestdataListValue v6 = new TestdataListValue("6");
        TestdataListValue v7 = new TestdataListValue("7");
        TestdataListEntity e1 = new TestdataListEntity("e1", v1, v2, v3, v4, v5, v6, v7);

        InnerScoreDirector<TestdataListSolution, SimpleScore> scoreDirector = mock(InnerScoreDirector.class);
        ListVariableDescriptor<TestdataListSolution> variableDescriptor =
                TestdataListEntity.buildVariableDescriptorForValueList();

        SubListSwapMove<TestdataListSolution> move =
                new SubListSwapMove<>(variableDescriptor, sub(e1, 0, 1), sub(e1, 4, 3), false);

        AbstractMove<TestdataListSolution> undoMove = move.doMove(scoreDirector);

        assertThat(e1.getValueList()).containsExactly(v5, v6, v7, v2, v3, v4, v1);

        verify(scoreDirector).beforeSubListChanged(variableDescriptor, e1, 0, 7);
        verify(scoreDirector).afterSubListChanged(variableDescriptor, e1, 0, 7);
        // TODO or this more fine-grained? (Do we allow multiple notifications per entity? (Yes))
        // verify(scoreDirector).beforeSubListChanged(variableDescriptor, e1, 0, 1);
        // verify(scoreDirector).afterSubListChanged(variableDescriptor, e1, 0, 3);
        // verify(scoreDirector).beforeSubListChanged(variableDescriptor, e1, 4, 7);
        // verify(scoreDirector).afterSubListChanged(variableDescriptor, e1, 6, 7);
        verify(scoreDirector).triggerVariableListeners();
        verifyNoMoreInteractions(scoreDirector);

        undoMove.doMove(scoreDirector);

        assertThat(e1.getValueList()).containsExactly(v1, v2, v3, v4, v5, v6, v7);
    }

    @Test
    void toStringTest() {
        TestdataListEntity e1 = new TestdataListEntity("e1");
        TestdataListEntity e2 = new TestdataListEntity("e2");

        ListVariableDescriptor<TestdataListSolution> variableDescriptor =
                TestdataListEntity.buildVariableDescriptorForValueList();

        assertThat(new SubListSwapMove<>(variableDescriptor, sub(e1, 1, 3), sub(e1, 0, 1), false))
                .hasToString("{e1[0..1]} <-> {e1[1..4]}");
        assertThat(new SubListSwapMove<>(variableDescriptor, sub(e1, 0, 1), sub(e2, 1, 5), false))
                .hasToString("{e1[0..1]} <-> {e2[1..6]}");
        assertThat(new SubListSwapMove<>(variableDescriptor, sub(e1, 0, 1), sub(e2, 1, 5), true))
                .hasToString("{e1[0..1]} <-reversing-> {e2[1..6]}");
    }

    static SubList sub(TestdataListEntity entity, int fromIndex, int length) {
        return new SubList(entity, fromIndex, length);
    }
}
