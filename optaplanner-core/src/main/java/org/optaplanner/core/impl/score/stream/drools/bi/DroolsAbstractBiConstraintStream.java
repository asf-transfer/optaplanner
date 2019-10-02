/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
package org.optaplanner.core.impl.score.stream.drools.bi;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.ToIntBiFunction;
import java.util.function.ToLongBiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.drools.model.Declaration;
import org.drools.model.Global;
import org.drools.model.PatternDSL;
import org.drools.model.RuleItemBuilder;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.holder.AbstractScoreHolder;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.bi.BiConstraintCollector;
import org.optaplanner.core.api.score.stream.bi.BiConstraintStream;
import org.optaplanner.core.api.score.stream.tri.TriConstraintStream;
import org.optaplanner.core.api.score.stream.tri.TriJoiner;
import org.optaplanner.core.api.score.stream.uni.UniConstraintStream;
import org.optaplanner.core.impl.score.stream.drools.DroolsConstraint;
import org.optaplanner.core.impl.score.stream.drools.DroolsConstraintFactory;
import org.optaplanner.core.impl.score.stream.drools.common.DroolsAbstractConstraintStream;
import org.optaplanner.core.impl.score.stream.drools.uni.DroolsAbstractUniConstraintStream;
import org.optaplanner.core.impl.score.stream.drools.uni.DroolsFromUniConstraintStream;

public abstract class DroolsAbstractBiConstraintStream<Solution_, A, B>
        extends DroolsAbstractConstraintStream<Solution_>
        implements BiConstraintStream<A, B> {

    protected final DroolsAbstractBiConstraintStream<Solution_, A, B> parent;
    protected final List<DroolsAbstractBiConstraintStream<Solution_, A, B>> childStreamList = new ArrayList<>(2);

    public DroolsAbstractBiConstraintStream(DroolsConstraintFactory<Solution_> constraintFactory,
            DroolsAbstractBiConstraintStream<Solution_, A, B> parent) {
        super(constraintFactory);
        if (parent == null && !(this instanceof DroolsJoinBiConstraintStream)) {
            throw new IllegalArgumentException("Parent of stream (" + this + ") must not be null, or not join stream.");
        }
        this.parent = parent;
    }

    public DroolsAbstractUniConstraintStream<Solution_, A> getLeftParentStream() {
        return parent.getLeftParentStream();
    }

    public DroolsAbstractUniConstraintStream<Solution_, B> getRightParentStream() {
        return parent.getRightParentStream();
    }

    @Override
    public BiConstraintStream<A, B> filter(BiPredicate<A, B> predicate) {
        DroolsAbstractBiConstraintStream<Solution_, A, B> stream =
                new DroolsFilterBiConstraintStream<>(constraintFactory, this, predicate);
        childStreamList.add(stream);
        return stream;
    }

    @Override
    public <C> TriConstraintStream<A, B, C> join(UniConstraintStream<C> otherStream) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <C> TriConstraintStream<A, B, C> join(UniConstraintStream<C> otherStream, TriJoiner<A, B, C> joiner) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <C> TriConstraintStream<A, B, C> join(Class<C> otherClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <C> TriConstraintStream<A, B, C> join(Class<C> otherClass, TriJoiner<A, B, C> joiner) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <C> TriConstraintStream<A, B, C> join(Class<C> otherClass, TriJoiner<A, B, C> joiner1, TriJoiner<A, B, C> joiner2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <C> TriConstraintStream<A, B, C> join(Class<C> otherClass, TriJoiner<A, B, C> joiner1, TriJoiner<A, B, C> joiner2, TriJoiner<A, B, C> joiner3) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <C> TriConstraintStream<A, B, C> join(Class<C> otherClass, TriJoiner<A, B, C> joiner1, TriJoiner<A, B, C> joiner2, TriJoiner<A, B, C> joiner3, TriJoiner<A, B, C> joiner4) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <C> TriConstraintStream<A, B, C> join(Class<C> otherClass, TriJoiner<A, B, C>... joiners) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <GroupKey_> UniConstraintStream<GroupKey_> groupBy(BiFunction<A, B, GroupKey_> groupKeyMapping) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <GroupKey_, ResultContainer_, Result_> BiConstraintStream<GroupKey_, Result_> groupBy(BiFunction<A, B, GroupKey_> groupKeyMapping, BiConstraintCollector<A, B, ResultContainer_, Result_> collector) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <GroupKeyA_, GroupKeyB_> BiConstraintStream<GroupKeyA_, GroupKeyB_> groupBy(BiFunction<A, B, GroupKeyA_> groupKeyAMapping, BiFunction<A, B, GroupKeyB_> groupKeyBMapping) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <GroupKeyA_, GroupKeyB_, ResultContainer_, Result_> TriConstraintStream<GroupKeyA_, GroupKeyB_, Result_> groupBy(BiFunction<A, B, GroupKeyA_> groupKeyAMapping, BiFunction<A, B, GroupKeyB_> groupKeyBMapping, BiConstraintCollector<A, B, ResultContainer_, Result_> collector) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Constraint penalize(String constraintPackage, String constraintName, Score<?> constraintWeight,
            ToIntBiFunction<A, B> matchWeigher) {
        return impactScore(constraintPackage, constraintName, constraintWeight, matchWeigher, false);
    }

    @Override
    public Constraint penalizeLong(String constraintPackage, String constraintName, Score<?> constraintWeight,
            ToLongBiFunction<A, B> matchWeigher) {
        return impactScoreLong(constraintPackage, constraintName, constraintWeight, matchWeigher, false);
    }

    @Override
    public Constraint penalizeBigDecimal(String constraintPackage, String constraintName, Score<?> constraintWeight,
            BiFunction<A, B, BigDecimal> matchWeigher) {
        return impactScoreBigDecimal(constraintPackage, constraintName, constraintWeight, matchWeigher, false);
    }

    @Override
    public Constraint penalizeConfigurable(String constraintPackage, String constraintName,
            ToIntBiFunction<A, B> matchWeigher) {
        return impactScoreConfigurable(constraintPackage, constraintName, matchWeigher, false);
    }

    @Override
    public Constraint penalizeConfigurableLong(String constraintPackage, String constraintName,
            ToLongBiFunction<A, B> matchWeigher) {
        return impactScoreConfigurableLong(constraintPackage, constraintName, matchWeigher, false);
    }

    @Override
    public Constraint penalizeConfigurableBigDecimal(String constraintPackage, String constraintName,
            BiFunction<A, B, BigDecimal> matchWeigher) {
        return impactScoreConfigurableBigDecimal(constraintPackage, constraintName, matchWeigher, false);
    }

    @Override
    public Constraint reward(String constraintPackage, String constraintName, Score<?> constraintWeight,
            ToIntBiFunction<A, B> matchWeigher) {
        return impactScore(constraintPackage, constraintName, constraintWeight, matchWeigher, true);
    }

    @Override
    public Constraint rewardLong(String constraintPackage, String constraintName, Score<?> constraintWeight,
            ToLongBiFunction<A, B> matchWeigher) {
        return impactScoreLong(constraintPackage, constraintName, constraintWeight, matchWeigher, true);
    }

    @Override
    public Constraint rewardBigDecimal(String constraintPackage, String constraintName, Score<?> constraintWeight,
            BiFunction<A, B, BigDecimal> matchWeigher) {
        return impactScoreBigDecimal(constraintPackage, constraintName, constraintWeight, matchWeigher, true);

    }

    @Override
    public Constraint rewardConfigurable(String constraintPackage, String constraintName,
            ToIntBiFunction<A, B> matchWeigher) {
        return impactScoreConfigurable(constraintPackage, constraintName, matchWeigher, true);
    }

    @Override
    public Constraint rewardConfigurableLong(String constraintPackage, String constraintName,
            ToLongBiFunction<A, B> matchWeigher) {
        return impactScoreConfigurableLong(constraintPackage, constraintName, matchWeigher, true);
    }

    @Override
    public Constraint rewardConfigurableBigDecimal(String constraintPackage, String constraintName,
            BiFunction<A, B, BigDecimal> matchWeigher) {
        return impactScoreConfigurableBigDecimal(constraintPackage, constraintName, matchWeigher, true);
    }

    @Override
    protected final Constraint impactScore(String constraintPackage, String constraintName, Score<?> constraintWeight,
            boolean positively) {
        DroolsConstraint<Solution_> constraint = buildConstraint(constraintPackage, constraintName, constraintWeight,
                positively);
        DroolsScoringBiConstraintStream<Solution_, A, B> stream =
                new DroolsScoringBiConstraintStream<>(constraintFactory, this, constraint);
        childStreamList.add(stream);
        return constraint;
    }

    protected final Constraint impactScore(String constraintPackage, String constraintName,
            Score<?> constraintWeight, ToIntBiFunction<A, B> matchWeigher, boolean positively) {
        DroolsConstraint<Solution_> constraint = buildConstraint(constraintPackage, constraintName, constraintWeight,
                positively);
        DroolsScoringBiConstraintStream<Solution_, A, B> stream =
                new DroolsScoringBiConstraintStream<>(constraintFactory, this, constraint, matchWeigher);
        childStreamList.add(stream);
        return constraint;
    }

    protected final Constraint impactScoreLong(String constraintPackage, String constraintName,
            Score<?> constraintWeight, ToLongBiFunction<A, B> matchWeigher, boolean positively) {
        DroolsConstraint<Solution_> constraint = buildConstraint(constraintPackage, constraintName, constraintWeight,
                positively);
        DroolsScoringBiConstraintStream<Solution_, A, B> stream =
                new DroolsScoringBiConstraintStream<>(constraintFactory, this, constraint, matchWeigher);
        childStreamList.add(stream);
        return constraint;
    }

    protected final Constraint impactScoreBigDecimal(String constraintPackage, String constraintName,
            Score<?> constraintWeight, BiFunction<A, B, BigDecimal> matchWeigher, boolean positively) {
        DroolsConstraint<Solution_> constraint = buildConstraint(constraintPackage, constraintName, constraintWeight,
                positively);
        DroolsScoringBiConstraintStream<Solution_, A, B> stream =
                new DroolsScoringBiConstraintStream<>(constraintFactory, this, constraint, matchWeigher);
        childStreamList.add(stream);
        return constraint;
    }

    @Override
    protected final Constraint impactScoreConfigurable(String constraintPackage, String constraintName, boolean positively) {
        DroolsConstraint<Solution_> constraint = buildConstraintConfigurable(constraintPackage, constraintName,
                positively);
        DroolsScoringBiConstraintStream<Solution_, A, B> stream =
                new DroolsScoringBiConstraintStream<>(constraintFactory, this, constraint);
        childStreamList.add(stream);
        return constraint;
    }

    protected final Constraint impactScoreConfigurable(String constraintPackage, String constraintName,
            ToIntBiFunction<A, B> matchWeigher, boolean positively) {
        DroolsConstraint<Solution_> constraint = buildConstraintConfigurable(constraintPackage, constraintName,
                positively);
        DroolsScoringBiConstraintStream<Solution_, A, B> stream =
                new DroolsScoringBiConstraintStream<>(constraintFactory, this, constraint, matchWeigher);
        childStreamList.add(stream);
        return constraint;
    }

    protected final Constraint impactScoreConfigurableLong(String constraintPackage, String constraintName,
            ToLongBiFunction<A, B> matchWeigher, boolean positively) {
        DroolsConstraint<Solution_> constraint = buildConstraintConfigurable(constraintPackage, constraintName,
                positively);
        DroolsScoringBiConstraintStream<Solution_, A, B> stream =
                new DroolsScoringBiConstraintStream<>(constraintFactory, this, constraint, matchWeigher);
        childStreamList.add(stream);
        return constraint;
    }

    protected final Constraint impactScoreConfigurableBigDecimal(String constraintPackage, String constraintName,
            BiFunction<A, B, BigDecimal> matchWeigher, boolean positively) {
        DroolsConstraint<Solution_> constraint = buildConstraintConfigurable(constraintPackage, constraintName,
                positively);
        DroolsScoringBiConstraintStream<Solution_, A, B> stream =
                new DroolsScoringBiConstraintStream<>(constraintFactory, this, constraint, matchWeigher);
        childStreamList.add(stream);
        return constraint;
    }

    // ************************************************************************
    // Pattern creation
    // ************************************************************************

    @Override
    public void createRuleItemBuilders(List<RuleItemBuilder<?>> ruleItemBuilderList,
            Global<? extends AbstractScoreHolder> scoreHolderGlobal) {
        for (DroolsAbstractBiConstraintStream<Solution_, A, B> childStream : childStreamList) {
            childStream.createRuleItemBuilders(ruleItemBuilderList, scoreHolderGlobal);
        }
    }

    @Override
    public List<DroolsFromUniConstraintStream<Solution_, Object>> getFromStreamList() {
        if (parent == null) {
            DroolsJoinBiConstraintStream<Solution_, A, B> joinStream =
                    (DroolsJoinBiConstraintStream<Solution_, A, B>) this;
            List<DroolsFromUniConstraintStream<Solution_, Object>> leftParentFromStreamList =
                    joinStream.getLeftParentStream().getFromStreamList();
            List<DroolsFromUniConstraintStream<Solution_, Object>> rightParentFromStreamList =
                    joinStream.getRightParentStream().getFromStreamList();
            return Stream.concat(leftParentFromStreamList.stream(), rightParentFromStreamList.stream())
                    .distinct()
                    .collect(Collectors.toList());
        } else {
            return parent.getFromStreamList();
        }
    }

    public abstract Declaration<A> getLeftVariableDeclaration();

    public abstract PatternDSL.PatternDef<A> getLeftPattern();

    public abstract Declaration<B> getRightVariableDeclaration();

    public abstract PatternDSL.PatternDef<B> getRightPattern();


}
