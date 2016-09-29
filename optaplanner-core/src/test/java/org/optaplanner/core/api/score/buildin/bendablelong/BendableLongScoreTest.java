/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.api.score.buildin.bendablelong;

import org.junit.Test;
import org.optaplanner.core.api.score.buildin.AbstractScoreTest;
import org.optaplanner.core.impl.score.buildin.bendablelong.BendableLongScoreDefinition;
import org.optaplanner.core.impl.testdata.util.PlannerAssert;
import org.optaplanner.core.impl.testdata.util.PlannerTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class BendableLongScoreTest extends AbstractScoreTest {

    private BendableLongScoreDefinition scoreDefinitionHSS = new BendableLongScoreDefinition(1, 2);

    @Test
    public void parseScore() {
        assertThat(scoreDefinitionHSS.parseScore("[-5432109876]hard/[-9876543210/-3456789012]soft"))
                .isEqualTo(scoreDefinitionHSS.createScoreInitialized(-5432109876L, -9876543210L, -3456789012L));
        assertThat(scoreDefinitionHSS.parseScore("-7init/[-5432109876]hard/[-9876543210/-3456789012]soft"))
                .isEqualTo(scoreDefinitionHSS.createScore(-7, -5432109876L, -9876543210L, -3456789012L));
    }

    @Test
    public void testToString() {
        assertEquals("[-5432109876]hard/[-9876543210/-3456789012]soft",
                scoreDefinitionHSS.createScoreInitialized(-5432109876L, -9876543210L, -3456789012L).toString());
        assertEquals("-7init/[-5432109876]hard/[-9876543210/-3456789012]soft",
                scoreDefinitionHSS.createScore(-7, -5432109876L, -9876543210L, -3456789012L).toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseScoreIllegalArgument() {
        scoreDefinitionHSS.parseScore("-147");
    }

    @Test
    public void getHardOrSoftScore() {
        BendableLongScore initializedScore = scoreDefinitionHSS.createScoreInitialized(-5L, -10L, -200L);
        assertThat(initializedScore.getHardOrSoftScore(0)).isEqualTo(-5L);
        assertThat(initializedScore.getHardOrSoftScore(1)).isEqualTo(-10L);
        assertThat(initializedScore.getHardOrSoftScore(2)).isEqualTo(-200L);
    }

    @Test
    public void toInitializedScoreHSS() {
        assertThat(scoreDefinitionHSS.createScoreInitialized(-5432109876L, -9876543210L, -3456789012L).toInitializedScore())
                .isEqualTo(scoreDefinitionHSS.createScoreInitialized(-5432109876L, -9876543210L, -3456789012L));
        assertThat(scoreDefinitionHSS.createScore(-7, -5432109876L, -9876543210L, -3456789012L).toInitializedScore())
                .isEqualTo(scoreDefinitionHSS.createScoreInitialized(-5432109876L, -9876543210L, -3456789012L));
    }

    @Test
    public void feasibleHSS() {
        assertScoreNotFeasible(
                scoreDefinitionHSS.createScoreInitialized(-5L, -300L, -9876543210L),
                scoreDefinitionHSS.createScore(-7, -5L, -300L, -9876543210L),
                scoreDefinitionHSS.createScore(-7, 0L, -300L, -9876543210L)
        );
        assertScoreFeasible(
                scoreDefinitionHSS.createScoreInitialized(0L, -300L, -9876543210L),
                scoreDefinitionHSS.createScoreInitialized(2L, -300L, -9876543210L),
                scoreDefinitionHSS.createScore(0, 0L, -300L, -9876543210L)
        );
    }

    @Test
    public void addHSS() {
        assertThat(scoreDefinitionHSS.createScoreInitialized(1111111111L, -20L, -9876543210L).add(
                        scoreDefinitionHSS.createScoreInitialized(2222222222L, -300L, 9876543210L)))
                .isEqualTo(scoreDefinitionHSS.createScoreInitialized(3333333333L, -320L, 0L));
        assertThat(scoreDefinitionHSS.createScore(-70, 1111111111L, -20L, -9876543210L).add(
                        scoreDefinitionHSS.createScore(-7, 2222222222L, -300L, 9876543210L)))
                .isEqualTo(scoreDefinitionHSS.createScore(-77, 3333333333L, -320L, 0L));
    }

    @Test
    public void subtractHSS() {
        assertThat(scoreDefinitionHSS.createScoreInitialized(3333333333L, -20L, -5555555555L).subtract(
                        scoreDefinitionHSS.createScoreInitialized(1111111111L, -300L, 3333333333L)))
                .isEqualTo(scoreDefinitionHSS.createScoreInitialized(2222222222L, 280L, -8888888888L));
        assertThat(scoreDefinitionHSS.createScore(-70, 3333333333L, -20L, -5555555555L).subtract(
                        scoreDefinitionHSS.createScore(-7, 1111111111L, -300L, 3333333333L)))
                .isEqualTo(scoreDefinitionHSS.createScore(-63, 2222222222L, 280L, -8888888888L));
    }

    @Test
    public void multiplyHSS() {
        assertThat(scoreDefinitionHSS.createScoreInitialized(5000000000L, -5000000000L, 5000000000L).multiply(1.2))
                .isEqualTo(scoreDefinitionHSS.createScoreInitialized(6000000000L, -6000000000L, 6000000000L));
        assertThat(scoreDefinitionHSS.createScoreInitialized(1L, -1L, 1L).multiply(1.2))
                .isEqualTo(scoreDefinitionHSS.createScoreInitialized(1L, -2L, 1L));
        assertThat(scoreDefinitionHSS.createScoreInitialized(4L, -4L, 4L).multiply(1.2))
                .isEqualTo(scoreDefinitionHSS.createScoreInitialized(4L, -5L, 4L));
        assertThat(scoreDefinitionHSS.createScore(-7, 4L, -5L, 6L).multiply(2.0))
                .isEqualTo(scoreDefinitionHSS.createScore(-14, 8L, -10L, 12L));
    }

    @Test
    public void divideHSS() {
        assertThat(scoreDefinitionHSS.createScoreInitialized(25000000000L, -25000000000L, 25000000000L).divide(5.0))
                .isEqualTo(scoreDefinitionHSS.createScoreInitialized(5000000000L, -5000000000L, 5000000000L));
        assertThat(scoreDefinitionHSS.createScoreInitialized(21L, -21L, 21L).divide(5.0))
                .isEqualTo(scoreDefinitionHSS.createScoreInitialized(4L, -5L, 4L));
        assertThat(scoreDefinitionHSS.createScoreInitialized(24L, -24L, 24L).divide(5.0))
                .isEqualTo(scoreDefinitionHSS.createScoreInitialized(4L, -5L, 4L));
        assertThat(scoreDefinitionHSS.createScore(-14, 8L, -10L, 12L).divide(2.0))
                .isEqualTo(scoreDefinitionHSS.createScore(-7, 4L, -5L, 6L));
    }

    @Test
    public void powerHSS() {
        assertThat(scoreDefinitionHSS.createScoreInitialized(300000L, -400000L, 500000L).power(2.0))
                .isEqualTo(scoreDefinitionHSS.createScoreInitialized(90000000000L, 160000000000L, 250000000000L));
        assertThat(scoreDefinitionHSS.createScoreInitialized(90000000000L, 160000000000L, 250000000000L).power(0.5))
                .isEqualTo(scoreDefinitionHSS.createScoreInitialized(300000L, 400000L, 500000L));
        assertThat(scoreDefinitionHSS.createScore(-7, 3L, -4L, 5L).power(3.0))
                .isEqualTo(scoreDefinitionHSS.createScore(-343, 27L, -64L, 125L));
    }

    @Test
    public void negateHSS() {
        assertThat(scoreDefinitionHSS.createScoreInitialized(3000000000L, -4000000000L, 5000000000L).negate())
                .isEqualTo(scoreDefinitionHSS.createScoreInitialized(-3000000000L, 4000000000L, -5000000000L));
        assertThat(scoreDefinitionHSS.createScoreInitialized(-3000000000L, 4000000000L, -5000000000L).negate())
                .isEqualTo(scoreDefinitionHSS.createScoreInitialized(3000000000L, -4000000000L, 5000000000L));
    }

    @Test
    public void equalsAndHashCodeHSS() {
        assertScoresEqualsAndHashCode(
                scoreDefinitionHSS.createScoreInitialized(-10L, -200L, -3000L),
                scoreDefinitionHSS.createScoreInitialized(-10L, -200L, -3000L),
                scoreDefinitionHSS.createScore(0, -10L, -200L, -3000L)
        );
        assertScoresEqualsAndHashCode(
                scoreDefinitionHSS.createScore(-7, -10L, -200L, -3000L),
                scoreDefinitionHSS.createScore(-7, -10L, -200L, -3000L)
        );
        assertScoresNotEquals(
                scoreDefinitionHSS.createScoreInitialized(-10L, -200L, -3000L),
                scoreDefinitionHSS.createScoreInitialized(-30L, -200L, -3000L),
                scoreDefinitionHSS.createScoreInitialized(-10L, -400L, -3000L),
                scoreDefinitionHSS.createScoreInitialized(-10L, -400L, -5000L),
                scoreDefinitionHSS.createScore(-7, -10L, -200L, -3000L)
        );
    }

    @Test
    public void compareToHSS() {
        PlannerAssert.assertCompareToOrder(
                scoreDefinitionHSS.createScore(-8, 0L, 0L, 0L),
                scoreDefinitionHSS.createScore(-7, -20L, -20L, -20L),
                scoreDefinitionHSS.createScore(-7, -1L, -300L, -4000L),
                scoreDefinitionHSS.createScore(-7, 0L, 0L, 0L),
                scoreDefinitionHSS.createScore(-7, 0L, 0L, 1L),
                scoreDefinitionHSS.createScore(-7, 0L, 1L, 0L),
                scoreDefinitionHSS.createScoreInitialized(-20L, Long.MIN_VALUE, Long.MIN_VALUE),
                scoreDefinitionHSS.createScoreInitialized(-20L, Long.MIN_VALUE, -20L),
                scoreDefinitionHSS.createScoreInitialized(-20L, Long.MIN_VALUE, 1L),
                scoreDefinitionHSS.createScoreInitialized(-20L, -300L, -4000L),
                scoreDefinitionHSS.createScoreInitialized(-20L, -300L, -300L),
                scoreDefinitionHSS.createScoreInitialized(-20L, -300L, -20L),
                scoreDefinitionHSS.createScoreInitialized(-20L, -300L, 300L),
                scoreDefinitionHSS.createScoreInitialized(-20L, -20L, -300L),
                scoreDefinitionHSS.createScoreInitialized(-20L, -20L, 0L),
                scoreDefinitionHSS.createScoreInitialized(-20L, -20L, 1L),
                scoreDefinitionHSS.createScoreInitialized(-1L, -300L, -4000L),
                scoreDefinitionHSS.createScoreInitialized(-1L, -300L, -20L),
                scoreDefinitionHSS.createScoreInitialized(-1L, -20L, -300L),
                scoreDefinitionHSS.createScoreInitialized(1L, Long.MIN_VALUE, -20L),
                scoreDefinitionHSS.createScoreInitialized(1L, -20L, Long.MIN_VALUE)
        );
    }

    private BendableLongScoreDefinition scoreDefinitionHHSSS = new BendableLongScoreDefinition(2, 3);

    @Test
    public void feasibleHHSSS() {
        assertScoreNotFeasible(
                scoreDefinitionHHSSS.createScoreInitialized(-5L, 0L, -300L, -4000000000L, -5000L),
                scoreDefinitionHHSSS.createScoreInitialized(0L, -5000000000L, -300L, -4000L, -5000L)
        );
        assertScoreFeasible(
                scoreDefinitionHHSSS.createScoreInitialized(0L, 0L, -300000000000L, -4000L, -5000L),
                scoreDefinitionHHSSS.createScoreInitialized(0L, 2L, -300L, -4000L, -50000000000L),
                scoreDefinitionHHSSS.createScoreInitialized(2000000000L, 0L, -300L, -4000L, -5000L)
        );
    }

    @Test
    public void addHHSSS() {
        assertThat(scoreDefinitionHHSSS.createScoreInitialized(20000000000L, -20000000000L, -4000000000000L, 0L, 0L).add(
                        scoreDefinitionHHSSS.createScoreInitialized(-1000000000L, -300000000000L, 4000000000000L, 0L, 0L)))
                .isEqualTo(scoreDefinitionHHSSS.createScoreInitialized(19000000000L, -320000000000L, 0L, 0L, 0L));
    }

    @Test
    public void subtractHHSSS() {
        assertThat(scoreDefinitionHHSSS.createScoreInitialized(20000000000L, -20000000000L, -4000000000000L, 0L, 0L).subtract(
                        scoreDefinitionHHSSS.createScoreInitialized(-1000000000L, -300000000000L, 4000000000000L, 0L, 0L)))
                .isEqualTo(scoreDefinitionHHSSS.createScoreInitialized(21000000000L, 280000000000L, -8000000000000L, 0L, 0L));
    }

    @Test
    public void multiplyHHSSS() {
        assertThat(scoreDefinitionHHSSS.createScoreInitialized(5000000000L, -5000000000L, 5000000000L, 0L, 0L).multiply(1.2))
                .isEqualTo(scoreDefinitionHHSSS.createScoreInitialized(6000000000L, -6000000000L, 6000000000L, 0L, 0L));
        assertThat(scoreDefinitionHHSSS.createScoreInitialized(1, -1, 1, 0, 0).multiply(1.2))
                .isEqualTo(scoreDefinitionHHSSS.createScoreInitialized(1, -2, 1, 0, 0));
        assertThat(scoreDefinitionHHSSS.createScoreInitialized(4, -4, 4, 0, 0).multiply(1.2))
                .isEqualTo(scoreDefinitionHHSSS.createScoreInitialized(4, -5, 4, 0, 0));
    }

    @Test
    public void divideHHSSS() {
        assertThat(scoreDefinitionHHSSS.createScoreInitialized(25000000000L, -25000000000L, 25000000000L, 0L, 0L).divide(5.0))
                .isEqualTo(scoreDefinitionHHSSS.createScoreInitialized(5000000000L, -5000000000L, 5000000000L, 0L, 0L));
        assertThat(scoreDefinitionHHSSS.createScoreInitialized(21, -21, 21, 0, 0).divide(5.0))
                .isEqualTo(scoreDefinitionHHSSS.createScoreInitialized(4, -5, 4, 0, 0));
        assertThat(scoreDefinitionHHSSS.createScoreInitialized(24, -24, 24, 0, 0).divide(5.0))
                .isEqualTo(scoreDefinitionHHSSS.createScoreInitialized(4, -5, 4, 0, 0));
    }

    @Test
    public void powerHHSSS() {
        assertThat(scoreDefinitionHHSSS.createScoreInitialized(200000L, -400000L, 500000L, 0L, 0L).power(2.0))
                .isEqualTo(scoreDefinitionHHSSS.createScoreInitialized(40000000000L, 160000000000L, 250000000000L, 0L, 0L));
        assertThat(scoreDefinitionHHSSS.createScoreInitialized(9L, 16L, 25L, 0L, 0L).power(0.5))
                .isEqualTo(scoreDefinitionHHSSS.createScoreInitialized(3L, 4L, 5L, 0L, 0L));
    }

    @Test
    public void negateHHSSS() {
        assertThat(scoreDefinitionHHSSS.createScoreInitialized(3000000000L, -4000000000L, 5000000000L, 0L, 0L).negate())
                .isEqualTo(scoreDefinitionHHSSS.createScoreInitialized(-3000000000L, 4000000000L, -5000000000L, 0L, 0L));
        assertThat(scoreDefinitionHHSSS.createScoreInitialized(-3L, 4L, -5L, 0L, 0L).negate())
                .isEqualTo(scoreDefinitionHHSSS.createScoreInitialized(3L, -4L, 5L, 0L, 0L));
    }

    @Test
    public void equalsAndHashCodeHHSSS() {
        assertScoresEqualsAndHashCode(
                scoreDefinitionHHSSS.createScoreInitialized(-10000000000L, -20000000000L, -30000000000L, 0L, 0L),
                scoreDefinitionHHSSS.createScoreInitialized(-10000000000L, -20000000000L, -30000000000L, 0L, 0L)
        );
    }

    @Test
    public void compareToHHSSS() {
        PlannerAssert.assertCompareToOrder(
                scoreDefinitionHHSSS.createScoreInitialized(-20L, Long.MIN_VALUE, Long.MIN_VALUE, 0L, 0L),
                scoreDefinitionHHSSS.createScoreInitialized(-20L, Long.MIN_VALUE, -20L, 0L, 0L),
                scoreDefinitionHHSSS.createScoreInitialized(-20L, Long.MIN_VALUE, 1L, 0L, 0L),
                scoreDefinitionHHSSS.createScoreInitialized(-20L, -300L, -4000L, 0L, 0L),
                scoreDefinitionHHSSS.createScoreInitialized(-20L, -300L, -300L, 0L, 0L),
                scoreDefinitionHHSSS.createScoreInitialized(-20L, -300L, -20L, 0L, 0L),
                scoreDefinitionHHSSS.createScoreInitialized(-20L, -300L, 300L, 0L, 0L),
                scoreDefinitionHHSSS.createScoreInitialized(-20L, -20L, -300L, 0L, 0L),
                scoreDefinitionHHSSS.createScoreInitialized(-20L, -20L, 0L, 0L, 0L),
                scoreDefinitionHHSSS.createScoreInitialized(-20L, -20L, 1L, 0L, 0L),
                scoreDefinitionHHSSS.createScoreInitialized(-1L, -300L, -4000L, 0L, 0L),
                scoreDefinitionHHSSS.createScoreInitialized(-1L, -300L, -20L, 0L, 0L),
                scoreDefinitionHHSSS.createScoreInitialized(-1L, -20L, -300L, 0L, 0L),
                scoreDefinitionHHSSS.createScoreInitialized(1L, Long.MIN_VALUE, -20L, 0L, 0L),
                scoreDefinitionHHSSS.createScoreInitialized(1L, -20L, Long.MIN_VALUE, 0L, 0L)
        );
    }

    @Test
    public void serializeAndDeserialize() {
        PlannerTestUtils.serializeAndDeserializeWithAll(
                scoreDefinitionHSS.createScoreInitialized(-12L, 3400L, -56L),
                output -> {
                    assertThat(output.getInitScore()).isEqualTo(0);
                    assertThat(output.getHardScore(0)).isEqualTo(-12L);
                    assertThat(output.getSoftScore(0)).isEqualTo(3400L);
                    assertThat(output.getSoftScore(1)).isEqualTo(-56L);
                }
        );
        PlannerTestUtils.serializeAndDeserializeWithAll(
                scoreDefinitionHSS.createScore(-7, -12L, 3400L, -56L),
                output -> {
                    assertThat(output.getInitScore()).isEqualTo(-7);
                    assertThat(output.getHardScore(0)).isEqualTo(-12L);
                    assertThat(output.getSoftScore(0)).isEqualTo(3400L);
                    assertThat(output.getSoftScore(1)).isEqualTo(-56L);
                }
        );
    }

}
