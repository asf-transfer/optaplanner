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

package org.optaplanner.core.config.heuristic.selector.move.composite;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

import org.optaplanner.core.config.heuristic.selector.move.MoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.factory.MoveIteratorFactoryConfig;
import org.optaplanner.core.config.heuristic.selector.move.factory.MoveListFactoryConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.ChangeMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.PillarChangeMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.PillarSwapMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.SwapMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.chained.SubChainChangeMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.chained.SubChainSwapMoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.generic.chained.TailChainSwapMoveSelectorConfig;
import org.optaplanner.core.config.util.ConfigUtils;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionProbabilityWeightFactory;

@XmlType(propOrder = {
        "moveSelectorList",
        "selectorProbabilityWeightFactoryClass"
})
public class UnionMoveSelectorConfig extends MoveSelectorConfig<UnionMoveSelectorConfig> {

    public static final String XML_ELEMENT_NAME = "unionMoveSelector";

    @XmlElements({
            @XmlElement(name = CartesianProductMoveSelectorConfig.XML_ELEMENT_NAME,
                    type = CartesianProductMoveSelectorConfig.class),
            @XmlElement(name = ChangeMoveSelectorConfig.XML_ELEMENT_NAME, type = ChangeMoveSelectorConfig.class),
            @XmlElement(name = MoveIteratorFactoryConfig.XML_ELEMENT_NAME, type = MoveIteratorFactoryConfig.class),
            @XmlElement(name = MoveListFactoryConfig.XML_ELEMENT_NAME, type = MoveListFactoryConfig.class),
            @XmlElement(name = PillarChangeMoveSelectorConfig.XML_ELEMENT_NAME,
                    type = PillarChangeMoveSelectorConfig.class),
            @XmlElement(name = PillarSwapMoveSelectorConfig.XML_ELEMENT_NAME, type = PillarSwapMoveSelectorConfig.class),
            @XmlElement(name = SubChainChangeMoveSelectorConfig.XML_ELEMENT_NAME,
                    type = SubChainChangeMoveSelectorConfig.class),
            @XmlElement(name = SubChainSwapMoveSelectorConfig.XML_ELEMENT_NAME,
                    type = SubChainSwapMoveSelectorConfig.class),
            @XmlElement(name = SwapMoveSelectorConfig.XML_ELEMENT_NAME, type = SwapMoveSelectorConfig.class),
            @XmlElement(name = TailChainSwapMoveSelectorConfig.XML_ELEMENT_NAME,
                    type = TailChainSwapMoveSelectorConfig.class),
            @XmlElement(name = UnionMoveSelectorConfig.XML_ELEMENT_NAME, type = UnionMoveSelectorConfig.class)
    })
    private List<MoveSelectorConfig> moveSelectorList = null;

    private Class<? extends SelectionProbabilityWeightFactory> selectorProbabilityWeightFactoryClass = null;

    // ************************************************************************
    // Constructors and simple getters/setters
    // ************************************************************************

    public UnionMoveSelectorConfig() {
    }

    public UnionMoveSelectorConfig(List<MoveSelectorConfig> moveSelectorList) {
        this.moveSelectorList = moveSelectorList;
    }

    /**
     * @deprecated in favor of {@link #getMoveSelectorList()}.
     * @return sometimes null
     */
    @Deprecated
    public List<MoveSelectorConfig> getMoveSelectorConfigList() {
        return getMoveSelectorList();
    }

    /**
     * @deprecated in favor of {@link #setMoveSelectorList(List)}.
     * @param moveSelectorConfigList sometimes null
     */
    @Deprecated
    public void setMoveSelectorConfigList(List<MoveSelectorConfig> moveSelectorConfigList) {
        setMoveSelectorList(moveSelectorConfigList);
    }

    public List<MoveSelectorConfig> getMoveSelectorList() {
        return moveSelectorList;
    }

    public void setMoveSelectorList(List<MoveSelectorConfig> moveSelectorConfigList) {
        this.moveSelectorList = moveSelectorConfigList;
    }

    public Class<? extends SelectionProbabilityWeightFactory> getSelectorProbabilityWeightFactoryClass() {
        return selectorProbabilityWeightFactoryClass;
    }

    public void setSelectorProbabilityWeightFactoryClass(
            Class<? extends SelectionProbabilityWeightFactory> selectorProbabilityWeightFactoryClass) {
        this.selectorProbabilityWeightFactoryClass = selectorProbabilityWeightFactoryClass;
    }

    // ************************************************************************
    // With methods
    // ************************************************************************

    public UnionMoveSelectorConfig withMoveSelectorList(List<MoveSelectorConfig> moveSelectorConfigList) {
        this.moveSelectorList = moveSelectorConfigList;
        return this;
    }

    public UnionMoveSelectorConfig withMoveSelectors(MoveSelectorConfig... moveSelectorConfigs) {
        this.moveSelectorList = Arrays.asList(moveSelectorConfigs);
        return this;
    }

    public UnionMoveSelectorConfig withSelectorProbabilityWeightFactoryClass(
            Class<? extends SelectionProbabilityWeightFactory> selectorProbabilityWeightFactoryClass) {
        this.selectorProbabilityWeightFactoryClass = selectorProbabilityWeightFactoryClass;
        return this;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void extractLeafMoveSelectorConfigsIntoList(List<MoveSelectorConfig> leafMoveSelectorConfigList) {
        for (MoveSelectorConfig moveSelectorConfig : moveSelectorList) {
            moveSelectorConfig.extractLeafMoveSelectorConfigsIntoList(leafMoveSelectorConfigList);
        }
    }

    @Override
    public UnionMoveSelectorConfig inherit(UnionMoveSelectorConfig inheritedConfig) {
        super.inherit(inheritedConfig);
        moveSelectorList = ConfigUtils.inheritMergeableListConfig(moveSelectorList, inheritedConfig.getMoveSelectorList());
        selectorProbabilityWeightFactoryClass = ConfigUtils.inheritOverwritableProperty(
                selectorProbabilityWeightFactoryClass, inheritedConfig.getSelectorProbabilityWeightFactoryClass());
        return this;
    }

    @Override
    public UnionMoveSelectorConfig copyConfig() {
        return new UnionMoveSelectorConfig().inherit(this);
    }

    @Override
    public void visitReferencedClasses(Consumer<Class<?>> classVisitor) {
        visitCommonReferencedClasses(classVisitor);
        if (moveSelectorList != null) {
            moveSelectorList.forEach(ms -> ms.visitReferencedClasses(classVisitor));
        }
        classVisitor.accept(selectorProbabilityWeightFactoryClass);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + moveSelectorList + ")";
    }

}
