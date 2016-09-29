/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.localsearch.decider.acceptor.composite;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.optaplanner.core.impl.localsearch.decider.acceptor.Acceptor;
import org.optaplanner.core.impl.localsearch.decider.acceptor.CompositeAcceptor;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchMoveScope;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.optaplanner.core.impl.testdata.util.PlannerAssert.*;

public class CompositeAcceptorTest {
    @Test
    public void phaseLifecycle() {
        Acceptor acceptor1 = mock(Acceptor.class);
        Acceptor acceptor2 = mock(Acceptor.class);
        Acceptor acceptor3 = mock(Acceptor.class);
        CompositeAcceptor compositeAcceptor = new CompositeAcceptor(acceptor1, acceptor2, acceptor3);

        compositeAcceptor.solvingStarted(null);
        compositeAcceptor.phaseStarted(null);
        compositeAcceptor.stepStarted(null);
        compositeAcceptor.stepEnded(null);
        compositeAcceptor.stepStarted(null);
        compositeAcceptor.stepEnded(null);
        compositeAcceptor.phaseEnded(null);
        compositeAcceptor.phaseStarted(null);
        compositeAcceptor.stepStarted(null);
        compositeAcceptor.stepEnded(null);
        compositeAcceptor.phaseEnded(null);
        compositeAcceptor.solvingEnded(null);

        verifyPhaseLifecycle(acceptor1, 1, 2, 3);
        verifyPhaseLifecycle(acceptor2, 1, 2, 3);
        verifyPhaseLifecycle(acceptor3, 1, 2, 3);
    }

    @Test
    public void isAccepted() {
        assertEquals(true, isCompositeAccepted(true, true, true));
        assertEquals(false, isCompositeAccepted(false, true, true));
        assertEquals(false, isCompositeAccepted(true, false, true));
        assertEquals(false, isCompositeAccepted(true, true, false));
        assertEquals(false, isCompositeAccepted(false, false, false));
    }

    private boolean isCompositeAccepted(boolean... childAccepts) {
        List<Acceptor> acceptorList = new ArrayList<>(childAccepts.length);
        for (boolean childAccept : childAccepts) {
            Acceptor acceptor = mock(Acceptor.class);
            when(acceptor.isAccepted(any(LocalSearchMoveScope.class))).thenReturn(childAccept);
            acceptorList.add(acceptor);
        }
        CompositeAcceptor acceptor = new CompositeAcceptor(acceptorList);
        return acceptor.isAccepted(mock(LocalSearchMoveScope.class));
    }

}
