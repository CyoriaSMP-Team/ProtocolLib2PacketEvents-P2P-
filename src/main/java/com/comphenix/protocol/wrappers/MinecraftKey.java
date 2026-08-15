/*
 * ProtocolLib2PacketEvents - clean-room key wrapper.
 */
package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;

import java.util.Locale;
import java.util.Objects;

/** Version-neutral representation of a Minecraft namespaced key. */
public class MinecraftKey {
    private final String prefix;
    private final String key;

    public MinecraftKey(String prefix, String key) {
        this.prefix = prefix == null || prefix.isBlank() ? "minecraft" : prefix;
        this.key = Objects.requireNonNull(key, "key");
    }
    public MinecraftKey(String key) { this("minecraft", key); }
    public static MinecraftKey fromHandle(Object handle) {
        if (handle instanceof MinecraftKey key) return key;
        return handle == null ? null : new MinecraftKey(String.valueOf(handle));
    }
    @Deprecated public static MinecraftKey fromEnum(Enum<?> value) {
        return new MinecraftKey(value.name().toLowerCase(Locale.ENGLISH).replace('_', '.'));
    }
    public String getPrefix() { return prefix; }
    public String getKey() { return key; }
    public String getFullKey() { return prefix + ":" + key; }
    @Deprecated public String getEnumFormat() { return key.toUpperCase(Locale.ENGLISH).replace('.', '_'); }
    public Object getHandle() { return this; }
    public static EquivalentConverter<MinecraftKey> getConverter() {
        return new EquivalentConverter<>() {
            @Override public MinecraftKey getSpecific(Object generic) { return fromHandle(generic); }
            @Override public Object getGeneric(MinecraftKey specific) { return specific; }
            @Override public Class<MinecraftKey> getSpecificType() { return MinecraftKey.class; }
            @Override public Class<?> getGenericType() { return MinecraftKey.class; }
        };
    }
    @Override public boolean equals(Object other) {
        return other instanceof MinecraftKey keyValue && prefix.equals(keyValue.prefix) && key.equals(keyValue.key);
    }
    @Override public int hashCode() { return Objects.hash(prefix, key); }
    @Override public String toString() { return getFullKey(); }
}
