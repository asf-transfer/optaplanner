/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.optaplanner.core.impl.heuristic.selector.move.composite;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.common.iterator.SelectionIterator;
import org.optaplanner.core.impl.heuristic.selector.move.MoveSelector;
import org.optaplanner.core.impl.heuristic.selector.move.generic.ChangeMoveSelector;
import org.optaplanner.core.impl.heuristic.selector.move.generic.SwapMoveSelector;

final class UniformRandomUnionMoveIterator<Solution_> extends SelectionIterator<Move<Solution_>> {

    private final List<Iterator<Move<Solution_>>> moveIteratorList;
    private final Random workingRandom;

    public UniformRandomUnionMoveIterator(List<MoveSelector<Solution_>> childMoveSelectorList, Random workingRandom) {
        ArrayList<Iterator<Move<Solution_>>> list = new ArrayList<>();

        // See https://issues.redhat.com/browse/PLANNER-2933
        Iterator iterator;
        for (Object s : childMoveSelectorList) {
            if (s instanceof SwapMoveSelector) {
                iterator = ((SwapMoveSelector) s).iterator();
            } else if (s instanceof ChangeMoveSelector) {
                iterator = ((ChangeMoveSelector) s).iterator();
            } else {
                iterator = ((MoveSelector) s).iterator();
            }
            if (iterator.hasNext()) {
                list.add(iterator);
            }
        }
        this.moveIteratorList = list;
        this.workingRandom = workingRandom;
    }

    @Override
    public boolean hasNext() {
        return !moveIteratorList.isEmpty();
    }

    @Override
    public Move<Solution_> next() {
        int index = workingRandom.nextInt(moveIteratorList.size());
        Iterator<Move<Solution_>> moveIterator = moveIteratorList.get(index);
        Move<Solution_> next = moveIterator.next();
        if (!moveIterator.hasNext()) {
            moveIteratorList.remove(index);
        }
        return next;
    }
}
