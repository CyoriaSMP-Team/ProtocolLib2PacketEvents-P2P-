package com.comphenix.protocol.paper;

import com.comphenix.protocol.ProtocolLib;
import com.comphenix.protocol.internal.PlatformProvider;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import io.netty.channel.Channel;

import java.util.List;
import java.util.function.Consumer;

public final class PaperPlatformProvider implements PlatformProvider {
    public PaperPlatformProvider() { }
    public boolean hasEarlyChannelInitialization() { return false; }
    public Runnable registerChannelInitializer(Consumer<Channel> initializer) { return () -> { }; }
    public void registerCommand(ProtocolLib library, String name, CommandExecutor executor) { }
    record CommandMetadata(String description, String usage, String permission, List<String> aliases) { }
    static class RegisteredCommand extends Command {
        private final CommandExecutor executor;
        RegisteredCommand(String name, CommandExecutor executor) { super(name); this.executor=executor; }
        public boolean execute(CommandSender sender,String label,String[] args){return executor != null && executor.onCommand(sender,this,label,args);}
    }
    record RegisteredBasicCommand(String name, CommandMetadata metadata, Command command, CommandExecutor executor) { }
}
