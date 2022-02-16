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

package org.optaplanner.constraint.streams.bavet.common;

import java.util.ArrayDeque;
import java.util.Queue;

import org.optaplanner.constraint.streams.bavet.BavetConstraintSession;

public abstract class BavetAbstractNode implements BavetNode {

    protected final BavetConstraintSession session;
    protected final int nodeIndex;

    protected final Queue<BavetAbstractTuple> dirtyTupleQueue;

    public BavetAbstractNode(BavetConstraintSession session, int nodeIndex) {
        this.session = session;
        this.nodeIndex = nodeIndex;
        dirtyTupleQueue = new ArrayDeque<>(1000);
    }

    public abstract void refresh(BavetAbstractTuple tuple);

    // ************************************************************************
    // Getters/setters
    // ************************************************************************

    @Override
    public final int getNodeIndex() {
        return nodeIndex;
    }

    @Override
    public final void transitionTuple(BavetAbstractTuple tuple, BavetTupleState newState) {
        if (tuple.isDirty()) {
            if (tuple.getState() != newState) {
                if ((tuple.getState() == BavetTupleState.CREATING && newState == BavetTupleState.DYING)) {
                    tuple.setState(BavetTupleState.ABORTING);
                } else if ((tuple.getState() == BavetTupleState.UPDATING && newState == BavetTupleState.DYING)) {
                    tuple.setState(BavetTupleState.DYING);
                } else {
                    throw new IllegalStateException("The tuple (" + tuple
                            + ") already has a dirty state (" + tuple.getState()
                            + ") so it cannot transition to newState (" + newState + ").");
                }
            }
            // Don't add it to the queue twice
            return;
        }
        tuple.setState(newState);
        dirtyTupleQueue.add(tuple);
    }

    @Override
    public final void calculateScore() {
        dirtyTupleQueue.forEach(tuple -> {
            refresh(tuple);
            switch (tuple.getState()) {
                case CREATING:
                case UPDATING:
                    tuple.setState(BavetTupleState.OK);
                    return;
                case DYING:
                case ABORTING:
                    tuple.setState(BavetTupleState.DEAD);
                    return;
                case DEAD:
                    throw new IllegalStateException("Impossible state: The tuple (" + tuple + ") in node (" +
                            tuple.getNode() + ") is already in the dead state (" + tuple.getState() + ").");
                default:
                    throw new IllegalStateException("Impossible state: Tuple (" + tuple + ") in node (" +
                            tuple.getNode() + ") is in an unexpected state (" + tuple.getState() + ").");
            }
        });
        dirtyTupleQueue.clear();
    }

}
