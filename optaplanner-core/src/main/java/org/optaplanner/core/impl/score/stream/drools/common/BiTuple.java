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

package org.optaplanner.core.impl.score.stream.drools.common;

import java.util.Objects;

public final class BiTuple<A, B> {
    public final A _1;
    public final B _2;
    private final int hashCode;

    public BiTuple(A _1, B _2) {
        this._1 = _1;
        this._2 = _2;
        this.hashCode = Objects.hash(_1, _2);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !Objects.equals(getClass(), o.getClass())) {
            return false;
        }
        final BiTuple<?, ?> other = (BiTuple<?, ?>) o;
        return Objects.equals(_1, other._1) &&
                Objects.equals(_2, other._2);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "BiTuple(" + _1 + ", " + _2 + ")";
    }
}
