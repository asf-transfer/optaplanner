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

package org.optaplanner.core.impl.score.stream.drools.model.nodes;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;

import org.optaplanner.core.impl.score.stream.common.AbstractJoiner;

public abstract class AbstractConstraintModelJoiningNode<OtherFactType_, JoinerType_ extends AbstractJoiner>
        extends AbstractConstraintModelChildNode implements Supplier<JoinerType_> {

    private final Class<OtherFactType_> otherFactType;
    private final JoinerType_ joiner;

    AbstractConstraintModelJoiningNode(Class<OtherFactType_> otherFactType, JoinerType_ joiner,
            ConstraintModelNodeType type) {
        super(type);
        if (type != ConstraintModelNodeType.IF_EXISTS && type != ConstraintModelNodeType.IF_NOT_EXISTS &&
                type != ConstraintModelNodeType.JOIN) {
            throw new IllegalStateException("Given node type (" + type + ") is not one of the join types.");
        }
        this.otherFactType = requireNonNull(otherFactType);
        this.joiner = requireNonNull(joiner);
    }

    public final Class<OtherFactType_> getOtherFactType() {
        return otherFactType;
    }

    @Override
    public final JoinerType_ get() {
        return joiner;
    }
}
