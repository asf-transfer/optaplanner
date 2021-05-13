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

package org.optaplanner.core.impl.util;

import java.util.function.Function;

public class Interval<_IntervalValue, _PointValue extends Comparable<_PointValue>> {
    final _IntervalValue value;
    final IntervalSplitPoint<_IntervalValue, _PointValue> startSplitPoint;
    final IntervalSplitPoint<_IntervalValue, _PointValue> endSplitPoint;

    public Interval(_IntervalValue value, Function<_IntervalValue, _PointValue> startMapping, Function<_IntervalValue, _PointValue> endMapping) {
        this.value = value;
        this.startSplitPoint = new IntervalSplitPoint<>(startMapping.apply(value));
        this.endSplitPoint = new IntervalSplitPoint<>(endMapping.apply(value));
    }

    public _IntervalValue getValue() {
        return value;
    }

    public _PointValue getStart() {
        return startSplitPoint.splitPoint;
    }

    public _PointValue getEnd() {
        return endSplitPoint.splitPoint;
    }

    public IntervalSplitPoint<_IntervalValue,_PointValue> getStartSplitPoint() {
        return startSplitPoint;
    }

    public IntervalSplitPoint<_IntervalValue,_PointValue> getEndSplitPoint() {
        return endSplitPoint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Interval<?,?> that = (Interval<?,?>) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(value);
    }

    @Override public String toString() {
        return "Interval{" +
                "value=" + value +
                ", start=" + getStart() +
                ", end=" + getEnd() +
                '}';
    }
}
