package org.optaplanner.quarkus.testdata.gizmo;

import java.util.Collections;
import java.util.List;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.entity.PlanningPin;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.CustomShadowVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariableReference;

/*
 *  Should have one of every annotation, even annotations that
 *  don't make sense on an entity, to make sure everything works
 *  a-ok.
 */
@PlanningEntity
public class TestDataKitchenSinkEntity {

    private Integer intVariable;

    @CustomShadowVariable(
            variableListenerClass = DummyVariableListener.class,
            sources = {
                    @PlanningVariableReference(entityClass = TestDataKitchenSinkEntity.class,
                            variableName = "stringVariable")
            })
    private String myShadow;

    @PlanningVariable(valueRangeProviderRefs = { "names" })
    private String stringVariable;

    @PlanningPin
    private boolean isPinned;

    @PlanningVariable(valueRangeProviderRefs = { "ints" })
    private Integer getIntVariable() {
        return intVariable;
    }

    public void setIntVariable(Integer val) {
        intVariable = val;
    }

    public Integer testGetIntVariable() {
        return intVariable;
    }

    public String testGetStringVariable() {
        return stringVariable;
    }

    @ValueRangeProvider(id = "ints")
    private List<Integer> myIntValueRange() {
        return Collections.singletonList(1);
    }

    @ValueRangeProvider(id = "names")
    public List<String> myStringValueRange() {
        return Collections.singletonList("A");
    }

}
