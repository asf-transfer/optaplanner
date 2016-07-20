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
package org.optaplanner.core.impl.score.director.drools.testgen.fact;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class ListValueProvider extends AbstractValueProvider {

    private final String identifier;
    private final Type typeArgument;
    private final Map<Object, TestGenFact> existingInstances;
    private final List<Class<?>> imports = new ArrayList<Class<?>>();

    public ListValueProvider(Object value, String identifier, Type genericType, Map<Object, TestGenFact> existingInstances) {
        super(value);
        this.identifier = identifier;
        this.typeArgument = genericType;
        this.existingInstances = existingInstances;
        imports.add(ArrayList.class);
        imports.add((Class<?>) genericType);
        for (Object item : ((java.util.List<?>) value)) {
            imports.add(item.getClass());
        }
    }

    public List<TestGenFact> getFacts() {
        ArrayList<TestGenFact> facts = new ArrayList<TestGenFact>();
        for (Object o : (List<?>) value) {
            TestGenFact fact = existingInstances.get(o);
            if (fact != null) {
                facts.add(fact);
            }
        }
        return facts;
    }

    public List<Class<?>> getImports() {
        return imports;
    }

    @Override
    public void printSetup(StringBuilder sb) {
        String e = ((Class<?>) typeArgument).getSimpleName();
        sb.append(String.format("        ArrayList<%s> %s = new ArrayList<%s>();%n", e, identifier, e));
        for (Object item : ((java.util.List<?>) value)) {
            sb.append(String.format("        //%s%n", item));
            sb.append(String.format("        %s.add(%s);%n", identifier, existingInstances.get(item)));
        }
    }

    @Override
    public String toString() {
        return identifier;
    }

}
