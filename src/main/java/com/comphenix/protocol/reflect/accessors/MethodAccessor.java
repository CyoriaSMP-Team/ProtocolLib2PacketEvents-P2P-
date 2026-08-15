/*
 * ProtocolLib2PacketEvents (P2P) - a drop-in ProtocolLib compatibility layer
 * powered by PacketEvents.
 */
package com.comphenix.protocol.reflect.accessors;

import java.lang.reflect.Method;

/** Small reflection accessor matching ProtocolLib's public interface. */
public interface MethodAccessor {
    Object invoke(Object target, Object... args);
    Method getMethod();
}
