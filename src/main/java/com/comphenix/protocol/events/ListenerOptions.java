package com.comphenix.protocol.events;

/** Optional listener execution and verification flags. */
public enum ListenerOptions {
    @Deprecated DISABLE_GAMEPHASE_DETECTION,
    SKIP_PLUGIN_VERIFIER,
    ASYNC,
    SYNC
}
