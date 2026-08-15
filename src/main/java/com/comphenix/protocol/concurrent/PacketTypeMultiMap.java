package com.comphenix.protocol.concurrent;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.ListenerPriority;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Associates packet types with values while keeping whitelist priority order. */
public class PacketTypeMultiMap<T> {
    private final Map<PacketType, SortedCopyOnWriteSet<T,PriorityHolder>> map = new HashMap<>();
    public synchronized void put(ListeningWhitelist key,T value){if(key==null||value==null)throw new NullPointerException();for(PacketType type:key.getTypes())map.computeIfAbsent(type,t->new SortedCopyOnWriteSet<>()).add(value,new PriorityHolder(key.getPriority()));}
    public synchronized List<PacketType> remove(ListeningWhitelist key,T value){if(key==null||value==null)throw new NullPointerException();List<PacketType> removed=new ArrayList<>();for(PacketType type:key.getTypes()){SortedCopyOnWriteSet<T,PriorityHolder> set=map.get(type);if(set!=null&&set.remove(value)&&set.isEmpty()){map.remove(type);removed.add(type);}}return removed;}
    public synchronized ImmutableSet<PacketType> getPacketTypes(){return ImmutableSet.copyOf(map.keySet());}
    public synchronized boolean contains(PacketType type){return map.containsKey(type);}
    public Iterable<T> get(PacketType type){SortedCopyOnWriteSet<T,PriorityHolder> set; synchronized(this){set=map.get(type);} return set==null?Collections::emptyIterator:set;}
    public synchronized Iterable<T> values(){List<T> result=new ArrayList<>();for(SortedCopyOnWriteSet<T,PriorityHolder> set:map.values())for(T value:set)result.add(value);return result;}
    public synchronized void clear(){map.clear();}
    public static final class PriorityHolder implements Comparable<PriorityHolder>{private final ListenerPriority priority;public PriorityHolder(ListenerPriority p){priority=p==null?ListenerPriority.NORMAL:p;}public PriorityHolder(ListeningWhitelist whitelist){this(whitelist==null?ListenerPriority.NORMAL:whitelist.getPriority());}public int compareTo(PriorityHolder o){return Integer.compare(priority.getSlot(),o.priority.getSlot());}}
}
