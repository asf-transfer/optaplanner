package org.optaplanner.core.api.domain.solution;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.optaplanner.core.api.domain.constraintweight.ConstraintConfiguration;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.solver.change.ProblemChange;

/**
 * Specifies that a property (or a field) on a {@link PlanningSolution} class is a problem fact.
 * A problem fact must not change during solving (except through a {@link ProblemChange} event).
 * <p>
 * The constraints in a {@link ConstraintProvider} rely on problem facts for {@link ConstraintFactory#forEach(Class)}.
 * Alternatively, scoreDRL relies on problem facts too.
 * <p>
 * Do not annotate a {@link PlanningEntity planning entity} or a {@link ConstraintConfiguration planning paramerization}
 * as a problem fact: they are automatically available as facts for {@link ConstraintFactory#forEach(Class)} or DRL.
 *
 * @see ProblemFactCollectionProperty
 */
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
public @interface ProblemFactProperty {

}
