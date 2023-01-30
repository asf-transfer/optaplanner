package org.optaplanner.core.impl.heuristic.selector.move.generic.list;

import java.util.Objects;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.PlanningListVariable;
import org.optaplanner.core.config.heuristic.selector.common.SelectionCacheType;
import org.optaplanner.core.config.heuristic.selector.common.SelectionOrder;
import org.optaplanner.core.config.heuristic.selector.entity.EntitySelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.list.ListKOptMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.value.ValueSelectorConfig;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.ListVariableDescriptor;
import org.optaplanner.core.impl.heuristic.HeuristicConfigPolicy;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.AbstractMoveSelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.MoveSelector;
import org.optaplanner.core.impl.heuristic.selector.value.ValueSelector;
import org.optaplanner.core.impl.heuristic.selector.value.ValueSelectorFactory;

public class ListKOptMoveSelectorFactory<Solution_>
        extends AbstractMoveSelectorFactory<Solution_, ListKOptMoveSelectorConfig> {

    private static final int DEFAULT_MINIMUM_K = 2;
    private static final int DEFAULT_MAXIMUM_K = 3;

    public ListKOptMoveSelectorFactory(ListKOptMoveSelectorConfig moveSelectorConfig) {
        super(moveSelectorConfig);
    }

    @Override
    protected MoveSelector<Solution_> buildBaseMoveSelector(HeuristicConfigPolicy<Solution_> configPolicy,
            SelectionCacheType minimumCacheType, boolean randomSelection) {
        SelectionOrder selectionOrder = SelectionOrder.fromRandomSelectionBoolean(randomSelection);
        EntitySelectorConfig entitySelectorConfig = new EntitySelectorConfig();
        //entitySelectorConfig.setCacheType(SelectionCacheType.PHASE);
        EntitySelector<Solution_> entitySelector =
                EntitySelectorFactory.<Solution_> create(entitySelectorConfig)
                        .buildEntitySelector(configPolicy, minimumCacheType, selectionOrder);
        // TODO support coexistence of list and basic variables https://issues.redhat.com/browse/PLANNER-2755
        GenuineVariableDescriptor<Solution_> variableDescriptor =
                getTheOnlyVariableDescriptor(entitySelector.getEntityDescriptor());
        if (!variableDescriptor.isListVariable()) {
            throw new IllegalArgumentException("The subListSwapMoveSelector (" + config
                    + ") can only be used when the domain model has a list variable."
                    + " Check your @" + PlanningEntity.class.getSimpleName()
                    + " and make sure it has a @" + PlanningListVariable.class.getSimpleName() + ".");
        }

        ValueSelector<Solution_> valueSelector = buildEntityDependentValueSelector(configPolicy,
                entitySelector.getEntityDescriptor(), minimumCacheType, selectionOrder);
        int minimumK = Objects.requireNonNullElse(config.getMinimumK(), DEFAULT_MINIMUM_K);
        int maximumK = Objects.requireNonNullElse(config.getMaximumK(), DEFAULT_MAXIMUM_K);
        return new ListKOptMoveSelector<>(((ListVariableDescriptor<Solution_>) variableDescriptor), entitySelector, valueSelector,
                minimumK, maximumK);
    }

    private ValueSelector<Solution_> buildEntityDependentValueSelector(
            HeuristicConfigPolicy<Solution_> configPolicy, EntityDescriptor<Solution_> entityDescriptor,
            SelectionCacheType minimumCacheType, SelectionOrder inheritedSelectionOrder) {
        ValueSelectorConfig valueSelectorConfig = new ValueSelectorConfig();
        ValueSelector<Solution_> valueSelector =
                ValueSelectorFactory.<Solution_> create(new ValueSelectorConfig())
                        .buildValueSelector(configPolicy, entityDescriptor, minimumCacheType, inheritedSelectionOrder);
        return valueSelector;
    }
}
