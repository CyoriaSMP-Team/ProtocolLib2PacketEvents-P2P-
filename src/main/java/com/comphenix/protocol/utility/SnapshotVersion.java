package com.comphenix.protocol.utility;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses the YYYY-wWW[-suffix] snapshot spelling used by Minecraft. */
public class SnapshotVersion implements Comparable<SnapshotVersion>, Serializable {
    private static final Pattern PATTERN = Pattern.compile("(\\d{4})[.-]?[wW](\\d{1,2})(?:[-.]?(.*))?");
    private final String value;
    private final int week;
    private final Date date;

    public SnapshotVersion(String value) {
        this.value = Objects.requireNonNull(value, "value");
        Matcher matcher = PATTERN.matcher(value);
        Date parsedDate;
        if (!matcher.matches()) { week = -1; parsedDate = new Date(0); }
        else {
            week = Integer.parseInt(matcher.group(2));
            try {
                parsedDate = java.sql.Date.valueOf(LocalDate.ofYearDay(Integer.parseInt(matcher.group(1)), 1)
                        .plusWeeks(Math.max(0, week - 1)));
            } catch (RuntimeException ignored) { parsedDate = new Date(0); }
        }
        date = parsedDate;
    }
    public int getSnapshotWeekVersion() { return week; }
    public Date getSnapshotDate() { return new Date(date.getTime()); }
    public String getSnapshotString() { return value; }
    @Override public int compareTo(SnapshotVersion other) { return value.compareTo(other.value); }
    @Override public boolean equals(Object other) { return other instanceof SnapshotVersion v && value.equals(v.value); }
    @Override public int hashCode() { return value.hashCode(); }
    @Override public String toString() { return value; }
}
