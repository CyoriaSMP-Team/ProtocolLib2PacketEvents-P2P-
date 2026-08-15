package com.comphenix.protocol.injector.netty.channel;

import com.comphenix.protocol.injector.netty.WirePacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

final class WirePacketEncoder extends MessageToByteEncoder<WirePacket> {
    @Override public boolean acceptOutboundMessage(Object message) { return message instanceof WirePacket; }
    @Override public boolean isSharable() { return true; }
    @Override protected void encode(ChannelHandlerContext context, WirePacket packet, ByteBuf out) {
        int value = packet.getId();
        while ((value & ~0x7f) != 0) { out.writeByte((value & 0x7f) | 0x80); value >>>= 7; }
        out.writeByte(value);
        out.writeBytes(packet.getBytes());
    }
}
