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

import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Populates {@link PacketType}'s runtime registries by reflecting over the
 * {@code Client}/{@code Server} enums nested in PacketEvents' own {@code PacketType}.
 * <p>
 * This runs against whichever PacketEvents build is actually loaded on the server, which is
 * deliberately <em>not</em> assumed to be the one the plugin was compiled against - the
 * generated constants in {@link PacketType} resolve themselves against whatever this finds.
 */
final class PacketTypeRegistryBuilder {

    private static final String PE_PACKET_TYPE =
            "com.github.retrooper.packetevents.protocol.packettype.PacketType";

    private PacketTypeRegistryBuilder() {
    }

    static int build(Map<String, PacketType> byKey, Map<PacketTypeCommon, PacketType> byHandle) {
        int total = 0;
        try {
            Class<?> root = Class.forName(PE_PACKET_TYPE);
            for (Class<?> phaseClass : root.getDeclaredClasses()) {
                PacketType.Protocol protocol = matchProtocol(phaseClass.getSimpleName());
                if (protocol == null) {
                    continue;
                }
                for (Class<?> sideClass : phaseClass.getDeclaredClasses()) {
                    PacketType.Sender sender = matchSender(sideClass.getSimpleName());
                    if (sender == null || !sideClass.isEnum()) {
                        continue;
                    }
                    for (Object constant : sideClass.getEnumConstants()) {
                        PacketTypeCommon handle = (PacketTypeCommon) constant;
                        String name = ((Enum<?>) constant).name();
                        PacketType type = new PacketType(protocol, sender, name, handle);
                        byKey.put(PacketType.key(protocol, sender, name), type);
                        byHandle.put(handle, type);
                        total++;
                    }
                }
            }
        } catch (ReflectiveOperationException | LinkageError e) {
            Logger.getLogger("ProtocolLib2PacketEvents").log(Level.SEVERE,
                    "Failed to build the PacketType registry from PacketEvents - "
                            + "every packet type constant will be unsupported and no listener will fire", e);
        }
        return total;
    }

    private static PacketType.Protocol matchProtocol(String simpleName) {
        for (PacketType.Protocol protocol : PacketType.Protocol.values()) {
            if (protocol.name().equalsIgnoreCase(simpleName)) {
                return protocol;
            }
        }
        return null;
    }

    private static PacketType.Sender matchSender(String simpleName) {
        if ("Client".equals(simpleName)) {
            return PacketType.Sender.CLIENT;
        }
        if ("Server".equals(simpleName)) {
            return PacketType.Sender.SERVER;
        }
        return null;
    }
}
