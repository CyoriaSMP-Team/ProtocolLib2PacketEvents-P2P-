/* ProtocolLib2PacketEvents - clean-room particle wrapper. */
package com.comphenix.protocol.wrappers;

import org.bukkit.Particle;

public class WrappedParticle<T> {
    private final Particle particle;
    private final T data;
    private final Object handle;
    private WrappedParticle(Particle particle, T data) { this.particle = particle; this.data = data; this.handle = this; }
    public Particle getParticle() { return particle; }
    public T getData() { return data; }
    public Object getHandle() { return handle; }
    public static WrappedParticle<?> fromHandle(Object handle) {
        return handle instanceof WrappedParticle<?> value ? value : null;
    }
    public static <T> WrappedParticle<T> create(Particle particle, T data) {
        if (particle == null) throw new IllegalArgumentException("particle cannot be null");
        return new WrappedParticle<>(particle, data);
    }
    @Override public String toString() { return "WrappedParticle[" + particle + ", data=" + data + "]"; }
}
