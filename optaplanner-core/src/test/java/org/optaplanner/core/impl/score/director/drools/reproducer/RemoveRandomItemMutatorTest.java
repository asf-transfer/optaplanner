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
package org.optaplanner.core.impl.score.director.drools.reproducer;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RemoveRandomItemMutatorTest {

    private static final int LIST_SIZE = 10;
    private ArrayList<Integer> list = new ArrayList<Integer>();

    @Before
    public void setUp() {
        for (int i = 0; i < LIST_SIZE; i++) {
            list.add(i);
        }
    }

    @Test
    public void testRemoveAll() {
        RemoveRandomItemMutator<Integer> m = new RemoveRandomItemMutator<Integer>(list);
        ArrayList<Integer> removed = new ArrayList<Integer>();
        for (int i = 0; i < LIST_SIZE; i++) {
            assertTrue(m.canMutate());
            m.mutate();
            removed.add(m.getRemovedItem());
            assertEquals(LIST_SIZE - i - 1, m.getResult().size());
        }
        assertFalse(m.canMutate());

        for (int i = 0; i < LIST_SIZE; i++) {
            assertTrue(removed.contains(list.get(i)));
        }
    }

    @Test
    public void testRevert() {
        RemoveRandomItemMutator<Integer> m = new RemoveRandomItemMutator<Integer>(list);
        m.mutate();
        int removedItem = m.getRemovedItem();
        m.revert();
        assertTrue(m.getResult().contains(removedItem));
        assertEquals(LIST_SIZE, m.getResult().size());
    }

    @Test
    public void testImpossibleMutation() {
        RemoveRandomItemMutator<Integer> m = new RemoveRandomItemMutator<Integer>(list);
        ArrayList<Integer> removed = new ArrayList<Integer>();
        for (int i = 0; i < LIST_SIZE; i++) {
            assertTrue(m.canMutate());
            m.mutate();
            removed.add(m.getRemovedItem());
            m.revert();
        }
        assertFalse(m.canMutate());

        for (int i = 0; i < LIST_SIZE; i++) {
            assertTrue(removed.contains(list.get(i)));
        }
    }
}
