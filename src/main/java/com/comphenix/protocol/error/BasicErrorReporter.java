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
import java.io.PrintStream;
import org.bukkit.plugin.Plugin;
import com.comphenix.protocol.error.Report.ReportBuilder;

public class BasicErrorReporter implements ErrorReporter {

    private final Logger logger;
    private final PrintStream output;

    public BasicErrorReporter() {
        this(Logger.getLogger("ProtocolLib2PacketEvents"), null);
    }

    public BasicErrorReporter(Logger logger) {
        this(logger, null);
    }

    public BasicErrorReporter(PrintStream output) {
        this(Logger.getLogger("ProtocolLib2PacketEvents"), output);
    }

    private BasicErrorReporter(Logger logger, PrintStream output) {
        this.logger = logger == null ? Logger.getLogger("ProtocolLib2PacketEvents") : logger;
        this.output = output;
    }

    @Override
    public void reportMinimal(Object sender, String methodName, Throwable error) {
        if (output != null) { output.println("[" + describe(sender) + "] Error in " + methodName + ": " + error); if (error != null) error.printStackTrace(output); return; }
        logger.log(Level.SEVERE, "[" + describe(sender) + "] Error in " + methodName + ": " + error, error);
    }

    @Override
    public void reportWarning(Object sender, String message) {
        if (output != null) { output.println("[" + describe(sender) + "] " + message); return; }
        logger.log(Level.WARNING, "[" + describe(sender) + "] " + message);
    }

    @Override
    public void reportWarning(Object sender, String message, Throwable error) {
        if (output != null) { output.println("[" + describe(sender) + "] " + message); if (error != null) error.printStackTrace(output); return; }
        logger.log(Level.WARNING, "[" + describe(sender) + "] " + message, error);
    }

    @Override
    public void reportDetailed(Object sender, String message, Throwable error) {
        if (output != null) { output.println("[" + describe(sender) + "] " + message); if (error != null) error.printStackTrace(output); return; }
        logger.log(Level.SEVERE, "[" + describe(sender) + "] " + message, error);
    }

    public void reportMinimal(Plugin sender, String methodName, Throwable error) { ErrorReporter.super.reportMinimal(sender, methodName, error); }
    public void reportMinimal(Plugin sender, String methodName, Throwable error, Object... parameters) { ErrorReporter.super.reportMinimal(sender, methodName, error, parameters); }
    public void reportDebug(Object sender, Report report) { ErrorReporter.super.reportDebug(sender, report); }
    public void reportDebug(Object sender, ReportBuilder builder) { ErrorReporter.super.reportDebug(sender, builder); }
    public void reportWarning(Object sender, Report report) { ErrorReporter.super.reportWarning(sender, report); }
    public void reportWarning(Object sender, ReportBuilder builder) { ErrorReporter.super.reportWarning(sender, builder); }
    public void reportDetailed(Object sender, Report report) { ErrorReporter.super.reportDetailed(sender, report); }
    public void reportDetailed(Object sender, ReportBuilder builder) { ErrorReporter.super.reportDetailed(sender, builder); }

    private static String describe(Object sender) {
        return sender == null ? "unknown" : sender.getClass().getSimpleName();
    }
}
