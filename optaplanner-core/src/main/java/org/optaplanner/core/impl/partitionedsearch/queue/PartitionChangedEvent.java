/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.partitionedsearch.queue;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.impl.partitionedsearch.scope.PartitionChangeMove;

/**
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 */
public final class PartitionChangedEvent<Solution_> {

    private final int partIndex;
    private final long eventIndex;
    private final PartitionChangedEventType type;
    private final PartitionChangeMove<Solution_> move;
    private final Throwable throwable;

    public PartitionChangedEvent(int partIndex, long eventIndex, PartitionChangedEventType type) {
        this.partIndex = partIndex;
        this.eventIndex = eventIndex;
        this.type = type;
        move = null;
        throwable = null;
    }

    public PartitionChangedEvent(int partIndex, long eventIndex, PartitionChangeMove<Solution_>  move) {
        this.partIndex = partIndex;
        this.eventIndex = eventIndex;
        type = PartitionChangedEventType.MOVE;
        this.move = move;
        throwable = null;
    }

    public PartitionChangedEvent(int partIndex, long eventIndex, Throwable throwable) {
        this.partIndex = partIndex;
        this.eventIndex = eventIndex;
        type = PartitionChangedEventType.EXCEPTION_THROWN;
        move = null;
        this.throwable = throwable;
    }

    public int getPartIndex() {
        return partIndex;
    }

    public Long getEventIndex() {
        return eventIndex;
    }

    public PartitionChangedEventType getType() {
        return type;
    }

    public PartitionChangeMove<Solution_>  getMove() {
        return move;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public enum PartitionChangedEventType {
        MOVE,
        FINISHED,
        EXCEPTION_THROWN;
    }

}
