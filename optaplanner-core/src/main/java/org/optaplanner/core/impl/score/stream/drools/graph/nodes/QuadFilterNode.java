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

package org.optaplanner.core.impl.score.stream.drools.graph.nodes;

import java.util.Objects;
import java.util.function.Supplier;

import org.optaplanner.core.api.function.QuadPredicate;

final class QuadFilterNode<A, B, C, D> extends AbstractConstraintModelChildNode
        implements QuadConstraintGraphNode<A, B, C, D>, Supplier<QuadPredicate<A, B, C, D>> {

    private final QuadPredicate<A, B, C, D> predicate;

    QuadFilterNode(QuadPredicate<A, B, C, D> predicate) {
        super(ConstraintGraphNodeType.FILTER);
        this.predicate = Objects.requireNonNull(predicate);
    }

    @Override
    public QuadPredicate<A, B, C, D> get() {
        return predicate;
    }
}
