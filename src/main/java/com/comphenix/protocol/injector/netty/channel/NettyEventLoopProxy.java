package com.comphenix.protocol.injector.netty.channel;

import io.netty.channel.AbstractEventLoop;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.ProgressivePromise;
import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/** Event-loop facade that delegates execution to the PacketEvents-owned loop. */
final class NettyEventLoopProxy extends AbstractEventLoop {
    private final EventLoop delegate;
    @SuppressWarnings("unused") private final NettyChannelInjector injector;

    public NettyEventLoopProxy(EventLoop delegate, NettyChannelInjector injector) {
        this.delegate = delegate;
        this.injector = injector;
    }

    public EventLoopGroup parent() { return delegate.parent(); }
    public EventLoop next() { return delegate.next(); }
    public boolean inEventLoop() { return delegate.inEventLoop(); }
    public boolean inEventLoop(Thread thread) { return delegate.inEventLoop(thread); }
    public <V> Promise<V> newPromise() { return delegate.newPromise(); }
    public <V> ProgressivePromise<V> newProgressivePromise() { return delegate.newProgressivePromise(); }
    public <V> Future<V> newSucceededFuture(V value) { return delegate.newSucceededFuture(value); }
    public <V> Future<V> newFailedFuture(Throwable cause) { return delegate.newFailedFuture(cause); }
    public boolean isShuttingDown() { return delegate.isShuttingDown(); }
    public Future<?> shutdownGracefully() { return delegate.shutdownGracefully(); }
    public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) { return delegate.shutdownGracefully(quietPeriod, timeout, unit); }
    public Future<?> terminationFuture() { return delegate.terminationFuture(); }
    public void shutdown() { delegate.shutdown(); }
    public List<Runnable> shutdownNow() { return delegate.shutdownNow(); }
    public Iterator<EventExecutor> iterator() { return delegate.iterator(); }
    public Future<?> submit(Runnable task) { return delegate.submit(task); }
    public <T> Future<T> submit(Runnable task, T result) { return delegate.submit(task, result); }
    public <T> Future<T> submit(Callable<T> task) { return delegate.submit(task); }
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) { return delegate.schedule(command, delay, unit); }
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) { return delegate.schedule(callable, delay, unit); }
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) { return delegate.scheduleAtFixedRate(command, initialDelay, period, unit); }
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) { return delegate.scheduleWithFixedDelay(command, initialDelay, delay, unit); }
    public boolean isShutdown() { return delegate.isShutdown(); }
    public boolean isTerminated() { return delegate.isTerminated(); }
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException { return delegate.awaitTermination(timeout, unit); }
    public <T> List<java.util.concurrent.Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException { return delegate.invokeAll(tasks); }
    public <T> List<java.util.concurrent.Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException { return delegate.invokeAll(tasks, timeout, unit); }
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException { return delegate.invokeAny(tasks); }
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException { return delegate.invokeAny(tasks, timeout, unit); }
    public void execute(Runnable command) { delegate.execute(command); }
    public void lazyExecute(Runnable command) { delegate.execute(command); }
    public void forEach(Consumer<? super EventExecutor> action) { delegate.forEach(action); }
    public Spliterator<EventExecutor> spliterator() { return delegate.spliterator(); }
    public ChannelFuture register(Channel channel) { return delegate.register(channel); }
    public ChannelFuture register(ChannelPromise promise) { return delegate.register(promise); }
    public ChannelFuture register(Channel channel, ChannelPromise promise) { return delegate.register(channel, promise); }
}
