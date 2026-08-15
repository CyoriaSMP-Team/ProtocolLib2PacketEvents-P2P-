/*
 * ProtocolLib2PacketEvents - clean-room error reporter.
 */
package com.comphenix.protocol.error;

import org.bukkit.plugin.Plugin;
/** Decorator that lets subclasses filter reports before forwarding them. */
public class DelegatedErrorReporter implements ErrorReporter {
    private final ErrorReporter delegated;

    public DelegatedErrorReporter(ErrorReporter delegated) {
        if (delegated == null) throw new IllegalArgumentException("delegated cannot be null");
        this.delegated = delegated;
    }

    public ErrorReporter getDelegated() {
        return delegated;
    }

    public void reportMinimal(Plugin sender, String methodName, Throwable error) { delegated.reportMinimal(sender, methodName, error); }
    public void reportMinimal(Plugin sender, String methodName, Throwable error, Object... parameters) { delegated.reportMinimal(sender, methodName, error, parameters); }

    @Override public void reportMinimal(Object sender, String methodName, Throwable error) {
        delegated.reportMinimal(sender, methodName, error);
    }
    @Override public void reportWarning(Object sender, String message) {
        delegated.reportWarning(sender, message);
    }
    @Override public void reportWarning(Object sender, String message, Throwable error) {
        delegated.reportWarning(sender, message, error);
    }
    @Override public void reportDetailed(Object sender, String message, Throwable error) {
        delegated.reportDetailed(sender, message, error);
    }
    @Override public void reportDebug(Object sender, Report report) {
        Report result = filterReport(sender, report, false);
        if (result != null) delegated.reportDebug(sender, result);
    }
    @Override public void reportWarning(Object sender, Report report) {
        Report result = filterReport(sender, report, false);
        if (result != null) delegated.reportWarning(sender, result);
    }
    @Override public void reportDetailed(Object sender, Report report) {
        Report result = filterReport(sender, report, true);
        if (result != null) delegated.reportDetailed(sender, result);
    }
    @Override public void reportWarning(Object sender, Report.ReportBuilder builder) {
        reportWarning(sender, builder == null ? null : builder.build());
    }
    @Override public void reportDetailed(Object sender, Report.ReportBuilder builder) {
        reportDetailed(sender, builder == null ? null : builder.build());
    }
    @Override public void reportDebug(Object sender, Report.ReportBuilder builder) {
        reportDebug(sender, builder == null ? null : builder.build());
    }

    protected Report filterReport(Object sender, Report report, boolean detailed) {
        return report;
    }
}
