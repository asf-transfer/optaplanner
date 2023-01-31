package org.optaplanner.core.impl.testdata.domain.list;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.heuristic.selector.SelectorTestUtils;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.move.generic.list.ElementDestinationSelector;
import org.optaplanner.core.impl.heuristic.selector.move.generic.list.ElementRef;
import org.optaplanner.core.impl.heuristic.selector.value.EntityIndependentValueSelector;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;

public class TestdataListUtils {

    private TestdataListUtils() {
    }

    public static int listSize(TestdataListEntity entity) {
        return entity.getValueList().size();
    }

    public static EntitySelector<TestdataListSolution> mockEntitySelector(Object... entities) {
        return SelectorTestUtils.mockEntitySelector(TestdataListEntity.class, entities);
    }

    public static EntityIndependentValueSelector<TestdataListSolution> mockEntityIndependentValueSelector(Object... values) {
        return SelectorTestUtils.mockEntityIndependentValueSelector(TestdataListEntity.class, "valueList", values);
    }

    public static EntityIndependentValueSelector<TestdataListSolution>
            mockNeverEndingEntityIndependentValueSelector(Object... values) {
        EntityIndependentValueSelector<TestdataListSolution> valueSelector =
                SelectorTestUtils.mockEntityIndependentValueSelector(TestdataListEntity.class, "valueList", values);
        when(valueSelector.isNeverEnding()).thenReturn(true);
        when(valueSelector.iterator()).thenAnswer(invocation -> cyclicIterator(Arrays.asList(values)));
        return valueSelector;
    }

    public static ElementDestinationSelector<TestdataListSolution> mockNeverEndingDestinationSelector(
            ElementRef... elementRefs) {
        return mockNeverEndingDestinationSelector(elementRefs.length, elementRefs);
    }

    public static ElementDestinationSelector<TestdataListSolution> mockNeverEndingDestinationSelector(long size,
            ElementRef... elementRefs) {
        ElementDestinationSelector<TestdataListSolution> destinationSelector = mock(ElementDestinationSelector.class);
        when(destinationSelector.isCountable()).thenReturn(true);
        when(destinationSelector.isNeverEnding()).thenReturn(true);
        when(destinationSelector.getSize()).thenReturn(size);
        when(destinationSelector.iterator()).thenAnswer(invocation -> cyclicIterator(Arrays.asList(elementRefs)));
        return destinationSelector;
    }

    public static ElementDestinationSelector<TestdataListSolution> mockDestinationSelector(ElementRef... elementRefs) {
        ElementDestinationSelector<TestdataListSolution> destinationSelector = mock(ElementDestinationSelector.class);
        List<ElementRef> refList = Arrays.asList(elementRefs);
        when(destinationSelector.isCountable()).thenReturn(true);
        when(destinationSelector.isNeverEnding()).thenReturn(false);
        when(destinationSelector.getSize()).thenReturn((long) refList.size());
        when(destinationSelector.iterator()).thenAnswer(invocation -> refList.iterator());
        return destinationSelector;
    }

    public static ListVariableDescriptor<TestdataListSolution> getListVariableDescriptor(
            InnerScoreDirector<TestdataListSolution, ?> scoreDirector) {
        return (ListVariableDescriptor<TestdataListSolution>) scoreDirector
                .getSolutionDescriptor()
                .getEntityDescriptorStrict(TestdataListEntity.class)
                .getGenuineVariableDescriptor("valueList");
    }

    private static <T> Iterator<T> cyclicIterator(List<T> elements) {
        if (elements.isEmpty()) {
            return Collections.emptyIterator();
        }
        if (elements.size() == 1) {
            return new Iterator<>() {

                private final T element = elements.get(0);

                @Override
                public boolean hasNext() {
                    return true;
                }

                @Override
                public T next() {
                    return element;
                }
            };
        }
        return new Iterator<>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public T next() {
                T element = elements.get(i % elements.size());
                i++;
                return element;
            }
        };
    }
}
