package com.comphenix.protocol.reflect.accessors;

import java.lang.reflect.Constructor;

public interface ConstructorAccessor {
    ConstructorAccessor NO_OP_ACCESSOR = new ConstructorAccessor() {
        @Override public Object invoke(Object... args) { return null; }
        @Override public Constructor<?> getConstructor() { return null; }
    };

    Object invoke(Object... args);
    Constructor<?> getConstructor();
}
