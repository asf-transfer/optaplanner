package org.optaplanner.constraint.streams.drools.tri;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.drools.model.functions.Function1;
import org.drools.model.functions.Function2;
import org.drools.model.functions.Function3;
import org.drools.model.functions.Function4;
import org.optaplanner.constraint.streams.drools.DroolsConstraintFactory;
import org.optaplanner.constraint.streams.drools.bi.DroolsAbstractBiConstraintStream;
import org.optaplanner.constraint.streams.drools.common.TriLeftHandSide;
import org.optaplanner.constraint.streams.drools.quad.DroolsAbstractQuadConstraintStream;
import org.optaplanner.constraint.streams.drools.uni.DroolsAbstractUniConstraintStream;
import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.score.stream.bi.BiConstraintCollector;
import org.optaplanner.core.api.score.stream.quad.QuadConstraintCollector;
import org.optaplanner.core.api.score.stream.tri.TriConstraintCollector;
import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;

public final class DroolsGroupingTriConstraintStream<Solution_, NewA, NewB, NewC>
        extends DroolsAbstractTriConstraintStream<Solution_, NewA, NewB, NewC> {

    private final Supplier<TriLeftHandSide<NewA, NewB, NewC>> leftHandSide;

    public <A> DroolsGroupingTriConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractUniConstraintStream<Solution_, A> parent, UniConstraintCollector<A, ?, NewA> collectorA,
            UniConstraintCollector<A, ?, NewB> collectorB, UniConstraintCollector<A, ?, NewC> collectorC) {
        super(constraintFactory, parent.getRetrievalSemantics());
        this.leftHandSide = () -> parent.createLeftHandSide().andGroupBy(collectorA, collectorB, collectorC);
    }

    public <A, B> DroolsGroupingTriConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractBiConstraintStream<Solution_, A, B> parent, BiConstraintCollector<A, B, ?, NewA> collectorA,
            BiConstraintCollector<A, B, ?, NewB> collectorB, BiConstraintCollector<A, B, ?, NewC> collectorC) {
        super(constraintFactory, parent.getRetrievalSemantics());
        this.leftHandSide = () -> parent.createLeftHandSide().andGroupBy(collectorA, collectorB, collectorC);
    }

    public <A, B, C> DroolsGroupingTriConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractTriConstraintStream<Solution_, A, B, C> parent,
            TriConstraintCollector<A, B, C, ?, NewA> collectorA, TriConstraintCollector<A, B, C, ?, NewB> collectorB,
            TriConstraintCollector<A, B, C, ?, NewC> collectorC) {
        super(constraintFactory, parent.getRetrievalSemantics());
        this.leftHandSide = () -> parent.createLeftHandSide().andGroupBy(collectorA, collectorB, collectorC);
    }

    public <A, B, C, D> DroolsGroupingTriConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractQuadConstraintStream<Solution_, A, B, C, D> parent,
            QuadConstraintCollector<A, B, C, D, ?, NewA> collectorA,
            QuadConstraintCollector<A, B, C, D, ?, NewB> collectorB,
            QuadConstraintCollector<A, B, C, D, ?, NewC> collectorC) {
        super(constraintFactory, parent.getRetrievalSemantics());
        this.leftHandSide = () -> parent.createLeftHandSide().andGroupBy(collectorA, collectorB, collectorC);
    }

    public <A> DroolsGroupingTriConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractUniConstraintStream<Solution_, A> parent, Function<A, NewA> groupKeyAMapping,
            UniConstraintCollector<A, ?, NewB> collectorB, UniConstraintCollector<A, ?, NewC> collectorC) {
        super(constraintFactory, parent.getRetrievalSemantics());
        Function1<A, NewA> convertedMapping = constraintFactory.getInternalsFactory().convert(groupKeyAMapping);
        this.leftHandSide = () -> parent.createLeftHandSide().andGroupBy(convertedMapping, collectorB, collectorC);
    }

    public <A, B> DroolsGroupingTriConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractBiConstraintStream<Solution_, A, B> parent, BiFunction<A, B, NewA> groupKeyAMapping,
            BiConstraintCollector<A, B, ?, NewB> collectorB, BiConstraintCollector<A, B, ?, NewC> collectorC) {
        super(constraintFactory, parent.getRetrievalSemantics());
        Function2<A, B, NewA> convertedMapping = constraintFactory.getInternalsFactory().convert(groupKeyAMapping);
        this.leftHandSide = () -> parent.createLeftHandSide().andGroupBy(convertedMapping, collectorB, collectorC);
    }

    public <A, B, C> DroolsGroupingTriConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractTriConstraintStream<Solution_, A, B, C> parent, TriFunction<A, B, C, NewA> groupKeyAMapping,
            TriConstraintCollector<A, B, C, ?, NewB> collectorB, TriConstraintCollector<A, B, C, ?, NewC> collectorC) {
        super(constraintFactory, parent.getRetrievalSemantics());
        Function3<A, B, C, NewA> convertedAMapping = constraintFactory.getInternalsFactory().convert(groupKeyAMapping);
        this.leftHandSide = () -> parent.createLeftHandSide().andGroupBy(convertedAMapping, collectorB, collectorC);
    }

    public <A, B, C, D> DroolsGroupingTriConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractQuadConstraintStream<Solution_, A, B, C, D> parent,
            QuadFunction<A, B, C, D, NewA> groupKeyAMapping, QuadConstraintCollector<A, B, C, D, ?, NewB> collectorB,
            QuadConstraintCollector<A, B, C, D, ?, NewC> collectorC) {
        super(constraintFactory, parent.getRetrievalSemantics());
        Function4<A, B, C, D, NewA> convertedMapping = constraintFactory.getInternalsFactory().convert(groupKeyAMapping);
        this.leftHandSide = () -> parent.createLeftHandSide().andGroupBy(convertedMapping, collectorB, collectorC);
    }

    public <A> DroolsGroupingTriConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractUniConstraintStream<Solution_, A> parent, Function<A, NewA> groupKeyAMapping,
            Function<A, NewB> groupKeyBMapping, UniConstraintCollector<A, ?, NewC> collector) {
        super(constraintFactory, parent.getRetrievalSemantics());
        Function1<A, NewA> convertedAMapping = constraintFactory.getInternalsFactory().convert(groupKeyAMapping);
        Function1<A, NewB> convertedBMapping = constraintFactory.getInternalsFactory().convert(groupKeyBMapping);
        this.leftHandSide = () -> parent.createLeftHandSide().andGroupBy(convertedAMapping, convertedBMapping, collector);
    }

    public <A, B> DroolsGroupingTriConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractBiConstraintStream<Solution_, A, B> parent, BiFunction<A, B, NewA> groupKeyAMapping,
            BiFunction<A, B, NewB> groupKeyBMapping, BiConstraintCollector<A, B, ?, NewC> collector) {
        super(constraintFactory, parent.getRetrievalSemantics());
        Function2<A, B, NewA> convertedAMapping = constraintFactory.getInternalsFactory().convert(groupKeyAMapping);
        Function2<A, B, NewB> convertedBMapping = constraintFactory.getInternalsFactory().convert(groupKeyBMapping);
        this.leftHandSide = () -> parent.createLeftHandSide().andGroupBy(convertedAMapping, convertedBMapping, collector);
    }

    public <A, B, C> DroolsGroupingTriConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractTriConstraintStream<Solution_, A, B, C> parent, TriFunction<A, B, C, NewA> groupKeyAMapping,
            TriFunction<A, B, C, NewB> groupKeyBMapping, TriConstraintCollector<A, B, C, ?, NewC> collector) {
        super(constraintFactory, parent.getRetrievalSemantics());
        Function3<A, B, C, NewA> convertedAMapping = constraintFactory.getInternalsFactory().convert(groupKeyAMapping);
        Function3<A, B, C, NewB> convertedBMapping = constraintFactory.getInternalsFactory().convert(groupKeyBMapping);
        this.leftHandSide = () -> parent.createLeftHandSide().andGroupBy(convertedAMapping, convertedBMapping, collector);
    }

    public <A, B, C, D> DroolsGroupingTriConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractQuadConstraintStream<Solution_, A, B, C, D> parent,
            QuadFunction<A, B, C, D, NewA> groupKeyAMapping, QuadFunction<A, B, C, D, NewB> groupKeyBMapping,
            QuadConstraintCollector<A, B, C, D, ?, NewC> collector) {
        super(constraintFactory, parent.getRetrievalSemantics());
        Function4<A, B, C, D, NewA> convertedAMapping = constraintFactory.getInternalsFactory().convert(groupKeyAMapping);
        Function4<A, B, C, D, NewB> convertedBMapping = constraintFactory.getInternalsFactory().convert(groupKeyBMapping);
        this.leftHandSide = () -> parent.createLeftHandSide().andGroupBy(convertedAMapping, convertedBMapping, collector);
    }

    public <A> DroolsGroupingTriConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractUniConstraintStream<Solution_, A> parent, Function<A, NewA> groupKeyAMapping,
            Function<A, NewB> groupKeyBMapping, Function<A, NewC> groupKeyCMapping) {
        super(constraintFactory, parent.getRetrievalSemantics());
        Function1<A, NewA> convertedAMapping = constraintFactory.getInternalsFactory().convert(groupKeyAMapping);
        Function1<A, NewB> convertedBMapping = constraintFactory.getInternalsFactory().convert(groupKeyBMapping);
        Function1<A, NewC> convertedCMapping = constraintFactory.getInternalsFactory().convert(groupKeyCMapping);
        this.leftHandSide =
                () -> parent.createLeftHandSide().andGroupBy(convertedAMapping, convertedBMapping, convertedCMapping);
    }

    public <A, B> DroolsGroupingTriConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractBiConstraintStream<Solution_, A, B> parent, BiFunction<A, B, NewA> groupKeyAMapping,
            BiFunction<A, B, NewB> groupKeyBMapping, BiFunction<A, B, NewC> groupKeyCMapping) {
        super(constraintFactory, parent.getRetrievalSemantics());
        Function2<A, B, NewA> convertedAMapping = constraintFactory.getInternalsFactory().convert(groupKeyAMapping);
        Function2<A, B, NewB> convertedBMapping = constraintFactory.getInternalsFactory().convert(groupKeyBMapping);
        Function2<A, B, NewC> convertedCMapping = constraintFactory.getInternalsFactory().convert(groupKeyCMapping);
        this.leftHandSide =
                () -> parent.createLeftHandSide().andGroupBy(convertedAMapping, convertedBMapping, convertedCMapping);
    }

    public <A, B, C> DroolsGroupingTriConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractTriConstraintStream<Solution_, A, B, C> parent, TriFunction<A, B, C, NewA> groupKeyAMapping,
            TriFunction<A, B, C, NewB> groupKeyBMapping, TriFunction<A, B, C, NewC> groupKeyCMapping) {
        super(constraintFactory, parent.getRetrievalSemantics());
        Function3<A, B, C, NewA> convertedAMapping = constraintFactory.getInternalsFactory().convert(groupKeyAMapping);
        Function3<A, B, C, NewB> convertedBMapping = constraintFactory.getInternalsFactory().convert(groupKeyBMapping);
        Function3<A, B, C, NewC> convertedCMapping = constraintFactory.getInternalsFactory().convert(groupKeyCMapping);
        this.leftHandSide =
                () -> parent.createLeftHandSide().andGroupBy(convertedAMapping, convertedBMapping, convertedCMapping);
    }

    public <A, B, C, D> DroolsGroupingTriConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractQuadConstraintStream<Solution_, A, B, C, D> parent,
            QuadFunction<A, B, C, D, NewA> groupKeyAMapping, QuadFunction<A, B, C, D, NewB> groupKeyBMapping,
            QuadFunction<A, B, C, D, NewC> groupKeyCMapping) {
        super(constraintFactory, parent.getRetrievalSemantics());
        Function4<A, B, C, D, NewA> convertedAMapping = constraintFactory.getInternalsFactory().convert(groupKeyAMapping);
        Function4<A, B, C, D, NewB> convertedBMapping = constraintFactory.getInternalsFactory().convert(groupKeyBMapping);
        Function4<A, B, C, D, NewC> convertedCMapping = constraintFactory.getInternalsFactory().convert(groupKeyCMapping);
        this.leftHandSide =
                () -> parent.createLeftHandSide().andGroupBy(convertedAMapping, convertedBMapping, convertedCMapping);
    }

    @Override
    public boolean guaranteesDistinct() {
        return true;
    }

    // ************************************************************************
    // Pattern creation
    // ************************************************************************

    @Override
    public TriLeftHandSide<NewA, NewB, NewC> createLeftHandSide() {
        return leftHandSide.get();
    }

    @Override
    public String toString() {
        return "TriGroup() with " + getChildStreams().size() + " children";
    }
}
