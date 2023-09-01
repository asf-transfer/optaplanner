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

package org.optaplanner.core.impl.heuristic.selector.list.nearby;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import org.optaplanner.core.impl.domain.variable.supply.SupplyManager;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.AbstractNearbyDistanceMatrixDemand;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMatrix;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyDistanceMeter;
import org.optaplanner.core.impl.heuristic.selector.common.nearby.NearbyRandom;
import org.optaplanner.core.impl.heuristic.selector.list.RandomSubListSelector;
import org.optaplanner.core.impl.heuristic.selector.list.mimic.MimicReplayingSubListSelector;

/**
 * Demands a distance matrix where both the origins and nearby destinations are planning values.
 * <p>
 * Calculating {@link NearbyDistanceMatrix} is very expensive,
 * therefore we want to reuse it as much as possible.
 * <p>
 * In cases where the demand represents the same nearby selector (as defined by
 * {@link SubListNearbySubListMatrixDemand#equals(Object)})
 * the {@link SupplyManager} ensures that the same supply instance is returned
 * with the pre-computed {@link NearbyDistanceMatrix}.
 *
 * @param <Solution_>
 * @param <Origin_> planning values
 * @param <Destination_> planning values
 */
final class SubListNearbySubListMatrixDemand<Solution_, Origin_, Destination_>
        extends
        AbstractNearbyDistanceMatrixDemand<Origin_, Destination_, RandomSubListSelector<Solution_>, MimicReplayingSubListSelector<Solution_>> {

    private final ToIntFunction<Origin_> destinationSizeFunction;

    public SubListNearbySubListMatrixDemand(
            NearbyDistanceMeter<Origin_, Destination_> meter,
            NearbyRandom random,
            RandomSubListSelector<Solution_> childSubListSelector,
            MimicReplayingSubListSelector<Solution_> replayingOriginSubListSelector,
            ToIntFunction<Origin_> destinationSizeFunction) {
        super(meter, random, childSubListSelector, replayingOriginSubListSelector);
        this.destinationSizeFunction = destinationSizeFunction;
    }

    @Override
    protected NearbyDistanceMatrix<Origin_, Destination_> supplyNearbyDistanceMatrix() {
        final long childSize = childSelector.getValueCount();
        if (childSize > Integer.MAX_VALUE) {
            throw new IllegalStateException("The childSize (" + childSize + ") is higher than Integer.MAX_VALUE.");
        }

        long originSize = replayingSelector.getValueCount();
        if (originSize > Integer.MAX_VALUE) {
            throw new IllegalStateException("The originSubListSelector (" + replayingSelector
                    + ") has a subListSize (" + originSize
                    + ") which is higher than Integer.MAX_VALUE.");
        }
        // Destinations: values extracted from a subList selector.
        // Distance "matrix" cannot contain subLists because:
        // 1. Its elements are exposed to the user-implemented NearbyDistanceMeter. SubList is an internal class.
        // 2. The matrix is static; it's computed once when solving starts and then never changes.
        //    The subLists available in the solution change with every step.
        Function<Origin_, Iterator<Destination_>> destinationIteratorProvider =
                origin -> (Iterator<Destination_>) childSelector.endingValueIterator();
        NearbyDistanceMatrix<Origin_, Destination_> nearbyDistanceMatrix =
                new NearbyDistanceMatrix<>(meter, (int) originSize, destinationIteratorProvider, destinationSizeFunction);
        // Origins: values extracted from a subList selector.
        replayingSelector.endingValueIterator()
                .forEachRemaining(origin -> nearbyDistanceMatrix.addAllDestinations((Origin_) origin));
        return nearbyDistanceMatrix;
    }

}
