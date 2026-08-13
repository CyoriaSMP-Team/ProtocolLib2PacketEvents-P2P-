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
package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.github.retrooper.packetevents.util.adventure.AdventureSerializer;
import net.kyori.adventure.text.Component;

/**
 * Chat component handle, mirroring ProtocolLib's {@code WrappedChatComponent}.
 * <p>
 * ProtocolLib wraps an NMS {@code IChatBaseComponent}; here the underlying value is an
 * Adventure {@link Component}, which is what PacketEvents' wrappers store. JSON is produced
 * and parsed through PacketEvents' own serializer so the result matches exactly what
 * PacketEvents would put on the wire.
 */
public class WrappedChatComponent {

    private final Component handle;

    private WrappedChatComponent(Component handle) {
        this.handle = handle;
    }

    /** Wraps an Adventure component directly. */
    public static WrappedChatComponent fromComponent(Component component) {
        return component == null ? null : new WrappedChatComponent(component);
    }

    /** Parses a chat component from its JSON representation. */
    public static WrappedChatComponent fromJson(String json) {
        return json == null ? null : new WrappedChatComponent(AdventureSerializer.parseComponent(json));
    }

    /** Builds a plain-text component, honouring legacy section-sign colour codes. */
    public static WrappedChatComponent fromLegacyText(String legacy) {
        return legacy == null ? null : new WrappedChatComponent(AdventureSerializer.fromLegacyFormat(legacy));
    }

    /**
     * Builds a component from plain text. Matches ProtocolLib's {@code fromText}, which treats
     * its argument as literal text rather than JSON.
     */
    public static WrappedChatComponent fromText(String text) {
        return text == null ? null : new WrappedChatComponent(Component.text(text));
    }

    /** The JSON form of this component. */
    public String getJson() {
        return AdventureSerializer.toJson(handle);
    }

    /** The legacy section-sign form of this component. */
    public String getLegacyText() {
        return AdventureSerializer.toLegacyFormat(handle);
    }

    /** The underlying Adventure component PacketEvents stores. */
    public Component getHandle() {
        return handle;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof WrappedChatComponent && handle.equals(((WrappedChatComponent) o).handle);
    }

    @Override
    public int hashCode() {
        return handle == null ? 0 : handle.hashCode();
    }

    @Override
    public String toString() {
        return "WrappedChatComponent[" + getJson() + "]";
    }

    public static EquivalentConverter<WrappedChatComponent> getConverter() {
        return CONVERTER;
    }

    private static final EquivalentConverter<WrappedChatComponent> CONVERTER = new EquivalentConverter<>() {
        @Override
        public WrappedChatComponent getSpecific(Object generic) {
            return fromComponent((Component) generic);
        }

        @Override
        public Object getGeneric(WrappedChatComponent specific) {
            return specific == null ? null : specific.getHandle();
        }

        @Override
        public Class<WrappedChatComponent> getSpecificType() {
            return WrappedChatComponent.class;
        }

        @Override
        public Class<?> getGenericType() {
            return Component.class;
        }
    };
}
