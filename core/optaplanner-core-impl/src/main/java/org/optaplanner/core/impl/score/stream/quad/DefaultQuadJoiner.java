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

package org.optaplanner.core.impl.score.stream.quad;

import java.util.Arrays;
import java.util.function.Function;

import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.score.stream.quad.QuadJoiner;
import org.optaplanner.core.impl.score.stream.common.AbstractJoiner;
import org.optaplanner.core.impl.score.stream.common.JoinerType;

public final class DefaultQuadJoiner<A, B, C, D> extends AbstractJoiner<D> implements QuadJoiner<A, B, C, D> {

    private final JoinerType[] joinerTypes;
    private final TriFunction<A, B, C, ?>[] leftMappings;
    private final Function<D, ?>[] rightMappings;

    public DefaultQuadJoiner(TriFunction<A, B, C, ?> leftMapping, JoinerType joinerType, Function<D, ?> rightMapping) {
        this.joinerTypes = new JoinerType[] { joinerType };
        this.leftMappings = new TriFunction[] { leftMapping };
        this.rightMappings = new Function[] { rightMapping };
    }

    public DefaultQuadJoiner(QuadJoiner<A, B, C, D>... joiners) {
        this.joinerTypes = Arrays.stream(joiners)
                .map(joiner -> (DefaultQuadJoiner<A, B, C, D>) joiner)
                .flatMap(joiner -> Arrays.stream(joiner.getJoinerTypes()))
                .toArray(JoinerType[]::new);
        this.leftMappings = Arrays.stream(joiners)
                .map(joiner -> (DefaultQuadJoiner<A, B, C, D>) joiner)
                .flatMap(joiner -> {
                    int joinerCount = joiner.getJoinerCount();
                    TriFunction[] mappings = new TriFunction[joinerCount];
                    for (int i = 0; i < joinerCount; i++) {
                        mappings[i] = joiner.getLeftMapping(i);
                    }
                    return Arrays.stream(mappings);
                })
                .toArray(TriFunction[]::new);
        this.rightMappings = Arrays.stream(joiners)
                .map(joiner -> (DefaultQuadJoiner<A, B, C, D>) joiner)
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

    public TriFunction<A, B, C, Object> getLeftMapping(int index) {
        return (TriFunction<A, B, C, Object>) leftMappings[index];
    }

    @Override
    public JoinerType[] getJoinerTypes() {
        return joinerTypes;
    }

    @Override
    public Function<D, Object> getRightMapping(int index) {
        return (Function<D, Object>) rightMappings[index];
    }

    public boolean matches(A a, B b, C c, D d) {
        JoinerType[] joinerTypes = getJoinerTypes();
        for (int i = 0; i < joinerTypes.length; i++) {
            JoinerType joinerType = joinerTypes[i];
            Object leftMapping = getLeftMapping(i).apply(a, b, c);
            Object rightMapping = getRightMapping(i).apply(d);
            if (!joinerType.matches(leftMapping, rightMapping)) {
                return false;
            }
        }
        return true;
    }
}
