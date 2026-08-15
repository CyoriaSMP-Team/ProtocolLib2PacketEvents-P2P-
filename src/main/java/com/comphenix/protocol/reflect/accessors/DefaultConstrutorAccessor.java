package com.comphenix.protocol.reflect.accessors;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;

final class DefaultConstrutorAccessor implements ConstructorAccessor {
    private final Constructor<?> constructor;
    private final MethodHandle unusedHandle;

    public DefaultConstrutorAccessor(Constructor<?> constructor, MethodHandle unusedHandle) {
        this.constructor = constructor;
        this.unusedHandle = unusedHandle;
    }

    @Override
    public Object invoke(Object... args) {
        try {
            return constructor.newInstance(args);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Unable to construct " + constructor, ex);
        }
    }

    @Override public Constructor<?> getConstructor() { return constructor; }
}
