package com.comphenix.protocol.injector;

import com.comphenix.protocol.ProtocolManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public interface InternalManager extends ProtocolManager {
    void registerEvents(PluginManager manager, Plugin plugin);
    void close();
    boolean isDebug();
    void setDebug(boolean value);
}
