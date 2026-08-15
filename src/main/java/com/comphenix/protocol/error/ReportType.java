package com.comphenix.protocol.error;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/** Strongly typed report description. */
public class ReportType {
    private final String errorFormat;
    protected String reportName;

    public ReportType(String errorFormat) {
        if (errorFormat == null) throw new IllegalArgumentException("errorFormat cannot be null");
        this.errorFormat = errorFormat;
    }

    public String getMessage(Object[] parameters) {
        return parameters == null || parameters.length == 0 ? toString() : String.format(errorFormat, parameters);
    }

    @Override public String toString() { return errorFormat; }

    public static Class<?> getSenderClass(Object sender) {
        if (sender == null) throw new IllegalArgumentException("sender cannot be null");
        return sender instanceof Class<?> clazz ? clazz : sender.getClass();
    }

    public static String getReportName(Object sender, ReportType type) {
        if (type == null) throw new IllegalArgumentException("type cannot be null");
        Class<?> senderClass = getSenderClass(sender);
        if (type.reportName != null) return type.reportName;
        for (Class<?> current = senderClass; current != null; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) && field.getType() == ReportType.class) {
                    try {
                        field.setAccessible(true);
                        if (field.get(null) == type) {
                            return type.reportName = current.getCanonicalName() + "#" + field.getName();
                        }
                    } catch (ReflectiveOperationException ignored) { }
                }
            }
        }
        return type.reportName = type.toString();
    }

    public static ReportType[] getReports(Class<?> sender) {
        if (sender == null) throw new IllegalArgumentException("sender cannot be null");
        List<ReportType> reports = new ArrayList<>();
        for (Class<?> current = sender; current != null; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) || field.getType() != ReportType.class) continue;
                try {
                    field.setAccessible(true);
                    reports.add((ReportType) field.get(null));
                } catch (ReflectiveOperationException ignored) { }
            }
        }
        return reports.toArray(new ReportType[0]);
    }
}
