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
import com.comphenix.protocol.AsynchronousManager;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.internal.BackendCoordinator;
import com.comphenix.protocol.internal.PacketNetworkProcessor;
import com.comphenix.protocol.internal.VersionAdapterRegistry;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.injector.netty.WirePacket;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.collect.ImmutableSet;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.event.ProtocolPacketEvent;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import com.comphenix.protocol.injector.temporary.TemporaryPlayerAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

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
public class PacketManagerImpl implements ProtocolManager, ListenerManager {

    private final ErrorReporter errorReporter;
    private final CopyOnWriteArrayList<PacketListener> listeners = new CopyOnWriteArrayList<>();
    private final BackendCoordinator backend = new BackendCoordinator();

    /**
     * Packet type -> listeners for it, in priority order. Replaced wholesale on every
     * registration change so dispatch never needs to synchronize.
     */
    private volatile Map<PacketType, PacketListener[]> sendingIndex = new HashMap<>();
    private volatile Map<PacketType, PacketListener[]> receivingIndex = new HashMap<>();

    /** Set once the async manager exists; dispatch hands it a copy of each handled event. */
    private volatile AsynchronousManagerImpl asynchronousManager;
    private volatile boolean closed;

    public PacketManagerImpl(ErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
    }

    public void setAsynchronousManager(AsynchronousManagerImpl asynchronousManager) {
        this.asynchronousManager = asynchronousManager;
        if (asynchronousManager != null) {
            for (PacketListener listener : listeners) {
                registerIfAsync(listener);
            }
        }
    }

    @Override
    public AsynchronousManager getAsynchronousManager() {
        return asynchronousManager;
    }

    @Override
    public void addPacketListener(PacketListener listener) {
        listeners.addIfAbsent(listener);
        registerIfAsync(listener);
        rebuildIndex();
    }

    @Override
    public void removePacketListener(PacketListener listener) {
        if (listeners.remove(listener)) {
            unregisterIfAsync(listener);
            rebuildIndex();
        }
    }

    @Override
    public void removePacketListeners(Plugin plugin) {
        List<PacketListener> removed = new ArrayList<>();
        for (PacketListener listener : listeners) {
            if (plugin.equals(listener.getPlugin()) && listeners.remove(listener)) {
                removed.add(listener);
                unregisterIfAsync(listener);
            }
        }
        if (!removed.isEmpty()) {
            rebuildIndex();
        }
    }

    @Override
    public ImmutableSet<PacketListener> getPacketListeners() {
        return ImmutableSet.copyOf(listeners);
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
        if (filters && backend.backendFor(packet) instanceof com.comphenix.protocol.internal.DirectPacketBackend) {
            sendDirectWithFilters(receiver, packet);
            return;
        }
        backend.send(receiver, packet, filters);
    }

    @Override
    public void broadcastServerPacket(PacketContainer packet) {
        for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            backend.send(player, packet, true);
        }
    }

    @Override
    public Entity getEntityFromID(World world, int entityId) {
        return io.github.retrooper.packetevents.util.SpigotConversionUtil.getEntityById(world, entityId);
    }

    @Override
    public void receiveClientPacket(Player sender, PacketContainer packet) {
        receiveClientPacket(sender, packet, true);
    }

    @Override
    public void sendWirePacket(Player receiver, int id, byte[] bytes) {
        backend.sendWire(receiver, id, bytes);
    }

    @Override
    public void sendServerPacket(Player receiver, PacketContainer packet,
                                 NetworkMarker marker, boolean filters) {
        PacketEvent event = PacketEvent.fromServer(this, packet, marker, receiver);
        if (filters) {
            PacketListener[] bucket = sendingIndex.get(packet.getType());
            if (bucket != null) {
                for (PacketListener listener : bucket) {
                    invokeListener(listener, event, true);
                }
            }
        }
        AsynchronousManagerImpl async = asynchronousManager;
        if (!event.isCancelled() && async != null && async.hasAsynchronousListeners(event)) {
            async.processAndWait(event);
        }
        if (event.isCancelled()) {
            return;
        }
        if (!event.getNetworkMarker().getOutputHandlers().isEmpty()) {
            Object buffer = packet.serializeToBuffer();
            if (buffer == null) {
                throw new IllegalStateException("Cannot apply output handlers without a serialized buffer");
            }
            byte[] bytes = com.github.retrooper.packetevents.netty.buffer.ByteBufHelper.copyBytes(buffer);
            for (var handler : event.getNetworkMarker().getOutputHandlers()) {
                bytes = handler.handle(event, bytes);
                if (bytes == null) throw new IllegalStateException("PacketOutputHandler returned null");
            }
            com.comphenix.protocol.internal.DirectNettyBackend.sendWire(receiver, packet.getId(), bytes);
        } else {
            // The listeners above already ran.  Passing filters=true here would
            // re-enter PacketEvents and dispatch the same ProtocolLib event twice.
            backend.send(receiver, packet, false);
        }
        PacketNetworkProcessor.complete(event, this);
    }

    /** Direct/NMS packets do not pass through PacketEvents' event manager, so fan out the
     * ProtocolLib listeners here before writing them to the channel. */
    private void sendDirectWithFilters(Player receiver, PacketContainer packet) {
        if (packet == null) throw new IllegalArgumentException("packet cannot be null");
        PacketEvent event = PacketEvent.fromServer(this, packet,
                new NetworkMarker(ConnectionSide.SERVER_SIDE, packet.getType()), receiver);
        PacketListener[] bucket = sendingIndex.get(packet.getType());
        if (bucket != null) {
            for (PacketListener listener : bucket) {
                invokeListener(listener, event, true);
            }
        }
        AsynchronousManagerImpl async = asynchronousManager;
        if (!event.isCancelled() && async != null && async.hasAsynchronousListeners(event)) {
            async.processAndWait(event);
        }
        if (event.isCancelled()) return;

        if (!event.getNetworkMarker().getOutputHandlers().isEmpty()) {
            Object raw = packet.serializeToBuffer();
            if (raw == null) throw new IllegalStateException("No raw buffer is available for output handlers on " + packet.getType());
            byte[] bytes = com.github.retrooper.packetevents.netty.buffer.ByteBufHelper.copyBytes(raw);
            for (var handler : event.getNetworkMarker().getOutputHandlers()) {
                bytes = handler.handle(event, bytes);
                if (bytes == null) throw new IllegalStateException("PacketOutputHandler returned null");
            }
            com.comphenix.protocol.internal.DirectNettyBackend.sendWire(receiver, packet.getId(), bytes);
        } else {
            backend.send(receiver, packet, false);
        }
        PacketNetworkProcessor.complete(event, this);
    }

    @Override
    public void receiveClientPacket(Player sender, PacketContainer packet, boolean filters) {
        if (filters && backend.backendFor(packet) instanceof com.comphenix.protocol.internal.DirectPacketBackend) {
            receiveDirectWithFilters(sender, packet);
        } else {
            backend.receive(sender, packet, filters);
        }
    }

    @Override
    public void receiveClientPacket(Player sender, PacketContainer packet,
                                    NetworkMarker marker, boolean filters) {
        receiveClientPacket(sender, packet, filters);
    }

    private void receiveDirectWithFilters(Player sender, PacketContainer packet) {
        if (packet == null) throw new IllegalArgumentException("packet cannot be null");
        PacketEvent event = PacketEvent.fromClient(this, packet,
                new NetworkMarker(ConnectionSide.CLIENT_SIDE, packet.getType()), sender);
        PacketListener[] bucket = receivingIndex.get(packet.getType());
        if (bucket != null) {
            for (PacketListener listener : bucket) {
                invokeListener(listener, event, false);
            }
        }
        AsynchronousManagerImpl async = asynchronousManager;
        if (!event.isCancelled() && async != null && async.hasAsynchronousListeners(event)) {
            async.processAndWait(event);
        }
        if (event.isCancelled()) return;
        backend.receive(sender, packet, false);
        PacketNetworkProcessor.complete(event, this);
    }

    @Override
    public int getProtocolVersion(Player player) {
        if (player == null || PacketEvents.getAPI() == null) {
            return Integer.MIN_VALUE;
        }
        var version = PacketEvents.getAPI().getPlayerManager().getClientVersion(player);
        return version == null ? Integer.MIN_VALUE : version.getProtocolVersion();
    }

    @Override
    public Set<PacketType> getSendingFilterTypes() {
        return filteredTypes(true);
    }

    @Override
    public Set<PacketType> getReceivingFilterTypes() {
        return filteredTypes(false);
    }

    private Set<PacketType> filteredTypes(boolean sending) {
        return java.util.Collections.unmodifiableSet(
                new java.util.LinkedHashSet<>((sending ? sendingIndex : receivingIndex).keySet()));
    }

    @Override
    public void broadcastServerPacket(PacketContainer packet, Entity entity, boolean includeTracker) {
        if (entity == null) throw new IllegalArgumentException("entity cannot be null");
        for (Player player : getEntityTrackers(entity)) {
            if (includeTracker || !(entity instanceof Player) || !player.equals(entity)) {
                sendServerPacket(player, packet);
            }
        }
    }

    @Override
    public void broadcastServerPacket(PacketContainer packet, Location origin, int maxObserverDistance) {
        if (origin == null || origin.getWorld() == null) return;
        double maxDistanceSquared = (double) maxObserverDistance * maxObserverDistance;
        for (Player player : origin.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(origin) <= maxDistanceSquared) {
                sendServerPacket(player, packet);
            }
        }
    }

    @Override
    public void broadcastServerPacket(PacketContainer packet, Collection<? extends Player> targetPlayers) {
        if (targetPlayers == null) return;
        for (Player player : targetPlayers) sendServerPacket(player, packet);
    }

    @Override
    public List<Player> getEntityTrackers(Entity entity) {
        return VersionAdapterRegistry.current().trackers(entity);
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    public void close() {
        closed = true;
        listeners.clear();
        sendingIndex = new HashMap<>();
        receivingIndex = new HashMap<>();
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

    @Override
    public boolean hasInboundListener(PacketType type) {
        return type != null && receivingIndex.containsKey(type);
    }

    @Override
    public boolean hasOutboundListener(PacketType type) {
        return type != null && sendingIndex.containsKey(type);
    }

    @Override
    public boolean hasMainThreadListener(PacketType type) {
        return hasInboundListener(type) || hasOutboundListener(type);
    }

    @Override
    public void invokeInboundPacketListeners(PacketEvent event) {
        dispatchDirect(event, false, false);
    }

    @Override
    public void invokeOutboundPacketListeners(PacketEvent event) {
        dispatchDirect(event, true, false);
    }

    @Override
    public boolean dispatchInboundPacket(PacketEvent event) {
        return dispatchDirect(event, false, true);
    }

    @Override
    public boolean dispatchOutboundPacket(PacketEvent event) {
        return dispatchDirect(event, true, true);
    }

    /** Dispatches a raw packet exactly once when the direct Netty fallback owns the packet. */
    private boolean dispatchDirect(PacketEvent event, boolean sending, boolean complete) {
        if (event == null || event.getPacket() == null || event.getPacketType() == null) {
            return true;
        }
        PacketListener[] bucket = (sending ? sendingIndex : receivingIndex).get(event.getPacketType());
        if (bucket != null) {
            for (PacketListener listener : bucket) {
                invokeListener(listener, event, sending);
            }
        }
        AsynchronousManagerImpl async = asynchronousManager;
        if (!event.isCancelled() && async != null && async.hasAsynchronousListeners(event)) {
            async.processAndWait(event);
        }
        if (complete && !event.isCancelled()) {
            PacketNetworkProcessor.complete(event, this);
        }
        return !event.isCancelled();
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
        Player player = event.getPlayer() instanceof Player existing
                ? existing : TemporaryPlayerAdapter.create(event.getUser());
        if (player == null) return;

        PacketContainer container = sending
                ? new PacketContainer(type, (PacketSendEvent) event)
                : new PacketContainer(type, (PacketReceiveEvent) event);
        PacketEvent packetEvent = sending
                ? PacketEvent.fromServer(this, container,
                        new NetworkMarker(NetworkMarkerSide.SERVER.side, type), player)
                : PacketEvent.fromClient(this, container,
                        new NetworkMarker(NetworkMarkerSide.CLIENT.side, type), player);

        if (bucket != null) {
            for (PacketListener listener : bucket) {
                if (!shouldRunSynchronously(listener, sending)) {
                    continue;
                }
                try {
                    invokeListener(listener, packetEvent, sending);
                } catch (Exception e) {
                    // invokeListener already reports listener failures. Keep this guard for
                    // scheduler/runtime failures so one bad plugin cannot break the PE callback.
                    errorReporter.reportDetailed(listener,
                            "Error while handling " + (sending ? "sending" : "receiving") + " of " + type, e);
                }
            }
        }

        if (packetEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        // Hold the PacketEvents callback while async listeners finish. This is the bridge's
        // hold/release point: mutations and cancellation still affect the packet on the wire.
        if (anyAsync) {
            async.processAndWait(packetEvent);
            if (packetEvent.isCancelled()) {
                event.setCancelled(true);
                return;
            }
        }

        // PacketEvents only rewrites the outgoing buffer from the wrapper when the event is
        // marked for re-encoding; otherwise EventManager drops the wrapper reference and any
        // edits a listener made would be silently discarded. Since a listener ran and may have
        // mutated the packet, force the re-encode here.
        if (container.hasStructuredAccess()) {
            event.markForReEncode(true);
        }
        PacketNetworkProcessor.applyOutputHandlers(event, packetEvent);
        PacketNetworkProcessor.complete(packetEvent, this);
    }

    private enum NetworkMarkerSide {
        SERVER(com.comphenix.protocol.events.ConnectionSide.SERVER_SIDE),
        CLIENT(com.comphenix.protocol.events.ConnectionSide.CLIENT_SIDE);

        private final com.comphenix.protocol.events.ConnectionSide side;

        NetworkMarkerSide(com.comphenix.protocol.events.ConnectionSide side) {
            this.side = side;
        }
    }

    private void registerIfAsync(PacketListener listener) {
        AsynchronousManagerImpl async = asynchronousManager;
        if (async == null || listener == null) {
            return;
        }
        if (hasOption(listener, true, ListenerOptions.ASYNC)
                || hasOption(listener, false, ListenerOptions.ASYNC)) {
            async.registerAsyncHandler(listener);
        }
    }

    private void unregisterIfAsync(PacketListener listener) {
        AsynchronousManagerImpl async = asynchronousManager;
        if (async == null || listener == null) {
            return;
        }
        async.unregisterAsyncHandler(listener);
    }

    private static boolean hasOption(PacketListener listener, boolean sending, ListenerOptions option) {
        var whitelist = sending ? listener.getSendingWhitelist() : listener.getReceivingWhitelist();
        return whitelist != null && whitelist.getOptions().contains(option);
    }

    private static boolean shouldRunSynchronously(PacketListener listener, boolean sending) {
        return !hasOption(listener, sending, ListenerOptions.ASYNC);
    }

    /**
     * Invoke a non-ASYNC ProtocolLib listener on Bukkit's primary thread. PacketEvents invokes
     * its listeners from a channel/event-loop thread on modern servers, while ProtocolLib's
     * regular listener contract is synchronous. The latch is intentional: cancellation and
     * packet mutations must be visible before the PE callback is allowed to continue.
     */
    private void invokeListener(PacketListener listener, PacketEvent event, boolean sending) {
        if (!shouldRunSynchronously(listener, sending)) {
            return;
        }
        Runnable callback = () -> {
            try {
                if (sending) {
                    listener.onPacketSending(event);
                } else {
                    listener.onPacketReceiving(event);
                }
            } catch (Throwable error) {
                errorReporter.reportDetailed(listener,
                        "Error while handling " + (sending ? "sending" : "receiving")
                                + " of " + event.getPacketType(), error);
            }
        };

        Plugin plugin = listener.getPlugin();
        if (Bukkit.isPrimaryThread() || plugin == null || plugin.getServer() == null) {
            callback.run();
            return;
        }

        CountDownLatch completed = new CountDownLatch(1);
        AtomicReference<Throwable> schedulingFailure = new AtomicReference<>();
        BukkitTask task;
        try {
            task = plugin.getServer().getScheduler().runTask(plugin, () -> {
                try {
                    callback.run();
                } finally {
                    completed.countDown();
                }
            });
        } catch (Throwable error) {
            schedulingFailure.set(error);
            task = null;
        }
        if (schedulingFailure.get() != null) {
            errorReporter.reportDetailed(listener, "Unable to schedule synchronous packet listener", schedulingFailure.get());
            return;
        }
        try {
            if (!completed.await(5, TimeUnit.SECONDS)) {
                if (task != null) {
                    task.cancel();
                }
                errorReporter.reportWarning(listener,
                        "Synchronous packet listener timed out; packet callback was released", null);
            }
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            if (task != null) {
                task.cancel();
            }
            errorReporter.reportWarning(listener, "Interrupted while waiting for synchronous packet listener", error);
        }
    }
}
