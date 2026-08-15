package com.comphenix.protocol.reflect.accessors;

import java.lang.reflect.Field;
import java.lang.invoke.MethodHandle;

final class DefaultFieldAccessor implements FieldAccessor {
    private final Field field;

    public DefaultFieldAccessor(Field field) {
        this.field = field;
        this.field.setAccessible(true);
    }

    public DefaultFieldAccessor(Field field, MethodHandle getter, MethodHandle setter, boolean forceAccess) {
        this(field);
        if (forceAccess) this.field.setAccessible(true);
    }

    @Override public Object get(Object instance) {
        try { return field.get(instance); }
        catch (IllegalAccessException ex) { throw new IllegalStateException("Unable to read " + field, ex); }
    }

    @Override public void set(Object instance, Object value) {
        try { field.set(instance, value); }
        catch (IllegalAccessException ex) { throw new IllegalStateException("Unable to write " + field, ex); }
    }

    @Override public Field getField() { return field; }
}
