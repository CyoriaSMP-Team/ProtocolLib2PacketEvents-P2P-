/*
 * ProtocolLib2PacketEvents (P2P) - a drop-in ProtocolLib compatibility layer
 * powered by PacketEvents.
 */
package com.comphenix.protocol.injector.server;

import com.comphenix.protocol.injector.netty.Injector;
import com.comphenix.protocol.injector.netty.ReflectiveInjector;
import org.bukkit.entity.Player;

/** Legacy socket-injector facade for plugins that still use FastLogin's reflection path. */
public final class SocketInjector {
    private final Injector injector;

    public SocketInjector(Player player) {
        this.injector = new ReflectiveInjector(player, TemporaryPlayerFactory.findNetworkManager(player));
    }

    public Injector getInjector() {
        return injector;
    }
}
