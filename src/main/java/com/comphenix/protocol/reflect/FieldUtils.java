/*
 * ProtocolLib2PacketEvents (P2P) - a drop-in ProtocolLib compatibility layer
 * powered by PacketEvents.
 */
package com.comphenix.protocol.reflect;

import java.lang.reflect.Field;

/** Field helper facade retained for legacy ProtocolLib plugins. */
public final class FieldUtils {
    private FieldUtils() {
    }

    public static Object readField(Object target, String name, boolean forceAccess) {
        if (target == null) throw new IllegalArgumentException("target cannot be null");
        try {
            Field field = find(target.getClass(), name);
            if (forceAccess) field.setAccessible(true);
            return field.get(target);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to read field " + name, e);
        }
    }

    public static Object readField(Object target, String name) {
        return readField(target, name, false);
    }

    public static void writeField(Object target, String name, Object value, boolean forceAccess) {
        if (target == null) throw new IllegalArgumentException("target cannot be null");
        try {
            Field field = find(target.getClass(), name);
            if (forceAccess) field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to write field " + name, e);
        }
    }

    private static Field find(Class<?> type, String name) throws NoSuchFieldException {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
            }
        }
        throw new NoSuchFieldException(name);
    }
}
