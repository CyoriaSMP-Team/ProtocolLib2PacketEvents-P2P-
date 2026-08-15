/*
 * ProtocolLib2PacketEvents - clean-room plugin context helper.
 */
package com.comphenix.protocol.error;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/** Best-effort attribution of a stack frame to a Bukkit plugin. */
public final class PluginContext {
    private PluginContext() {
    }

    public static String getPluginCaller(Exception error) {
        if (error == null) return null;
        for (StackTraceElement element : error.getStackTrace()) {
            String name = getPluginName(element);
            if (name != null) return name;
        }
        return null;
    }

    public static String getPluginName(StackTraceElement element) {
        if (element == null) return null;
        try {
            if (Bukkit.getPluginManager() == null) return null;
            Class<?> type = Class.forName(element.getClassName());
            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                ClassLoader loader = plugin.getClass().getClassLoader();
                if (loader == type.getClassLoader()) return plugin.getName();
            }
        } catch (Throwable ignored) {
        }
        return null;
    }
}
