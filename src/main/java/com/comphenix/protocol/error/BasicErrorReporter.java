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
package com.comphenix.protocol.error;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BasicErrorReporter implements ErrorReporter {

    private final Logger logger;

    public BasicErrorReporter(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void reportMinimal(Object sender, String methodName, Throwable error) {
        logger.log(Level.SEVERE, "[" + describe(sender) + "] Error in " + methodName + ": " + error, error);
    }

    @Override
    public void reportWarning(Object sender, String message) {
        logger.log(Level.WARNING, "[" + describe(sender) + "] " + message);
    }

    @Override
    public void reportWarning(Object sender, String message, Throwable error) {
        logger.log(Level.WARNING, "[" + describe(sender) + "] " + message, error);
    }

    @Override
    public void reportDetailed(Object sender, String message, Throwable error) {
        logger.log(Level.SEVERE, "[" + describe(sender) + "] " + message, error);
    }

    private static String describe(Object sender) {
        return sender == null ? "unknown" : sender.getClass().getSimpleName();
    }
}
