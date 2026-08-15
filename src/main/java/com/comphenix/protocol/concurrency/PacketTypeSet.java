package com.comphenix.protocol.concurrency;

import com.comphenix.protocol.PacketType;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/** Packet type set that also tracks the associated runtime packet classes. */
public class PacketTypeSet {
    private final Set<PacketType> types = new HashSet<>();
    private final Set<Class<?>> classes = new HashSet<>();
    public PacketTypeSet() {}
    public PacketTypeSet(Collection<? extends PacketType> values) { addAll(values); }
    public synchronized void addType(PacketType type) { if (type == null) return; types.add(type); if (type.getPacketClass()!=null) classes.add(type.getPacketClass()); }
    public synchronized void addAll(Iterable<? extends PacketType> values) { for (PacketType type: values) addType(type); }
    public synchronized void removeType(PacketType type) { if (type == null) return; types.remove(type); if (type.getPacketClass()!=null && types.stream().noneMatch(t -> type.getPacketClass().equals(t.getPacketClass()))) classes.remove(type.getPacketClass()); }
    public synchronized void removeAll(Iterable<? extends PacketType> values) { for (PacketType type: values) removeType(type); }
    public synchronized boolean contains(PacketType type) { return types.contains(type); }
    public synchronized boolean contains(Class<?> type) { return classes.contains(type); }
    public synchronized boolean containsPacket(Object packet) { return packet != null && classes.contains(packet.getClass()); }
    public synchronized Set<PacketType> values() { return ImmutableSet.copyOf(types); }
    public synchronized int size() { return types.size(); }
    public synchronized void clear() { types.clear(); classes.clear(); }
}
