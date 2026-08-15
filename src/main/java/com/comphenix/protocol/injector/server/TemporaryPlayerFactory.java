/*
 * ProtocolLib2PacketEvents (P2P) - a drop-in ProtocolLib compatibility layer
 * powered by PacketEvents.
 */
package com.comphenix.protocol.injector.server;

import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import javax.crypto.SecretKey;

/** Compatibility factory for the old ProtocolLib login injector API. */
public final class TemporaryPlayerFactory {
    private TemporaryPlayerFactory() {
    }

    public static SocketInjector getInjectorFromPlayer(Player player) {
        return player == null ? null : new SocketInjector(player);
    }

    public static void setInjectorForPlayer(Player player, SocketInjector injector) {
        // PacketEvents owns the live injector; retained only for source compatibility.
    }

    static Object findNetworkManager(Player player) {
        try {
            Method getHandle = player.getClass().getMethod("getHandle");
            Object handle = getHandle.invoke(player);
            return findConnection(handle, Collections.newSetFromMap(new IdentityHashMap<>()), 4);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static Object findConnection(Object value, Set<Object> seen, int depth) {
        if (value == null || depth < 0 || !seen.add(value)) return null;
        Class<?> type = value.getClass();
        String name = type.getName().toLowerCase(java.util.Locale.ROOT);
        if ((name.contains("networkmanager") || name.endsWith(".connection") || name.contains("connection"))
                && acceptsSecretKey(type)) {
            return value;
        }
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || field.getType().isPrimitive()) continue;
                String fieldName = field.getName().toLowerCase(java.util.Locale.ROOT);
                if (!fieldName.contains("connection") && !fieldName.contains("network")) continue;
                try {
                    field.setAccessible(true);
                    Object child = field.get(value);
                    Object found = findConnection(child, seen, depth - 1);
                    if (found != null) return found;
                } catch (IllegalAccessException ignored) {
                }
            }
        }
        return null;
    }

    private static boolean acceptsSecretKey(Class<?> type) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getParameterCount() == 1 && method.getParameterTypes()[0].isAssignableFrom(SecretKey.class)) {
                    return true;
                }
            }
        }
        return false;
    }
}
