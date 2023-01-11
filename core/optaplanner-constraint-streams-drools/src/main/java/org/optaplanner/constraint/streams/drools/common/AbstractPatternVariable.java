package org.optaplanner.constraint.streams.drools.common;

import static org.drools.model.PatternDSL.betaIndexedBy;
import static org.optaplanner.constraint.streams.drools.common.AbstractLeftHandSide.getConstraintType;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.drools.model.BetaIndex;
import org.drools.model.BetaIndex2;
import org.drools.model.BetaIndex3;
import org.drools.model.PatternDSL;
import org.drools.model.Variable;
import org.drools.model.functions.Function1;
import org.drools.model.functions.Function2;
import org.drools.model.functions.Function3;
import org.drools.model.functions.Predicate1;
import org.drools.model.functions.Predicate2;
import org.drools.model.functions.Predicate3;
import org.drools.model.functions.Predicate4;
import org.drools.model.view.ViewItem;
import org.optaplanner.constraint.streams.common.bi.DefaultBiJoiner;
import org.optaplanner.constraint.streams.common.quad.DefaultQuadJoiner;
import org.optaplanner.constraint.streams.common.tri.DefaultTriJoiner;
import org.optaplanner.constraint.streams.drools.DroolsInternalsFactory;
import org.optaplanner.core.impl.score.stream.JoinerType;

abstract class AbstractPatternVariable<A, PatternVar_, Child_ extends AbstractPatternVariable<A, PatternVar_, Child_>>
        implements PatternVariable<A, PatternVar_, Child_> {

    private final Variable<A> primaryVariable;
    private final PatternDSL.PatternDef<PatternVar_> pattern;
    private final List<ViewItem<?>> prerequisiteExpressions;
    private final List<ViewItem<?>> dependentExpressions;
    private final DroolsInternalsFactory internalsFactory;

    protected AbstractPatternVariable(Variable<A> aVariable, PatternDSL.PatternDef<PatternVar_> pattern,
            List<ViewItem<?>> prerequisiteExpressions, List<ViewItem<?>> dependentExpressions,
            DroolsInternalsFactory internalsFactory) {
        this.primaryVariable = aVariable;
        this.pattern = pattern;
        this.prerequisiteExpressions = prerequisiteExpressions;
        this.dependentExpressions = dependentExpressions;
        this.internalsFactory = internalsFactory;
    }

    protected AbstractPatternVariable(AbstractPatternVariable<?, PatternVar_, ?> patternCreator, Variable<A> boundVariable) {
        this(boundVariable, patternCreator.getPattern(), patternCreator.getPrerequisiteExpressions(),
                patternCreator.getDependentExpressions(), patternCreator.getInternalsFactory());
    }

    protected AbstractPatternVariable(AbstractPatternVariable<A, PatternVar_, ?> patternCreator,
            ViewItem<?> dependentExpression) {
        this.primaryVariable = patternCreator.primaryVariable;
        this.pattern = patternCreator.pattern;
        this.prerequisiteExpressions = patternCreator.prerequisiteExpressions;
        this.dependentExpressions = Stream.concat(patternCreator.dependentExpressions.stream(), Stream.of(dependentExpression))
                .collect(Collectors.toList());
        this.internalsFactory = patternCreator.internalsFactory;
    }

    @Override
    public Variable<A> getPrimaryVariable() {
        return primaryVariable;
    }

    public PatternDSL.PatternDef<PatternVar_> getPattern() {
        return pattern;
    }

    @Override
    public List<ViewItem<?>> getPrerequisiteExpressions() {
        return prerequisiteExpressions;
    }

    @Override
    public List<ViewItem<?>> getDependentExpressions() {
        return dependentExpressions;
    }

    @Override
    public DroolsInternalsFactory getInternalsFactory() {
        return internalsFactory;
    }

    /**
     * Variable values can be either read directly from the pattern variable (see {@link DirectPatternVariable}
     * or indirectly by applying a mapping function to it (see {@link IndirectPatternVariable}.
     * This method abstracts this behavior, so that the surrounding code may be shared between both implementations.
     *
     * @param patternVar never null, pattern variable to extract the value from
     * @return value of the variable
     */
    protected abstract A extract(PatternVar_ patternVar);

    @Override
    public final Child_ filter(Predicate1<A> predicate) {
        pattern.expr("Filter using " + predicate, a -> predicate.test(extract(a)));
        return (Child_) this;
    }

    @Override
    public final <LeftJoinVar_> Child_ filter(Predicate2<LeftJoinVar_, A> predicate,
            Variable<LeftJoinVar_> leftJoinVariable) {
        pattern.expr("Filter using " + predicate, leftJoinVariable,
                (a, leftJoinVar) -> predicate.test(leftJoinVar, extract(a)));
        return (Child_) this;
    }

    @Override
    public final <LeftJoinVarA_, LeftJoinVarB_> Child_ filter(
            Predicate3<LeftJoinVarA_, LeftJoinVarB_, A> predicate, Variable<LeftJoinVarA_> leftJoinVariableA,
            Variable<LeftJoinVarB_> leftJoinVariableB) {
        pattern.expr("Filter using " + predicate, leftJoinVariableA, leftJoinVariableB,
                (a, leftJoinVarA, leftJoinVarB) -> predicate.test(leftJoinVarA, leftJoinVarB, extract(a)));
        return (Child_) this;
    }

    @Override
    public final <LeftJoinVarA_, LeftJoinVarB_, LeftJoinVarC_> Child_ filter(
            Predicate4<LeftJoinVarA_, LeftJoinVarB_, LeftJoinVarC_, A> predicate,
            Variable<LeftJoinVarA_> leftJoinVariableA, Variable<LeftJoinVarB_> leftJoinVariableB,
            Variable<LeftJoinVarC_> leftJoinVariableC) {
        pattern.expr("Filter using " + predicate, leftJoinVariableA, leftJoinVariableB, leftJoinVariableC,
                (a, leftJoinVarA, leftJoinVarB, leftJoinVarC) -> predicate.test(leftJoinVarA, leftJoinVarB, leftJoinVarC,
                        extract(a)));
        return (Child_) this;
    }

    @Override
    public final <LeftJoinVar_> Child_ filterForJoin(Variable<LeftJoinVar_> leftJoinVar,
            DefaultBiJoiner<LeftJoinVar_, A> joiner, JoinerType joinerType, int mappingIndex) {
        Function1<LeftJoinVar_, Object> leftMapping = internalsFactory.convert(joiner.getLeftMapping(mappingIndex));
        Function1<A, Object> rightMapping = internalsFactory.convert(joiner.getRightMapping(mappingIndex));
        Function1<PatternVar_, Object> rightExtractor = b -> rightMapping.apply(extract(b));
        Predicate2<PatternVar_, LeftJoinVar_> predicate =
                (b, a) -> joinerType.matches(leftMapping.apply(a), rightExtractor.apply(b));
        BetaIndex<PatternVar_, LeftJoinVar_, ?> index =
                createBetaIndex(joinerType, mappingIndex, leftMapping, rightExtractor);
        pattern.expr("Join using joiner #" + mappingIndex + " in " + joiner, leftJoinVar, predicate, index);
        return (Child_) this;
    }

    private <LeftJoinVar_> BetaIndex<PatternVar_, LeftJoinVar_, ?> createBetaIndex(JoinerType joinerType, int mappingIndex,
            Function1<LeftJoinVar_, Object> leftMapping, Function1<PatternVar_, Object> rightExtractor) {
        if (joinerType == JoinerType.EQUAL) {
            return betaIndexedBy(Object.class, getConstraintType(joinerType), mappingIndex, rightExtractor, leftMapping,
                    Object.class);
        } else { // Drools beta index on LT/LTE/GT/GTE requires Comparable.
            JoinerType reversedJoinerType = joinerType.flip();
            // TODO fix the Comparable
            return betaIndexedBy(Comparable.class, getConstraintType(reversedJoinerType), mappingIndex,
                    c -> (Comparable) rightExtractor.apply(c), leftMapping, Comparable.class);
        }
    }

    @Override
    public final <LeftJoinVarA_, LeftJoinVarB_> Child_ filterForJoin(Variable<LeftJoinVarA_> leftJoinVarA,
            Variable<LeftJoinVarB_> leftJoinVarB, DefaultTriJoiner<LeftJoinVarA_, LeftJoinVarB_, A> joiner,
            JoinerType joinerType, int mappingIndex) {
        Function2<LeftJoinVarA_, LeftJoinVarB_, Object> leftMapping =
                internalsFactory.convert(joiner.getLeftMapping(mappingIndex));
        Function1<A, Object> rightMapping = internalsFactory.convert(joiner.getRightMapping(mappingIndex));
        Function1<PatternVar_, Object> rightExtractor = b -> rightMapping.apply(extract(b));
        Predicate3<PatternVar_, LeftJoinVarA_, LeftJoinVarB_> predicate =
                (c, a, b) -> joinerType.matches(leftMapping.apply(a, b), rightExtractor.apply(c));
        BetaIndex2<PatternVar_, LeftJoinVarA_, LeftJoinVarB_, ?> index =
                createBetaIndex(joinerType, mappingIndex, leftMapping, rightExtractor);
        pattern.expr("Join using joiner #" + mappingIndex + " in " + joiner, leftJoinVarA, leftJoinVarB, predicate, index);
        return (Child_) this;
    }

    private <LeftJoinVarA_, LeftJoinVarB_> BetaIndex2<PatternVar_, LeftJoinVarA_, LeftJoinVarB_, ?> createBetaIndex(
            JoinerType joinerType, int mappingIndex, Function2<LeftJoinVarA_, LeftJoinVarB_, Object> leftMapping,
            Function1<PatternVar_, Object> rightExtractor) {
        if (joinerType == JoinerType.EQUAL) {
            return betaIndexedBy(Object.class, getConstraintType(joinerType), mappingIndex, rightExtractor, leftMapping,
                    Object.class);
        } else { // Drools beta index on LT/LTE/GT/GTE requires Comparable.
            JoinerType reversedJoinerType = joinerType.flip();
            // TODO fix the Comparable
            return betaIndexedBy(Comparable.class, getConstraintType(reversedJoinerType), mappingIndex,
                    c -> (Comparable) rightExtractor.apply(c), leftMapping, Comparable.class);
        }
    }

    @Override
    public final <LeftJoinVarA_, LeftJoinVarB_, LeftJoinVarC_> Child_ filterForJoin(Variable<LeftJoinVarA_> leftJoinVarA,
            Variable<LeftJoinVarB_> leftJoinVarB, Variable<LeftJoinVarC_> leftJoinVarC,
            DefaultQuadJoiner<LeftJoinVarA_, LeftJoinVarB_, LeftJoinVarC_, A> joiner, JoinerType joinerType,
            int mappingIndex) {
        Function3<LeftJoinVarA_, LeftJoinVarB_, LeftJoinVarC_, Object> leftMapping =
                internalsFactory.convert(joiner.getLeftMapping(mappingIndex));
        Function1<A, Object> rightMapping = internalsFactory.convert(joiner.getRightMapping(mappingIndex));
        Function1<PatternVar_, Object> rightExtractor = b -> rightMapping.apply(extract(b));
        Predicate4<PatternVar_, LeftJoinVarA_, LeftJoinVarB_, LeftJoinVarC_> predicate =
                (d, a, b, c) -> joinerType.matches(leftMapping.apply(a, b, c), rightExtractor.apply(d));
        BetaIndex3<PatternVar_, LeftJoinVarA_, LeftJoinVarB_, LeftJoinVarC_, ?> index =
                createBetaIndex(joinerType, mappingIndex, leftMapping, rightExtractor);
        pattern.expr("Join using joiner #" + mappingIndex + " in " + joiner, leftJoinVarA, leftJoinVarB,
                leftJoinVarC, predicate, index);
        return (Child_) this;
    }

    private <LeftJoinVarA_, LeftJoinVarB_, LeftJoinVarC_>
            BetaIndex3<PatternVar_, LeftJoinVarA_, LeftJoinVarB_, LeftJoinVarC_, ?> createBetaIndex(JoinerType joinerType,
                    int mappingIndex, Function3<LeftJoinVarA_, LeftJoinVarB_, LeftJoinVarC_, Object> leftMapping,
                    Function1<PatternVar_, Object> rightExtractor) {
        if (joinerType == JoinerType.EQUAL) {
            return betaIndexedBy(Object.class, getConstraintType(joinerType), mappingIndex, rightExtractor, leftMapping,
                    Object.class);
        } else { // Drools beta index on LT/LTE/GT/GTE requires Comparable.
            JoinerType reversedJoinerType = joinerType.flip();
            // TODO fix the Comparable
            return betaIndexedBy(Comparable.class, getConstraintType(reversedJoinerType), mappingIndex,
                    c -> (Comparable) rightExtractor.apply(c), leftMapping, Comparable.class);
        }
    }

    @Override
    public final <BoundVar_> Child_ bind(Variable<BoundVar_> boundVariable, Function1<A, BoundVar_> bindingFunction) {
        pattern.bind(boundVariable, a -> bindingFunction.apply(extract(a)));
        return (Child_) this;
    }

    @Override
    public final List<ViewItem<?>> build() {
        Stream<ViewItem<?>> prerequisites = prerequisiteExpressions.stream();
        Stream<ViewItem<?>> dependents = dependentExpressions.stream();
        return Stream.concat(Stream.concat(prerequisites, Stream.of(pattern)), dependents)
                .collect(Collectors.toList());
    }
}
