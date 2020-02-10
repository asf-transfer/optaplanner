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

package org.optaplanner.core.impl.score.stream.bi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.optaplanner.core.api.score.stream.bi.BiJoiner;
import org.optaplanner.core.impl.score.stream.common.AbstractJoiner;
import org.optaplanner.core.impl.score.stream.common.JoinerType;

public abstract class AbstractBiJoiner<A, B> extends AbstractJoiner<B> implements BiJoiner<A, B> {

    private final BiPredicate<A, B> filter;

    protected AbstractBiJoiner() {
        this.filter = null;
    }

    protected AbstractBiJoiner(BiPredicate<A, B> filter) {
        this.filter = filter;
    }

    @SafeVarargs
    public static <A, B> AbstractBiJoiner<A, B> merge(BiJoiner<A, B>... joiners) {
        List<SingleBiJoiner<A, B>> joinerList = new ArrayList<>(joiners.length);
        for (BiJoiner<A, B> joiner : joiners) {
            if (joiner instanceof NoneBiJoiner) {
                // Ignore it
            } else if (joiner instanceof SingleBiJoiner) {
                joinerList.add((SingleBiJoiner<A, B>) joiner);
            } else if (joiner instanceof CompositeBiJoiner) {
                joinerList.addAll(((CompositeBiJoiner<A, B>) joiner).getJoinerList());
            } else {
                // Filtering joiners are merged by composing their filter lambdas.
                throw new IllegalArgumentException("The joiner class (" + joiner.getClass() + ") is not supported.");
            }
        }
        if (joinerList.isEmpty()) {
            return new NoneBiJoiner<>();
        } else if (joinerList.size() == 1) {
            return joinerList.get(0);
        }
        return new CompositeBiJoiner<>(joinerList);
    }

    public boolean matches(A a, B b) {
        JoinerType[] joinerTypes = getJoinerTypes();
        for (int i = 0; i < joinerTypes.length; i++) {
            JoinerType joinerType = joinerTypes[i];
            Object leftMapping = getLeftMapping(i).apply(a);
            Object rightMapping = getRightMapping(i).apply(b);
            if (!joinerType.matches(leftMapping, rightMapping)) {
                return false;
            }
        }
        return true;
    }

    public abstract Function<A, Object> getLeftMapping(int index);

    public Function<A, Object[]> getLeftCombinedMapping() {
        Function<A, Object>[] mappings = IntStream.range(0, getJoinerTypes().length)
                .mapToObj(this::getLeftMapping)
                .toArray(Function[]::new);
        return (A a) -> Arrays.stream(mappings)
                .map(f -> f.apply(a))
                .toArray();
    }

    public BiPredicate<A, B> getFilter() {
        return filter;
    }

}
