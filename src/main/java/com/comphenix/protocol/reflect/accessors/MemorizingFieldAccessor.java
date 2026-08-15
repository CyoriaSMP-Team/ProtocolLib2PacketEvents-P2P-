package com.comphenix.protocol.reflect.accessors;

import java.lang.reflect.Field;

final class MemorizingFieldAccessor implements FieldAccessor {
    private static final Object NIL = new Object();
    private final FieldAccessor inner;
    private volatile Object value = NIL;

    public MemorizingFieldAccessor(FieldAccessor inner) { this.inner = inner; }
    @Override public Object get(Object instance) { if (value == NIL) value = inner.get(instance); return value; }
    @Override public void set(Object instance, Object newValue) { inner.set(instance, newValue); value = newValue; }
    @Override public Field getField() { return inner.getField(); }
}
