/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.heuristic.selector.move.generic;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;
import org.optaplanner.core.impl.testdata.domain.entityproviding.TestdataEntityProvidingEntity;
import org.optaplanner.core.impl.testdata.domain.multivar.TestdataMultiVarEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.optaplanner.core.impl.testdata.util.PlannerAssert.*;

public class PillarSwapMoveTest {

    @Test
    public void isMoveDoableValueRangeProviderOnEntity() {
        TestdataValue v1 = new TestdataValue("1");
        TestdataValue v2 = new TestdataValue("2");
        TestdataValue v3 = new TestdataValue("3");
        TestdataValue v4 = new TestdataValue("4");
        TestdataValue v5 = new TestdataValue("5");

        TestdataEntityProvidingEntity a = new TestdataEntityProvidingEntity("a", Arrays.asList(v1, v2, v3), null);
        TestdataEntityProvidingEntity b = new TestdataEntityProvidingEntity("b", Arrays.asList(v2, v3, v4, v5), null);
        TestdataEntityProvidingEntity c = new TestdataEntityProvidingEntity("c", Arrays.asList(v4, v5), null);
        TestdataEntityProvidingEntity z = new TestdataEntityProvidingEntity("z", Arrays.asList(v1, v2, v3, v4, v5), null);

        ScoreDirector scoreDirector = mock(ScoreDirector.class);
        List<GenuineVariableDescriptor> variableDescriptorList = TestdataEntityProvidingEntity
                .buildEntityDescriptor().getGenuineVariableDescriptorList();

        PillarSwapMove abMove = new PillarSwapMove(variableDescriptorList, Arrays.<Object>asList(a), Arrays.<Object>asList(b));
        a.setValue(v1);
        b.setValue(v2);
        assertThat(abMove.isMoveDoable(scoreDirector)).isEqualTo(false);
        a.setValue(v2);
        b.setValue(v2);
        assertThat(abMove.isMoveDoable(scoreDirector)).isEqualTo(false);
        a.setValue(v2);
        b.setValue(v3);
        assertThat(abMove.isMoveDoable(scoreDirector)).isEqualTo(true);
        a.setValue(v3);
        b.setValue(v2);
        assertThat(abMove.isMoveDoable(scoreDirector)).isEqualTo(true);
        a.setValue(v3);
        b.setValue(v3);
        assertThat(abMove.isMoveDoable(scoreDirector)).isEqualTo(false);
        a.setValue(v2);
        b.setValue(v4);
        assertThat(abMove.isMoveDoable(scoreDirector)).isEqualTo(false);

        PillarSwapMove acMove = new PillarSwapMove(variableDescriptorList, Arrays.<Object>asList(a), Arrays.<Object>asList(c));
        a.setValue(v1);
        c.setValue(v4);
        assertThat(acMove.isMoveDoable(scoreDirector)).isEqualTo(false);
        a.setValue(v2);
        c.setValue(v5);
        assertThat(acMove.isMoveDoable(scoreDirector)).isEqualTo(false);

        PillarSwapMove bcMove = new PillarSwapMove(variableDescriptorList, Arrays.<Object>asList(b), Arrays.<Object>asList(c));
        b.setValue(v2);
        c.setValue(v4);
        assertThat(bcMove.isMoveDoable(scoreDirector)).isEqualTo(false);
        b.setValue(v4);
        c.setValue(v5);
        assertThat(bcMove.isMoveDoable(scoreDirector)).isEqualTo(true);
        b.setValue(v5);
        c.setValue(v4);
        assertThat(bcMove.isMoveDoable(scoreDirector)).isEqualTo(true);
        b.setValue(v5);
        c.setValue(v5);
        assertThat(bcMove.isMoveDoable(scoreDirector)).isEqualTo(false);

        PillarSwapMove abzMove = new PillarSwapMove(variableDescriptorList, Arrays.<Object>asList(a, b), Arrays.<Object>asList(z));
        a.setValue(v2);
        b.setValue(v2);
        z.setValue(v4);
        assertThat(abzMove.isMoveDoable(scoreDirector)).isEqualTo(false);
        a.setValue(v2);
        b.setValue(v2);
        z.setValue(v1);
        assertThat(abzMove.isMoveDoable(scoreDirector)).isEqualTo(false);
        a.setValue(v2);
        b.setValue(v2);
        z.setValue(v3);
        assertThat(abzMove.isMoveDoable(scoreDirector)).isEqualTo(true);
        a.setValue(v3);
        b.setValue(v3);
        z.setValue(v2);
        assertThat(abzMove.isMoveDoable(scoreDirector)).isEqualTo(true);
        a.setValue(v2);
        b.setValue(v2);
        z.setValue(v2);
        assertThat(abzMove.isMoveDoable(scoreDirector)).isEqualTo(false);
    }

    @Test
    public void doMove() {
        TestdataValue v1 = new TestdataValue("1");
        TestdataValue v2 = new TestdataValue("2");
        TestdataValue v3 = new TestdataValue("3");
        TestdataValue v4 = new TestdataValue("4");
        TestdataValue v5 = new TestdataValue("5");

        TestdataEntityProvidingEntity a = new TestdataEntityProvidingEntity("a", Arrays.asList(v1, v2, v3, v4), null);
        TestdataEntityProvidingEntity b = new TestdataEntityProvidingEntity("b", Arrays.asList(v2, v3, v4, v5), null);
        TestdataEntityProvidingEntity c = new TestdataEntityProvidingEntity("c", Arrays.asList(v4, v5), null);
        TestdataEntityProvidingEntity z = new TestdataEntityProvidingEntity("z", Arrays.asList(v1, v2, v3, v4, v5), null);

        ScoreDirector scoreDirector = mock(ScoreDirector.class);
        List<GenuineVariableDescriptor> variableDescriptorList = TestdataEntityProvidingEntity
                .buildEntityDescriptor().getGenuineVariableDescriptorList();

        PillarSwapMove abMove = new PillarSwapMove(variableDescriptorList,
                Arrays.<Object>asList(a), Arrays.<Object>asList(b));

        a.setValue(v1);
        b.setValue(v1);
        abMove.doMove(scoreDirector);
        assertThat(a.getValue()).isEqualTo(v1);
        assertThat(b.getValue()).isEqualTo(v1);

        a.setValue(v2);
        b.setValue(v1);
        abMove.doMove(scoreDirector);
        assertThat(a.getValue()).isEqualTo(v1);
        assertThat(b.getValue()).isEqualTo(v2);

        a.setValue(v3);
        b.setValue(v2);
        abMove.doMove(scoreDirector);
        assertThat(a.getValue()).isEqualTo(v2);
        assertThat(b.getValue()).isEqualTo(v3);
        abMove.doMove(scoreDirector);
        assertThat(a.getValue()).isEqualTo(v3);
        assertThat(b.getValue()).isEqualTo(v2);

        PillarSwapMove abzMove = new PillarSwapMove(variableDescriptorList, Arrays.<Object>asList(a, b), Arrays.<Object>asList(z));

        a.setValue(v3);
        b.setValue(v3);
        z.setValue(v2);
        abzMove.doMove(scoreDirector);
        assertThat(a.getValue()).isEqualTo(v2);
        assertThat(b.getValue()).isEqualTo(v2);
        assertThat(z.getValue()).isEqualTo(v3);
        abzMove.doMove(scoreDirector);
        assertThat(a.getValue()).isEqualTo(v3);
        assertThat(b.getValue()).isEqualTo(v3);
        assertThat(z.getValue()).isEqualTo(v2);

        a.setValue(v3);
        b.setValue(v3);
        z.setValue(v4);
        abzMove.doMove(scoreDirector);
        assertThat(a.getValue()).isEqualTo(v4);
        assertThat(b.getValue()).isEqualTo(v4);
        assertThat(z.getValue()).isEqualTo(v3);
        abzMove.doMove(scoreDirector);
        assertThat(a.getValue()).isEqualTo(v3);
        assertThat(b.getValue()).isEqualTo(v3);
        assertThat(z.getValue()).isEqualTo(v4);

        PillarSwapMove abczMove = new PillarSwapMove(variableDescriptorList, Arrays.<Object>asList(a), Arrays.<Object>asList(b, c, z));

        a.setValue(v2);
        b.setValue(v3);
        c.setValue(v3);
        z.setValue(v3);
        abczMove.doMove(scoreDirector);
        assertThat(a.getValue()).isEqualTo(v3);
        assertThat(b.getValue()).isEqualTo(v2);
        assertThat(c.getValue()).isEqualTo(v2);
        assertThat(z.getValue()).isEqualTo(v2);
        abczMove.doMove(scoreDirector);
        assertThat(a.getValue()).isEqualTo(v2);
        assertThat(b.getValue()).isEqualTo(v3);
        assertThat(c.getValue()).isEqualTo(v3);
        assertThat(z.getValue()).isEqualTo(v3);

        PillarSwapMove abczMove2 = new PillarSwapMove(variableDescriptorList, Arrays.<Object>asList(a, b), Arrays.<Object>asList(c, z));

        a.setValue(v4);
        b.setValue(v4);
        c.setValue(v3);
        z.setValue(v3);
        abczMove2.doMove(scoreDirector);
        assertThat(a.getValue()).isEqualTo(v3);
        assertThat(b.getValue()).isEqualTo(v3);
        assertThat(c.getValue()).isEqualTo(v4);
        assertThat(z.getValue()).isEqualTo(v4);
        abczMove2.doMove(scoreDirector);
        assertThat(a.getValue()).isEqualTo(v4);
        assertThat(b.getValue()).isEqualTo(v4);
        assertThat(c.getValue()).isEqualTo(v3);
        assertThat(z.getValue()).isEqualTo(v3);
    }

    @Test
    public void getters() {
        GenuineVariableDescriptor primaryDescriptor = TestdataMultiVarEntity.buildVariableDescriptorForPrimaryValue();
        GenuineVariableDescriptor secondaryDescriptor = TestdataMultiVarEntity.buildVariableDescriptorForSecondaryValue();
        PillarSwapMove move = new PillarSwapMove(Arrays.asList(primaryDescriptor),
                Arrays.<Object>asList(new TestdataMultiVarEntity("a"), new TestdataMultiVarEntity("b")),
                Arrays.<Object>asList(new TestdataMultiVarEntity("c"), new TestdataMultiVarEntity("d")));
        assertCollectionContainsExactly(move.getVariableNameList(), "primaryValue");
        assertAllCodesOfCollection(move.getLeftPillar(), "a", "b");
        assertAllCodesOfCollection(move.getRightPillar(), "c", "d");

        move = new PillarSwapMove(Arrays.asList(primaryDescriptor, secondaryDescriptor),
                Arrays.<Object>asList(new TestdataMultiVarEntity("e"), new TestdataMultiVarEntity("f")),
                Arrays.<Object>asList(new TestdataMultiVarEntity("g"), new TestdataMultiVarEntity("h")));
        assertCollectionContainsExactly(move.getVariableNameList(), "primaryValue", "secondaryValue");
        assertAllCodesOfCollection(move.getLeftPillar(), "e", "f");
        assertAllCodesOfCollection(move.getRightPillar(), "g", "h");
    }

    @Test
    public void toStringTest() {
        TestdataValue v1 = new TestdataValue("v1");
        TestdataValue v2 = new TestdataValue("v2");
        TestdataEntity a = new TestdataEntity("a", null);
        TestdataEntity b = new TestdataEntity("b", null);
        TestdataEntity c = new TestdataEntity("c", v1);
        TestdataEntity d = new TestdataEntity("d", v1);
        TestdataEntity e = new TestdataEntity("e", v1);
        TestdataEntity f = new TestdataEntity("f", v2);
        TestdataEntity g = new TestdataEntity("g", v2);
        List<GenuineVariableDescriptor> variableDescriptorList = TestdataEntity.buildEntityDescriptor()
                .getGenuineVariableDescriptorList();

        assertThat(new PillarSwapMove(variableDescriptorList, Arrays.<Object>asList(a, b),
                Arrays.<Object>asList(c, d, e)).toString()).isEqualTo("[a, b] {null} <-> [c, d, e] {v1}");
        assertThat(new PillarSwapMove(variableDescriptorList, Arrays.<Object>asList(b),
                Arrays.<Object>asList(c)).toString()).isEqualTo("[b] {null} <-> [c] {v1}");
        assertThat(new PillarSwapMove(variableDescriptorList, Arrays.<Object>asList(f, g),
                Arrays.<Object>asList(c, d, e)).toString()).isEqualTo("[f, g] {v2} <-> [c, d, e] {v1}");
    }

}
