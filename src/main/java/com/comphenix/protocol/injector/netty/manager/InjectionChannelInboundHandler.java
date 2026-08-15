package com.comphenix.protocol.injector.netty.manager;

import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.injector.netty.channel.InjectionFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

final class InjectionChannelInboundHandler extends ChannelInboundHandlerAdapter {
    private final ErrorReporter reporter;
    private final InjectionFactory factory;
    public InjectionChannelInboundHandler(ErrorReporter reporter, InjectionFactory factory) { this.reporter=reporter;this.factory=factory; }
    @Override public void channelActive(ChannelHandlerContext context) {
        try { factory.fromChannel(context.channel()).inject(); context.fireChannelActive(); }
        catch (Throwable error) { if (reporter != null) reporter.reportDetailed(this, "Unable to inject Netty channel", error); context.fireChannelActive(); }
    }
    @Override public boolean isSharable() { return true; }
}
