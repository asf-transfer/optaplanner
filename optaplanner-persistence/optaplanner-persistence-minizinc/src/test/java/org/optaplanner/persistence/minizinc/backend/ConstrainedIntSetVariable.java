/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.persistence.minizinc.backend;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

// Copy of MyIntSetVariable, due to how model is generated
@PlanningEntity
public class ConstrainedIntSetVariable implements IntSetVariable {

    @PlanningVariable(valueRangeProviderRefs = { "intSetValueRange" })
    IntSet value;

    public ConstrainedIntSetVariable() {
        this(new IntSet());
    }

    public ConstrainedIntSetVariable(int... values) {
        this(new IntSet(values));
    }

    public ConstrainedIntSetVariable(IntSet value) {
        this.value = value;
    }

    @Override
    public IntSet getValue() {
        return value;
    }
}
