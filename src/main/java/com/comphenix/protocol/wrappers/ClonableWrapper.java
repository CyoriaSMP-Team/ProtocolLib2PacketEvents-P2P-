package com.comphenix.protocol.wrappers;

/** Common handle/clone contract used by ProtocolLib wrapper objects. */
public interface ClonableWrapper {
    Object getHandle();

    ClonableWrapper deepClone();
}
