/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.domain.valuerange.buildin.primint;

import java.util.Random;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class IntValueRangeTest {

    @Test
    public void getSize() {
        assertThat(new IntValueRange(0, 10).getSize()).isEqualTo(10L);
        assertThat(new IntValueRange(100, 120).getSize()).isEqualTo(20L);
        assertThat(new IntValueRange(-15, 25).getSize()).isEqualTo(40L);
        assertThat(new IntValueRange(7, 7).getSize()).isEqualTo(0L);
        assertThat(new IntValueRange(-1000, Integer.MAX_VALUE - 100).getSize()).isEqualTo(Integer.MAX_VALUE + 900L);
        // IncrementUnit
        assertThat(new IntValueRange(0, 10, 2).getSize()).isEqualTo(5L);
        assertThat(new IntValueRange(-1, 9, 2).getSize()).isEqualTo(5L);
        assertThat(new IntValueRange(100, 120, 5).getSize()).isEqualTo(4L);
    }

    @Test
    public void get() {
        assertThat(new IntValueRange(0, 10).get(3L).intValue()).isEqualTo(3);
        assertThat(new IntValueRange(100, 120).get(3L).intValue()).isEqualTo(103);
        assertThat(new IntValueRange(-5, 25).get(1L).intValue()).isEqualTo(-4);
        assertThat(new IntValueRange(-5, 25).get(6L).intValue()).isEqualTo(1);
        assertThat(new IntValueRange(-1000, Integer.MAX_VALUE - 100).get(1004L).intValue()).isEqualTo(4);
        // IncrementUnit
        assertThat(new IntValueRange(0, 10, 2).get(3L).intValue()).isEqualTo(6);
        assertThat(new IntValueRange(-1, 9, 2).get(3L).intValue()).isEqualTo(5);
        assertThat(new IntValueRange(100, 120, 5).get(3L).intValue()).isEqualTo(115);
    }

    @Test
    public void contains() {
        assertThat(new IntValueRange(0, 10).contains(3)).isEqualTo(true);
        assertThat(new IntValueRange(0, 10).contains(10)).isEqualTo(false);
        assertThat(new IntValueRange(0, 10).contains(null)).isEqualTo(false);
        assertThat(new IntValueRange(100, 120).contains(100)).isEqualTo(true);
        assertThat(new IntValueRange(100, 120).contains(99)).isEqualTo(false);
        assertThat(new IntValueRange(-5, 25).contains(-4)).isEqualTo(true);
        assertThat(new IntValueRange(-5, 25).contains(-20)).isEqualTo(false);
        // IncrementUnit
        assertThat(new IntValueRange(0, 10, 2).contains(2)).isEqualTo(true);
        assertThat(new IntValueRange(0, 10, 2).contains(3)).isEqualTo(false);
        assertThat(new IntValueRange(-1, 9, 2).contains(1)).isEqualTo(true);
        assertThat(new IntValueRange(-1, 9, 2).contains(2)).isEqualTo(false);
        assertThat(new IntValueRange(100, 120, 5).contains(115)).isEqualTo(true);
        assertThat(new IntValueRange(100, 120, 5).contains(114)).isEqualTo(false);
    }

    @Test
    public void createOriginalIterator() {
        assertAllElementsOfIterator(new IntValueRange(0, 7).createOriginalIterator(), 0, 1, 2, 3, 4, 5, 6);
        assertAllElementsOfIterator(new IntValueRange(100, 104).createOriginalIterator(), 100, 101, 102, 103);
        assertAllElementsOfIterator(new IntValueRange(-4, 3).createOriginalIterator(), -4, -3, -2, -1, 0, 1, 2);
        assertAllElementsOfIterator(new IntValueRange(7, 7).createOriginalIterator());
        // IncrementUnit
        assertAllElementsOfIterator(new IntValueRange(0, 10, 2).createOriginalIterator(), 0, 2, 4, 6, 8);
        assertAllElementsOfIterator(new IntValueRange(-1, 9, 2).createOriginalIterator(), -1, 1, 3, 5, 7);
        assertAllElementsOfIterator(new IntValueRange(100, 120, 5).createOriginalIterator(), 100, 105, 110, 115);
    }

    @Test
    public void createRandomIterator() {
        Random workingRandom = mock(Random.class);

        when(workingRandom.nextInt(anyInt())).thenReturn(3, 0);
        assertElementsOfIterator(new IntValueRange(0, 7).createRandomIterator(workingRandom), 3, 0);
        when(workingRandom.nextInt(anyInt())).thenReturn(3, 0);
        assertElementsOfIterator(new IntValueRange(100, 104).createRandomIterator(workingRandom), 103, 100);
        when(workingRandom.nextInt(anyInt())).thenReturn(3, 0);
        assertElementsOfIterator(new IntValueRange(-4, 3).createRandomIterator(workingRandom), -1, -4);
        assertAllElementsOfIterator(new IntValueRange(7, 7).createRandomIterator(workingRandom));
        // IncrementUnit
        when(workingRandom.nextInt(anyInt())).thenReturn(3, 0);
        assertElementsOfIterator(new IntValueRange(0, 10, 2).createRandomIterator(workingRandom), 6, 0);
        when(workingRandom.nextInt(anyInt())).thenReturn(3, 0);
        assertElementsOfIterator(new IntValueRange(-1, 9, 2).createRandomIterator(workingRandom), 5, -1);
        when(workingRandom.nextInt(anyInt())).thenReturn(3, 0);
        assertElementsOfIterator(new IntValueRange(100, 120, 5).createRandomIterator(workingRandom), 115, 100);
    }

}
