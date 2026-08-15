/*
 * ProtocolLib2PacketEvents (P2P) - a drop-in ProtocolLib compatibility layer
 * powered by PacketEvents.
 *
 * Copyright (C) 2026 CyoriaSMP Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.comphenix.protocol;

import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.scheduler.ProtocolScheduler;
import org.bukkit.plugin.Plugin;

/**
 * Static entry point mirroring ProtocolLib's own {@code ProtocolLibrary}. Populated by
 * {@link ProtocolLib#onEnable()} - calling any getter before the plugin has enabled
 * throws {@link IllegalStateException}, exactly like the real ProtocolLib.
 */
public class ProtocolLibrary {

    public static final String MINIMUM_MINECRAFT_VERSION = "1.8";
    public static final String MAXIMUM_MINECRAFT_VERSION = "26.2";
    public static final String MINECRAFT_LAST_RELEASE_DATE = "2026-06-16";

    private static volatile Plugin plugin;
    private static volatile ProtocolConfig config;
    private static volatile ProtocolManager protocolManager;
    private static volatile ProtocolScheduler scheduler;
    private static volatile AsynchronousManager asynchronousManager;
    private static volatile ErrorReporter errorReporter;
    private static volatile String version;
    private static volatile boolean updatesDisabled;
    private static volatile boolean initialized;

    public ProtocolLibrary() {
    }

    static void init(ProtocolManager protocolManager, AsynchronousManager asynchronousManager,
                      ErrorReporter errorReporter, String version) {
        ProtocolLibrary.protocolManager = protocolManager;
        ProtocolLibrary.asynchronousManager = asynchronousManager;
        ProtocolLibrary.errorReporter = errorReporter;
        ProtocolLibrary.version = version;
    }

    protected static void init(Plugin plugin, ProtocolConfig config, ProtocolManager manager,
                               ProtocolScheduler scheduler, ErrorReporter reporter) {
        if (initialized) {
            throw new IllegalStateException("ProtocolLib has already been initialized.");
        }
        ProtocolLibrary.plugin = plugin;
        ProtocolLibrary.config = config;
        ProtocolLibrary.protocolManager = manager;
        ProtocolLibrary.scheduler = scheduler;
        ProtocolLibrary.errorReporter = reporter;
        initialized = true;
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public static ProtocolConfig getConfig() {
        return config;
    }

    public static ProtocolScheduler getScheduler() {
        return scheduler;
    }

    public static void disableUpdates() {
        updatesDisabled = true;
    }

    public static boolean updatesDisabled() {
        return updatesDisabled;
    }

    static void attachRuntime(AsynchronousManager asynchronousManager, String version) {
        ProtocolLibrary.asynchronousManager = asynchronousManager;
        ProtocolLibrary.version = version;
    }

    /** Clears the statics on plugin disable so a reload cannot hand out stale managers. */
    static void shutdown() {
        initialized = false;
        plugin = null;
        config = null;
        protocolManager = null;
        scheduler = null;
        asynchronousManager = null;
        errorReporter = null;
        version = null;
        updatesDisabled = false;
    }

    public static ProtocolManager getProtocolManager() {
        return require(protocolManager, "ProtocolManager");
    }

    public static AsynchronousManager getAsynchronousManager() {
        return require(asynchronousManager, "AsynchronousManager");
    }

    public static ErrorReporter getErrorReporter() {
        return require(errorReporter, "ErrorReporter");
    }

    public static String getVersion() {
        return require(version, "version");
    }

    private static <T> T require(T value, String name) {
        if (value == null) {
            throw new IllegalStateException("ProtocolLib2PacketEvents has not finished enabling yet (" + name + " unavailable)");
        }
        return value;
    }
}
