/*
 * ProtocolLib2PacketEvents (P2P) - a drop-in ProtocolLib compatibility layer
 * powered by PacketEvents.
 */
package com.comphenix.protocol.utility;

import org.bukkit.inventory.ItemStack;

import java.util.Base64;

/** Bukkit-native item serializer compatible with the legacy StreamSerializer method surface. */
public final class StreamSerializer {
    private static final StreamSerializer DEFAULT = new StreamSerializer();

    private StreamSerializer() {
    }

    public static StreamSerializer getDefault() {
        return DEFAULT;
    }

    public String serializeItemStack(ItemStack stack) {
        return stack == null ? null : Base64.getEncoder().encodeToString(stack.serializeAsBytes());
    }

    public ItemStack deserializeItemStack(String encoded) {
        if (encoded == null || encoded.isEmpty()) return null;
        return ItemStack.deserializeBytes(Base64.getDecoder().decode(encoded));
    }
}
