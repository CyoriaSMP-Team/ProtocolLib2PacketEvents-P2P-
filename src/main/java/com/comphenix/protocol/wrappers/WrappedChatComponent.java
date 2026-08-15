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
public class WrappedChatComponent extends AbstractWrapper implements ClonableWrapper {

    private WrappedChatComponent(Component handle) {
        super(Component.class);
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

    /** Parse the legacy chat message into the component sequence used by ProtocolLib. */
    public static WrappedChatComponent[] fromChatMessage(String message) {
        if (message == null) return new WrappedChatComponent[0];
        return new WrappedChatComponent[] { fromLegacyText(message) };
    }

    /** The JSON form of this component. */
    public String getJson() {
        return AdventureSerializer.toJson((Component) handle);
    }

    /** The legacy section-sign form of this component. */
    public String getLegacyText() {
        return AdventureSerializer.toLegacyFormat((Component) handle);
    }

    /** PacketEvents-typed view used by this bridge's converter. */
    public Component getComponent() {
        return (Component) handle;
    }

    public void setJson(String json) {
        if (json == null) throw new IllegalArgumentException("json cannot be null");
        this.handle = AdventureSerializer.parseComponent(json);
    }

    public WrappedChatComponent deepClone() {
        return handle == null ? null : fromJson(getJson());
    }

    public static WrappedChatComponent fromHandle(Object handle) {
        return handle instanceof Component ? fromComponent((Component) handle) : null;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof WrappedChatComponent && java.util.Objects.equals(handle, ((WrappedChatComponent) o).handle);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(handle);
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
            return fromHandle(generic);
        }

        @Override
        public Object getGeneric(WrappedChatComponent specific) {
            return specific == null ? null : specific.getComponent();
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
