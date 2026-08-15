/*
 * ProtocolLib2PacketEvents (P2P) - a drop-in ProtocolLib compatibility layer
 * powered by PacketEvents.
 */
package com.comphenix.protocol.reflect;

import java.lang.reflect.Method;

/** Method invocation facade matching the old ProtocolLib helper. */
public final class MethodUtils {
    private MethodUtils() {
    }

    public static Object invokeMethod(Object target, String name, Object... args) {
        if (target == null) throw new IllegalArgumentException("target cannot be null");
        Object[] actual = args == null ? new Object[0] : args;
        Method candidate = null;
        for (Class<?> current = target.getClass(); current != null && candidate == null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(name) && method.getParameterCount() == actual.length
                        && compatible(method.getParameterTypes(), actual)) {
                    candidate = method;
                    break;
                }
            }
        }
        if (candidate == null) throw new IllegalArgumentException("Unable to find method " + name);
        try {
            candidate.setAccessible(true);
            return candidate.invoke(target, actual);
        } catch (ReflectiveOperationException e) {
            Throwable cause = e instanceof java.lang.reflect.InvocationTargetException
                    && ((java.lang.reflect.InvocationTargetException) e).getCause() != null
                    ? ((java.lang.reflect.InvocationTargetException) e).getCause() : e;
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            throw new IllegalStateException("Unable to invoke method " + name, cause);
        }
    }

    private static boolean compatible(Class<?>[] types, Object[] values) {
        for (int i = 0; i < types.length; i++) {
            if (values[i] == null) {
                if (types[i].isPrimitive()) return false;
            } else if (!box(types[i]).isInstance(values[i])) {
                return false;
            }
        }
        return true;
    }

    private static Class<?> box(Class<?> type) {
        if (!type.isPrimitive()) return type;
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == float.class) return Float.class;
        if (type == double.class) return Double.class;
        if (type == char.class) return Character.class;
        return type;
    }
}
