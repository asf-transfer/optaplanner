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
import org.optaplanner.constraint.streams.bavet.uni.BavetGroupUniConstraintStream;
import org.optaplanner.constraint.streams.bavet.uni.UniTuple;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.ConstraintStream;

abstract class BavetAbstractUniGroupBridgeBiConstraintStream<Solution_, A, B, NewA>
        extends BavetAbstractBiConstraintStream<Solution_, A, B> {

    protected final BavetAbstractBiConstraintStream<Solution_, A, B> parent;
    protected BavetGroupUniConstraintStream<Solution_, NewA> groupStream;

    public BavetAbstractUniGroupBridgeBiConstraintStream(BavetConstraintFactory<Solution_> constraintFactory,
            BavetAbstractBiConstraintStream<Solution_, A, B> parent) {
        super(constraintFactory, parent.getRetrievalSemantics());
        this.parent = parent;
    }

    @Override
    public final boolean guaranteesDistinct() {
        return true;
    }

    public final void setGroupStream(BavetGroupUniConstraintStream<Solution_, NewA> groupStream) {
        this.groupStream = groupStream;
    }

    // ************************************************************************
    // Node creation
    // ************************************************************************

    @Override
    public final void collectActiveConstraintStreams(Set<BavetAbstractConstraintStream<Solution_>> constraintStreamSet) {
        parent.collectActiveConstraintStreams(constraintStreamSet);
        constraintStreamSet.add(this);
    }

    @Override
    public final <Score_ extends Score<Score_>> void buildNode(NodeBuildHelper<Score_> buildHelper) {
        if (!childStreamList.isEmpty()) {
            throw new IllegalStateException("Impossible state: the stream (" + this
                    + ") has an non-empty childStreamList (" + childStreamList + ") but it's a groupBy bridge.");
        }
        int inputStoreIndex = buildHelper.reserveTupleStoreIndex(parent.getTupleSource());
        int outputStoreSize = buildHelper.extractTupleStoreSize(groupStream);
        AbstractGroupNode<BiTuple<A, B>, UniTuple<NewA>, ?, ?> node =
                createNode(inputStoreIndex,
                        buildHelper.getAggregatedInsert(groupStream.getChildStreamList()),
                        buildHelper.getAggregatedRetract(groupStream.getChildStreamList()), outputStoreSize);
        buildHelper.addNode(node);
        buildHelper.putInsertRetract(this, node::insert, node::retract);
    }

    protected abstract AbstractGroupBiNode<A, B, UniTuple<NewA>, ?, ?, ?> createNode(int inputStoreIndex,
            Consumer<UniTuple<NewA>> insert, Consumer<UniTuple<NewA>> retract, int outputStoreSize);

    @Override
    public final ConstraintStream getTupleSource() {
        return parent.getTupleSource();
    }

}
