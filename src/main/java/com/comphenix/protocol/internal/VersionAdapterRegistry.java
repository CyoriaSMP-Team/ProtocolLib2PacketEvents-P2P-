/*
 * ProtocolLib2PacketEvents - clean-room version adapter registry.
 */
package com.comphenix.protocol.internal;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Centralized feature detection so version checks do not leak through packet code. */
public final class VersionAdapterRegistry {
    private static volatile VersionAdapter active = new BukkitFeatureAdapter();

    private VersionAdapterRegistry() {
    }

    public static VersionAdapter current() {
        return active;
    }

    public static void refresh() {
        active = new BukkitFeatureAdapter();
    }

    private static final class BukkitFeatureAdapter implements VersionAdapter {
        @Override public ServerVersion serverVersion() {
            if (PacketEvents.getAPI() == null) return ServerVersion.ERROR;
            return PacketEvents.getAPI().getServerManager().getVersion();
        }
        @Override public boolean supportsNativeHandle(com.comphenix.protocol.PacketType type) {
            return type != null && type.isSupported();
        }
        @Override public Object createNativePacket(com.comphenix.protocol.PacketType type, Object... arguments) {
            throw new UnsupportedOperationException("Native NMS construction is not exposed by PacketEvents for " + type);
        }
        @Override public List<Player> trackers(Entity entity) {
            if (entity == null || entity.getWorld() == null) return Collections.emptyList();
            double view = Math.max(2, entity.getWorld().getViewDistance()) * 16.0;
            double squared = view * view;
            List<Player> result = new ArrayList<>();
            for (Player player : entity.getWorld().getPlayers()) {
                if (player.getLocation().distanceSquared(entity.getLocation()) <= squared) result.add(player);
            }
            return result;
        }
    }
}
