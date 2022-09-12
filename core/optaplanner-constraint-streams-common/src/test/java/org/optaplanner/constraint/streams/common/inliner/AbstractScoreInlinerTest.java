package org.optaplanner.constraint.streams.common.inliner;

import java.util.Objects;

import org.optaplanner.constraint.streams.common.AbstractConstraint;
import org.optaplanner.constraint.streams.common.InnerConstraintFactory;
import org.optaplanner.constraint.streams.common.ScoreImpactType;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.DefaultConstraintJustification;
import org.optaplanner.core.api.score.stream.uni.UniConstraintStream;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;

public abstract class AbstractScoreInlinerTest<Solution_, Score_ extends Score<Score_>> {

    protected static final JustificationsSupplier EMPTY_JUSTIFICATIONS_SUPPLIER = DefaultConstraintJustification::of;

    protected final boolean constraintMatchEnabled = true;
    private final TestConstraintFactory<Solution_, Score_> constraintFactory =
            new TestConstraintFactory<>(buildSolutionDescriptor());

    abstract protected SolutionDescriptor<Solution_> buildSolutionDescriptor();

    protected TestConstraint<Solution_, Score_> buildConstraint(Score_ constraintWeight) {
        return new TestConstraint<>(constraintFactory, "Test Constraint", constraintWeight);
    }

    public static final class TestConstraintFactory<Solution_, Score_ extends Score<Score_>>
            extends InnerConstraintFactory<Solution_, TestConstraint<Solution_, Score_>> {

        private final SolutionDescriptor<Solution_> solutionDescriptor;

        public TestConstraintFactory(SolutionDescriptor<Solution_> solutionDescriptor) {
            this.solutionDescriptor = Objects.requireNonNull(solutionDescriptor);
        }

        @Override
        public SolutionDescriptor<Solution_> getSolutionDescriptor() {
            return solutionDescriptor;
        }

        @Override
        public String getDefaultConstraintPackage() {
            return "constraintPackage";
        }

        @Override
        public <A> UniConstraintStream<A> forEachIncludingNullVars(Class<A> sourceClass) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <A> UniConstraintStream<A> fromUnfiltered(Class<A> fromClass) {
            throw new UnsupportedOperationException();
        }
    };

    public static final class TestConstraint<Solution_, Score_ extends Score<Score_>>
            extends AbstractConstraint<Solution_, TestConstraint<Solution_, Score_>, TestConstraintFactory<Solution_, Score_>> {

        protected TestConstraint(TestConstraintFactory<Solution_, Score_> constraintFactory, String constraintName,
                Score_ constraintWeight) {
            super(constraintFactory, constraintFactory.getDefaultConstraintPackage(), constraintName,
                    solution -> constraintWeight, ScoreImpactType.REWARD, false, null);
        }
    }

}
