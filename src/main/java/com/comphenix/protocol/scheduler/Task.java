/*
 * ProtocolLib2PacketEvents - clean-room compatibility API.
 */
package com.comphenix.protocol.scheduler;

/** A cancellable task returned by the ProtocolLib scheduler facade. */
public interface Task {
    void cancel();
}
