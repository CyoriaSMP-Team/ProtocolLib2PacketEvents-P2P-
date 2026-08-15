/*
 * ProtocolLib2PacketEvents (P2P) - a drop-in ProtocolLib compatibility layer
 * powered by PacketEvents.
 */
package com.comphenix.protocol.reflect.accessors;

import java.lang.reflect.Method;

/** Reflection accessor factory for legacy ProtocolLib consumers. */
public final class Accessors {
    private Accessors() {
    }

    public static MethodAccessor getMethodAccessor(Class<?> instanceClass, String methodName,
                                                   Class<?>... parameters) {
        Method method = findMethod(instanceClass, methodName, parameters);
        if (method == null) {
            throw new IllegalArgumentException("Unable to find method " + methodName + " on " + instanceClass);
        }
        return getMethodAccessor(method);
    }

    public static MethodAccessor getMethodAccessorOrNull(Class<?> instanceClass, String methodName,
                                                        Class<?>... parameters) {
        Method method = findMethod(instanceClass, methodName, parameters);
        return method == null ? null : getMethodAccessor(method);
    }

    public static MethodAccessor getMethodAccessor(Method method) {
        if (method == null) throw new IllegalArgumentException("method cannot be null");
        method.setAccessible(true);
        return new ReflectiveMethodAccessor(method);
    }

    private static Method findMethod(Class<?> type, String name, Class<?>[] parameters) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(name) && java.util.Arrays.equals(method.getParameterTypes(), parameters)) {
                    return method;
                }
            }
        }
        try {
            return type.getMethod(name, parameters);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private static final class ReflectiveMethodAccessor implements MethodAccessor {
        private final Method method;

        private ReflectiveMethodAccessor(Method method) {
            this.method = method;
        }

        @Override
        public Object invoke(Object target, Object... args) {
            try {
                return method.invoke(target, args);
            } catch (ReflectiveOperationException e) {
                Throwable cause = e instanceof java.lang.reflect.InvocationTargetException
                        && ((java.lang.reflect.InvocationTargetException) e).getCause() != null
                        ? ((java.lang.reflect.InvocationTargetException) e).getCause() : e;
                if (cause instanceof RuntimeException) throw (RuntimeException) cause;
                throw new IllegalStateException("Unable to invoke " + method, cause);
            }
        }

        @Override
        public Method getMethod() {
            return method;
        }
    }
}
