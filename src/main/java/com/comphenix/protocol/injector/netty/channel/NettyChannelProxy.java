package com.comphenix.protocol.injector.netty.channel;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundInvoker;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelProgressivePromise;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.net.SocketAddress;

/** Channel view that changes only the event-loop identity used by ProtocolLib. */
final class NettyChannelProxy implements Channel {
    private final Channel delegate;
    private final EventLoop eventLoop;
    @SuppressWarnings("unused") private final NettyChannelInjector injector;

    public NettyChannelProxy(Channel delegate, EventLoop eventLoop, NettyChannelInjector injector) {
        if (delegate == null) throw new NullPointerException("delegate");
        this.delegate = delegate;
        this.eventLoop = eventLoop;
        this.injector = injector;
    }

    public ChannelId id() { return delegate.id(); }
    public EventLoop eventLoop() { return eventLoop == null ? delegate.eventLoop() : eventLoop; }
    public Channel parent() { return delegate.parent(); }
    public ChannelConfig config() { return delegate.config(); }
    public boolean isOpen() { return delegate.isOpen(); }
    public boolean isRegistered() { return delegate.isRegistered(); }
    public boolean isActive() { return delegate.isActive(); }
    public ChannelMetadata metadata() { return delegate.metadata(); }
    public SocketAddress localAddress() { return delegate.localAddress(); }
    public SocketAddress remoteAddress() { return delegate.remoteAddress(); }
    public ChannelFuture closeFuture() { return delegate.closeFuture(); }
    public boolean isWritable() { return delegate.isWritable(); }
    public long bytesBeforeUnwritable() { return delegate.bytesBeforeUnwritable(); }
    public long bytesBeforeWritable() { return delegate.bytesBeforeWritable(); }
    public Channel.Unsafe unsafe() { return delegate.unsafe(); }
    public ChannelPipeline pipeline() { return delegate.pipeline(); }
    public ByteBufAllocator alloc() { return delegate.alloc(); }

    public ChannelPromise newPromise() { return delegate.newPromise(); }
    public ChannelProgressivePromise newProgressivePromise() { return delegate.newProgressivePromise(); }
    public ChannelFuture newSucceededFuture() { return delegate.newSucceededFuture(); }
    public ChannelFuture newFailedFuture(Throwable cause) { return delegate.newFailedFuture(cause); }
    public ChannelPromise voidPromise() { return delegate.voidPromise(); }
    public ChannelFuture bind(SocketAddress localAddress) { return delegate.bind(localAddress); }
    public ChannelFuture connect(SocketAddress remoteAddress) { return delegate.connect(remoteAddress); }
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) { return delegate.connect(remoteAddress, localAddress); }
    public ChannelFuture disconnect() { return delegate.disconnect(); }
    public ChannelFuture close() { return delegate.close(); }
    public ChannelFuture deregister() { return delegate.deregister(); }
    public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) { return delegate.bind(localAddress, promise); }
    public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) { return delegate.connect(remoteAddress, promise); }
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) { return delegate.connect(remoteAddress, localAddress, promise); }
    public ChannelFuture disconnect(ChannelPromise promise) { return delegate.disconnect(promise); }
    public ChannelFuture close(ChannelPromise promise) { return delegate.close(promise); }
    public ChannelFuture deregister(ChannelPromise promise) { return delegate.deregister(promise); }
    public Channel read() { delegate.read(); return this; }
    public ChannelFuture write(Object message) { return delegate.write(message); }
    public ChannelFuture write(Object message, ChannelPromise promise) { return delegate.write(message, promise); }
    public Channel flush() { delegate.flush(); return this; }
    public ChannelFuture writeAndFlush(Object message, ChannelPromise promise) { return delegate.writeAndFlush(message, promise); }
    public ChannelFuture writeAndFlush(Object message) { return delegate.writeAndFlush(message); }
    public <T> Attribute<T> attr(AttributeKey<T> key) { return delegate.attr(key); }
    public <T> boolean hasAttr(AttributeKey<T> key) { return delegate.hasAttr(key); }
    public int compareTo(Channel other) { return delegate.compareTo(other); }

}
