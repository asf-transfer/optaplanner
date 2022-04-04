/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.constraint.streams;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.optaplanner.constraint.streams.bi.DefaultBiJoiner;
import org.optaplanner.constraint.streams.bi.FilteringBiJoiner;
import org.optaplanner.constraint.streams.penta.DefaultPentaJoiner;
import org.optaplanner.constraint.streams.penta.FilteringPentaJoiner;
import org.optaplanner.constraint.streams.quad.DefaultQuadJoiner;
import org.optaplanner.constraint.streams.quad.FilteringQuadJoiner;
import org.optaplanner.constraint.streams.tri.DefaultTriJoiner;
import org.optaplanner.constraint.streams.tri.FilteringTriJoiner;
import org.optaplanner.core.api.function.PentaPredicate;
import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.function.QuadPredicate;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.function.TriPredicate;
import org.optaplanner.core.api.score.stream.bi.BiJoiner;
import org.optaplanner.core.api.score.stream.penta.PentaJoiner;
import org.optaplanner.core.api.score.stream.quad.QuadJoiner;
import org.optaplanner.core.api.score.stream.tri.TriJoiner;
import org.optaplanner.core.impl.score.stream.JoinerService;
import org.optaplanner.core.impl.score.stream.JoinerType;

/**
 * In order to facilitate node-sharing,
 * this class needs to ensure that the same joiner instance is returned for the same inputs.
 * (For example, if {@link #newBiJoiner(BiPredicate)} is used with the same predicate twice,
 * only a single instance of a {@link BiJoiner} is returned.
 */
public final class DefaultJoinerService implements JoinerService {

    private final Map<Set<Object>, Object> joinerCache = new HashMap<>();

    @Override
    public <A, B> BiJoiner<A, B> newBiJoiner(BiPredicate<A, B> filter) {
        return cache(s -> new FilteringBiJoiner<>(filter), filter);
    }

    /**
     * Cache a joiner on first access.
     *
     * @param constructor used to cache the joiner
     * @param data forms a {@link Set} of objects used as cache key
     * @param <Joiner_>
     * @return never null
     */
    private <Joiner_> Joiner_ cache(Function<Set<Object>, Joiner_> constructor, Object... data) {
        /*
         * We want to use Java's immutable sets for storage efficiency,
         * but those do not allow for duplicate elements at construction time.
         * So we use this workaround, which handles duplicate elements correctly.
         */
        Set<Object> cacheKey = Set.copyOf(Arrays.asList(data));
        // Finally we cache the joiner.
        return (Joiner_) joinerCache.computeIfAbsent(cacheKey, constructor);
    }

    @Override
    public <A, B, Property_> BiJoiner<A, B> newBiJoiner(Function<A, Property_> leftMapping, JoinerType joinerType,
            Function<B, Property_> rightMapping) {
        return cache(s -> new DefaultBiJoiner<>(leftMapping, joinerType, rightMapping),
                leftMapping, rightMapping, joinerType);
    }

    @Override
    public <A, B, C> TriJoiner<A, B, C> newTriJoiner(TriPredicate<A, B, C> filter) {
        return cache(s -> new FilteringTriJoiner<>(filter), filter);
    }

    @Override
    public <A, B, C, Property_> TriJoiner<A, B, C> newTriJoiner(BiFunction<A, B, Property_> leftMapping, JoinerType joinerType,
            Function<C, Property_> rightMapping) {
        return cache(s -> new DefaultTriJoiner<>(leftMapping, joinerType, rightMapping),
                leftMapping, rightMapping, joinerType);
    }

    @Override
    public <A, B, C, D> QuadJoiner<A, B, C, D> newQuadJoiner(QuadPredicate<A, B, C, D> filter) {
        return cache(s -> new FilteringQuadJoiner<>(filter), filter);
    }

    @Override
    public <A, B, C, D, Property_> QuadJoiner<A, B, C, D> newQuadJoiner(TriFunction<A, B, C, Property_> leftMapping,
            JoinerType joinerType, Function<D, Property_> rightMapping) {
        return cache(s -> new DefaultQuadJoiner<>(leftMapping, joinerType, rightMapping),
                leftMapping, rightMapping, joinerType);
    }

    @Override
    public <A, B, C, D, E> PentaJoiner<A, B, C, D, E> newPentaJoiner(PentaPredicate<A, B, C, D, E> filter) {
        return cache(s -> new FilteringPentaJoiner<>(filter), filter);
    }

    @Override
    public <A, B, C, D, E, Property_> PentaJoiner<A, B, C, D, E> newPentaJoiner(QuadFunction<A, B, C, D, Property_> leftMapping,
            JoinerType joinerType, Function<E, Property_> rightMapping) {
        return cache(s -> new DefaultPentaJoiner<>(leftMapping, joinerType, rightMapping),
                leftMapping, rightMapping, joinerType);
    }
}
