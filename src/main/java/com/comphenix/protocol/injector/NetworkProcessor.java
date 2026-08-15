package com.comphenix.protocol.injector;

import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.internal.PacketNetworkProcessor;

/** Compatibility facade for post-event processing. */
public class NetworkProcessor {
    private final ErrorReporter reporter;
    public NetworkProcessor(ErrorReporter reporter) { this.reporter = reporter; }
    public void invokePostEvent(PacketEvent event, NetworkMarker marker) {
        try { PacketNetworkProcessor.complete(event, event == null ? null : (com.comphenix.protocol.PacketStream) event.getSource()); }
        catch (Throwable error) { if (reporter != null) reporter.reportDetailed(this, "Post event failed", error); }
    }
}
