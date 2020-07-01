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

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.util.function.BiFunction;

import org.optaplanner.core.api.score.stream.bi.BiConstraintCollector;

final class BiToBiGroupingNode<A, B, NewA, NewB>
        extends AbstractConstraintModelGroupingNode<BiFunction<A, B, ?>, BiConstraintCollector<A, B, ?, ?>>
        implements BiConstraintModelNode<NewA, NewB> {

    BiToBiGroupingNode(BiFunction<A, B, NewA> aMapping, BiFunction<A, B, NewB> bMapping) {
        super(asList(aMapping, bMapping), emptyList());
    }

    BiToBiGroupingNode(BiFunction<A, B, NewA> mapping, BiConstraintCollector<A, B, ?, NewB> collector) {
        super(singletonList(mapping), singletonList(collector));
    }

}
