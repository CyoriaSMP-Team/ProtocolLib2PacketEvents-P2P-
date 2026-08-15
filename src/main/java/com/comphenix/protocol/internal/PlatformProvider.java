package com.comphenix.protocol.internal;

import com.comphenix.protocol.ProtocolLib;
import org.bukkit.command.CommandExecutor;
import io.netty.channel.Channel;

import java.util.function.Consumer;

public interface PlatformProvider {
    static PlatformProvider get() { return Holder.INSTANCE; }
    boolean hasEarlyChannelInitialization();
    Runnable registerChannelInitializer(Consumer<Channel> initializer);
    void registerCommand(ProtocolLib library, String name, CommandExecutor executor);
    final class Holder { private static final PlatformProvider INSTANCE = new PlatformProvider() {
        public boolean hasEarlyChannelInitialization(){return false;}
        public Runnable registerChannelInitializer(Consumer<Channel> initializer){return () -> { };}
        public void registerCommand(ProtocolLib library,String name,CommandExecutor executor){ }
    }; private Holder(){} }
}
