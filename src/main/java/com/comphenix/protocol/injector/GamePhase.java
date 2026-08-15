package com.comphenix.protocol.injector;

/** Packet lifecycle phase used by legacy ProtocolLib listeners. */
@Deprecated
public enum GamePhase {
    LOGIN,
    PLAYING,
    BOTH;

    public boolean hasLogin() { return this == LOGIN || this == BOTH; }
    public boolean hasPlaying() { return this == PLAYING || this == BOTH; }
}
