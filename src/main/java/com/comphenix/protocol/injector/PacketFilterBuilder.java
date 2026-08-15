package com.comphenix.protocol.injector;

import com.comphenix.protocol.ProtocolLib;
import com.comphenix.protocol.async.AsyncFilterManager;
import com.comphenix.protocol.error.BasicErrorReporter;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.scheduler.ProtocolScheduler;
import com.comphenix.protocol.utility.MinecraftVersion;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

public class PacketFilterBuilder {
    private Server server;
    private Plugin library;
    private MinecraftVersion minecraftVersion;
    private ErrorReporter reporter = new BasicErrorReporter();
    private ProtocolScheduler scheduler;
    public PacketFilterBuilder() { }
    public PacketFilterBuilder server(Server value) { server = value; return this; }
    public PacketFilterBuilder library(ProtocolLib value) { library = value; return this; }
    public PacketFilterBuilder minecraftVersion(MinecraftVersion value) { minecraftVersion = value; return this; }
    public PacketFilterBuilder reporter(ErrorReporter value) { reporter = value == null ? new BasicErrorReporter() : value; return this; }
    public Server getServer() { return server; }
    public Plugin getLibrary() { return library; }
    public MinecraftVersion getMinecraftVersion() { return minecraftVersion; }
    public ErrorReporter getReporter() { return reporter; }
    public AsyncFilterManager getAsyncManager() { return new AsyncFilterManager(reporter, scheduler); }
    public InternalManager build() { return new PacketFilterManager(this); }
}
