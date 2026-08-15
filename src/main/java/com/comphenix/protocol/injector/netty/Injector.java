/*
 * ProtocolLib2PacketEvents (P2P) - a drop-in ProtocolLib compatibility layer
 * powered by PacketEvents.
 */
package com.comphenix.protocol.injector.netty;

/** Reflective bridge holder used by legacy login plugins to reach the server connection. */
public final class Injector {
    public final Object networkManager;

    public Injector(Object networkManager) {
        this.networkManager = networkManager;
    }
}
