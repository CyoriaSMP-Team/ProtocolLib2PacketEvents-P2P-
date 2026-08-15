package com.comphenix.protocol.timing;

import com.comphenix.protocol.PacketType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PluginTimingTracker implements TimingTracker {
    private final Map<PacketType, StatisticsStream> statistics=new ConcurrentHashMap<>();
    public PluginTimingTracker() { }
    public void track(PacketType type,Runnable action){long start=System.nanoTime();try{if(action!=null)action.run();}finally{statistics.computeIfAbsent(type,k->new StatisticsStream()).observe(System.nanoTime()-start);}}
    public boolean hasReceivedData(){return !statistics.isEmpty();} public Map<PacketType,StatisticsStream> getStatistics(){return java.util.Collections.unmodifiableMap(statistics);}
}
