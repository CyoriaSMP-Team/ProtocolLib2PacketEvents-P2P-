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
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Player profile handle, mirroring ProtocolLib's {@code WrappedGameProfile}.
 * Backed by PacketEvents' {@link UserProfile} rather than Mojang's authlib GameProfile.
 */
public class WrappedGameProfile {

    private final UserProfile handle;

    private WrappedGameProfile(UserProfile handle) {
        this.handle = handle;
    }

    public WrappedGameProfile(UUID uuid, String name) {
        this(new UserProfile(uuid, name));
    }

    public static WrappedGameProfile fromHandle(UserProfile profile) {
        return profile == null ? null : new WrappedGameProfile(profile);
    }

    public UUID getUUID() {
        return handle.getUUID();
    }

    /** ProtocolLib exposes the UUID as a string under this name. */
    public String getId() {
        UUID uuid = handle.getUUID();
        return uuid == null ? null : uuid.toString();
    }

    public String getName() {
        return handle.getName();
    }

    /** Skin/cape texture properties attached to this profile. */
    public List<TextureProperty> getProperties() {
        List<TextureProperty> properties = handle.getTextureProperties();
        return properties == null ? new ArrayList<>() : properties;
    }

    /** A copy of this profile with a different name, leaving this instance untouched. */
    public WrappedGameProfile withName(String name) {
        return new WrappedGameProfile(new UserProfile(handle.getUUID(), name, handle.getTextureProperties()));
    }

    /** A copy of this profile with a different UUID, leaving this instance untouched. */
    public WrappedGameProfile withId(UUID uuid) {
        return new WrappedGameProfile(new UserProfile(uuid, handle.getName(), handle.getTextureProperties()));
    }

    public UserProfile getHandle() {
        return handle;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof WrappedGameProfile && handle.equals(((WrappedGameProfile) o).handle);
    }

    @Override
    public int hashCode() {
        return handle == null ? 0 : handle.hashCode();
    }

    @Override
    public String toString() {
        return "WrappedGameProfile[uuid=" + getUUID() + ", name=" + getName() + "]";
    }

    public static EquivalentConverter<WrappedGameProfile> getConverter() {
        return CONVERTER;
    }

    private static final EquivalentConverter<WrappedGameProfile> CONVERTER = new EquivalentConverter<>() {
        @Override
        public WrappedGameProfile getSpecific(Object generic) {
            return fromHandle((UserProfile) generic);
        }

        @Override
        public Object getGeneric(WrappedGameProfile specific) {
            return specific == null ? null : specific.getHandle();
        }

        @Override
        public Class<WrappedGameProfile> getSpecificType() {
            return WrappedGameProfile.class;
        }

        @Override
        public Class<?> getGenericType() {
            return UserProfile.class;
        }
    };
}
