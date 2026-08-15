package com.comphenix.protocol.injector.collection;

import com.comphenix.protocol.concurrent.PacketTypeListenerSet;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;

public class OutboundPacketListenerSet extends PacketListenerSet {
    public OutboundPacketListenerSet(PacketTypeListenerSet types,ErrorReporter reporter){super(types,reporter);}
    protected ListeningWhitelist getListeningWhitelist(PacketListener listener){return listener.getSendingWhitelist();}
    protected void invokeListener(PacketEvent event,PacketListener listener){event.setReadOnly(listener.getSendingWhitelist().getPriority()==ListenerPriority.MONITOR);listener.onPacketSending(event);event.setReadOnly(false);}
    @Override public void invoke(PacketEvent event, ListenerPriority priority){super.invoke(event, priority);}
}
