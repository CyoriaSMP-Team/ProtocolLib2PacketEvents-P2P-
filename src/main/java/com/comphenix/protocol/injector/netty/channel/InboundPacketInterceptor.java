package com.comphenix.protocol.injector.netty.channel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

final class InboundPacketInterceptor extends ChannelInboundHandlerAdapter {
    private final NettyChannelInjector injector;
    public InboundPacketInterceptor(NettyChannelInjector injector) { this.injector = injector; }
    @Override public void channelRead(ChannelHandlerContext context, Object message) throws Exception { injector.fireInbound(context, message); }
}
