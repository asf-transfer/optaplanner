/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.score.stream.common.inliner;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.api.score.buildin.bendablebigdecimal.BendableBigDecimalScore;
import org.optaplanner.core.api.score.buildin.bendablelong.BendableLongScore;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.score.buildin.hardmediumsoftbigdecimal.HardMediumSoftBigDecimalScore;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.api.score.buildin.simplebigdecimal.SimpleBigDecimalScore;
import org.optaplanner.core.api.score.buildin.simplelong.SimpleLongScore;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.impl.score.buildin.bendable.BendableScoreDefinition;
import org.optaplanner.core.impl.score.buildin.bendablebigdecimal.BendableBigDecimalScoreDefinition;
import org.optaplanner.core.impl.score.buildin.bendablelong.BendableLongScoreDefinition;
import org.optaplanner.core.impl.score.buildin.hardmediumsoft.HardMediumSoftScoreDefinition;
import org.optaplanner.core.impl.score.buildin.hardmediumsoftbigdecimal.HardMediumSoftBigDecimalScoreDefinition;
import org.optaplanner.core.impl.score.buildin.hardmediumsoftlong.HardMediumSoftLongScoreDefinition;
import org.optaplanner.core.impl.score.buildin.hardsoft.HardSoftScoreDefinition;
import org.optaplanner.core.impl.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScoreDefinition;
import org.optaplanner.core.impl.score.buildin.hardsoftlong.HardSoftLongScoreDefinition;
import org.optaplanner.core.impl.score.buildin.simple.SimpleScoreDefinition;
import org.optaplanner.core.impl.score.buildin.simplebigdecimal.SimpleBigDecimalScoreDefinition;
import org.optaplanner.core.impl.score.buildin.simplelong.SimpleLongScoreDefinition;
import org.optaplanner.core.impl.score.constraint.DefaultConstraintMatchTotal;
import org.optaplanner.core.impl.score.constraint.DefaultIndictment;
import org.optaplanner.core.impl.score.definition.ScoreDefinition;

public abstract class AbstractScoreInliner<Score_ extends Score<Score_>> {

    public static <Score_ extends Score<Score_>, ScoreInliner_ extends AbstractScoreInliner<Score_>> ScoreInliner_
            buildScoreInliner(ScoreDefinition<Score_> scoreDefinition, Map<Constraint, Score_> constraintIdToWeightMap,
                    boolean constraintMatchEnabled) {
        if (scoreDefinition instanceof SimpleScoreDefinition) {
            return (ScoreInliner_) new SimpleScoreInliner((Map<Constraint, SimpleScore>) constraintIdToWeightMap,
                    constraintMatchEnabled);
        } else if (scoreDefinition instanceof SimpleLongScoreDefinition) {
            return (ScoreInliner_) new SimpleLongScoreInliner((Map<Constraint, SimpleLongScore>) constraintIdToWeightMap,
                    constraintMatchEnabled);
        } else if (scoreDefinition instanceof SimpleBigDecimalScoreDefinition) {
            return (ScoreInliner_) new SimpleBigDecimalScoreInliner(
                    (Map<Constraint, SimpleBigDecimalScore>) constraintIdToWeightMap, constraintMatchEnabled);
        } else if (scoreDefinition instanceof HardSoftScoreDefinition) {
            return (ScoreInliner_) new HardSoftScoreInliner((Map<Constraint, HardSoftScore>) constraintIdToWeightMap,
                    constraintMatchEnabled);
        } else if (scoreDefinition instanceof HardSoftLongScoreDefinition) {
            return (ScoreInliner_) new HardSoftLongScoreInliner((Map<Constraint, HardSoftLongScore>) constraintIdToWeightMap,
                    constraintMatchEnabled);
        } else if (scoreDefinition instanceof HardSoftBigDecimalScoreDefinition) {
            return (ScoreInliner_) new HardSoftBigDecimalScoreInliner(
                    (Map<Constraint, HardSoftBigDecimalScore>) constraintIdToWeightMap, constraintMatchEnabled);
        } else if (scoreDefinition instanceof HardMediumSoftScoreDefinition) {
            return (ScoreInliner_) new HardMediumSoftScoreInliner(
                    (Map<Constraint, HardMediumSoftScore>) constraintIdToWeightMap, constraintMatchEnabled);
        } else if (scoreDefinition instanceof HardMediumSoftLongScoreDefinition) {
            return (ScoreInliner_) new HardMediumSoftLongScoreInliner(
                    (Map<Constraint, HardMediumSoftLongScore>) constraintIdToWeightMap, constraintMatchEnabled);
        } else if (scoreDefinition instanceof HardMediumSoftBigDecimalScoreDefinition) {
            return (ScoreInliner_) new HardMediumSoftBigDecimalScoreInliner(
                    (Map<Constraint, HardMediumSoftBigDecimalScore>) constraintIdToWeightMap, constraintMatchEnabled);
        } else if (scoreDefinition instanceof BendableScoreDefinition) {
            BendableScoreDefinition bendableScoreDefinition = (BendableScoreDefinition) scoreDefinition;
            return (ScoreInliner_) new BendableScoreInliner((Map<Constraint, BendableScore>) constraintIdToWeightMap,
                    constraintMatchEnabled, bendableScoreDefinition.getHardLevelsSize(),
                    bendableScoreDefinition.getSoftLevelsSize());
        } else if (scoreDefinition instanceof BendableLongScoreDefinition) {
            BendableLongScoreDefinition bendableScoreDefinition = (BendableLongScoreDefinition) scoreDefinition;
            return (ScoreInliner_) new BendableLongScoreInliner((Map<Constraint, BendableLongScore>) constraintIdToWeightMap,
                    constraintMatchEnabled, bendableScoreDefinition.getHardLevelsSize(),
                    bendableScoreDefinition.getSoftLevelsSize());
        } else if (scoreDefinition instanceof BendableBigDecimalScoreDefinition) {
            BendableBigDecimalScoreDefinition bendableScoreDefinition = (BendableBigDecimalScoreDefinition) scoreDefinition;
            return (ScoreInliner_) new BendableBigDecimalScoreInliner(
                    (Map<Constraint, BendableBigDecimalScore>) constraintIdToWeightMap, constraintMatchEnabled,
                    bendableScoreDefinition.getHardLevelsSize(), bendableScoreDefinition.getSoftLevelsSize());
        } else {
            throw new UnsupportedOperationException("Impossible state: unknown score definition (" +
                    scoreDefinition.getClass().getCanonicalName() + ").");
        }
    }

    private final Map<String, Score_> constraintIdToWeightMap;
    protected final boolean constraintMatchEnabled;
    private final Map<String, DefaultConstraintMatchTotal<Score_>> constraintMatchTotalMap;
    private final Map<Object, DefaultIndictment<Score_>> indictmentMap;

    protected AbstractScoreInliner(Map<Constraint, Score_> constraintToWeightMap, boolean constraintMatchEnabled) {
        this.constraintIdToWeightMap = Objects.requireNonNull(constraintToWeightMap).entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().getConstraintId(), Map.Entry::getValue));
        this.constraintMatchEnabled = constraintMatchEnabled;
        this.constraintMatchTotalMap = constraintMatchEnabled ? new LinkedHashMap<>() : null;
        this.indictmentMap = constraintMatchEnabled ? new LinkedHashMap<>() : null;
    }

    public abstract Score_ extractScore(int initScore);

    /**
     * Create a new instance of {@link WeightedScoreImpacter} for a particular constraint.
     * 
     * @param constraint never null
     * @return never null
     */
    public abstract WeightedScoreImpacter buildWeightedScoreImpacter(Constraint constraint);

    protected final Runnable addConstraintMatch(Constraint constraint, Score_ constraintWeight, Score_ score,
            List<Object> justificationList) {
        String constraintPackage = constraint.getConstraintPackage();
        String constraintName = constraint.getConstraintName();
        DefaultConstraintMatchTotal<Score_> constraintMatchTotal = constraintMatchTotalMap.computeIfAbsent(
                constraint.getConstraintId(),
                key -> new DefaultConstraintMatchTotal<>(constraintPackage, constraintName, constraintWeight));
        ConstraintMatch<Score_> constraintMatch = constraintMatchTotal.addConstraintMatch(justificationList, score);
        DefaultIndictment<Score_>[] indictments = justificationList.stream()
                .distinct() // One match might have the same justification twice
                .map(justification -> {
                    DefaultIndictment<Score_> indictment = indictmentMap.computeIfAbsent(justification,
                            key -> new DefaultIndictment<>(justification, constraintMatch.getScore().zero()));
                    indictment.addConstraintMatch(constraintMatch);
                    return indictment;
                }).toArray(DefaultIndictment[]::new);
        return () -> {
            constraintMatchTotal.removeConstraintMatch(constraintMatch);
            if (constraintMatchTotal.getConstraintMatchSet().isEmpty()) {
                constraintMatchTotalMap.remove(constraint.getConstraintId());
            }
            for (DefaultIndictment<Score_> indictment : indictments) {
                indictment.removeConstraintMatch(constraintMatch);
                if (indictment.getConstraintMatchSet().isEmpty()) {
                    indictmentMap.remove(indictment.getJustification());
                }
            }
        };
    }

    public final Map<String, ConstraintMatchTotal<Score_>> getConstraintMatchTotalMap() {
        // Unchecked assignment necessary as CMT and DefaultCMT incompatible in the Map generics.
        return (Map) constraintMatchTotalMap;
    }

    public final Map<Object, Indictment<Score_>> getIndictmentMap() {
        // Unchecked assignment necessary as Indictment and DefaultIndictment incompatible in the Map generics.
        return (Map) indictmentMap;
    }

    protected final Score_ getConstraintWeight(Constraint constraint) {
        Score_ constraintWeight = constraintIdToWeightMap.get(constraint.getConstraintId());
        if (constraintWeight == null || constraintWeight.isZero()) {
            throw new IllegalArgumentException("Impossible state: The constraintWeight (" +
                    constraintWeight + ") cannot be zero, constraint (" + constraint +
                    ") should have been culled during node creation.");
        }
        return constraintWeight;
    }

}
