package org.optaplanner.constraint.streams.bavet.tri;

import java.util.Set;

import org.optaplanner.constraint.streams.bavet.BavetConstraintFactory;
import org.optaplanner.constraint.streams.bavet.common.BavetAbstractConstraintStream;
import org.optaplanner.constraint.streams.bavet.common.GroupNodeConstructor;
import org.optaplanner.constraint.streams.bavet.common.NodeBuildHelper;
import org.optaplanner.constraint.streams.bavet.common.TupleLifecycle;
import org.optaplanner.constraint.streams.bavet.uni.BavetGroupUniConstraintStream;
import org.optaplanner.constraint.streams.bavet.uni.UniTuple;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.ConstraintStream;

final class BavetUniGroupBridgeTriConstraintStream<Solution_, A, B, C, NewA>
        extends BavetAbstractTriConstraintStream<Solution_, A, B, C> {

    private final BavetAbstractTriConstraintStream<Solution_, A, B, C> parent;
    private BavetGroupUniConstraintStream<Solution_, NewA> groupStream;
    private final GroupNodeConstructor<UniTuple<NewA>> nodeConstructor;

    public BavetUniGroupBridgeTriConstraintStream(BavetConstraintFactory<Solution_> constraintFactory, BavetAbstractTriConstraintStream<Solution_, A, B, C> parent, GroupNodeConstructor<UniTuple<NewA>> nodeConstructor) {
        super(constraintFactory, parent.getRetrievalSemantics());
        this.parent = parent;
        this.nodeConstructor = nodeConstructor;
    }

    @Override
    public boolean guaranteesDistinct() {
        return true;
    }

    public void setGroupStream(BavetGroupUniConstraintStream<Solution_, NewA> groupStream) {
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
    public final <Score_ extends Score<Score_>> void buildNode(NodeBuildHelper<Score_> buildHelper) {
        if (!childStreamList.isEmpty()) {
            throw new IllegalStateException("Impossible state: the stream (" + this
                    + ") has an non-empty childStreamList (" + childStreamList + ") but it's a groupBy bridge.");
        }
        int groupStoreIndex = buildHelper.reserveTupleStoreIndex(parent.getTupleSource());
        int undoStoreIndex = buildHelper.reserveTupleStoreIndex(parent.getTupleSource());
        TupleLifecycle<UniTuple<NewA>> tupleLifecycle =
                buildHelper.getAggregatedTupleLifecycle(groupStream.getChildStreamList());
        int outputStoreSize = buildHelper.extractTupleStoreSize(groupStream);
        var node = nodeConstructor.apply(groupStoreIndex, undoStoreIndex, tupleLifecycle, outputStoreSize);
        buildHelper.addNode(node, this);
    }

    @Override
    public ConstraintStream getTupleSource() {
        return parent.getTupleSource();
    }

}
