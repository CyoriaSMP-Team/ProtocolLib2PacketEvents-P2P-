/*
 * ProtocolLib2PacketEvents - clean-room error reporter.
 */
package com.comphenix.protocol.error;

import org.bukkit.plugin.Plugin;

/** Reporter useful in tests: every warning/error becomes an exception. */
public class RethrowErrorReporter implements ErrorReporter {
    public RethrowErrorReporter() { }
    public void reportMinimal(Plugin sender, String methodName, Throwable error) { reportMinimal((Object) sender, methodName, error); }
    public void reportMinimal(Plugin sender, String methodName, Throwable error, Object... parameters) { reportMinimal((Object) sender, methodName + java.util.Arrays.toString(parameters), error); }
    @Override public void reportMinimal(Object sender, String methodName, Throwable error) {
        throw new RuntimeException("Minimal error by " + sender + " in " + methodName, error);
    }
    @Override public void reportWarning(Object sender, String message) {
        throw new RuntimeException("Warning by " + sender + ": " + message);
    }
    @Override public void reportWarning(Object sender, String message, Throwable error) {
        throw new RuntimeException("Warning by " + sender + ": " + message, error);
    }
    @Override public void reportDetailed(Object sender, String message, Throwable error) {
        throw new RuntimeException("Detailed error by " + sender + ": " + message, error);
    }
    @Override public void reportDebug(Object sender, Report report) {
        // Debug reports are intentionally ignored by this test reporter.
    }
    public void reportDebug(Object sender, Report.ReportBuilder builder) { reportDebug(sender, builder == null ? null : builder.build()); }
    @Override public void reportWarning(Object sender, Report report) {
        throw new RuntimeException("Warning by " + sender + ": " + report);
    }
    public void reportWarning(Object sender, Report.ReportBuilder builder) { reportWarning(sender, builder == null ? null : builder.build()); }
    @Override public void reportDetailed(Object sender, Report report) {
        throw new RuntimeException("Detailed error by " + sender + ": " + report,
                report == null ? null : report.getException());
    }
    public void reportDetailed(Object sender, Report.ReportBuilder builder) { reportDetailed(sender, builder == null ? null : builder.build()); }
}
