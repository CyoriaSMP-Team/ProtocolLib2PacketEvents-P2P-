/*
 * ProtocolLib2PacketEvents - clean-room scheduled packet contract.
 */
package com.comphenix.protocol.events;

import com.comphenix.protocol.PacketStream;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.ProtocolLibrary;
import org.bukkit.entity.Player;

/** A packet sent or received after its parent packet has completed. */
public class ScheduledPacket {
    protected PacketContainer packet;
    protected Player target;
    protected boolean filtered;

    public ScheduledPacket(PacketContainer packet, Player target, boolean filtered) {
        setPacket(packet);
        setTarget(target);
        setFiltered(filtered);
    }

    public static ScheduledPacket fromSilent(PacketContainer packet, Player target) {
        return new ScheduledPacket(packet, target, false);
    }

    public static ScheduledPacket fromFiltered(PacketContainer packet, Player target) {
        return new ScheduledPacket(packet, target, true);
    }

    public PacketContainer getPacket() {
        return packet;
    }

    public void setPacket(PacketContainer packet) {
        if (packet == null) throw new IllegalArgumentException("packet cannot be null");
        this.packet = packet;
    }

    public Player getTarget() {
        return target;
    }

    public void setTarget(Player target) {
        if (target == null) throw new IllegalArgumentException("target cannot be null");
        this.target = target;
    }

    public boolean isFiltered() {
        return filtered;
    }

    public void setFiltered(boolean filtered) {
        this.filtered = filtered;
    }

    public Sender getSender() {
        return packet.getType().getSender();
    }

    public void schedule() {
        schedule(ProtocolLibrary.getProtocolManager());
    }

    public void schedule(PacketStream stream) {
        if (stream == null) throw new IllegalArgumentException("stream cannot be null");
        if (getSender() == Sender.CLIENT) {
            stream.receiveClientPacket(target, packet, filtered);
        } else {
            stream.sendServerPacket(target, packet, filtered);
        }
    }

    @Override
    public String toString() {
        return "ScheduledPacket[packet=" + packet + ", target=" + target + ", filtered=" + filtered + "]";
    }
}
