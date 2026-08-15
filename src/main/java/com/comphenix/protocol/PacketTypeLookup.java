package com.comphenix.protocol;

import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.collections.IntegerMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Version-neutral packet lookup used by the legacy ProtocolLib API.
 *
 * <p>The old implementation depended on Minecraft's obfuscated class names.  P2P
 * keeps the same lookup contract, but fills it with the portable class identity
 * supplied by PacketEvents and with the current wire id when the server exposes it.</p>
 */
class PacketTypeLookup {
    public static class ProtocolSenderLookup {
        public final IntegerMap<PacketType> HANDSHAKE_CLIENT = new IntegerMap<>();
        public final IntegerMap<PacketType> HANDSHAKE_SERVER = new IntegerMap<>();
        public final IntegerMap<PacketType> GAME_CLIENT = new IntegerMap<>();
        public final IntegerMap<PacketType> GAME_SERVER = new IntegerMap<>();
        public final IntegerMap<PacketType> STATUS_CLIENT = new IntegerMap<>();
        public final IntegerMap<PacketType> STATUS_SERVER = new IntegerMap<>();
        public final IntegerMap<PacketType> LOGIN_CLIENT = new IntegerMap<>();
        public final IntegerMap<PacketType> LOGIN_SERVER = new IntegerMap<>();
        public final IntegerMap<PacketType> CONFIGURATION_CLIENT = new IntegerMap<>();
        public final IntegerMap<PacketType> CONFIGURATION_SERVER = new IntegerMap<>();

        public IntegerMap<PacketType> getMap(Protocol protocol, Sender sender) {
            return switch (protocol) {
                case HANDSHAKING -> sender == Sender.CLIENT ? HANDSHAKE_CLIENT : HANDSHAKE_SERVER;
                case PLAY -> sender == Sender.CLIENT ? GAME_CLIENT : GAME_SERVER;
                case STATUS -> sender == Sender.CLIENT ? STATUS_CLIENT : STATUS_SERVER;
                case LOGIN -> sender == Sender.CLIENT ? LOGIN_CLIENT : LOGIN_SERVER;
                case CONFIGURATION -> sender == Sender.CLIENT ? CONFIGURATION_CLIENT : CONFIGURATION_SERVER;
                default -> throw new IllegalArgumentException("Unable to find protocol " + protocol);
            };
        }
    }

    public static class ClassLookup {
        public final Map<String, PacketType> HANDSHAKE_CLIENT = new ConcurrentHashMap<>();
        public final Map<String, PacketType> HANDSHAKE_SERVER = new ConcurrentHashMap<>();
        public final Map<String, PacketType> GAME_CLIENT = new ConcurrentHashMap<>();
        public final Map<String, PacketType> GAME_SERVER = new ConcurrentHashMap<>();
        public final Map<String, PacketType> STATUS_CLIENT = new ConcurrentHashMap<>();
        public final Map<String, PacketType> STATUS_SERVER = new ConcurrentHashMap<>();
        public final Map<String, PacketType> LOGIN_CLIENT = new ConcurrentHashMap<>();
        public final Map<String, PacketType> LOGIN_SERVER = new ConcurrentHashMap<>();
        public final Map<String, PacketType> CONFIGURATION_CLIENT = new ConcurrentHashMap<>();
        public final Map<String, PacketType> CONFIGURATION_SERVER = new ConcurrentHashMap<>();

        public Map<String, PacketType> getMap(Protocol protocol, Sender sender) {
            return switch (protocol) {
                case HANDSHAKING -> sender == Sender.CLIENT ? HANDSHAKE_CLIENT : HANDSHAKE_SERVER;
                case PLAY -> sender == Sender.CLIENT ? GAME_CLIENT : GAME_SERVER;
                case STATUS -> sender == Sender.CLIENT ? STATUS_CLIENT : STATUS_SERVER;
                case LOGIN -> sender == Sender.CLIENT ? LOGIN_CLIENT : LOGIN_SERVER;
                case CONFIGURATION -> sender == Sender.CLIENT ? CONFIGURATION_CLIENT : CONFIGURATION_SERVER;
                default -> throw new IllegalArgumentException("Unable to find protocol " + protocol);
            };
        }
    }

    private final ProtocolSenderLookup idLookup = new ProtocolSenderLookup();
    private final ClassLookup classLookup = new ClassLookup();
    private final Map<String, Collection<PacketType>> nameLookup = new ConcurrentHashMap<>();

    public PacketTypeLookup addPacketTypes(Iterable<? extends PacketType> types) {
        if (types == null) throw new NullPointerException("types cannot be null");
        for (PacketType type : types) {
            if (type == null) continue;
            int id = type.getCurrentId();
            if (id >= 0) {
                idLookup.getMap(type.getProtocol(), type.getSender()).put(id, type);
            }
            Class<?> packetClass = type.getPacketClass();
            if (packetClass != null) {
                classLookup.getMap(type.getProtocol(), type.getSender()).put(packetClass.getName(), type);
            }
            nameLookup.computeIfAbsent(type.name(), ignored ->
                    Collections.synchronizedList(new ArrayList<>())).add(type);
        }
        return this;
    }

    /** @deprecated legacy numeric ids are not stable and are not available here. */
    @Deprecated public PacketType getFromLegacy(int packetId) { return null; }

    public Collection<PacketType> getFromName(String name) {
        Collection<PacketType> found = nameLookup.get(name);
        return found == null ? Collections.emptyList() : Collections.unmodifiableCollection(found);
    }

    /** @deprecated legacy numeric ids are not stable and are not available here. */
    @Deprecated public PacketType getFromLegacy(int packetId, Sender preference) { return null; }

    /** @deprecated current ids are version-specific; retained for API compatibility. */
    @Deprecated public PacketType getFromCurrent(Protocol protocol, Sender sender, int packetId) {
        return idLookup.getMap(protocol, sender).get(packetId);
    }

    public PacketType getFromCurrent(Protocol protocol, Sender sender, String name) {
        return classLookup.getMap(protocol, sender).get(name);
    }

    public ClassLookup getClassLookup() { return classLookup; }
}
