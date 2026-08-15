package com.comphenix.protocol.async;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.collection.InboundPacketListenerSet;
import com.comphenix.protocol.injector.collection.OutboundPacketListenerSet;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Owns one ordered inbound and outbound release queue per player. */
class PlayerSendingHandler {
    private final ConcurrentMap<Player, QueueContainer> queues = new ConcurrentHashMap<>();
    private final OutboundPacketListenerSet outboundTimeoutListeners;
    private final InboundPacketListenerSet inboundTimeoutListeners;
    private volatile boolean cleaningUp;

    public PlayerSendingHandler(OutboundPacketListenerSet outbound, InboundPacketListenerSet inbound) {
        this.outboundTimeoutListeners = outbound;
        this.inboundTimeoutListeners = inbound;
    }

    public synchronized void initializeScheduler() {
        cleaningUp = false;
    }

    public PacketSendingQueue getSendingQueue(PacketEvent event) { return getSendingQueue(event, true); }

    public PacketSendingQueue getSendingQueue(PacketEvent event, boolean create) {
        if (event == null || event.getPlayer() == null || cleaningUp) return null;
        QueueContainer queue = create
                ? queues.computeIfAbsent(event.getPlayer(), ignored -> new QueueContainer())
                : queues.get(event.getPlayer());
        return queue == null ? null : event.isServerPacket() ? queue.outbound : queue.inbound;
    }

    public void sendAllPackets() {
        trySendServerPackets(false);
        trySendClientPackets(false);
    }

    public void sendServerPackets(List<PacketType> types, boolean onMainThread) {
        for (QueueContainer queue : queues.values()) queue.outbound.signalPacketUpdate(types, onMainThread);
    }

    public void sendClientPackets(List<PacketType> types, boolean onMainThread) {
        for (QueueContainer queue : queues.values()) queue.inbound.signalPacketUpdate(types, onMainThread);
    }

    public void trySendServerPackets(boolean onMainThread) {
        for (QueueContainer queue : queues.values()) queue.outbound.trySendPackets(onMainThread);
    }

    public void trySendClientPackets(boolean onMainThread) {
        for (QueueContainer queue : queues.values()) queue.inbound.trySendPackets(onMainThread);
    }

    public List<PacketSendingQueue> getServerQueues() {
        return queues.values().stream().map(queue -> queue.outbound).toList();
    }

    public List<PacketSendingQueue> getClientQueues() {
        return queues.values().stream().map(queue -> queue.inbound).toList();
    }

    public void cleanupAll() {
        cleaningUp = true;
        queues.values().forEach(QueueContainer::cleanupAll);
        queues.clear();
    }

    public void removePlayer(Player player) {
        QueueContainer queue = player == null ? null : queues.remove(player);
        if (queue != null) queue.cleanupAll();
    }

    class QueueContainer {
        final PacketSendingQueue outbound = new PacketSendingQueue(false, Runnable::run) {
            @Override protected void onPacketTimeout(PacketEvent event) {
                if (outboundTimeoutListeners != null) outboundTimeoutListeners.invoke(event);
            }
        };
        final PacketSendingQueue inbound = new PacketSendingQueue(false, Runnable::run) {
            @Override protected void onPacketTimeout(PacketEvent event) {
                if (inboundTimeoutListeners != null) inboundTimeoutListeners.invoke(event);
            }
        };

        public QueueContainer() {
        }

        public PacketSendingQueue getOutboundQueue() {
            return outbound;
        }

        public PacketSendingQueue getInboundQueue() {
            return inbound;
        }

        void cleanupAll() {
            outbound.cleanupAll();
            inbound.cleanupAll();
        }
    }
}
