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

import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import org.optaplanner.constraint.streams.bavet.BavetConstraintFactory;
import org.optaplanner.constraint.streams.bavet.common.AbstractInserter;
import org.optaplanner.constraint.streams.bavet.common.AbstractUpdater;
import org.optaplanner.constraint.streams.bavet.common.BavetAbstractConstraintStream;
import org.optaplanner.constraint.streams.bavet.common.NodeBuildHelper;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.ConstraintStream;

public final class BavetFilterBiConstraintStream<Solution_, A, B> extends BavetAbstractBiConstraintStream<Solution_, A, B> {

    private final BavetAbstractBiConstraintStream<Solution_, A, B> parent;
    private final BiPredicate<A, B> predicate;

    public BavetFilterBiConstraintStream(BavetConstraintFactory<Solution_> constraintFactory,
            BavetAbstractBiConstraintStream<Solution_, A, B> parent,
            BiPredicate<A, B> predicate) {
        super(constraintFactory, parent.getRetrievalSemantics());
        this.parent = parent;
        this.predicate = predicate;
        if (predicate == null) {
            throw new IllegalArgumentException("The predicate (null) cannot be null.");
        }
    }

    @Override
    public boolean guaranteesDistinct() {
        return parent.guaranteesDistinct();
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
    public ConstraintStream getTupleSource() {
        return parent.getTupleSource();
    }

    @Override
    public <Score_ extends Score<Score_>> void buildNode(NodeBuildHelper<Score_> buildHelper) {
        buildHelper.<BiTuple<A, B>> putInsertUpdateRetract(this, childStreamList,
                insert -> new ConditionalBiInserter<>(predicate, insert),
                (update, retract) -> new ConditionalBiUpdater<>(predicate, update, retract));
    }

    private static final class ConditionalBiInserter<A, B> extends AbstractInserter<BiTuple<A, B>> {
        private final BiPredicate<A, B> predicate;

        public ConditionalBiInserter(BiPredicate<A, B> predicate, Consumer<BiTuple<A, B>> insert) {
            super(insert);
            this.predicate = predicate;
        }

        @Override
        protected boolean test(BiTuple<A, B> tuple) {
            return predicate.test(tuple.factA, tuple.factB);
        }

    }

    private static final class ConditionalBiUpdater<A, B> extends AbstractUpdater<BiTuple<A, B>> {
        private final BiPredicate<A, B> predicate;

        public ConditionalBiUpdater(BiPredicate<A, B> predicate, Consumer<BiTuple<A, B>> update,
                Consumer<BiTuple<A, B>> retract) {
            super(update, retract);
            this.predicate = predicate;
        }

        @Override
        protected boolean test(BiTuple<A, B> tuple) {
            return predicate.test(tuple.factA, tuple.factB);
        }

    }

    // ************************************************************************
    // Equality for node sharing
    // ************************************************************************

    @Override
    public int hashCode() {
        return Objects.hash(parent, predicate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof BavetFilterBiConstraintStream) {
            BavetFilterBiConstraintStream<?, ?, ?> other = (BavetFilterBiConstraintStream<?, ?, ?>) o;
            return parent == other.parent
                    && predicate == other.predicate;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "Filter() with " + childStreamList.size() + " children";
    }

    // ************************************************************************
    // Getters/setters
    // ************************************************************************

}
