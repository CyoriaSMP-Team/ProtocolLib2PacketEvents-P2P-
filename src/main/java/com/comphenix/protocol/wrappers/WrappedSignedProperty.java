/*
 * ProtocolLib2PacketEvents (P2P) - a drop-in ProtocolLib compatibility layer
 * powered by PacketEvents.
 */
package com.comphenix.protocol.wrappers;

import com.github.retrooper.packetevents.protocol.player.TextureProperty;

import java.security.PublicKey;
import java.util.Objects;

/** Version-neutral signed texture/property wrapper. */
public class WrappedSignedProperty {

    private final String name;
    private final String value;
    private final String signature;

    public WrappedSignedProperty(String name, String value, String signature) {
        this.name = name;
        this.value = value;
        this.signature = signature;
    }

    public static WrappedSignedProperty fromValues(String name, String value, String signature) {
        return new WrappedSignedProperty(name, value, signature);
    }

    public static WrappedSignedProperty fromHandle(Object handle) {
        if (handle instanceof WrappedSignedProperty) {
            return (WrappedSignedProperty) handle;
        }
        if (handle instanceof TextureProperty) {
            TextureProperty property = (TextureProperty) handle;
            return fromValues(property.getName(), property.getValue(), property.getSignature());
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getSignature() {
        return signature;
    }

    public boolean hasSignature() {
        return signature != null;
    }

    public boolean isSignatureValid(PublicKey key) {
        return false;
    }

    /** PacketEvents handle used by the profile converter. */
    public Object getHandle() {
        return new TextureProperty(name, value, signature);
    }

    public TextureProperty toTextureProperty() {
        return new TextureProperty(name, value, signature);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof WrappedSignedProperty)) return false;
        WrappedSignedProperty that = (WrappedSignedProperty) other;
        return Objects.equals(name, that.name) && Objects.equals(value, that.value)
                && Objects.equals(signature, that.signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, signature);
    }

    @Override
    public String toString() {
        return "WrappedSignedProperty[name=" + name + ", value=" + value + ", signature=" + signature + "]";
    }
}
