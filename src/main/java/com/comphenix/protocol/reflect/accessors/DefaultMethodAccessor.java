package com.comphenix.protocol.reflect.accessors;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

final class DefaultMethodAccessor implements MethodAccessor {
    private final Method method;
    private final MethodHandle unusedHandle;

    public DefaultMethodAccessor(Method method, MethodHandle unusedHandle, boolean staticMethod) {
        this.method = method;
        this.unusedHandle = unusedHandle;
        this.method.setAccessible(true);
    }

    @Override public Object invoke(Object target, Object... args) {
        try { return method.invoke(target, args); }
        catch (ReflectiveOperationException ex) { throw new IllegalStateException("Unable to invoke " + method, ex); }
    }

    @Override public Method getMethod() { return method; }
}
