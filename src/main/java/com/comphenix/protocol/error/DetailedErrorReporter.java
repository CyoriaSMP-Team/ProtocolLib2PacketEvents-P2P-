package com.comphenix.protocol.error;

import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Full reporter facade with global parameters and bounded error accounting. */
public class DetailedErrorReporter implements ErrorReporter {
    public static final ReportType REPORT_EXCEPTION_COUNT = new ReportType("Exception count: %s");
    public static final String SECOND_LEVEL_PREFIX = "[ProtocolLib] ";
    public static final String DEFAULT_PREFIX = "[ProtocolLib] ";
    public static final String DEFAULT_SUPPORT_URL = "https://github.com/dmulloy2/ProtocolLib/issues";
    public static final String ERROR_PERMISSION = "protocollib.errors";
    public static final int DEFAULT_MAX_ERROR_COUNT = 10;

    private final Map<String, Object> globals = new ConcurrentHashMap<>();
    private volatile boolean detailedReporting = true;
    private volatile int errorCount;
    private volatile int maxErrorCount = DEFAULT_MAX_ERROR_COUNT;
    private volatile String supportUrl = DEFAULT_SUPPORT_URL;
    private volatile String prefix = DEFAULT_PREFIX;
    private volatile Logger logger;

    public DetailedErrorReporter(Plugin plugin) { this(plugin, DEFAULT_PREFIX, DEFAULT_SUPPORT_URL, DEFAULT_MAX_ERROR_COUNT, plugin == null ? Logger.getLogger("ProtocolLib") : plugin.getLogger()); }
    public DetailedErrorReporter(Plugin plugin, String prefix, String supportUrl) { this(plugin, prefix, supportUrl, DEFAULT_MAX_ERROR_COUNT, plugin == null ? Logger.getLogger("ProtocolLib") : plugin.getLogger()); }
    public DetailedErrorReporter(Plugin plugin, String prefix, String supportUrl, int maxErrors, Logger logger) {
        this.prefix = prefix == null ? DEFAULT_PREFIX : prefix;
        this.supportUrl = supportUrl == null ? DEFAULT_SUPPORT_URL : supportUrl;
        this.maxErrorCount = Math.max(0, maxErrors);
        this.logger = logger == null ? Logger.getLogger("ProtocolLib") : logger;
    }
    public boolean isDetailedReporting() { return detailedReporting; }
    public void setDetailedReporting(boolean value) { detailedReporting = value; }
    public void reportMinimal(Plugin sender, String method, Throwable error, Object... params) { reportMinimal((Object) sender, method + java.util.Arrays.toString(params), error); }
    public void reportMinimal(Plugin sender, String method, Throwable error) { reportMinimal((Object) sender, method, error); }
    public boolean reportMinimalNoSpam(Plugin sender, String method, Throwable error) { if (errorCount >= maxErrorCount) return false; reportMinimal(sender, method, error); return true; }
    @Override public void reportMinimal(Object sender, String method, Throwable error) { log(Level.SEVERE, sender, method, error); }
    @Override public void reportWarning(Object sender, String message) { log(Level.WARNING, sender, message, null); }
    @Override public void reportWarning(Object sender, String message, Throwable error) { log(Level.WARNING, sender, message, error); }
    @Override public void reportDetailed(Object sender, String message, Throwable error) { if (detailedReporting) log(Level.SEVERE, sender, message, error); }
    public void reportDebug(Object sender, Report.ReportBuilder builder) { if (detailedReporting) reportDebug(sender, builder == null ? null : builder.build()); }
    public void reportDebug(Object sender, Report report) { if (detailedReporting && report != null) log(Level.FINE, sender, report.getReportMessage(), report.getException()); }
    public void reportWarning(Object sender, Report.ReportBuilder builder) { reportWarning(sender, builder == null ? null : builder.build()); }
    public void reportWarning(Object sender, Report report) { if (report != null) reportWarning(sender, report.getReportMessage(), report.getException()); }
    public void reportDetailed(Object sender, Report.ReportBuilder builder) { reportDetailed(sender, builder == null ? null : builder.build()); }
    public void reportDetailed(Object sender, Report report) { if (report != null) reportDetailed(sender, report.getReportMessage(), report.getException()); }
    public static String getStringDescription(Object value) { return String.valueOf(value); }
    public int getErrorCount() { return errorCount; }
    public void setErrorCount(int value) { errorCount = Math.max(0, value); }
    public int getMaxErrorCount() { return maxErrorCount; }
    public void setMaxErrorCount(int value) { maxErrorCount = Math.max(0, value); }
    public void addGlobalParameter(String key, Object value) { if (key != null) globals.put(key, value); }
    public Object getGlobalParameter(String key) { return globals.get(key); }
    public void clearGlobalParameters() { globals.clear(); }
    public Set<String> globalParameters() { return Collections.unmodifiableSet(globals.keySet()); }
    public String getSupportURL() { return supportUrl; }
    public void setSupportURL(String value) { supportUrl = value; }
    public String getPrefix() { return prefix; }
    public void setPrefix(String value) { prefix = value; }
    public Logger getLogger() { return logger; }
    public void setLogger(Logger value) { logger = value == null ? Logger.getLogger("ProtocolLib") : value; }
    private void log(Level level, Object sender, String message, Throwable error) {
        errorCount++;
        String text = prefix + '[' + (sender == null ? "unknown" : sender.getClass().getSimpleName()) + "] " + message;
        if (error == null) logger.log(level, text); else logger.log(level, text, error);
    }
}
