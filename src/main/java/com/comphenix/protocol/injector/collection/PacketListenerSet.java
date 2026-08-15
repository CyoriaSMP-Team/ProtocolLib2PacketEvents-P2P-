package com.comphenix.protocol.injector.collection;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.concurrent.PacketTypeListenerSet;
import com.comphenix.protocol.concurrent.PacketTypeMultiMap;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.google.common.collect.ImmutableSet;

/** Shared listener collection used by both inbound and outbound dispatch. */
public abstract class PacketListenerSet {
    protected final PacketTypeMultiMap<PacketListener> map=new PacketTypeMultiMap<>();
    protected final PacketTypeListenerSet mainThreadPacketTypes;
    protected final ErrorReporter errorReporter;
    public PacketListenerSet(PacketTypeListenerSet mainThreadPacketTypes,ErrorReporter errorReporter){this.mainThreadPacketTypes=mainThreadPacketTypes;this.errorReporter=errorReporter;}
    protected abstract ListeningWhitelist getListeningWhitelist(PacketListener listener);
    public void addListener(PacketListener listener){ListeningWhitelist whitelist=getListeningWhitelist(listener);if(whitelist==null)return;map.put(whitelist,listener);if(mainThreadPacketTypes!=null)for(PacketType type:whitelist.getTypes())mainThreadPacketTypes.add(type,listener);}
    public void removeListener(PacketListener listener){ListeningWhitelist whitelist=getListeningWhitelist(listener);if(whitelist==null)return;map.remove(whitelist,listener);if(mainThreadPacketTypes!=null)for(PacketType type:whitelist.getTypes())mainThreadPacketTypes.remove(type,listener);}
    public final boolean containsPacketType(PacketType type){return map.contains(type);}
    public final ImmutableSet<PacketType> getPacketTypes(){return map.getPacketTypes();}
    public void invoke(PacketEvent event){invoke(event,null);}
    public void invoke(PacketEvent event,ListenerPriority priority){for(PacketListener listener:map.get(event.getPacketType())){ListeningWhitelist whitelist=getListeningWhitelist(listener);if(priority!=null&&whitelist.getPriority()!=priority)continue;try{invokeListener(event,listener);}catch(Throwable error){if(error instanceof OutOfMemoryError)throw (OutOfMemoryError)error;if(errorReporter!=null)errorReporter.reportMinimal(listener,"packet listener",error);}}}
    protected abstract void invokeListener(PacketEvent event,PacketListener listener);
    public void clear(){map.clear();}
}
