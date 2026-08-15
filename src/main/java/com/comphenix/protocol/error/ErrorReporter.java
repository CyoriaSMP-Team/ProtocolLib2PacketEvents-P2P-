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

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.error.Report.ReportBuilder;

public interface ErrorReporter {

    void reportMinimal(Object sender, String methodName, Throwable error);

    void reportWarning(Object sender, String message);

    void reportWarning(Object sender, String message, Throwable error);

    void reportDetailed(Object sender, String message, Throwable error);

    default void reportMinimal(Plugin sender, String methodName, Throwable error) {
        reportMinimal((Object) sender, methodName, error);
    }

    default void reportMinimal(Plugin sender, String methodName, Throwable error, Object... parameters) {
        reportMinimal((Object) sender, methodName + formatParameters(parameters), error);
    }

    default void reportDebug(Object sender, Report report) {
        if (report != null) reportWarning(sender, report.getReportMessage(), report.getException());
    }

    default void reportDebug(Object sender, ReportBuilder builder) {
        reportDebug(sender, builder == null ? null : builder.build());
    }

    default void reportWarning(Object sender, Report report) {
        if (report != null) reportWarning(sender, report.getReportMessage(), report.getException());
    }

    default void reportWarning(Object sender, ReportBuilder builder) {
        reportWarning(sender, builder == null ? null : builder.build());
    }

    default void reportDetailed(Object sender, Report report) {
        if (report != null) reportDetailed(sender, report.getReportMessage(), report.getException());
    }

    default void reportDetailed(Object sender, ReportBuilder builder) {
        reportDetailed(sender, builder == null ? null : builder.build());
    }

    private static String formatParameters(Object[] parameters) {
        return parameters == null || parameters.length == 0 ? "" : " " + java.util.Arrays.toString(parameters);
    }
}
