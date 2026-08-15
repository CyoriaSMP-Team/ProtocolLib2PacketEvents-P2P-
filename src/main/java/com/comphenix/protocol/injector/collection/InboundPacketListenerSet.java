package com.comphenix.protocol.injector.collection;

import com.comphenix.protocol.concurrent.PacketTypeListenerSet;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;

public class InboundPacketListenerSet extends PacketListenerSet {
    public InboundPacketListenerSet(PacketTypeListenerSet types,ErrorReporter reporter){super(types,reporter);}
    protected ListeningWhitelist getListeningWhitelist(PacketListener listener){return listener.getReceivingWhitelist();}
    protected void invokeListener(PacketEvent event,PacketListener listener){event.setReadOnly(listener.getReceivingWhitelist().getPriority()==ListenerPriority.MONITOR);listener.onPacketReceiving(event);event.setReadOnly(false);}
}
