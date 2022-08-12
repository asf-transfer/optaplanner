package org.optaplanner.core.api.score.buildin.hardmediumsoftbigdecimal;

import java.math.BigDecimal;

import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.api.domain.constraintweight.ConstraintWeight;
import org.optaplanner.core.api.score.holder.ScoreHolder;

/**
 * @see HardMediumSoftBigDecimalScoreHolder
 * @deprecated Score DRL is deprecated and will be removed in a future major version of OptaPlanner.
 *             See <a href="https://www.optaplanner.org/download/upgradeRecipe/drl-to-constraint-streams-migration.html">DRL to Constraint
 *             Streams migration recipe</a>.
 */
@Deprecated(forRemoval = true)
public interface HardMediumSoftBigDecimalScoreHolder extends ScoreHolder<HardMediumSoftBigDecimalScore> {

    /**
     * Penalize a match by the {@link ConstraintWeight} negated and multiplied with the weightMultiplier for all score levels.
     *
     * @param kcontext never null, the magic variable in DRL
     * @param weightMultiplier at least 0
     */
    void penalize(RuleContext kcontext, BigDecimal weightMultiplier);

    /**
     * Penalize a match by the {@link ConstraintWeight} negated and multiplied with the specific weightMultiplier per score
     * level.
     * Slower than {@link #penalize(RuleContext, BigDecimal)}.
     *
     * @param kcontext never null, the magic variable in DRL
     * @param hardWeightMultiplier at least 0
     * @param mediumWeightMultiplier at least 0
     * @param softWeightMultiplier at least 0
     */
    void penalize(RuleContext kcontext, BigDecimal hardWeightMultiplier, BigDecimal mediumWeightMultiplier,
            BigDecimal softWeightMultiplier);

    /**
     * Reward a match by the {@link ConstraintWeight} multiplied with the weightMultiplier for all score levels.
     *
     * @param kcontext never null, the magic variable in DRL
     * @param weightMultiplier at least 0
     */
    void reward(RuleContext kcontext, BigDecimal weightMultiplier);

    /**
     * Reward a match by the {@link ConstraintWeight} multiplied with the specific weightMultiplier per score level.
     * Slower than {@link #reward(RuleContext, BigDecimal)}.
     *
     * @param kcontext never null, the magic variable in DRL
     * @param hardWeightMultiplier at least 0
     * @param mediumWeightMultiplier at least 0
     * @param softWeightMultiplier at least 0
     */
    void reward(RuleContext kcontext, BigDecimal hardWeightMultiplier, BigDecimal mediumWeightMultiplier,
            BigDecimal softWeightMultiplier);

    void impactScore(RuleContext kcontext, BigDecimal weightMultiplier);

    /**
     * Add a hard constraint of specified weighting.
     *
     * This is typically used in Drools scoring to add a hard constraint match (negative value to indicate an infeasible
     * solution).
     *
     * @param kcontext never null, the magic variable in DRL
     * @param hardWeight never null, higher is better, negative for a penalty, positive for a reward
     */
    void addHardConstraintMatch(RuleContext kcontext, BigDecimal hardWeight);

    /**
     * Add a medium level constraint of specified weighting.
     *
     * This is typically used in Drools scoring to add a medium priority constraint match.
     *
     * @param kcontext never null, the magic variable in DRL
     * @param mediumWeight never null, higher is better, negative for a penalty, positive for a reward
     */
    void addMediumConstraintMatch(RuleContext kcontext, BigDecimal mediumWeight);

    /**
     * Add a soft constraint match of specified weighting.
     *
     * This is typically used in Drools scoring to add a low priority constraint match.
     *
     * @param kcontext never null, the magic variable in DRL
     * @param softWeight never null, higher is better, negative for a penalty, positive for a reward
     */
    void addSoftConstraintMatch(RuleContext kcontext, BigDecimal softWeight);

    /**
     * @param kcontext never null, the magic variable in DRL
     * @param hardWeight never null, higher is better, negative for a penalty, positive for a reward
     * @param mediumWeight never null, higher is better, negative for a penalty, positive for a reward
     * @param softWeight never null, higher is better, negative for a penalty, positive for a reward
     */
    void addMultiConstraintMatch(RuleContext kcontext, BigDecimal hardWeight, BigDecimal mediumWeight,
            BigDecimal softWeight);
}
