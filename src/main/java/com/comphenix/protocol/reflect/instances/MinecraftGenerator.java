package com.comphenix.protocol.reflect.instances;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/** Optional server-specific provider; PacketEvents owns the native constructors. */
public class MinecraftGenerator implements InstanceProvider {
    public static final UUID SYS_UUID = new UUID(0L, 0L);
    public static final Object AIR_ITEM_STACK = new ItemStack(Material.AIR);
    public static final InstanceProvider INSTANCE = new MinecraftGenerator();
    public MinecraftGenerator() { }
    @Override public Object create(@Nullable Class<?> type) {
        if (type == null) return null;
        if (type == UUID.class) return SYS_UUID;
        if (type.isEnum()) {
            Object[] constants = type.getEnumConstants();
            return constants == null || constants.length == 0 ? null : constants[0];
        }
        if (type == ItemStack.class) return AIR_ITEM_STACK;
        if (Map.class.isAssignableFrom(type)) {
            try { return type.getDeclaredConstructor().newInstance(); }
            catch (ReflectiveOperationException ignored) { return new HashMap<>(); }
        }
        return null;
    }
}
