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

/**
 * Static entry point mirroring ProtocolLib's own {@code ProtocolLibrary}. Populated by
 * {@link ProtocolLib#onEnable()} - calling any getter before the plugin has enabled
 * throws {@link IllegalStateException}, exactly like the real ProtocolLib.
 */
public final class ProtocolLibrary {

    private static volatile ProtocolManager protocolManager;
    private static volatile AsynchronousManager asynchronousManager;
    private static volatile ErrorReporter errorReporter;
    private static volatile String version;

    private ProtocolLibrary() {
    }

    static void init(ProtocolManager protocolManager, AsynchronousManager asynchronousManager,
                      ErrorReporter errorReporter, String version) {
        ProtocolLibrary.protocolManager = protocolManager;
        ProtocolLibrary.asynchronousManager = asynchronousManager;
        ProtocolLibrary.errorReporter = errorReporter;
        ProtocolLibrary.version = version;
    }

    /** Clears the statics on plugin disable so a reload cannot hand out stale managers. */
    static void shutdown() {
        protocolManager = null;
        asynchronousManager = null;
        errorReporter = null;
        version = null;
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
