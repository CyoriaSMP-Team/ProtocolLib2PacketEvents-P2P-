/*
 * ProtocolLib2PacketEvents - clean-room temporary-player state holder.
 */
package com.comphenix.protocol.injector.temporary;

import com.comphenix.protocol.injector.netty.Injector;

import java.util.Objects;

/**
 * State shared by the generated pre-login Bukkit Player implementation.
 *
 * <p>The class is deliberately concrete: ProtocolLib's temporary player is a
 * superclass for a generated Player implementation, not a marker interface.</p>
 */
public class TemporaryPlayer {
    protected volatile Injector injector;

    public TemporaryPlayer() {
    }

    public Injector getInjector() {
        return injector;
    }

    void setInjector(Injector injector) {
        Objects.requireNonNull(injector, "injector can't be null");
        if (this.injector != null) {
            throw new IllegalStateException("Can't redefine injector for temporary player");
        }
        this.injector = injector;
    }
}
