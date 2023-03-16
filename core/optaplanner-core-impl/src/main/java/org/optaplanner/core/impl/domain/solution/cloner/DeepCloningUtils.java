package org.optaplanner.core.impl.domain.solution.cloner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import org.optaplanner.core.api.domain.solution.cloner.DeepPlanningClone;
import org.optaplanner.core.api.domain.variable.PlanningListVariable;
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
import org.optaplanner.core.impl.domain.common.ReflectionHelper;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.util.Pair;

/**
 * @implNote This class is thread-safe.
 */
public final class DeepCloningUtils {

    // Instances of these classes will never be deep-cloned.
    public static final Set<Class<?>> IMMUTABLE_CLASSES = Set.of(
            // Numbers
            Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, BigInteger.class, BigDecimal.class,
            // Optional
            Optional.class, OptionalInt.class, OptionalLong.class, OptionalDouble.class,
            // Date and time
            Duration.class, Instant.class, LocalDate.class, LocalDateTime.class, LocalTime.class, MonthDay.class,
            OffsetDateTime.class, OffsetTime.class, Period.class, Year.class, YearMonth.class, ZonedDateTime.class,
            ZoneId.class, ZoneOffset.class,
            // Others
            Boolean.class, Character.class, String.class, UUID.class,
            // Score
            SimpleScore.class, SimpleLongScore.class, SimpleBigDecimalScore.class,
            HardSoftScore.class, HardSoftLongScore.class, HardSoftBigDecimalScore.class,
            HardMediumSoftScore.class, HardMediumSoftLongScore.class, HardMediumSoftBigDecimalScore.class,
            BendableScore.class, BendableLongScore.class, BendableBigDecimalScore.class);

    private final ConcurrentMap<Pair<Field, Class<?>>, Boolean> fieldDeepClonedMemoization = new ConcurrentMemoization<>();
    private final ConcurrentMap<Class<?>, Boolean> actualValueClassDeepClonedMemoization = new ConcurrentMemoization<>();
    private final SolutionDescriptor<?> solutionDescriptor;

    public DeepCloningUtils(SolutionDescriptor<?> solutionDescriptor) {
        this.solutionDescriptor = solutionDescriptor;
    }

    public SolutionDescriptor<?> getSolutionDescriptor() {
        return solutionDescriptor;
    }

    /**
     * Gets the deep cloning decision for a particular value assigned to a field,
     * memoizing the result.
     *
     * @param field the field to get the deep cloning decision of
     * @param owningClass the class that owns the field; can be different
     *        from the field's declaring class (ex: subclass)
     * @param actualValueClass the class of the value that is currently assigned
     *        to the field; can be different from the field type
     *        (ex: for the field "List myList", the actual value
     *        class might be ArrayList).
     * @return true iff the field should be deep cloned with a particular value.
     */
    public boolean getDeepCloneDecision(Field field, Class<?> owningClass, Class<?> actualValueClass) {
        if (retrieveDeepCloneDecisionForActualValueClass(actualValueClass)) {
            return true;
        }
        Pair<Field, Class<?>> pair = Pair.of(field, owningClass);
        return fieldDeepClonedMemoization.computeIfAbsent(pair,
                k -> isFieldDeepCloned(solutionDescriptor, k.getKey(), k.getValue()));
    }

    boolean retrieveDeepCloneDecisionForActualValueClass(Class<?> actualValueClass) {
        return actualValueClassDeepClonedMemoization.computeIfAbsent(actualValueClass, k -> isClassDeepCloned(solutionDescriptor, k));
    }

    /**
     * Gets the deep cloning decision for a field.
     *
     * @param field The field to get the deep cloning decision of
     * @param owningClass The class that owns the field; can be different
     *        from the field's declaring class (ex: subclass).
     * @return True iff the field should always be deep cloned (regardless of value).
     */
    public static boolean isFieldDeepCloned(SolutionDescriptor<?> solutionDescriptor, Field field, Class<?> owningClass) {
        Class<?> fieldType = field.getType();
        if (isImmutable(fieldType)) {
            return false;
        }
        return isFieldAnEntityPropertyOnSolution(solutionDescriptor, field, owningClass)
                || isFieldAnEntityOrSolution(solutionDescriptor, field)
                || isFieldAPlanningListVariable(field, owningClass)
                || isFieldADeepCloneProperty(field, owningClass);
    }

    static boolean isImmutable(Class<?> clz) {
        if (clz.isPrimitive() || clz.isEnum()) {
            return true;
        } else
            return IMMUTABLE_CLASSES.contains(clz);
    }

    /**
     * Return true only if a field represent an entity property on the solution class.
     * An entity property is one who type is a PlanningEntity or a collection
     * of PlanningEntity.
     *
     * @param field The field to get the deep cloning decision of
     * @param owningClass The class that owns the field; can be different
     *        from the field's declaring class (ex: subclass).
     * @return True only if the field is an entity property on the solution class.
     *         May return false if the field getter/setter is complex.
     */
    static boolean isFieldAnEntityPropertyOnSolution(SolutionDescriptor<?> solutionDescriptor, Field field, Class<?> owningClass) {
        if (!solutionDescriptor.getSolutionClass().isAssignableFrom(owningClass)) {
            return false;
        }

        // field.getDeclaringClass() is a superclass of or equal to the owningClass
        String fieldName = field.getName();
        // This assumes we're dealing with a simple getter/setter.
        // If that assumption is false, validateCloneSolution(...) fails-fast.
        if (solutionDescriptor.getEntityMemberAccessorMap().get(fieldName) != null) {
            return true;
        }
        // This assumes we're dealing with a simple getter/setter.
        // If that assumption is false, validateCloneSolution(...) fails-fast.
        return solutionDescriptor.getEntityCollectionMemberAccessorMap().get(fieldName) != null;
    }

    /**
     * Returns true iff a field represent an Entity/Solution or a collection
     * of Entity/Solution.
     *
     * @param field The field to get the deep cloning decision of
     * @return True only if the field represents or contains a PlanningEntity or PlanningSolution
     */
    private static boolean isFieldAnEntityOrSolution(SolutionDescriptor<?> solutionDescriptor, Field field) {
        Class<?> type = field.getType();
        if (isClassDeepCloned(solutionDescriptor, type)) {
            return true;
        }
        if (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)) {
            return isTypeArgumentDeepCloned(solutionDescriptor, field.getGenericType());
        } else if (type.isArray()) {
            return isClassDeepCloned(solutionDescriptor, type.getComponentType());
        }
        return false;
    }

    public static boolean isClassDeepCloned(SolutionDescriptor<?> solutionDescriptor, Class<?> type) {
        if (isImmutable(type)) {
            return false;
        }
        return solutionDescriptor.hasEntityDescriptor(type)
                || solutionDescriptor.getSolutionClass().isAssignableFrom(type)
                || type.isAnnotationPresent(DeepPlanningClone.class);
    }

    private static boolean isTypeArgumentDeepCloned(SolutionDescriptor<?> solutionDescriptor, Type genericType) {
        // Check the generic type arguments of the field.
        // It is possible for fields and methods, but not instances.
        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            for (Type actualTypeArgument : parameterizedType.getActualTypeArguments()) {
                if (actualTypeArgument instanceof Class
                        && isClassDeepCloned(solutionDescriptor, (Class<?>) actualTypeArgument)) {
                    return true;
                }
                if (isTypeArgumentDeepCloned(solutionDescriptor, actualTypeArgument)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isFieldADeepCloneProperty(Field field, Class<?> owningClass) {
        if (field.isAnnotationPresent(DeepPlanningClone.class)) {
            return true;
        }
        Method getterMethod = ReflectionHelper.getGetterMethod(owningClass, field.getName());
        return getterMethod != null && getterMethod.isAnnotationPresent(DeepPlanningClone.class);
    }

    private static boolean isFieldAPlanningListVariable(Field field, Class<?> owningClass) {
        if (!field.isAnnotationPresent(PlanningListVariable.class)) {
            Method getterMethod = ReflectionHelper.getGetterMethod(owningClass, field.getName());
            return getterMethod != null && getterMethod.isAnnotationPresent(PlanningListVariable.class);
        } else {
            return true;
        }
    }

}
