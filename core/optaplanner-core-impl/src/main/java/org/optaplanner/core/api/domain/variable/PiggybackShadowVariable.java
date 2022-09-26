package org.optaplanner.core.api.domain.variable;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.optaplanner.core.api.domain.entity.PlanningEntity;

/**
 * Specifies that a bean property (or a field) is a custom shadow of 1 or more {@link PlanningVariable}s or
 * {@link PlanningListVariable}s.
 * <p>
 * It is specified on a getter of a java bean property (or a field) of a {@link PlanningEntity} class.
 * <p>
 * TODO expand the Javadoc.
 */
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
public @interface PiggybackShadowVariable {

    /**
     * The {@link PlanningEntity} class of the planning variable.
     * <p>
     * Specified if the planning variable is on a different {@link Class} than the class that uses this referencing annotation.
     *
     * @return {@link NullEntityClass} when it is null (workaround for annotation limitation).
     *         Defaults to the same {@link Class} as the one that uses this annotation.
     */
    Class<?> entityClass() default NullEntityClass.class;

    /**
     * The source variable name.
     *
     * @return never null, a genuine or shadow variable name
     */
    String variableName();

    /** Workaround for annotation limitation in {@link #entityClass()}. */
    interface NullEntityClass {
    }
}
