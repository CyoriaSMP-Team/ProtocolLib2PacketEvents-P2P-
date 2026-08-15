package com.comphenix.protocol.spigot;

import com.comphenix.protocol.ProtocolLib;
import com.comphenix.protocol.internal.PlatformProvider;
import org.bukkit.command.CommandExecutor;
import io.netty.channel.Channel;

import java.util.function.Consumer;

public final class SpigotPlatformProvider implements PlatformProvider {
    public SpigotPlatformProvider() { }
    public boolean hasEarlyChannelInitialization() { return false; }
    public Runnable registerChannelInitializer(Consumer<Channel> initializer) { return () -> { }; }
    public void registerCommand(ProtocolLib library, String name, CommandExecutor executor) { }
}
