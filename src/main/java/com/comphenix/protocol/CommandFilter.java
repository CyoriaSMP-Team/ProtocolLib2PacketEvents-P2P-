package com.comphenix.protocol;

import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.plugin.Plugin;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Script-filter command facade with explicit failure handling. */
public class CommandFilter extends CommandBase {
    public static final ReportType REPORT_FALLBACK_ENGINE = new ReportType("Cannot create fallback script engine.");
    public static final ReportType REPORT_CANNOT_LOAD_FALLBACK_ENGINE = new ReportType("Cannot load fallback script engine.");
    public static final ReportType REPORT_PACKAGES_UNSUPPORTED_IN_ENGINE = new ReportType("Script engine does not support packages.");
    public static final ReportType REPORT_FILTER_REMOVED_FOR_ERROR = new ReportType("Filter removed after an error.");
    public static final ReportType REPORT_CANNOT_HANDLE_CONVERSATION = new ReportType("Cannot handle filter conversation.");
    public static final String NAME = "filter";

    private enum SubCommand {
        ADD, REMOVE
    }

    private final Plugin plugin;
    private final ProtocolConfig config;
    private final Map<String, Filter> filters = new ConcurrentHashMap<>();
    private final ScriptEngine engine;

    public CommandFilter(ErrorReporter reporter, Plugin plugin, ProtocolConfig config) {
        super(reporter, PERMISSION_ADMIN, "filter");
        this.plugin = plugin;
        this.config = config;
        this.engine = new ScriptEngineManager(plugin == null ? null : plugin.getClass().getClassLoader())
                .getEngineByName(config == null ? "JavaScript" : config.getScriptEngineName());
    }

    public boolean isInitialized() { return engine != null; }

    public boolean filterEvent(PacketEvent event) { return filterEvent(event, (ignored, filter, error) -> false); }

    public boolean filterEvent(PacketEvent event, FilterFailedHandler handler) {
        if (event == null) return true;
        for (Filter filter : filters.values()) {
            if (!filter.packets.isEmpty() && !filter.packets.contains(event.getPacketType())) continue;
            try {
                if (!filter.evaluate(engine, event)) return false;
            } catch (Exception error) {
                boolean keep = handler != null && handler.handle(event, filter, error);
                if (!keep) filters.remove(filter.name, filter);
                if (!keep) return false;
            }
        }
        return true;
    }

    /**
     * Conversation canceller retained for binary compatibility with the
     * upstream command surface. It accepts the multi-line filter body once
     * the current script engine can parse it.
     */
    private class CompilationSuccessCanceller implements MultipleLinesPrompt.MultipleConversationCanceller {
        @Override
        public boolean cancelBasedOnInput(ConversationContext context, String in) {
            throw new UnsupportedOperationException("Cannot cancel on the last line alone.");
        }

        @Override
        public void setConversation(Conversation conversation) {
            // No conversation state is required by this adapter.
        }

        @Override
        public boolean cancelBasedOnInput(ConversationContext context, String currentLine,
                                          StringBuilder lines, int lineCount) {
            if (engine == null) {
                return false;
            }
            try {
                engine.eval("function(event, packet) {\n" + lines);
                return true;
            } catch (ScriptException error) {
                int realLineCount = lineCount + 1;
                return error.getLineNumber() < realLineCount;
            }
        }

        @Override
        public CompilationSuccessCanceller clone() {
            return new CompilationSuccessCanceller();
        }
    }

    @Override protected boolean handleCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: /filter add|remove <name> [expression]");
            return true;
        }
        String action = args[0].toLowerCase(java.util.Locale.ROOT);
        if (action.equals("remove")) {
            Filter removed = args.length > 1 ? filters.remove(args[1]) : null;
            sender.sendMessage(removed == null ? "Filter not found." : "Filter removed.");
            return true;
        }
        if (!action.equals("add") || args.length < 3) {
            sender.sendMessage("Usage: /filter add <name> <JavaScript expression>");
            return true;
        }
        String expression = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        Filter filter = new Filter(args[1], expression, Set.of());
        try {
            filter.compile(engine);
            filters.put(filter.name, filter);
            sender.sendMessage("Filter added: " + filter.name);
        } catch (ScriptException error) {
            sender.sendMessage("Filter does not compile: " + error.getMessage());
        }
        return true;
    }

    @Override public String getName() { return "filter"; }
    @Override public String getPermission() { return PERMISSION_ADMIN; }

    public interface FilterFailedHandler {
        boolean handle(PacketEvent event, Filter filter, Exception exception);
    }

    public static class Filter {
        private final String name;
        private final String predicate;
        private final Set<PacketType> packets;

        public Filter(String name, String predicate, Set<PacketType> packets) {
            this.name = name;
            this.predicate = predicate == null ? "true" : predicate;
            this.packets = packets == null ? new HashSet<>() : new HashSet<>(packets);
        }

        public String getName() { return name; }
        public String getPredicate() { return predicate; }
        public Set<PacketType> getRanges() { return java.util.Collections.unmodifiableSet(packets); }

        public boolean evaluate(ScriptEngine engine, PacketEvent event) throws ScriptException {
            if (engine == null) throw new ScriptException("No JavaScript engine is available on this JVM");
            engine.put("event", event);
            engine.put("packet", event == null ? null : event.getPacket());
            Object result = engine.eval(predicate);
            return !(result instanceof Boolean) || (Boolean) result;
        }

        public void compile(ScriptEngine engine) throws ScriptException {
            if (engine == null) throw new ScriptException("No JavaScript engine is available on this JVM");
            engine.eval(predicate);
        }

        public void close(ScriptEngine engine) { }
    }
}
