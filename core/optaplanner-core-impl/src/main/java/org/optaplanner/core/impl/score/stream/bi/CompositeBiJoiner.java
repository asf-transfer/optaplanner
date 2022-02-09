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

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.optaplanner.core.api.score.stream.bi.BiJoiner;
import org.optaplanner.core.impl.score.stream.common.JoinerType;

public final class CompositeBiJoiner<A, B> extends AbstractBiJoiner<A, B> {

    private final JoinerType[] joinerTypes;
    private final Function<A, ?>[] leftMappings;
    private final Function<B, ?>[] rightMappings;

    public CompositeBiJoiner(BiJoiner<A, B>... joiners) {
        this.joinerTypes = Arrays.stream(joiners)
                .map(joiner -> (AbstractBiJoiner<A, B>) joiner)
                .flatMap(joiner -> Arrays.stream(joiner.getJoinerTypes()))
                .toArray(JoinerType[]::new);
        this.leftMappings = Arrays.stream(joiners)
                .map(joiner -> (AbstractBiJoiner<A, B>) joiner)
                .flatMap(joiner -> {
                    int joinerCount = joiner.getJoinerCount();
                    Function[] mappings = new Function[joinerCount];
                    for (int i = 0; i < joinerCount; i++) {
                        mappings[i] = joiner.getLeftMapping(i);
                    }
                    return Arrays.stream(mappings);
                })
                .toArray(Function[]::new);
        this.rightMappings = Arrays.stream(joiners)
                .map(joiner -> (AbstractBiJoiner<A, B>) joiner)
                .flatMap(joiner -> {
                    int joinerCount = joiner.getJoinerCount();
                    Function[] mappings = new Function[joinerCount];
                    for (int i = 0; i < joinerCount; i++) {
                        mappings[i] = joiner.getRightMapping(i);
                    }
                    return Arrays.stream(mappings);
                })
                .toArray(Function[]::new);
    }

    CompositeBiJoiner(List<SingleBiJoiner<A, B>> joinerList) {
        if (joinerList.isEmpty()) {
            throw new IllegalArgumentException("The joinerList (" + joinerList + ") must not be empty.");
        }
        this.joinerTypes = joinerList.stream()
                .map(SingleBiJoiner::getJoinerType)
                .toArray(JoinerType[]::new);
        this.leftMappings = joinerList.stream()
                .map(SingleBiJoiner::getLeftMapping)
                .toArray(Function[]::new);
        this.rightMappings = joinerList.stream()
                .map(SingleBiJoiner::getRightMapping)
                .toArray(Function[]::new);
    }

    // ************************************************************************
    // Builders
    // ************************************************************************

    @Override
    public Function<A, Object> getLeftMapping(int index) {
        return (Function<A, Object>) leftMappings[index];
    }

    @Override
    public JoinerType[] getJoinerTypes() {
        return joinerTypes;
    }

    @Override
    public Function<B, Object> getRightMapping(int index) {
        return (Function<B, Object>) rightMappings[index];
    }

}
