/*
 * ProtocolLib2PacketEvents - clean-room compatibility tests.
 */
package com.comphenix.protocol;

import com.comphenix.protocol.async.AsyncMarker;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.injector.netty.Injector;
import com.comphenix.protocol.injector.netty.WirePacket;
import com.comphenix.protocol.injector.temporary.TemporaryPlayer;
import com.comphenix.protocol.injector.temporary.TemporaryPlayerFactory;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import org.bukkit.entity.Player;
import com.github.retrooper.packetevents.PacketEvents;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class CompatibilityCoreTest {
    @Test
    void wirePacketIsImmutableAndComparable() {
        byte[] bytes = {1, 2, 3};
        WirePacket packet = new WirePacket(7, bytes);
        bytes[0] = 99;
        assertArrayEquals(new byte[]{1, 2, 3}, packet.getBytes());
        assertEquals(packet, new WirePacket(7, new byte[]{1, 2, 3}));
        byte[] returned = packet.getBytes();
        returned[1] = 99;
        assertEquals(2, packet.getBytes()[1]);
    }

    @Test
    void asyncMarkerTracksDelayExpiryAndOrder() {
        long now = System.currentTimeMillis();
        AsyncMarker marker = new AsyncMarker(4, now, 1000);
        assertEquals(4, marker.getOriginalSendingIndex());
        assertEquals(4, marker.getNewSendingIndex());
        assertEquals(1, marker.incrementProcessingDelay());
        assertEquals(0, marker.signal());
        marker.setNewSendingIndex(8);
        marker.setTimeout(now + 10);
        assertTrue(marker.hasExpired(now + 11));
        assertFalse(marker.hasExpired(now));
    }

    @Test
    void markerKeepsDirectionAndPostState() {
        NetworkMarker marker = new NetworkMarker(ConnectionSide.SERVER_SIDE, null);
        assertEquals(ConnectionSide.SERVER_SIDE, marker.getSide());
        assertTrue(marker.getScheduledPackets().isEmpty());
        assertNotNull(marker.getPostListeners());
    }

    @Test
    void temporaryPlayerIsAConcreteGeneratedPlayer() {
        Player player = TemporaryPlayerFactory.createTemporaryPlayer();
        assertTrue(player instanceof TemporaryPlayer);
        assertNull(TemporaryPlayerFactory.getInjectorFromPlayer(player));

        Injector injector = (Injector) Proxy.newProxyInstance(
                Injector.class.getClassLoader(), new Class<?>[]{Injector.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getPlayerName", "getPlayerUniqueId" -> null;
                    case "getAddress" -> null;
                    case "isConnected", "isInjected", "isClosed" -> false;
                    case "getPlayer" -> null;
                    case "getProtocolVersion" -> Integer.MIN_VALUE;
                    case "getCurrentProtocol" -> PacketType.Protocol.PLAY;
                    default -> null;
                });
        TemporaryPlayerFactory.setInjectorForPlayer(player, injector);
        assertSame(injector, TemporaryPlayerFactory.getInjectorFromPlayer(player));
        assertTrue(player.getName().startsWith("UNKNOWN["));
    }

    @Test
    void packetTypeHoldersExposeEnumValues() {
        assertTrue(PacketType.Play.Server.getInstance().values().contains(PacketType.Play.Server.SPAWN_ENTITY));
        assertTrue(PacketType.Play.Client.getInstance().values().contains(PacketType.Play.Client.TAB_COMPLETE));
    }

    @Test
    void metadataEnumsAndNbtUsePacketEventsTypes() {
        assumeTrue(PacketEvents.getAPI() != null, "PacketEvents API is supplied by the server integration test");
        WrappedDataWatcher watcher = new WrappedDataWatcher();
        watcher.setDirection(0, EnumWrappers.Direction.NORTH, true);
        assertEquals(EnumWrappers.Direction.NORTH, watcher.getDirection(0));

        NbtCompound original = NbtFactory.ofCompound("");
        original.put("name", "Cyoria");
        original.put("level", 42);
        watcher.setNBTCompound(1, original, true);
        assertTrue(watcher.getObject(1) instanceof com.github.retrooper.packetevents.protocol.nbt.NBTCompound);
        NbtCompound roundTrip = watcher.getNBTCompound(1);
        assertNotNull(roundTrip);
        assertEquals("Cyoria", roundTrip.getString("name"));
        assertEquals(42, roundTrip.getInteger("level"));
    }
}
