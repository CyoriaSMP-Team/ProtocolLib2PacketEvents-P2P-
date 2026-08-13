/*
 * ProtocolLib2PacketEvents (P2P) - a drop-in ProtocolLib compatibility layer
 * powered by PacketEvents.
 *
 * Copyright (C) 2026 CyoriaSMP Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.comphenix.protocol.injector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.event.ProtocolPacketEvent;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Fans PacketEvents' single event stream out to every registered ProtocolLib
 * {@link PacketListener}. One instance backs {@link com.comphenix.protocol.ProtocolLibrary}
 * for the whole server; the plugin's one PacketEvents listener forwards each packet here.
 * <p>
 * Listeners are indexed by packet type rather than scanned linearly, because dispatch runs on
 * the Netty thread for every single packet: with a linear scan, one plugin listening for one
 * packet type would still cost a whitelist lookup on every packet of every type. The index is
 * rebuilt on registration changes, which are rare, and read lock-free during dispatch.
 */
public class PacketManagerImpl implements ProtocolManager {

    private final ErrorReporter errorReporter;
    private final CopyOnWriteArrayList<PacketListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Packet type -> listeners for it, in priority order. Replaced wholesale on every
     * registration change so dispatch never needs to synchronize.
     */
    private volatile Map<PacketType, PacketListener[]> sendingIndex = new HashMap<>();
    private volatile Map<PacketType, PacketListener[]> receivingIndex = new HashMap<>();

    /** Set once the async manager exists; dispatch hands it a copy of each handled event. */
    private volatile AsynchronousManagerImpl asynchronousManager;

    public PacketManagerImpl(ErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
    }

    public void setAsynchronousManager(AsynchronousManagerImpl asynchronousManager) {
        this.asynchronousManager = asynchronousManager;
    }

    @Override
    public void addPacketListener(PacketListener listener) {
        listeners.addIfAbsent(listener);
        rebuildIndex();
    }

    @Override
    public void removePacketListener(PacketListener listener) {
        if (listeners.remove(listener)) {
            rebuildIndex();
        }
    }

    @Override
    public void removePacketListeners(Plugin plugin) {
        if (listeners.removeIf(listener -> plugin.equals(listener.getPlugin()))) {
            rebuildIndex();
        }
    }

    @Override
    public List<PacketListener> getPacketListeners() {
        return new ArrayList<>(listeners);
    }

    @Override
    public Set<PacketType> getListeningTypes() {
        Set<PacketType> out = new LinkedHashSet<>(sendingIndex.keySet());
        out.addAll(receivingIndex.keySet());
        return out;
    }

    private void rebuildIndex() {
        this.sendingIndex = buildIndex(true);
        this.receivingIndex = buildIndex(false);
    }

    private Map<PacketType, PacketListener[]> buildIndex(boolean sending) {
        Map<PacketType, List<PacketListener>> grouped = new HashMap<>();
        for (PacketListener listener : listeners) {
            var whitelist = sending ? listener.getSendingWhitelist() : listener.getReceivingWhitelist();
            if (whitelist == null || whitelist.isEmpty()) {
                continue;
            }
            for (PacketType type : whitelist.getTypes()) {
                grouped.computeIfAbsent(type, t -> new ArrayList<>()).add(listener);
            }
        }

        Map<PacketType, PacketListener[]> index = new HashMap<>(grouped.size() * 2);
        for (Map.Entry<PacketType, List<PacketListener>> entry : grouped.entrySet()) {
            List<PacketListener> bucket = entry.getValue();
            // ProtocolLib runs low priorities first so that high-priority listeners see, and can
            // overrule, their decisions. MONITOR therefore observes the final state.
            bucket.sort(Comparator.comparingInt(l -> priorityOf(l, sending).ordinal()));
            index.put(entry.getKey(), bucket.toArray(new PacketListener[0]));
        }
        return index;
    }

    private static ListenerPriority priorityOf(PacketListener listener, boolean sending) {
        var whitelist = sending ? listener.getSendingWhitelist() : listener.getReceivingWhitelist();
        return whitelist == null || whitelist.getPriority() == null
                ? ListenerPriority.NORMAL
                : whitelist.getPriority();
    }

    @Override
    public void sendServerPacket(Player receiver, PacketContainer packet) {
        sendServerPacket(receiver, packet, true);
    }

    @Override
    public void sendServerPacket(Player receiver, PacketContainer packet, boolean filters) {
        requireStructured(packet, "send");
        if (filters) {
            PacketEvents.getAPI().getPlayerManager().sendPacket(receiver, packet.getHandle());
        } else {
            PacketEvents.getAPI().getPlayerManager().sendPacketSilently(receiver, packet.getHandle());
        }
    }

    @Override
    public void broadcastServerPacket(PacketContainer packet) {
        requireStructured(packet, "broadcast");
        for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet.getHandle());
        }
    }

    @Override
    public Entity getEntityFromID(World world, int entityId) {
        return io.github.retrooper.packetevents.util.SpigotConversionUtil.getEntityById(world, entityId);
    }

    @Override
    public void receiveClientPacket(Player sender, PacketContainer packet) {
        requireStructured(packet, "receive");
        PacketEvents.getAPI().getPlayerManager().receivePacketSilently(sender, packet.getHandle());
    }

    private static void requireStructured(PacketContainer packet, String action) {
        if (!packet.hasStructuredAccess()) {
            throw new IllegalArgumentException("Cannot " + action + " " + packet.getType()
                    + ": PacketEvents has no wrapper for this packet type, so there is nothing to serialize. "
                    + "Only packets intercepted from the wire can be handled raw.");
        }
    }

    @Override
    public PacketContainer createPacket(PacketType type) {
        return new PacketContainer(type);
    }

    @Override
    public MinecraftVersion getMinecraftVersion() {
        return MinecraftVersion.current();
    }

    public void dispatchReceive(PacketReceiveEvent event) {
        dispatch(event, receivingIndex, false);
    }

    public void dispatchSend(PacketSendEvent event) {
        dispatch(event, sendingIndex, true);
    }

    private void dispatch(ProtocolPacketEvent event, Map<PacketType, PacketListener[]> index, boolean sending) {
        PacketType type = PacketType.fromPacketEvents(event.getPacketType());
        if (type == null) {
            return;
        }
        PacketListener[] bucket = index.get(type);
        AsynchronousManagerImpl async = this.asynchronousManager;
        boolean anyAsync = async != null && !async.getAsyncHandlers().isEmpty();
        if ((bucket == null || bucket.length == 0) && !anyAsync) {
            // Nothing is listening: skip decoding entirely. Building a PacketContainer parses
            // the packet, which is far too expensive to do for every packet on the server.
            return;
        }
        if (!(event.getPlayer() instanceof Player player)) {
            // Pre-login / non-Bukkit connection. ProtocolLib's API is Player-typed throughout,
            // so there is no meaningful event to hand a listener here.
            return;
        }

        PacketContainer container = sending
                ? new PacketContainer(type, (PacketSendEvent) event)
                : new PacketContainer(type, (PacketReceiveEvent) event);
        PacketEvent packetEvent = sending
                ? PacketEvent.fromServer(this, container, player, event)
                : PacketEvent.fromClient(this, container, player, event);

        if (bucket != null) {
            for (PacketListener listener : bucket) {
                try {
                    if (sending) {
                        listener.onPacketSending(packetEvent);
                    } else {
                        listener.onPacketReceiving(packetEvent);
                    }
                } catch (Exception e) {
                    errorReporter.reportDetailed(listener,
                            "Error while handling " + (sending ? "sending" : "receiving") + " of " + type, e);
                }
            }
        }

        if (packetEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        // Async listeners observe the packet after the synchronous ones have settled it. This
        // hands off and returns; the packet is not held on the wire (see AsynchronousManager).
        if (anyAsync) {
            async.enqueue(packetEvent);
        }

        // PacketEvents only rewrites the outgoing buffer from the wrapper when the event is
        // marked for re-encoding; otherwise EventManager drops the wrapper reference and any
        // edits a listener made would be silently discarded. Since a listener ran and may have
        // mutated the packet, force the re-encode here.
        if (container.hasStructuredAccess()) {
            event.markForReEncode(true);
        }
    }
}
