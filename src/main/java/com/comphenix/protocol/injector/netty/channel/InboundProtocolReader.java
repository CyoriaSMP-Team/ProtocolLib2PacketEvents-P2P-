package com.comphenix.protocol.injector.netty.channel;

import com.comphenix.protocol.PacketType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class InboundProtocolReader extends ChannelInboundHandlerAdapter {
    private final NettyChannelInjector injector;
    public InboundProtocolReader(NettyChannelInjector injector) { this.injector = injector; }
    public PacketType.Protocol getProtocol() { return injector == null ? PacketType.Protocol.UNKNOWN : injector.getCurrentProtocol(PacketType.Sender.CLIENT); }
    @Override public void channelRead(ChannelHandlerContext context, Object message) throws Exception { if (injector == null) context.fireChannelRead(message); else injector.fireInbound(context, message); }
}
