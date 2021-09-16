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

@PlanningEntity
public class MyBoolArrayVariable implements BoolArrayVariable {

    IndexSet index;

    @PlanningVariable(valueRangeProviderRefs = { "booleanRange" })
    Boolean value;

    public MyBoolArrayVariable() {
    }

    public MyBoolArrayVariable(int index, boolean value) {
        this.index = new IndexSet(index);
        this.value = value;
    }

    @Override
    public IndexSet getIndex(Class<?> markerClass) {
        return index;
    }

    @Override
    public Boolean getValue() {
        return value;
    }
}
