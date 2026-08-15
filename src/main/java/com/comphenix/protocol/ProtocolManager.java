/*
 * ProtocolLib2PacketEvents (P2P) - a drop-in ProtocolLib compatibility layer
 * powered by PacketEvents.
 */
package com.comphenix.protocol;

import com.comphenix.protocol.async.AsyncMarker;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.PacketConstructor;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.collect.ImmutableSet;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** Full ProtocolLib-facing protocol manager contract. */
public interface ProtocolManager extends PacketStream {
    int getProtocolVersion(Player player);

    /** Keep these declarations on ProtocolManager itself, as in ProtocolLib's
     * public contract.  They are also inherited from PacketStream. */
    @Override
    void sendServerPacket(Player receiver, PacketContainer packet, boolean filters);

    @Override
    void receiveClientPacket(Player sender, PacketContainer packet, boolean filters);

    void broadcastServerPacket(PacketContainer packet);

    default void broadcastServerPacket(PacketContainer packet, Entity entity, boolean includeTracker) {
        for (Player player : getEntityTrackers(entity)) {
            if (includeTracker || player != entity) sendServerPacket(player, packet);
        }
    }

    default void broadcastServerPacket(PacketContainer packet, Location origin, int maxObserverDistance) {
        if (origin == null || origin.getWorld() == null) return;
        double distance = (double) maxObserverDistance * maxObserverDistance;
        for (Player player : origin.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(origin) <= distance) sendServerPacket(player, packet);
        }
    }

    default void broadcastServerPacket(PacketContainer packet, Collection<? extends Player> targetPlayers) {
        if (targetPlayers == null) return;
        for (Player player : targetPlayers) sendServerPacket(player, packet);
    }

    ImmutableSet<PacketListener> getPacketListeners();

    void addPacketListener(PacketListener listener);

    void removePacketListener(PacketListener listener);

    void removePacketListeners(Plugin plugin);

    PacketContainer createPacket(PacketType type);

    default PacketContainer createPacket(PacketType type, boolean forceDefaults) {
        return createPacket(type);
    }

    default PacketConstructor createPacketConstructor(PacketType type, Object... arguments) {
        return PacketConstructor.DEFAULT.withPacket(type, arguments);
    }

    default void updateEntity(Entity entity, List<Player> observers) {
        throw new UnsupportedOperationException("entity refresh is not available from this backend");
    }

    Entity getEntityFromID(World world, int entityId);

    default List<Player> getEntityTrackers(Entity entity) {
        return Collections.emptyList();
    }

    default Set<PacketType> getSendingFilterTypes() {
        return getListeningTypes();
    }

    default Set<PacketType> getReceivingFilterTypes() {
        return getListeningTypes();
    }

    Set<PacketType> getListeningTypes();

    MinecraftVersion getMinecraftVersion();

    default boolean isClosed() {
        return false;
    }

    AsynchronousManager getAsynchronousManager();

    default void verifyWhitelist(PacketListener listener, ListeningWhitelist whitelist) {
        if (listener == null || ListeningWhitelist.isEmpty(whitelist)) {
            throw new IllegalArgumentException("listener and a non-empty whitelist are required");
        }
    }

    /** Historical misspelling retained by ProtocolLib. */
    @Deprecated
    default void recieveClientPacket(Player sender, PacketContainer packet) {
        receiveClientPacket(sender, packet);
    }

    /** Historical overload retained by ProtocolLib. */
    @Deprecated
    default void recieveClientPacket(Player sender, PacketContainer packet, boolean filters) {
        receiveClientPacket(sender, packet, filters);
    }
}
