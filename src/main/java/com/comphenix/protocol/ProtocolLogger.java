/*
 * ProtocolLib2PacketEvents - clean-room logging facade.
 */
package com.comphenix.protocol;

import com.comphenix.protocol.utility.MinecraftVersion;

import java.util.logging.Level;
import java.util.logging.Logger;

/** Shared logger retained for plugins that use ProtocolLib's static logging helper. */
public final class ProtocolLogger {
    private static volatile Logger logger = Logger.getLogger("ProtocolLib2PacketEvents");
    private static volatile boolean debugEnabled;

    public ProtocolLogger() {
    }

    public static void init(ProtocolLib plugin) {
        if (plugin != null) {
            logger = plugin.getLogger();
            debugEnabled = plugin.getConfig().getBoolean("global.debug", false);
        }
    }

    public static void log(Level level, String message, Object... args) {
        logger.log(level, format(message, args));
    }

    public static void log(String message, Object... args) {
        log(Level.INFO, message, args);
    }

    public static void log(Level level, String message, Throwable error) {
        logger.log(level, message, error);
    }

    public static void debug(String message, Object... args) {
        if (debugEnabled) {
            log(Level.INFO, "[debug] " + message, args);
        }
    }

    public static void debug(String message, Throwable error) {
        if (debugEnabled) {
            logger.log(Level.INFO, "[debug] " + message, error);
        }
    }

    public static void warnAbove(MinecraftVersion version, String message, Object... args) {
        if (version == null || MinecraftVersion.current().compareTo(version) > 0) {
            log(Level.WARNING, message, args);
        }
    }

    private static String format(String message, Object[] args) {
        if (args == null || args.length == 0) {
            return message;
        }
        try {
            return String.format(message, args);
        } catch (RuntimeException ignored) {
            return message + " " + java.util.Arrays.toString(args);
        }
    }
}
