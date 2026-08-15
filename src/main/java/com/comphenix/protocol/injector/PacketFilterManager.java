package com.comphenix.protocol.injector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.injector.netty.WirePacket;
import com.comphenix.protocol.injector.AsynchronousManagerImpl;
import com.comphenix.protocol.AsynchronousManager;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.injector.PacketConstructor;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class PacketFilterManager extends com.comphenix.protocol.injector.PacketManagerImpl implements ListenerManager, InternalManager {
    private volatile boolean debug;
    public PacketFilterManager(PacketFilterBuilder builder) {
        super(builder.getReporter());
        AsynchronousManagerImpl async = builder.getAsyncManager();
        setAsynchronousManager(async);
    }
    public static PacketFilterBuilder newBuilder() { return new PacketFilterBuilder(); }
    public void sendWirePacket(org.bukkit.entity.Player player, WirePacket packet) { if (packet != null) sendWirePacket(player, packet.getId(), packet.getBytes()); }
    @Override public void registerEvents(PluginManager manager, Plugin plugin) { }
    @Override public boolean isDebug() { return debug; }
    @Override public void setDebug(boolean value) { debug = value; }
    @Override public boolean hasInboundListener(PacketType type) { return getReceivingFilterTypes().contains(type); }
    @Override public boolean hasOutboundListener(PacketType type) { return getSendingFilterTypes().contains(type); }
    @Override public boolean hasMainThreadListener(PacketType type) { return hasInboundListener(type) || hasOutboundListener(type); }
    @Override public void invokeInboundPacketListeners(PacketEvent event) { invoke(event, false); }
    @Override public void invokeOutboundPacketListeners(PacketEvent event) { invoke(event, true); }
    private void invoke(PacketEvent event, boolean outbound) {
        for (PacketListener listener : getPacketListeners()) {
            ListeningWhitelist whitelist = outbound ? listener.getSendingWhitelist() : listener.getReceivingWhitelist();
            if (whitelist != null && whitelist.getTypes().contains(event.getPacketType())) {
                if (outbound) listener.onPacketSending(event); else listener.onPacketReceiving(event);
            }
        }
    }

    // Keep the concrete manager's historical surface explicit.  ProtocolLib plugins
    // commonly reflect on PacketFilterManager rather than only on ProtocolManager.
    @Override public boolean isClosed() { return super.isClosed(); }
    @Override public AsynchronousManager getAsynchronousManager() { return super.getAsynchronousManager(); }
    @Override public PacketContainer createPacket(PacketType type) { return super.createPacket(type); }
    @Override public PacketContainer createPacket(PacketType type, boolean forceDefaults) { return super.createPacket(type, forceDefaults); }
    @Override public PacketConstructor createPacketConstructor(PacketType type, Object... arguments) { return super.createPacketConstructor(type, arguments); }
    @Override public MinecraftVersion getMinecraftVersion() { return super.getMinecraftVersion(); }
    @Override public ImmutableSet<PacketListener> getPacketListeners() { return super.getPacketListeners(); }
    @Override public int getProtocolVersion(Player player) { return super.getProtocolVersion(player); }
    @Override public List<Player> getEntityTrackers(Entity entity) { return super.getEntityTrackers(entity); }
    @Override public Set<PacketType> getReceivingFilterTypes() { return super.getReceivingFilterTypes(); }
    @Override public Set<PacketType> getSendingFilterTypes() { return super.getSendingFilterTypes(); }
    @Override public Entity getEntityFromID(World world, int id) { return super.getEntityFromID(world, id); }
    @Override public void addPacketListener(PacketListener listener) { super.addPacketListener(listener); }
    @Override public void removePacketListener(PacketListener listener) { super.removePacketListener(listener); }
    @Override public void removePacketListeners(Plugin plugin) { super.removePacketListeners(plugin); }
    @Override public void broadcastServerPacket(PacketContainer packet) { super.broadcastServerPacket(packet); }
    @Override public void broadcastServerPacket(PacketContainer packet, Collection<? extends Player> players) { super.broadcastServerPacket(packet, players); }
    @Override public void broadcastServerPacket(PacketContainer packet, Location location, int distance) { super.broadcastServerPacket(packet, location, distance); }
    @Override public void broadcastServerPacket(PacketContainer packet, Entity entity, boolean includeTracker) { super.broadcastServerPacket(packet, entity, includeTracker); }
    @Override public void close() { super.close(); }
    @Override public void receiveClientPacket(Player player, PacketContainer packet) { super.receiveClientPacket(player, packet); }
    @Override public void receiveClientPacket(Player player, PacketContainer packet, boolean filters) { super.receiveClientPacket(player, packet, filters); }
    @Override public void receiveClientPacket(Player player, PacketContainer packet, NetworkMarker marker, boolean filters) { super.receiveClientPacket(player, packet, marker, filters); }
    @Override public void sendServerPacket(Player player, PacketContainer packet) { super.sendServerPacket(player, packet); }
    @Override public void sendServerPacket(Player player, PacketContainer packet, boolean filters) { super.sendServerPacket(player, packet, filters); }
    @Override public void sendServerPacket(Player player, PacketContainer packet, NetworkMarker marker, boolean filters) { super.sendServerPacket(player, packet, marker, filters); }
    @Override public void sendWirePacket(Player player, int id, byte[] bytes) { super.sendWirePacket(player, id, bytes); }
    @Override public void updateEntity(Entity entity, List<Player> observers) { super.updateEntity(entity, observers); }
    @Override public void verifyWhitelist(PacketListener listener, ListeningWhitelist whitelist) { super.verifyWhitelist(listener, whitelist); }
}
