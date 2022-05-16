/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.constraint.streams.bavet.bi;

import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.optaplanner.constraint.streams.bavet.BavetConstraintFactory;
import org.optaplanner.constraint.streams.bavet.quad.QuadTuple;
import org.optaplanner.core.api.score.stream.bi.BiConstraintCollector;

final class BavetGroupBridge1Mapping3CollectorBiConstraintStream<Solution_, A, B, NewA, ResultContainerB_, NewB, ResultContainerC_, NewC, ResultContainerD_, NewD>
        extends BavetAbstractQuadGroupBridgeBiConstraintStream<Solution_, A, B, NewA, NewB, NewC, NewD> {

    private final BiFunction<A, B, NewA> groupKeyMapping;
    private final BiConstraintCollector<A, B, ResultContainerB_, NewB> collectorB;
    private final BiConstraintCollector<A, B, ResultContainerC_, NewC> collectorC;
    private final BiConstraintCollector<A, B, ResultContainerD_, NewD> collectorD;

    public BavetGroupBridge1Mapping3CollectorBiConstraintStream(BavetConstraintFactory<Solution_> constraintFactory,
            BavetAbstractBiConstraintStream<Solution_, A, B> parent, BiFunction<A, B, NewA> groupKeyMapping,
            BiConstraintCollector<A, B, ResultContainerB_, NewB> collectorB,
            BiConstraintCollector<A, B, ResultContainerC_, NewC> collectorC,
            BiConstraintCollector<A, B, ResultContainerD_, NewD> collectorD) {
        super(constraintFactory, parent);
        this.groupKeyMapping = groupKeyMapping;
        this.collectorB = collectorB;
        this.collectorC = collectorC;
        this.collectorD = collectorD;
    }

    // ************************************************************************
    // Node creation
    // ************************************************************************

    @Override
    protected AbstractGroupBiNode<A, B, QuadTuple<NewA, NewB, NewC, NewD>, ?, ?, ?> createNode(int inputStoreIndex,
            Consumer<QuadTuple<NewA, NewB, NewC, NewD>> insert, Consumer<QuadTuple<NewA, NewB, NewC, NewD>> retract,
            int outputStoreSize) {
        return new Group1Mapping3CollectorBiNode<>(groupKeyMapping, inputStoreIndex, collectorB, collectorC, collectorD,
                insert, retract, outputStoreSize);
    }
}
