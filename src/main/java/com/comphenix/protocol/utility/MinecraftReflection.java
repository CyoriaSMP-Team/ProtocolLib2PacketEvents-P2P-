/*
 * ProtocolLib2PacketEvents (P2P) - a drop-in ProtocolLib compatibility layer
 * powered by PacketEvents.
 */
package com.comphenix.protocol.utility;

import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.inventory.ItemStack;

/** Version-neutral subset of ProtocolLib's MinecraftReflection facade. */
public final class MinecraftReflection {
    private MinecraftReflection() {
    }

    public static Class<?> getGameProfileClass() {
        return WrappedGameProfile.ProfileHandle.class;
    }

    public static Object getMinecraftItemStack(ItemStack stack) {
        return stack == null ? null : SpigotConversionUtil.fromBukkitItemStack(stack);
    }

    public static Class<?> getChunkCoordIntPair() {
        return ChunkCoordIntPair.class;
    }

    public static Class<?> getNullableNMS(String... names) {
        for (String name : names) {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException ignored) {
            }
        }
        return null;
    }
}
