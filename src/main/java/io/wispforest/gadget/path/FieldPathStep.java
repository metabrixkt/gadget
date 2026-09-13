package io.wispforest.gadget.path;

import net.auoeke.reflect.Accessor;

import java.lang.reflect.Field;

public record FieldPathStep(String className, String fieldName) implements PathStep {
    public static FieldPathStep forField(Field field) {
        return new FieldPathStep(field.getDeclaringClass().getName(), field.getName());
    }

    public String fieldId() {
        return className + "#" + fieldName;
    }

    @Override
    public Object follow(Object o) {
        return Accessor.get(o, fieldName);
    }

    @Override
    public void set(Object o, Object to) {
        Accessor.put(o, fieldName, to);
    }

    @Override
    public String toString() {
        return fieldName;
    }
}
