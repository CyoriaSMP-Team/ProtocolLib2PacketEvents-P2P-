package com.comphenix.protocol.injector.netty.manager;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;

final class InjectionChannelInitializer extends ChannelInboundHandlerAdapter {
    private final String name;
    private final ChannelInboundHandler handler;
    public InjectionChannelInitializer(String name, ChannelInboundHandler handler) { this.name=name;this.handler=handler; }
    @Override public void channelRead(ChannelHandlerContext context, Object message) {
        if (context.pipeline().get(name) == null) context.pipeline().addFirst(name, handler);
        context.fireChannelRead(message);
    }
    @Override public boolean isSharable() { return true; }
}
