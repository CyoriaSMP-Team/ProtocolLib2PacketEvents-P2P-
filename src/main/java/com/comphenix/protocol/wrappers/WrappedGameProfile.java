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
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Player profile handle, mirroring ProtocolLib's {@code WrappedGameProfile}.
 * Backed by PacketEvents' {@link UserProfile} rather than Mojang's authlib GameProfile.
 */
public class WrappedGameProfile {

    private final UserProfile handle;
    private final Multimap<String, WrappedSignedProperty> properties;
    private final ProfileHandle compatibilityHandle;

    private WrappedGameProfile(UserProfile handle) {
        this.handle = handle;
        this.properties = ArrayListMultimap.create();
        List<TextureProperty> textureProperties = handle.getTextureProperties();
        if (textureProperties != null) {
            for (TextureProperty property : textureProperties) {
                properties.put(property.getName(), WrappedSignedProperty.fromValues(
                        property.getName(), property.getValue(), property.getSignature()));
            }
        }
        this.compatibilityHandle = new ProfileHandle(handle.getUUID(), handle.getName(), properties);
    }

    public WrappedGameProfile(UUID uuid, String name) {
        this(new UserProfile(uuid, name));
    }

    public static WrappedGameProfile fromHandle(UserProfile profile) {
        return profile == null ? null : new WrappedGameProfile(profile);
    }

    /** Accepts either PacketEvents' profile or the compatibility handle exposed by getHandle(). */
    public static WrappedGameProfile fromHandle(Object profile) {
        if (profile == null) {
            return null;
        }
        if (profile instanceof WrappedGameProfile.ProfileHandle) {
            return ((ProfileHandle) profile).toWrappedProfile();
        }
        if (profile instanceof UserProfile) {
            return fromHandle((UserProfile) profile);
        }
        return null;
    }

    /** Creates a profile from a Bukkit player without touching version-specific NMS classes. */
    public static WrappedGameProfile fromPlayer(Player player) {
        return player == null ? null : new WrappedGameProfile(
                new UserProfile(player.getUniqueId(), player.getName()));
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
    public Multimap<String, WrappedSignedProperty> getProperties() {
        return properties;
    }

    /** A copy of this profile with a different name, leaving this instance untouched. */
    public WrappedGameProfile withName(String name) {
        return new WrappedGameProfile(new UserProfile(handle.getUUID(), name, textureProperties()));
    }

    /** A copy of this profile with a different UUID, leaving this instance untouched. */
    public WrappedGameProfile withId(UUID uuid) {
        return new WrappedGameProfile(new UserProfile(uuid, handle.getName(), textureProperties()));
    }

    /** The binary-compatible ProtocolLib handle view. */
    public Object getHandle() {
        syncProperties();
        return compatibilityHandle;
    }

    /** PacketEvents-typed profile used internally by PacketContainer converters. */
    public UserProfile getUserProfile() {
        syncProperties();
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

    private List<TextureProperty> textureProperties() {
        syncProperties();
        return handle.getTextureProperties();
    }

    private void syncProperties() {
        List<TextureProperty> converted = new ArrayList<>();
        for (Map.Entry<String, WrappedSignedProperty> entry : properties.entries()) {
            WrappedSignedProperty property = entry.getValue();
            converted.add(new TextureProperty(entry.getKey(), property.getValue(), property.getSignature()));
        }
        handle.setTextureProperties(converted);
        compatibilityHandle.update(handle.getUUID(), handle.getName());
    }

    public static EquivalentConverter<WrappedGameProfile> getConverter() {
        return CONVERTER;
    }

    private static final EquivalentConverter<WrappedGameProfile> CONVERTER = new EquivalentConverter<>() {
        @Override
        public WrappedGameProfile getSpecific(Object generic) {
            return fromHandle(generic);
        }

        @Override
        public Object getGeneric(WrappedGameProfile specific) {
            return specific == null ? null : specific.getUserProfile();
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

    /**
     * Small authlib-like object for old plugins that reflect getProperties() from the handle.
     * It deliberately stays independent of NMS so the bridge remains version-neutral.
     */
    public static final class ProfileHandle {
        private UUID id;
        private String name;
        private final Multimap<String, WrappedSignedProperty> properties;

        private ProfileHandle(UUID id, String name, Multimap<String, WrappedSignedProperty> properties) {
            this.id = id;
            this.name = name;
            this.properties = properties;
        }

        public UUID getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Multimap<String, WrappedSignedProperty> getProperties() {
            return properties;
        }

        private void update(UUID id, String name) {
            this.id = id;
            this.name = name;
        }

        private WrappedGameProfile toWrappedProfile() {
            List<TextureProperty> values = new ArrayList<>();
            for (Map.Entry<String, WrappedSignedProperty> entry : properties.entries()) {
                WrappedSignedProperty property = entry.getValue();
                values.add(new TextureProperty(entry.getKey(), property.getValue(), property.getSignature()));
            }
            return new WrappedGameProfile(new UserProfile(id, name, values));
        }
    }
}
