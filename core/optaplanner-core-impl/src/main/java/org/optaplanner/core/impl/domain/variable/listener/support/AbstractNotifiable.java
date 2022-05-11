/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.domain.variable.listener.support;

import java.util.ArrayDeque;
import java.util.Collection;

import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.variable.AbstractVariableListener;
import org.optaplanner.core.api.domain.variable.VariableListener;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.impl.domain.variable.ListVariableListener;

/**
 * Generic notifiable that receives and triggers {@link Notification}s for a specific variable listener of the type {@code T}.
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 * @param <T> the variable listener type
 */
abstract class AbstractNotifiable<Solution_, T extends AbstractVariableListener<Solution_, Object>>
        implements EntityNotifiable<Solution_> {

    private final ScoreDirector<Solution_> scoreDirector;
    private final int globalOrder;
    private final T variableListener;
    private final Collection<Notification<Solution_, ? super T>> notificationQueue;

    static <Solution_> EntityNotifiable<Solution_> buildNotifiable(
            ScoreDirector<Solution_> scoreDirector,
            AbstractVariableListener<Solution_, Object> variableListener,
            int globalOrder) {
        if (variableListener instanceof ListVariableListener) {
            return new ListVariableListenerNotifiable<>(
                    scoreDirector,
                    ((ListVariableListener<Solution_, Object>) variableListener),
                    globalOrder);
        } else {
            return new VariableListenerNotifiable<>(
                    scoreDirector,
                    ((VariableListener<Solution_, Object>) variableListener),
                    globalOrder);
        }
    }

    protected AbstractNotifiable(
            ScoreDirector<Solution_> scoreDirector,
            T variableListener,
            int globalOrder) {
        this.scoreDirector = scoreDirector;
        this.globalOrder = globalOrder;
        this.variableListener = variableListener;
        if (variableListener.requiresUniqueEntityEvents()) {
            notificationQueue = new SmallScalingOrderedSet<>();
        } else {
            notificationQueue = new ArrayDeque<>();
        }
    }

    @Override
    public void addNotification(EntityNotification<Solution_> notification) {
        if (notificationQueue.add(notification)) {
            notification.triggerBefore(variableListener, scoreDirector);
        }
    }

    public void addNotification(Notification<Solution_, T> notification) {
        if (notificationQueue.add(notification)) {
            notification.triggerBefore(variableListener, scoreDirector);
        }
    }

    @Override
    public void resetWorkingSolution() {
        variableListener.resetWorkingSolution(scoreDirector);
    }

    @Override
    public void closeVariableListener() {
        variableListener.close();
    }

    @Override
    public void triggerAllNotifications() {
        int notifiedCount = 0;
        for (Notification<Solution_, ? super T> notification : notificationQueue) {
            notification.triggerAfter(variableListener, scoreDirector);
            notifiedCount++;
        }
        if (notifiedCount != notificationQueue.size()) {
            throw new IllegalStateException("The variableListener (" + variableListener.getClass()
                    + ") has been notified with notifiedCount (" + notifiedCount
                    + ") but after being triggered, its notificationCount (" + notificationQueue.size()
                    + ") is different.\n"
                    + "Maybe that variableListener (" + variableListener.getClass()
                    + ") changed an upstream shadow variable (which is illegal).");
        }
        notificationQueue.clear();
    }

    @Override
    public String toString() {
        return "(" + globalOrder + ") " + variableListener;
    }
}
