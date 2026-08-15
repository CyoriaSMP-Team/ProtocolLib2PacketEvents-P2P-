/*
 * ProtocolLib2PacketEvents (P2P) - a drop-in ProtocolLib compatibility layer
 * powered by PacketEvents.
 */
package com.comphenix.protocol.reflect.accessors;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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

    public static FieldAccessor[] getFieldAccessorArray(Class<?> clazz, Class<?> fieldClass, boolean forceAccess) {
        List<FieldAccessor> result = new ArrayList<>();
        for (Class<?> current = clazz; current != null; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (field.getType().equals(fieldClass)) {
                    result.add(getFieldAccessor(field));
                }
            }
        }
        return result.toArray(new FieldAccessor[0]);
    }

    public static FieldAccessor getFieldAccessor(Class<?> instanceClass, Class<?> fieldClass, boolean forceAccess) {
        for (Class<?> current = instanceClass; current != null; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (field.getType().equals(fieldClass)) {
                    return getFieldAccessor(field);
                }
            }
        }
        throw new IllegalArgumentException("Unable to find field of type " + fieldClass + " on " + instanceClass);
    }

    public static FieldAccessor getFieldAccessorOrNull(Class<?> clazz, String fieldName, Class<?> fieldType) {
        for (Class<?> current = clazz; current != null; current = current.getSuperclass()) {
            try {
                Field field = current.getDeclaredField(fieldName);
                if (fieldType == null || fieldType.isAssignableFrom(field.getType())) return getFieldAccessor(field);
                return null;
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }

    public static FieldAccessor getFieldAccessor(Field field) {
        if (field == null) throw new IllegalArgumentException("field cannot be null");
        field.setAccessible(true);
        return new DefaultFieldAccessor(field);
    }

    public static FieldAccessor getMemorizing(FieldAccessor inner) {
        return new MemorizingFieldAccessor(inner);
    }

    public static ConstructorAccessor getConstructorAccessor(Class<?> instanceClass, Class<?>... parameters) {
        Constructor<?> constructor = findConstructor(instanceClass, parameters);
        if (constructor == null) throw new IllegalArgumentException("Unable to find constructor on " + instanceClass);
        return getConstructorAccessor(constructor);
    }

    public static ConstructorAccessor getConstructorAccessorOrNull(Class<?> clazz, Class<?>... parameters) {
        Constructor<?> constructor = findConstructor(clazz, parameters);
        return constructor == null ? null : getConstructorAccessor(constructor);
    }

    public static ConstructorAccessor getConstructorAccessor(Constructor<?> constructor) {
        if (constructor == null) throw new IllegalArgumentException("constructor cannot be null");
        constructor.setAccessible(true);
        return new DefaultConstrutorAccessor(constructor, null);
    }

    private static Constructor<?> findConstructor(Class<?> clazz, Class<?>... parameters) {
        try {
            return clazz.getDeclaredConstructor(parameters);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
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
