package org.optaplanner.core.impl.domain.variable.custom;

import java.util.Collection;
import java.util.Collections;

import org.optaplanner.core.api.domain.variable.AbstractVariableListener;
import org.optaplanner.core.impl.domain.variable.descriptor.VariableDescriptor;

public final class ListenerSources<Solution_> {

    private final AbstractVariableListener<Solution_, Object> variableListener;
    private final Collection<VariableDescriptor<Solution_>> sourceVariableDescriptors;

    public ListenerSources(AbstractVariableListener<Solution_, Object> variableListener,
            Collection<VariableDescriptor<Solution_>> sourceVariableDescriptors) {
        this.variableListener = variableListener;
        this.sourceVariableDescriptors = sourceVariableDescriptors;
    }

    public ListenerSources(AbstractVariableListener<Solution_, Object> variableListener,
            VariableDescriptor<Solution_> sourceVariableDescriptor) {
        this(variableListener, Collections.singleton(sourceVariableDescriptor));
    }

    public AbstractVariableListener<Solution_, Object> getVariableListener() {
        return variableListener;
    }

    public Collection<VariableDescriptor<Solution_>> getSourceVariableDescriptors() {
        return sourceVariableDescriptors;
    }

    public Collection<ListenerSources<Solution_>> toCollection() {
        return Collections.singleton(this);
    }
}
