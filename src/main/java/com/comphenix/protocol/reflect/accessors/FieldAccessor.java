package com.comphenix.protocol.reflect.accessors;

import java.lang.reflect.Field;

public interface FieldAccessor {
    FieldAccessor NO_OP_ACCESSOR = new FieldAccessor() {
        @Override public Object get(Object instance) { return null; }
        @Override public void set(Object instance, Object value) { }
        @Override public Field getField() { return null; }
    };

    Object get(Object instance);
    void set(Object instance, Object value);
    Field getField();
}
