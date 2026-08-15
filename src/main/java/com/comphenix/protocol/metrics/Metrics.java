package com.comphenix.protocol.metrics;

import com.comphenix.protocol.ProtocolLib;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/** Local bStats-compatible data model. P2P does not upload telemetry itself. */
public class Metrics {
    public static final int B_STATS_VERSION = 1;
    private final ProtocolLib plugin;
    private final List<CustomChart> charts = new ArrayList<>();

    public Metrics(ProtocolLib plugin) { this.plugin = plugin; }
    public void addCustomChart(CustomChart chart) { if (chart != null) charts.add(chart); }

    @SuppressWarnings("unchecked")
    public JSONObject getPluginData() {
        JSONObject result = new JSONObject();
        result.put("pluginVersion", plugin == null ? "unknown" : plugin.getDescription().getVersion());
        List<JSONObject> chartData = new ArrayList<>();
        for (CustomChart chart : charts) {
            try {
                JSONObject entry = new JSONObject();
                entry.put("chartId", chart.name);
                entry.put("data", chart.getChartData());
                chartData.add(entry);
            } catch (Exception ignored) {
                // A failing optional chart must not break the complete metrics payload.
            }
        }
        result.put("customCharts", chartData);
        return result;
    }

    public abstract static class CustomChart {
        final String name;
        final Callable<?> callable;
        CustomChart(String name, Callable<?> callable) {
            if (name == null || name.isEmpty()) throw new IllegalArgumentException("name cannot be empty");
            this.name = name;
            this.callable = callable;
        }
        protected abstract JSONObject getChartData() throws Exception;
        protected JSONObject values(Object value) {
            if (value == null) return null;
            JSONObject result = new JSONObject();
            result.put("value", value);
            return result;
        }
    }

    public static class SimplePie extends CustomChart {
        public SimplePie(String name, Callable<String> callable) { super(name, callable); }
        @Override protected JSONObject getChartData() throws Exception { return values(callable.call()); }
    }

    public static class DrilldownPie extends CustomChart {
        public DrilldownPie(String name, Callable<Map<String, Map<String, Integer>>> callable) { super(name, callable); }
        @Override public JSONObject getChartData() throws Exception {
            Map<String, Map<String, Integer>> source = castMap(callable.call());
            if (source == null || source.isEmpty()) return null;
            JSONObject values = new JSONObject();
            for (Map.Entry<String, Map<String, Integer>> entry : source.entrySet()) {
                JSONObject nested = new JSONObject();
                if (entry.getValue() != null) nested.putAll(entry.getValue());
                values.put(entry.getKey(), nested);
            }
            JSONObject result = new JSONObject();
            result.put("values", values);
            return result;
        }
    }

    public static class AdvancedPie extends CustomChart {
        public AdvancedPie(String name, Callable<Map<String, Integer>> callable) { super(name, callable); }
        @Override protected JSONObject getChartData() throws Exception { return mapData(callable.call()); }
    }

    public static class SimpleBarChart extends CustomChart {
        public SimpleBarChart(String name, Callable<Map<String, Integer>> callable) { super(name, callable); }
        @Override protected JSONObject getChartData() throws Exception { return mapData(callable.call()); }
    }

    public static class AdvancedBarChart extends CustomChart {
        public AdvancedBarChart(String name, Callable<Map<String, int[]>> callable) { super(name, callable); }
        @Override protected JSONObject getChartData() throws Exception {
            Map<String, int[]> source = castMap(callable.call());
            if (source == null || source.isEmpty()) return null;
            JSONObject values = new JSONObject();
            for (Map.Entry<String, int[]> entry : source.entrySet()) {
                List<Integer> numbers = new ArrayList<>();
                if (entry.getValue() != null) for (int value : entry.getValue()) numbers.add(value);
                values.put(entry.getKey(), numbers);
            }
            JSONObject result = new JSONObject(); result.put("values", values); return result;
        }
    }

    public static class MultiLineChart extends CustomChart {
        public MultiLineChart(String name, Callable<Map<String, Integer>> callable) { super(name, callable); }
        @Override protected JSONObject getChartData() throws Exception { return mapData(callable.call()); }
    }

    public static class SingleLineChart extends CustomChart {
        public SingleLineChart(String name, Callable<Integer> callable) { super(name, callable); }
        @Override protected JSONObject getChartData() throws Exception { return values(callable.call()); }
    }

    @SuppressWarnings("unchecked")
    private static JSONObject mapData(Object source) {
        if (!(source instanceof Map<?, ?> map) || map.isEmpty()) return null;
        JSONObject values = new JSONObject();
        for (Map.Entry<?, ?> entry : map.entrySet()) values.put(String.valueOf(entry.getKey()), entry.getValue());
        JSONObject result = new JSONObject(); result.put("values", values); return result;
    }

    @SuppressWarnings("unchecked")
    private static <K, V> Map<K, V> castMap(Object value) {
        return value instanceof Map<?, ?> ? (Map<K, V>) value : null;
    }
}
