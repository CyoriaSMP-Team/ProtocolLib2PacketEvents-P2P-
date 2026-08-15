package com.comphenix.protocol.injector.packet.internal;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.wrappers.AbstractWrapper;

public class ProtocolInfoWrapper extends AbstractWrapper {
    private final PacketType.Protocol protocol;
    private final PacketType.Sender sender;
    private final IdCodecWrapper codec;
    public ProtocolInfoWrapper(Object handle) { this(handle, PacketType.Protocol.UNKNOWN, PacketType.Sender.CLIENT, null); }
    private ProtocolInfoWrapper(Object handle, PacketType.Protocol protocol, PacketType.Sender sender, IdCodecWrapper codec) { super(handle == null ? Object.class : handle.getClass()); if (handle != null) setHandle(handle); this.protocol=protocol;this.sender=sender;this.codec=codec; }
    public static boolean isUnboundProtocol(Object handle) { return handle == null; }
    public static ProtocolInfoWrapper fromUnbound(Object handle, Object context) { return new ProtocolInfoWrapper(handle); }
    public PacketType.Protocol id() { return protocol; }
    public PacketType.Sender flow() { return sender; }
    public IdCodecWrapper codec() { return codec; }
}
