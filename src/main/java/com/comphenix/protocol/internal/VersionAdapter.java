/*
 * ProtocolLib2PacketEvents - clean-room version adapter contract.
 */
package com.comphenix.protocol.internal;

import com.comphenix.protocol.PacketType;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

/** Version-specific seam for the small amount of server reflection PE cannot provide. */
public interface VersionAdapter {
    ServerVersion serverVersion();
    boolean supportsNativeHandle(PacketType type);
    Object createNativePacket(PacketType type, Object... arguments);
    List<Player> trackers(Entity entity);
}
