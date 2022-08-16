package org.optaplanner.core.impl.domain.solution.cloner;

import java.lang.reflect.Field;
import java.util.Optional;

final class ByteFieldCloner<C> implements FieldCloner<C> {

    private static final FieldCloner INSTANCE = new ByteFieldCloner();

    public static <C> FieldCloner<C> getInstance() {
        return INSTANCE;
    }

    @Override
    public Optional<Unprocessed> clone(DeepCloningUtils deepCloningUtils, Field field, Class<? extends C> instanceClass,
            C original, C clone) {
        byte originalValue = getFieldValue(original, field);
        setFieldValue(clone, field, originalValue);
        return Optional.empty();
    }

    private static byte getFieldValue(Object bean, Field field) {
        try {
            return field.getByte(bean);
        } catch (IllegalAccessException e) {
            FieldCloner.failOnRead(bean, field, e);
            return 0;
        }
    }

    private static void setFieldValue(Object bean, Field field, byte value) {
        try {
            field.setByte(bean, value);
        } catch (IllegalAccessException e) {
            FieldCloner.failOnWrite(bean, field, value, e);
        }
    }

    private ByteFieldCloner() {

    }

}
