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

import java.util.Collections;
import java.util.Iterator;
import java.util.PrimitiveIterator;
import java.util.stream.IntStream;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.index.IndexVariableSupply;
import org.optaplanner.core.impl.domain.variable.inverserelation.SingletonInverseVariableSupply;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.common.iterator.UpcomingSelectionIterator;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.value.EntityIndependentValueSelector;

/**
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public class TmpOriginalListChangeIterator<Solution_> extends UpcomingSelectionIterator<Move<Solution_>> {

    private final ListVariableDescriptor<Solution_> listVariableDescriptor;
    private final SingletonInverseVariableSupply inverseVariableSupply;
    private final IndexVariableSupply indexVariableSupply;
    private final Iterator<Object> valueIterator;
    private final EntitySelector<Solution_> entitySelector;
    private Iterator<Object> toEntityIterator;
    private PrimitiveIterator.OfInt toIndexIterator;

    private Object upcomingFromEntity;
    private int upcomingFromIndex = 0;
    private Object upcomingToEntity;

    public TmpOriginalListChangeIterator(
            ListVariableDescriptor<Solution_> listVariableDescriptor,
            SingletonInverseVariableSupply inverseVariableSupply,
            IndexVariableSupply indexVariableSupply,
            EntityIndependentValueSelector<Solution_> valueSelector,
            EntitySelector<Solution_> entitySelector) {
        this.listVariableDescriptor = listVariableDescriptor;
        this.inverseVariableSupply = inverseVariableSupply;
        this.indexVariableSupply = indexVariableSupply;
        this.valueIterator = valueSelector.iterator();
        this.entitySelector = entitySelector;
        this.toEntityIterator = Collections.emptyIterator();
        this.toIndexIterator = IntStream.empty().iterator();
    }

    @Override
    protected Move<Solution_> createUpcomingSelection() {
        if (!toIndexIterator.hasNext()) {
            if (!toEntityIterator.hasNext()) {
                if (!valueIterator.hasNext()) {
                    return noUpcomingSelection();
                }
                Object upcomingValue = valueIterator.next();
                upcomingFromEntity = inverseVariableSupply.getInverseSingleton(upcomingValue);
                upcomingFromIndex = indexVariableSupply.getIndex(upcomingValue);

                toEntityIterator = entitySelector.iterator();
            }
            upcomingToEntity = toEntityIterator.next();
            toIndexIterator = listIndexIterator(upcomingToEntity);
        }

        return new ListChangeMove<>(
                listVariableDescriptor,
                upcomingFromEntity,
                upcomingFromIndex,
                upcomingToEntity,
                toIndexIterator.nextInt());
    }

    private PrimitiveIterator.OfInt listIndexIterator(Object entity) {
        return IntStream.rangeClosed(0, listVariableDescriptor.getListSize(entity)).iterator();
    }
}
