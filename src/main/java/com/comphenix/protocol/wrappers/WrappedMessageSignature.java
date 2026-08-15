/* ProtocolLib2PacketEvents - clean-room message-signature wrapper. */
package com.comphenix.protocol.wrappers;

import java.util.Arrays;

public class WrappedMessageSignature {
    private byte[] bytes;
    public WrappedMessageSignature(Object handle) {
        this(handle instanceof byte[] value ? value : new byte[0]);
    }
    public WrappedMessageSignature(byte[] bytes) { setBytes(bytes); }
    public byte[] getBytes() { return bytes.clone(); }
    public void setBytes(byte[] bytes) { this.bytes = bytes == null ? new byte[0] : bytes.clone(); }
    public Object getHandle() { return bytes.clone(); }
    @Override public boolean equals(Object other) { return other instanceof WrappedMessageSignature sig && Arrays.equals(bytes, sig.bytes); }
    @Override public int hashCode() { return Arrays.hashCode(bytes); }
}
