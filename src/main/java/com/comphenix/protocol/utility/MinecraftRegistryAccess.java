package com.comphenix.protocol.utility;

import com.comphenix.protocol.wrappers.codecs.WrappedDynamicOps;
import org.bukkit.Bukkit;

import java.lang.reflect.Method;

/** Registry facade; PacketEvents handles registry-aware codecs, so this is a pass-through. */
public class MinecraftRegistryAccess {
    private static volatile Object registryAccess;
    public MinecraftRegistryAccess() { }
    public static Object get() {
        Object cached = registryAccess;
        if (cached != null) return cached;
        Object server = Bukkit.getServer();
        Object handle = invokeNoArg(server, "getHandle");
        Object value = invokeNoArg(handle, "registryAccess", "getRegistryAccess", "registryAccessProvider");
        if (value == null) {
            value = invokeNoArg(server, "registryAccess", "getRegistryAccess");
        }
        if (value == null) {
            throw new UnsupportedOperationException("This server does not expose a registry access provider");
        }
        registryAccess = value;
        return value;
    }
    public static WrappedDynamicOps createSerializationContext(WrappedDynamicOps ops) { return ops; }

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
