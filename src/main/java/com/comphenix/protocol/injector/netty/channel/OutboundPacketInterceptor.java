package com.comphenix.protocol.injector.netty.channel;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * Sees the encoded ByteBuf after the server's packet encoder. Structured packets have already
 * gone through PacketEvents at this point; the injector only consumes the unmodelled fallback.
 */
final class OutboundPacketInterceptor extends ChannelDuplexHandler {
    private final NettyChannelInjector injector;

    OutboundPacketInterceptor(NettyChannelInjector injector) {
        this.injector = injector;
    }

    @Override
    public void write(ChannelHandlerContext context, Object message, ChannelPromise promise) throws Exception {
        injector.fireOutbound(context, message, promise);
    }
}
