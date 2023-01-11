package org.optaplanner.constraint.streams.drools;

import static org.drools.model.DSL.declarationOf;
import static org.drools.model.DSL.from;
import static org.drools.model.functions.Predicate1.Impl;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import org.drools.model.DSL;
import org.drools.model.Variable;
import org.drools.model.functions.Function1;
import org.drools.model.functions.Function2;
import org.drools.model.functions.Function3;
import org.drools.model.functions.Function4;
import org.drools.model.functions.Predicate1;
import org.drools.model.functions.Predicate2;
import org.drools.model.functions.Predicate3;
import org.drools.model.functions.Predicate4;
import org.drools.model.functions.Predicate5;
import org.optaplanner.core.api.function.PentaPredicate;
import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.function.QuadPredicate;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.function.TriPredicate;

/**
 * Used to {@link #createVariable(String) generate Drools variables} with unique names,
 * and to {@link #convert(Predicate) convert} some Java interfaces to their Drools equivalents.
 *
 * <p>
 * There is one crucial difference between {@link Function} and {@link Function1}.
 * Java's lambdas do not implement equality - even two method references pointing to the same method
 * will not produce the same object, or even two objects that would equal.
 * This is where Drools interfaces (such as {@link Predicate1} or {@link Function1} are different.
 * They use bytecode of the lambda to figure out if two lambda instances are equal.
 *
 * <p>
 * In order to help with Drools node sharing,
 * we use methods such as {@link #initPredicate(Predicate1)} and {@link #convert(Predicate)}
 * to make sure that every time the same lambda instance is passed,
 * it results in the same Drools type instance.
 * This does not help in the case described above,
 * where people pass the same method reference twice;
 * those will still be converted to two different {@link Function1} instances,
 * as the originals aren't equal either.
 * But it helps in situations where two rules are generated from the same streams,
 * in which case the references will be the same and we want to ensure that the matching Drools types are the same too.
 */
public final class DroolsInternalsFactory {

    private final AtomicLong counter = new AtomicLong(0);
    private final Map<Object, Object> instanceCacheMap = new IdentityHashMap<>();

    DroolsInternalsFactory() {
        // No external instances.
    }

    private String generateUniqueId(String baseName) {
        return baseName + "_" + counter.incrementAndGet();
    }

    /**
     * Declare a new {@link Variable} with a given name and no declared source.
     * Delegates to {@link DSL#declarationOf(Class, String)}.
     *
     * @param clz type of the variable. Using {@link Object} will work in all cases, but Drools will spend unnecessary
     *        amount of time looking up applicable instances of that variable, as it has to traverse instances of all
     *        types in the working memory. Therefore, it is desirable to be as specific as possible.
     * @param baseName name of the variable, mostly useful for debugging purposes. Will be decorated by a numeric
     *        identifier to prevent multiple variables of the same name to exist within left-hand side of a single rule.
     * @param <U> generic type of the input variable
     * @param <V> generic type of the output variable
     * @return new variable declaration, not yet bound to anything
     */
    public <U, V extends U> Variable<V> createVariable(Class<U> clz, String baseName) {
        return (Variable<V>) declarationOf(clz, generateUniqueId(baseName));
    }

    /**
     * Declares a new {@link Object}-typed variable, see {@link #createVariable(Class, String)} for details.
     */
    public <U> Variable<U> createVariable(String baseName) {
        return createVariable(Object.class, baseName);
    }

    /**
     * Declare a new {@link Variable} with a given name, which will hold the result of applying a given mapping
     * function on values of the provided variables.
     *
     * @param baseName name of the variable, mostly useful for debugging purposes. Will be decorated by a numeric
     *        identifier to prevent multiple variables of the same name to exist within left-hand side of a single rule.
     * @param source1 never null; value of this is passed to the mapping function
     * @param source2 never null; value of this is passed to the mapping function
     * @param mapping never null
     * @param <U> generic type of the first input variable
     * @param <V> generic type of the second input variable
     * @param <Result_> generic type of the new variable
     * @return never null
     */
    public <U, V, Result_> Variable<Result_> createVariable(String baseName, Variable<U> source1, Variable<V> source2,
            Function2<U, V, Result_> mapping) {
        return (Variable<Result_>) declarationOf(Object.class, generateUniqueId(baseName),
                from(source1, source2, (value1, value2) -> {
                    Result_ result = mapping.apply(value1, value2);
                    if (result instanceof Iterable) { // Avoid flattening, which is a default from() behavior.
                        return Collections.singleton(result);
                    }
                    return result;
                }));
    }

    /**
     * As defined by {@link #createVariable(String, Variable, Variable, Function2)}.
     */
    public <U, V, W, Result_> Variable<Result_> createVariable(String baseName, Variable<U> source1, Variable<V> source2,
            Variable<W> source3, Function3<U, V, W, Result_> mapping) {
        return (Variable<Result_>) declarationOf(Object.class, generateUniqueId(baseName),
                from(source1, source2, source3, (value1, value2, value3) -> {
                    Result_ result = mapping.apply(value1, value2, value3);
                    if (result instanceof Iterable) { // Avoid flattening, which is a default from() behavior.
                        return Collections.singleton(result);
                    }
                    return result;
                }));
    }

    /**
     * As defined by {@link #createVariable(String, Variable, Variable, Function2)}.
     */
    public <U, V, W, Y, Result_> Variable<Result_> createVariable(String baseName, Variable<U> source1, Variable<V> source2,
            Variable<W> source3, Variable<Y> source4, Function4<U, V, W, Y, Result_> mapping) {
        return (Variable<Result_>) declarationOf(Object.class, generateUniqueId(baseName),
                from(source1, source2, source3, source4, (value1, value2, value3, value4) -> {
                    Result_ result = mapping.apply(value1, value2, value3, value4);
                    if (result instanceof Iterable) { // Avoid flattening, which is a default from() behavior.
                        return Collections.singleton(result);
                    }
                    return result;
                }));
    }

    /**
     * Declare a new {@link Variable} with a given name, which will hold the individual results of applying the given
     * mapping function on the value of the provided variable.
     * Each such result will trigger a single rule firing.
     * (Default behavior of Drools' From node.)
     *
     * @param baseName name of the variable, mostly useful for debugging purposes. Will be decorated by a numeric
     *        identifier to prevent multiple variables of the same name to exist within left-hand side of a single rule.
     * @param source never null; value of this is passed to the mapping function
     * @param mapping never null
     * @param <U> generic type of the input variable
     * @param <Result_> generic type of the new variable
     * @return
     */
    public <U, Result_> Variable<Result_> createFlattenedVariable(String baseName, Variable<U> source,
            Function1<U, Iterable<Result_>> mapping) {
        return (Variable<Result_>) declarationOf(Object.class, generateUniqueId(baseName),
                from(source, mapping::apply)); // By default, from() flattens.
    }

    /**
     * The lambda equality properties are not guaranteed by {@link Predicate1}, but by its {@link Predicate1.Impl}.
     * Since we do not want to expose this implementation detail in the surrounding code,
     * every {@link Predicate1} needs to go through this method to be wrapped.
     *
     * @param predicate never null
     * @return never null, a wrapped and cached instance of the input predicate
     * @param <A>
     */
    public <A> Predicate1<A> initPredicate(Predicate1<A> predicate) {
        if (predicate instanceof Impl) {
            return predicate;
        }
        return (Predicate1<A>) instanceCacheMap.computeIfAbsent(predicate, k -> new Predicate1.Impl<>((Predicate1<A>) k));
    }

    /**
     * As defined by {@link #initPredicate(Predicate1)}.
     */
    public <A, B> Predicate2<A, B> initPredicate(Predicate2<A, B> predicate) {
        if (predicate instanceof Predicate2.Impl) {
            return predicate;
        }
        return (Predicate2<A, B>) instanceCacheMap.computeIfAbsent(predicate, k -> new Predicate2.Impl<>((Predicate2<A, B>) k));
    }

    /**
     * As defined by {@link #initPredicate(Predicate1)}.
     */
    public <A, B, C> Predicate3<A, B, C> initPredicate(Predicate3<A, B, C> predicate) {
        if (predicate instanceof Predicate3.Impl) {
            return predicate;
        }
        return (Predicate3<A, B, C>) instanceCacheMap.computeIfAbsent(predicate,
                k -> new Predicate3.Impl<>((Predicate3<A, B, C>) k));
    }

    /**
     * As defined by {@link #initPredicate(Predicate1)}.
     */
    public <A, B, C, D> Predicate4<A, B, C, D> initPredicate(Predicate4<A, B, C, D> predicate) {
        if (predicate instanceof Predicate4.Impl) {
            return predicate;
        }
        return (Predicate4<A, B, C, D>) instanceCacheMap.computeIfAbsent(predicate,
                k -> new Predicate4.Impl<>((Predicate4<A, B, C, D>) k));
    }

    /**
     * As defined by {@link #initPredicate(Predicate1)}.
     */
    public <A, B, C, D, E> Predicate5<A, B, C, D, E> initPredicate(Predicate5<A, B, C, D, E> predicate) {
        if (predicate instanceof Predicate5.Impl) {
            return predicate;
        }
        return (Predicate5<A, B, C, D, E>) instanceCacheMap.computeIfAbsent(predicate,
                k -> new Predicate5.Impl<>((Predicate5<A, B, C, D, E>) k));
    }

    /**
     * The lambda equality properties are not guaranteed by {@link Function1}, but by its {@link Function1.Impl}.
     * Since we do not want to expose this implementation detail in the surrounding code,
     * every {@link Function1} needs to go through this method to be wrapped.
     *
     * @param function never null
     * @return never null, a wrapped and cached instance of the input function
     * @param <A>
     */
    public <A, Result_> Function1<A, Result_> initFunction(Function1<A, Result_> function) {
        if (function instanceof Function1.Impl) {
            return function;
        }
        return (Function1<A, Result_>) instanceCacheMap.computeIfAbsent(function,
                k -> new Function1.Impl<>((Function1<A, Result_>) k));
    }

    /**
     * As defined by {@link #initFunction(Function1)}.
     */
    public <A, B, Result_> Function2<A, B, Result_> initFunction(Function2<A, B, Result_> function) {
        if (function instanceof Function2.Impl) {
            return function;
        }
        return (Function2<A, B, Result_>) instanceCacheMap.computeIfAbsent(function,
                k -> new Function2.Impl<>((Function2<A, B, Result_>) k));
    }

    /**
     * As defined by {@link #initFunction(Function1)}.
     */
    public <A, B, C, Result_> Function3<A, B, C, Result_> initFunction(Function3<A, B, C, Result_> function) {
        if (function instanceof Function3.Impl) {
            return function;
        }
        return (Function3<A, B, C, Result_>) instanceCacheMap.computeIfAbsent(function,
                k -> new Function3.Impl<>((Function3<A, B, C, Result_>) k));
    }

    /**
     * As defined by {@link #initFunction(Function1)}.
     */
    public <A, B, C, D, Result_> Function4<A, B, C, D, Result_> initFunction(Function4<A, B, C, D, Result_> function) {
        if (function instanceof Function4.Impl) {
            return function;
        }
        return (Function4<A, B, C, D, Result_>) instanceCacheMap.computeIfAbsent(function,
                k -> new Function4.Impl<>((Function4<A, B, C, D, Result_>) k));
    }

    /**
     * Converts Java's {@link Predicate} to Drools' {@link Predicate1},
     * caching the result in the process.
     * The resulting {@link Predicate1} has already gone through {@link #initPredicate(Predicate1)}.
     *
     * @param predicate
     * @return null if predicate is null
     * @param <A>
     */
    public <A> Predicate1<A> convert(Predicate<A> predicate) {
        if (predicate == null) {
            return null;
        }
        return (Predicate1<A>) instanceCacheMap.computeIfAbsent(predicate,
                k -> initPredicate(((Predicate<A>) k)::test));
    }

    /**
     * As defined by {@link #convert(Predicate)}.
     */
    public <A, B> Predicate2<A, B> convert(BiPredicate<A, B> predicate) {
        if (predicate == null) {
            return null;
        }
        return (Predicate2<A, B>) instanceCacheMap.computeIfAbsent(predicate,
                k -> initPredicate(((BiPredicate<A, B>) k)::test));
    }

    /**
     * As defined by {@link #convert(Predicate)}.
     */
    public <A, B, C> Predicate3<A, B, C> convert(TriPredicate<A, B, C> predicate) {
        if (predicate == null) {
            return null;
        }
        return (Predicate3<A, B, C>) instanceCacheMap.computeIfAbsent(predicate,
                k -> initPredicate(((TriPredicate<A, B, C>) k)::test));
    }

    /**
     * As defined by {@link #convert(Predicate)}.
     */
    public <A, B, C, D> Predicate4<A, B, C, D> convert(QuadPredicate<A, B, C, D> predicate) {
        if (predicate == null) {
            return null;
        }
        return (Predicate4<A, B, C, D>) instanceCacheMap.computeIfAbsent(predicate,
                k -> initPredicate(((QuadPredicate<A, B, C, D>) k)::test));
    }

    /**
     * As defined by {@link #convert(Predicate)}.
     */
    public <A, B, C, D, E> Predicate5<A, B, C, D, E> convert(PentaPredicate<A, B, C, D, E> predicate) {
        if (predicate == null) {
            return null;
        }
        return (Predicate5<A, B, C, D, E>) instanceCacheMap.computeIfAbsent(predicate,
                k -> initPredicate(((PentaPredicate<A, B, C, D, E>) k)::test));
    }

    /**
     * Converts Java's {@link Function} to Drools' {@link Function1},
     * caching the result in the process.
     * The resulting {@link Function1} has already gone through {@link #initFunction(Function1)}.
     *
     * @param function
     * @return never null
     * @param <A>
     */
    public <A, Result_> Function1<A, Result_> convert(Function<A, Result_> function) {
        return (Function1<A, Result_>) instanceCacheMap.computeIfAbsent(function,
                k -> initFunction(((Function<A, Result_>) k)::apply));
    }

    /**
     * As defined by {@link #convert(Function)}.
     */
    public <A, B, Result_> Function2<A, B, Result_> convert(BiFunction<A, B, Result_> function) {
        return (Function2<A, B, Result_>) instanceCacheMap.computeIfAbsent(function,
                k -> initFunction(((BiFunction<A, B, Result_>) k)::apply));
    }

    /**
     * As defined by {@link #convert(Function)}.
     */
    public <A, B, C, Result_> Function3<A, B, C, Result_> convert(TriFunction<A, B, C, Result_> function) {
        return (Function3<A, B, C, Result_>) instanceCacheMap.computeIfAbsent(function,
                k -> initFunction(((TriFunction<A, B, C, Result_>) k)::apply));
    }

    /**
     * As defined by {@link #convert(Function)}.
     */
    public <A, B, C, D, Result_> Function4<A, B, C, D, Result_> convert(QuadFunction<A, B, C, D, Result_> function) {
        return (Function4<A, B, C, D, Result_>) instanceCacheMap.computeIfAbsent(function,
                k -> initFunction(((QuadFunction<A, B, C, D, Result_>) k)::apply));
    }

    /**
     * Equivalent to {@link Predicate#and(Predicate)} for Drools' {@link Predicate2}.
     *
     * @param first never null
     * @param second never null
     * @return never null, applies both predicates in sequence
     * @param <A>
     * @param <B>
     */
    public <A, B> Predicate2<A, B> merge(Predicate2<A, B> first, Predicate2<A, B> second) {
        return initPredicate((a, b) -> first.test(a, b) && second.test(a, b));
    }

    /**
     * As defined by {@link #merge(Predicate2, Predicate2)}.
     */
    public <A, B, C> Predicate3<A, B, C> merge(Predicate3<A, B, C> first, Predicate3<A, B, C> second) {
        return initPredicate((a, b, c) -> first.test(a, b, c) && second.test(a, b, c));
    }

    /**
     * As defined by {@link #merge(Predicate2, Predicate2)}.
     */
    public <A, B, C, D> Predicate4<A, B, C, D> merge(Predicate4<A, B, C, D> first, Predicate4<A, B, C, D> second) {
        return initPredicate((a, b, c, d) -> first.test(a, b, c, d) && second.test(a, b, c, d));
    }

    /**
     * As defined by {@link #merge(Predicate2, Predicate2)}.
     */
    public <A, B, C, D, E> Predicate5<A, B, C, D, E> merge(Predicate5<A, B, C, D, E> first, Predicate5<A, B, C, D, E> second) {
        return initPredicate((a, b, c, d, e) -> first.test(a, b, c, d, e) && second.test(a, b, c, d, e));
    }

}
