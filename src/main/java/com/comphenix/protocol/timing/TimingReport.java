package com.comphenix.protocol.timing;

import com.comphenix.protocol.PacketType;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;
import java.util.TreeSet;

/** Immutable timing snapshot that can be written as a human-readable report. */
public class TimingReport {
    private final Date start;
    private final Date end;
    private final ImmutableMap<String, ImmutableMap<TimingListenerType, PluginTimingTracker>> data;

    public TimingReport(Date start, Date end,
                        ImmutableMap<String, ImmutableMap<TimingListenerType, PluginTimingTracker>> data) {
        this.start = start == null ? new Date() : new Date(start.getTime());
        this.end = end == null ? new Date() : new Date(end.getTime());
        this.data = data == null ? ImmutableMap.of() : data;
    }

    public void saveTo(Path path) throws IOException {
        if (path == null) throw new IllegalArgumentException("path cannot be null");
        StringBuilder output = new StringBuilder();
        output.append("Started: ").append(start).append('\n');
        output.append("Stopped: ").append(end).append(" (after ")
                .append(Math.abs(end.getTime() - start.getTime()) / 1000L).append(" seconds)\n\n");
        for (Map.Entry<String, ImmutableMap<TimingListenerType, PluginTimingTracker>> plugin : data.entrySet()) {
            output.append("=== PLUGIN ").append(plugin.getKey()).append(" ===\n");
            for (Map.Entry<TimingListenerType, PluginTimingTracker> type : plugin.getValue().entrySet()) {
                if (!type.getValue().hasReceivedData()) continue;
                output.append(" TYPE: ").append(type.getKey()).append("\n");
                output.append(" Protocol:      Name:                         Count:       Min (ms):       Max (ms):       Mean (ms):      Std (ms):\n");
                for (PacketType packet : new TreeSet<>(type.getValue().getStatistics().keySet())) {
                    StatisticsStream stats = type.getValue().getStatistics().get(packet);
                    if (stats != null && stats.getCount() > 0) appendStats(output, packet, stats);
                }
                output.append('\n');
            }
        }
        Files.writeString(path, output.toString());
    }

    private static void appendStats(StringBuilder output, PacketType packet, StatisticsStream stats) {
        output.append(String.format(" %-14s %-29s %-12d %-15.6f %-15.6f %-15.6f %.6f%n",
                packet.getProtocol(), packet.name(), stats.getCount(), nanosToMillis(stats.getMinimum()),
                nanosToMillis(stats.getMaximum()), nanosToMillis(stats.getMean()),
                nanosToMillis(stats.getStandardDeviation())));
    }

    private static double nanosToMillis(double value) { return value / 1_000_000.0; }
}
