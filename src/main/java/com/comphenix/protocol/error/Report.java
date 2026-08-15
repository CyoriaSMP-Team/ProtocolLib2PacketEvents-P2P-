package com.comphenix.protocol.error;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/** Immutable report object with the upstream builder contract. */
public class Report {
    private final ReportType type;
    private final Throwable exception;
    private final Object[] messageParameters;
    private final Object[] callerParameters;
    private final long rateLimit;

    public static class ReportBuilder {
        private ReportType type;
        private Throwable exception;
        private Object[] messageParameters;
        private Object[] callerParameters;
        private long rateLimit;

        public ReportBuilder type(ReportType type) { if (type == null) throw new IllegalArgumentException("type cannot be null"); this.type = type; return this; }
        public ReportBuilder error(Throwable exception) { this.exception = exception; return this; }
        public ReportBuilder messageParam(Object... parameters) { this.messageParameters = parameters; return this; }
        public ReportBuilder callerParam(Object... parameters) { this.callerParameters = parameters; return this; }
        public ReportBuilder rateLimit(long nanos) { if (nanos < 0) throw new IllegalArgumentException("rateLimit cannot be negative"); this.rateLimit = nanos; return this; }
        public ReportBuilder rateLimit(long amount, TimeUnit unit) { return rateLimit(TimeUnit.NANOSECONDS.convert(amount, unit)); }
        public Report build() { return new Report(type, exception, messageParameters, callerParameters, rateLimit); }
    }

    public static ReportBuilder newBuilder(ReportType type) { return new ReportBuilder().type(type); }

    protected Report(ReportType type, Throwable exception, Object[] messageParameters, Object[] callerParameters) {
        this(type, exception, messageParameters, callerParameters, 0);
    }

    protected Report(ReportType type, Throwable exception, Object[] messageParameters,
                     Object[] callerParameters, long rateLimit) {
        if (type == null) throw new IllegalArgumentException("type cannot be null");
        this.type = type;
        this.exception = exception;
        this.messageParameters = messageParameters == null ? null : messageParameters.clone();
        this.callerParameters = callerParameters == null ? null : callerParameters.clone();
        this.rateLimit = rateLimit;
    }

    public String getReportMessage() { return type.getMessage(messageParameters); }
    public Object[] getMessageParameters() { return messageParameters == null ? null : messageParameters.clone(); }
    public Object[] getCallerParameters() { return callerParameters == null ? null : callerParameters.clone(); }
    public ReportType getType() { return type; }
    public Throwable getException() { return exception; }
    public boolean hasMessageParameters() { return messageParameters != null && messageParameters.length > 0; }
    public boolean hasCallerParameters() { return callerParameters != null && callerParameters.length > 0; }
    public long getRateLimit() { return rateLimit; }

    @Override public int hashCode() { return 31 * type.hashCode() + Arrays.hashCode(messageParameters) + Arrays.hashCode(callerParameters); }
    @Override public boolean equals(Object object) {
        if (!(object instanceof Report other)) return false;
        return type == other.type && Arrays.equals(messageParameters, other.messageParameters)
                && Arrays.equals(callerParameters, other.callerParameters);
    }
}
