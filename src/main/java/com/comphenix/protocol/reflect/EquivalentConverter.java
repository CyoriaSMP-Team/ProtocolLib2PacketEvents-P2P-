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
package com.comphenix.protocol.reflect;

/**
 * Translates between the type a PacketEvents wrapper stores in a field (the "generic" value)
 * and the ProtocolLib/Bukkit type plugins expect to see (the "specific" value).
 * <p>
 * ProtocolLib's own {@code StructureModifier} works the same way: packet fields hold NMS types,
 * and converters project them into API types. Here the underlying type is a PacketEvents type
 * rather than an NMS one, which is what lets the conversion happen without touching NMS at all.
 *
 * @param <T> the ProtocolLib-facing type
 */
public interface EquivalentConverter<T> {

    /** Converts a PacketEvents field value into the ProtocolLib-facing type. */
    T getSpecific(Object generic);

    /** Converts a ProtocolLib-facing value back into the type the wrapper field holds. */
    Object getGeneric(T specific);

    /** The ProtocolLib-facing type this converter produces. */
    Class<T> getSpecificType();

    /**
     * The type of the wrapper field this converter applies to. {@link StructureModifier} uses
     * this to decide which fields a converted modifier should expose.
     */
    Class<?> getGenericType();
}
