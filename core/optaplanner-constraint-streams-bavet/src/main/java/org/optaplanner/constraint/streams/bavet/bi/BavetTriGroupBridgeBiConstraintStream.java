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

import java.util.Set;
import java.util.function.Consumer;

import org.optaplanner.constraint.streams.bavet.BavetConstraintFactory;
import org.optaplanner.constraint.streams.bavet.common.AbstractGroupNode;
import org.optaplanner.constraint.streams.bavet.common.BavetAbstractConstraintStream;
import org.optaplanner.constraint.streams.bavet.common.NodeBuildHelper;
import org.optaplanner.constraint.streams.bavet.tri.BavetGroupTriConstraintStream;
import org.optaplanner.constraint.streams.bavet.tri.TriTuple;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.ConstraintStream;

final class BavetTriGroupBridgeBiConstraintStream<Solution_, A, B, NewA, NewB, NewC>
        extends BavetAbstractBiConstraintStream<Solution_, A, B> {

    private final BavetAbstractBiConstraintStream<Solution_, A, B> parent;
    private BavetGroupTriConstraintStream<Solution_, NewA, NewB, NewC> groupStream;
    private final BiGroupNodeConstructor<A, B, TriTuple<NewA, NewB, NewC>> nodeConstructor;

    public BavetTriGroupBridgeBiConstraintStream(BavetConstraintFactory<Solution_> constraintFactory,
            BavetAbstractBiConstraintStream<Solution_, A, B> parent,
            BiGroupNodeConstructor<A, B, TriTuple<NewA, NewB, NewC>> nodeConstructor) {
        super(constraintFactory, parent.getRetrievalSemantics());
        this.parent = parent;
        this.nodeConstructor = nodeConstructor;
    }

    @Override
    public boolean guaranteesDistinct() {
        return true;
    }

    public void setGroupStream(BavetGroupTriConstraintStream<Solution_, NewA, NewB, NewC> groupStream) {
        this.groupStream = groupStream;
    }

    // ************************************************************************
    // Node creation
    // ************************************************************************

    @Override
    public void collectActiveConstraintStreams(Set<BavetAbstractConstraintStream<Solution_>> constraintStreamSet) {
        parent.collectActiveConstraintStreams(constraintStreamSet);
        constraintStreamSet.add(this);
    }

    @Override
    public <Score_ extends Score<Score_>> void buildNode(NodeBuildHelper<Score_> buildHelper) {
        if (!childStreamList.isEmpty()) {
            throw new IllegalStateException("Impossible state: the stream (" + this
                    + ") has an non-empty childStreamList (" + childStreamList + ") but it's a groupBy bridge.");
        }
        int inputStoreIndex = buildHelper.reserveTupleStoreIndex(parent.getTupleSource());
        Consumer<TriTuple<NewA, NewB, NewC>> insert = buildHelper.getAggregatedInsert(groupStream.getChildStreamList());
        Consumer<TriTuple<NewA, NewB, NewC>> retract = buildHelper.getAggregatedRetract(groupStream.getChildStreamList());
        int outputStoreSize = buildHelper.extractTupleStoreSize(groupStream);
        AbstractGroupNode<BiTuple<A, B>, TriTuple<NewA, NewB, NewC>, ?, ?> node =
                nodeConstructor.apply(inputStoreIndex, insert, retract, outputStoreSize);
        buildHelper.addNode(node);
        buildHelper.putInsertRetract(this, node::insert, node::retract);
    }

    @Override
    public ConstraintStream getTupleSource() {
        return parent.getTupleSource();
    }

}
