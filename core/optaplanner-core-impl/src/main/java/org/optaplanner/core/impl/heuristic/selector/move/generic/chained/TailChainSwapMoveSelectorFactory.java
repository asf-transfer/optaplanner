package org.optaplanner.core.impl.heuristic.selector.move.generic.chained;

import java.util.Objects;

import org.optaplanner.core.config.heuristic.selector.common.SelectionCacheType;
import org.optaplanner.core.config.heuristic.selector.common.SelectionOrder;
import org.optaplanner.core.config.heuristic.selector.entity.EntitySelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.chained.TailChainSwapMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.value.ValueSelectorConfig;
import org.optaplanner.core.impl.heuristic.HeuristicConfigPolicy;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.AbstractMoveSelectorFactory;
import org.optaplanner.core.impl.heuristic.selector.move.MoveSelector;
import org.optaplanner.core.impl.heuristic.selector.value.ValueSelector;
import org.optaplanner.core.impl.heuristic.selector.value.ValueSelectorFactory;
import org.optaplanner.core.impl.solver.ClassInstanceCache;

public class TailChainSwapMoveSelectorFactory<Solution_>
        extends AbstractMoveSelectorFactory<Solution_, TailChainSwapMoveSelectorConfig> {

    public TailChainSwapMoveSelectorFactory(TailChainSwapMoveSelectorConfig moveSelectorConfig) {
        super(moveSelectorConfig);
    }

    @Override
    protected MoveSelector<Solution_> buildBaseMoveSelector(HeuristicConfigPolicy<Solution_> configPolicy,
            SelectionCacheType minimumCacheType, boolean randomSelection, ClassInstanceCache instanceCache) {
        EntitySelectorConfig entitySelectorConfig =
                Objects.requireNonNullElseGet(config.getEntitySelectorConfig(), EntitySelectorConfig::new);
        ValueSelectorConfig valueSelectorConfig =
                Objects.requireNonNullElseGet(config.getValueSelectorConfig(), ValueSelectorConfig::new);
        SelectionOrder selectionOrder = SelectionOrder.fromRandomSelectionBoolean(randomSelection);
        EntitySelector<Solution_> entitySelector = EntitySelectorFactory.<Solution_> create(entitySelectorConfig)
                .buildEntitySelector(configPolicy, minimumCacheType, selectionOrder, instanceCache);
        ValueSelector<Solution_> valueSelector = ValueSelectorFactory.<Solution_> create(valueSelectorConfig)
                .buildValueSelector(configPolicy, entitySelector.getEntityDescriptor(), minimumCacheType, selectionOrder,
                        instanceCache);
        return new TailChainSwapMoveSelector<>(entitySelector, valueSelector, randomSelection);
    }
}
