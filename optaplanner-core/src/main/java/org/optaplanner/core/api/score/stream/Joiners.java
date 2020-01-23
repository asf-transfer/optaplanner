/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.api.score.stream;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.score.stream.bi.BiConstraintStream;
import org.optaplanner.core.api.score.stream.bi.BiJoiner;
import org.optaplanner.core.api.score.stream.quad.QuadJoiner;
import org.optaplanner.core.api.score.stream.tri.TriJoiner;
import org.optaplanner.core.api.score.stream.uni.UniConstraintStream;
import org.optaplanner.core.impl.score.stream.bi.FilteringBiJoiner;
import org.optaplanner.core.impl.score.stream.bi.SingleBiJoiner;
import org.optaplanner.core.impl.score.stream.common.JoinerType;
import org.optaplanner.core.impl.score.stream.quad.SingleQuadJoiner;
import org.optaplanner.core.impl.score.stream.tri.SingleTriJoiner;

/**
 * Creates an {@link BiJoiner}, {@link TriJoiner}, ... instance
 * for use in {@link UniConstraintStream#join(Class, BiJoiner)}, ...
 */
public final class Joiners {

    // TODO Support using non-natural comparators, such as lessThan(leftMapping, rightMapping, comparator).

    // ************************************************************************
    // BiJoiner
    // ************************************************************************

    public static <A> BiJoiner<A, A> equal() {
        return equal(Function.identity());
    }

    public static <A, Property_> BiJoiner<A, A> equal(Function<A, Property_> mapping) {
        return equal(mapping, mapping);
    }

    public static <A, B, Property_> BiJoiner<A, B> equal(Function<A, Property_> leftMapping,
            Function <B, Property_> rightMapping) {
        return new SingleBiJoiner<>(leftMapping, JoinerType.EQUAL, rightMapping);
    }

    public static <A, Property_ extends Comparable<Property_>> BiJoiner<A, A> lessThan(Function<A, Property_> mapping) {
        return lessThan(mapping, mapping);
    }

    public static <A, B, Property_ extends Comparable<Property_>> BiJoiner<A, B> lessThan(
            Function<A, Property_> leftMapping, Function<B, Property_> rightMapping) {
        return new SingleBiJoiner<>(leftMapping, JoinerType.LESS_THAN, rightMapping);
    }

    public static <A, Property_ extends Comparable<Property_>> BiJoiner<A, A> lessThanOrEqual(
            Function<A, Property_> mapping) {
        return lessThanOrEqual(mapping, mapping);
    }

    public static <A, B, Property_ extends Comparable<Property_>> BiJoiner<A, B> lessThanOrEqual(
            Function<A, Property_> leftMapping, Function<B, Property_> rightMapping) {
        return new SingleBiJoiner<>(leftMapping, JoinerType.LESS_THAN_OR_EQUAL, rightMapping);
    }

    public static <A, Property_ extends Comparable<Property_>> BiJoiner<A, A> greaterThan(
            Function<A, Property_> mapping) {
        return greaterThan(mapping, mapping);
    }

    public static <A, B, Property_ extends Comparable<Property_>> BiJoiner<A, B> greaterThan(
            Function<A, Property_> leftMapping, Function<B, Property_> rightMapping) {
        return new SingleBiJoiner<>(leftMapping, JoinerType.GREATER_THAN, rightMapping);
    }

    public static <A, Property_ extends Comparable<Property_>> BiJoiner<A, A> greaterThanOrEqual(
            Function<A, Property_> mapping) {
        return greaterThanOrEqual(mapping, mapping);
    }

    public static <A, B, Property_ extends Comparable<Property_>> BiJoiner<A, B> greaterThanOrEqual(
            Function<A, Property_> leftMapping, Function<B, Property_> rightMapping) {
        return new SingleBiJoiner<>(leftMapping, JoinerType.GREATER_THAN_OR_EQUAL, rightMapping);
    }

    /**
     * Applies a filter to the joined tuple, with the semantics of {@link BiConstraintStream#filter(BiPredicate)}.
     *
     * @param filter filter to apply
     * @param <A> type of the first fact in the tuple
     * @param <B> type of the second fact in the tuple
     * @return the joiner
     */
    public static <A, B> BiJoiner<A, B> filtering(BiPredicate<A, B> filter) {
        return new FilteringBiJoiner<>(filter);
    }

    /*
    // TODO implement these joiners
    public static <A, B, Property_> BiJoiner<A, B> containing(
            Function<A, ? extends Collection<Property_>> leftMapping, Function <B, Property_> rightMapping) {
        return new SingleBiJoiner<>(leftMapping, JoinerType.CONTAINING, rightMapping);
    }

    // TODO containedBy (inverse contains relationship)

    public static <A, Property_> BiJoiner<A, A> intersecting(
            Function<A, ? extends Collection<Property_>> mapping) {
        return intersecting(mapping, mapping);
    }

    public static <A, B, Property_> BiJoiner<A, B> intersecting(
            Function<A, ? extends Collection<Property_>> leftMapping,
            Function <B, ? extends Collection<Property_>> rightMapping) {
        return new SingleBiJoiner<>(leftMapping, JoinerType.INTERSECTING, rightMapping);
    }

    public static <A, Property_> BiJoiner<A, A> disjoint(Function<A, ? extends Collection<Property_>> mapping) {
        return disjoint(mapping, mapping);
    }

    public static <A, B, Property_> BiJoiner<A, B> disjoint(Function<A, ? extends Collection<Property_>> leftMapping,
            Function <B, ? extends Collection<Property_>> rightMapping) {
        return new SingleBiJoiner<>(leftMapping, JoinerType.DISJOINT, rightMapping);
    }
    */

    // TODO
    // join(..., planningVariableContainsCached(Talk::getPeriod, (Period a, Period b) -> a.overlaps(b)))
    // get the period value range, does a cartesian product on it, so it maps every period to an overlapping periodList
    // then keep an index from every period to all talks in an overlapping period (possible the same period)

    // ************************************************************************
    // TriJoiner
    // ************************************************************************

    public static <A, B, C, Property_> TriJoiner<A, B, C> equal(BiFunction<A, B, Property_> leftMapping,
            Function<C, Property_> rightMapping) {
        return new SingleTriJoiner<>(leftMapping, JoinerType.EQUAL, rightMapping);
    }

    public static <A, B, C, Property_ extends Comparable<Property_>> TriJoiner<A, B, C> lessThan(
            BiFunction<A, B, Property_> leftMapping, Function<C, Property_> rightMapping) {
        return new SingleTriJoiner<>(leftMapping, JoinerType.LESS_THAN, rightMapping);
    }

    public static <A, B, C, Property_ extends Comparable<Property_>> TriJoiner<A, B, C> lessThanOrEqual(
            BiFunction<A, B, Property_> leftMapping, Function<C, Property_> rightMapping) {
        return new SingleTriJoiner<>(leftMapping, JoinerType.LESS_THAN_OR_EQUAL, rightMapping);
    }

    public static <A, B, C, Property_ extends Comparable<Property_>> TriJoiner<A, B, C> greaterThan(
            BiFunction<A, B, Property_> leftMapping, Function<C, Property_> rightMapping) {
        return new SingleTriJoiner<>(leftMapping, JoinerType.GREATER_THAN, rightMapping);
    }

    public static <A, B, C, Property_ extends Comparable<Property_>> TriJoiner<A, B, C> greaterThanOrEqual(
            BiFunction<A, B, Property_> leftMapping, Function<C, Property_> rightMapping) {
        return new SingleTriJoiner<>(leftMapping, JoinerType.GREATER_THAN_OR_EQUAL, rightMapping);
    }

    // ************************************************************************
    // QuadJoiner
    // ************************************************************************

    public static <A, B, C, D, Property_> QuadJoiner<A, B, C, D> equal(
            TriFunction<A, B, C, Property_> leftMapping, Function <D, Property_> rightMapping) {
        return new SingleQuadJoiner<>(leftMapping, JoinerType.EQUAL, rightMapping);
    }

    public static <A, B, C, D, Property_ extends Comparable<Property_>> QuadJoiner<A, B, C, D> lessThan(
            TriFunction<A, B, C, Property_> leftMapping, Function<D, Property_> rightMapping) {
        return new SingleQuadJoiner<>(leftMapping, JoinerType.LESS_THAN, rightMapping);
    }

    public static <A, B, C, D, Property_ extends Comparable<Property_>> QuadJoiner<A, B, C, D> lessThanOrEqual(
            TriFunction<A, B, C, Property_> leftMapping, Function<D, Property_> rightMapping) {
        return new SingleQuadJoiner<>(leftMapping, JoinerType.LESS_THAN_OR_EQUAL, rightMapping);
    }

    public static <A, B, C, D, Property_ extends Comparable<Property_>> QuadJoiner<A, B, C, D> greaterThan(
            TriFunction<A, B, C, Property_> leftMapping, Function<D, Property_> rightMapping) {
        return new SingleQuadJoiner<>(leftMapping, JoinerType.GREATER_THAN, rightMapping);
    }

    public static <A, B, C, D, Property_ extends Comparable<Property_>> QuadJoiner<A, B, C, D> greaterThanOrEqual(
            TriFunction<A, B, C, Property_> leftMapping, Function<D, Property_> rightMapping) {
        return new SingleQuadJoiner<>(leftMapping, JoinerType.GREATER_THAN_OR_EQUAL, rightMapping);
    }

    private Joiners() {}

}
