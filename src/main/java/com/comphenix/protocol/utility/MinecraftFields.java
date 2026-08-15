package com.comphenix.protocol.utility;

import com.github.retrooper.packetevents.PacketEvents;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/** Version-neutral accessors. PacketEvents owns the connection lookup on modern servers. */
public final class MinecraftFields {
    private MinecraftFields() { }
    public static Object getNetworkManager(Player player) {
        return player == null || PacketEvents.getAPI() == null ? null : PacketEvents.getAPI().getPlayerManager().getChannel(player);
    }
    public static Object getPlayerConnection(Player player) {
        if (player == null) return null;
        Object handle = invokeNoArg(player, "getHandle");
        Object connection = findNamedMember(handle, "connection", "playerconnection", "packetlistener");
        return connection != null ? connection : getNetworkManager(player);
    }

    public static Object getPlayerConnection(Object player) {
        return player instanceof Player p ? getPlayerConnection(p) : findNamedMember(player,
                "connection", "playerconnection", "packetlistener");
    }

    public static Object getPlayerFromConnection(Object connection) {
        if (connection == null) return null;
        Set<Object> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        return findPlayer(connection, seen, 4);
    }

    private static Object findPlayer(Object value, Set<Object> seen, int depth) {
        if (value == null || depth < 0 || !seen.add(value)) return null;
        if (value instanceof Player) return value;
        Object direct = invokeNoArg(value, "getPlayer", "getBukkitPlayer", "getBukkitEntity");
        if (direct instanceof Player) return direct;
        for (Class<?> current = value.getClass(); current != null; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || field.getType().isPrimitive()) continue;
                String name = field.getName().toLowerCase(java.util.Locale.ROOT);
                if (!name.contains("player") && !name.contains("connection") && !name.contains("listener")) continue;
                try {
                    field.setAccessible(true);
                    Object found = findPlayer(field.get(value), seen, depth - 1);
                    if (found != null) return found;
                } catch (ReflectiveOperationException | RuntimeException ignored) { }
            }
        }
        return null;
    }

    private static Object findNamedMember(Object value, String... needles) {
        if (value == null) return null;
        for (Class<?> current = value.getClass(); current != null; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                String name = field.getName().toLowerCase(java.util.Locale.ROOT);
                for (String needle : needles) {
                    if (name.contains(needle)) {
                        try {
                            field.setAccessible(true);
                            return field.get(value);
                        } catch (ReflectiveOperationException | RuntimeException ignored) { }
                    }
                }
            }
        }
        return invokeNoArg(value, "getConnection", "connection", "getPacketListener");
    }

    private static Object invokeNoArg(Object target, String... names) {
        if (target == null) return null;
        for (String name : names) {
            try {
                Method method = target.getClass().getMethod(name);
                method.setAccessible(true);
                return method.invoke(target);
            } catch (ReflectiveOperationException | RuntimeException ignored) { }
        }
        return null;
    }
}
