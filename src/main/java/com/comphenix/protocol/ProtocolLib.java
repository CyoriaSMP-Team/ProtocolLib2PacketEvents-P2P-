/*
 * ProtocolLib2PacketEvents (P2P) - a drop-in ProtocolLib compatibility layer
 * powered by PacketEvents.
 *
 * Copyright (C) 2026 CyoriaSMP Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.comphenix.protocol;

import com.comphenix.protocol.error.BasicErrorReporter;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.injector.AsynchronousManagerImpl;
import com.comphenix.protocol.injector.PacketManagerImpl;
import com.comphenix.protocol.reflect.ObjectAllocator;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class. On enable this hooks a single internal listener into PacketEvents (already
 * initialized by the separate PacketEvents plugin this one depends on) and fans every packet out
 * to whatever ProtocolLib-style {@link com.comphenix.protocol.events.PacketListener}s other
 * plugins registered through {@link ProtocolLibrary#getProtocolManager()}.
 */
public class ProtocolLib extends JavaPlugin implements Listener {

    private PacketManagerImpl protocolManager;
    private AsynchronousManagerImpl asynchronousManager;
    private PacketListenerAbstract internalListener;

    @Override
    public void onEnable() {
        ErrorReporter errorReporter = new BasicErrorReporter(getLogger());
        this.protocolManager = new PacketManagerImpl(errorReporter);
        this.asynchronousManager = new AsynchronousManagerImpl(errorReporter);
        protocolManager.setAsynchronousManager(asynchronousManager);

        ProtocolLibrary.init(protocolManager, asynchronousManager, errorReporter, getDescription().getVersion());

        this.internalListener = new PacketListenerAbstract(PacketListenerPriority.NORMAL) {
            @Override
            public void onPacketReceive(PacketReceiveEvent event) {
                protocolManager.dispatchReceive(event);
            }

            @Override
            public void onPacketSend(PacketSendEvent event) {
                protocolManager.dispatchSend(event);
            }
        };
        PacketEvents.getAPI().getEventManager().registerListener(internalListener);
        getServer().getPluginManager().registerEvents(this, this);

        int packetTypeCount = PacketType.values().size();
        getLogger().info("Hooked into PacketEvents " + PacketEvents.getAPI().getVersion()
                + " - bridged " + packetTypeCount + " packet types"
                + " (packet allocation via " + ObjectAllocator.getStrategy() + ").");

        if (packetTypeCount == 0) {
            getLogger().severe("The PacketType registry is EMPTY. PacketEvents' internal class layout may have "
                    + "changed in a way this plugin's reflection-based registry builder does not handle. "
                    + "Every packet-type based listener registration will silently match nothing.");
        }
        if (!ObjectAllocator.isAvailable()) {
            getLogger().warning("No constructor-bypassing allocation strategy is available on this JVM. "
                    + "ProtocolManager.createPacket(...) will fail; intercepting and editing existing packets "
                    + "still works.");
        }
    }

    /** Releases a disconnecting player's async execution lane so it is not retained. */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (asynchronousManager != null) {
            asynchronousManager.releasePlayer(event.getPlayer().getUniqueId());
        }
    }

    @Override
    public void onDisable() {
        if (internalListener != null) {
            PacketEvents.getAPI().getEventManager().unregisterListener(internalListener);
        }
        if (asynchronousManager != null) {
            asynchronousManager.shutdown();
        }
        ProtocolLibrary.shutdown();
    }
}
