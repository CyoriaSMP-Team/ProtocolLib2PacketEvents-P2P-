package com.comphenix.protocol.concurrent;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketListener;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Thread-safe packet-type to listener index for fast dispatch. */
public class PacketTypeListenerSet {
    private final Map<PacketType,Set<PacketListener>> map=new HashMap<>();
    private final Set<Class<?>> classes=new HashSet<>();
    public synchronized boolean add(PacketType type,PacketListener listener){if(type==null||listener==null)throw new NullPointerException();boolean added=map.computeIfAbsent(type,t->new HashSet<>()).add(listener);if(added&&type.getPacketClass()!=null)classes.add(type.getPacketClass());return added;}
    public synchronized boolean remove(PacketType type,PacketListener listener){Set<PacketListener> set=map.get(type);if(set==null||!set.remove(listener))return false;if(set.isEmpty()){map.remove(type);if(type.getPacketClass()!=null)classes.remove(type.getPacketClass());}return true;}
    public synchronized boolean contains(PacketType type){return map.containsKey(type);}
    public synchronized boolean contains(Class<?> type){return classes.contains(type);}
    public synchronized ImmutableSet<PacketType> values(){return ImmutableSet.copyOf(map.keySet());}
    public synchronized void clear(){map.clear();classes.clear();}
}
