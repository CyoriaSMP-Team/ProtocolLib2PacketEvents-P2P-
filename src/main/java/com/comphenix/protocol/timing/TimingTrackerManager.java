package com.comphenix.protocol.timing;

import com.comphenix.protocol.events.PacketListener;
import com.google.common.collect.ImmutableMap;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/** Thread-safe manager for optional listener timing collection. */
public class TimingTrackerManager {
    private static final AtomicBoolean IS_TRACKING = new AtomicBoolean();
    private static volatile Date startTime;
    private static volatile Date stopTime;
    private static final Map<String, ImmutableMap<TimingListenerType, PluginTimingTracker>> TRACKERS =
            new ConcurrentHashMap<>();

    public TimingTrackerManager() { }

    public static boolean startTracking() {
        if (!IS_TRACKING.compareAndSet(false, true)) return false;
        startTime = Calendar.getInstance().getTime();
        stopTime = null;
        return true;
    }

    public static boolean isTracking() { return IS_TRACKING.get(); }

    public static boolean stopTracking() {
        if (!IS_TRACKING.compareAndSet(true, false)) return false;
        stopTime = Calendar.getInstance().getTime();
        return true;
    }

    public static TimingReport createReportAndReset() {
        Date started = startTime == null ? new Date() : startTime;
        Date stopped = stopTime == null ? new Date() : stopTime;
        TimingReport report = new TimingReport(started, stopped, ImmutableMap.copyOf(TRACKERS));
        TRACKERS.clear();
        startTime = null;
        stopTime = null;
        return report;
    }

    public static TimingTracker get(PacketListener listener, TimingListenerType type) {
        if (!IS_TRACKING.get() || listener == null || type == null) return TimingTracker.EMPTY;
        String plugin = listener.getPlugin() == null ? "unknown" : listener.getPlugin().getName();
        ImmutableMap<TimingListenerType, PluginTimingTracker> byType = TRACKERS.computeIfAbsent(
                plugin, ignored -> newTrackerMap());
        return byType.get(type);
    }

    private static ImmutableMap<TimingListenerType, PluginTimingTracker> newTrackerMap() {
        ImmutableMap.Builder<TimingListenerType, PluginTimingTracker> builder = ImmutableMap.builder();
        for (TimingListenerType type : TimingListenerType.values()) builder.put(type, new PluginTimingTracker());
        return builder.build();
    }
}
