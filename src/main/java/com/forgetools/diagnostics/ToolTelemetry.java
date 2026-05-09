package com.forgetools.diagnostics;

import com.forgetools.ForgeToolsPlugin;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

public class ToolTelemetry {

    private final ForgeToolsPlugin plugin;
    private final Map<String, AtomicLong> counters = new ConcurrentHashMap<>();

    public ToolTelemetry(ForgeToolsPlugin plugin) {
        this.plugin = plugin;
    }

    public void activation(String toolId, String context) {
        increment(toolId + ".activation");
        debug(toolId, "activation", context);
    }

    public void rejection(String toolId, String reason, String context) {
        increment(toolId + ".reject." + reason);
        debug(toolId, "reject:" + reason, context);
    }

    public void error(String toolId, Throwable error, String context) {
        increment(toolId + ".error");
        if (plugin.getConfig().getBoolean("debug.tools", false)) {
            plugin.getLogger().log(Level.WARNING,
                    "[tools] " + toolId + " error (" + context + "): " + error.getMessage(), error);
        }
    }

    public Map<String, AtomicLong> snapshot() {
        return Collections.unmodifiableMap(counters);
    }

    private void increment(String key) {
        counters.computeIfAbsent(key, k -> new AtomicLong()).incrementAndGet();
    }

    private void debug(String toolId, String type, String context) {
        if (!plugin.getConfig().getBoolean("debug.tools", false)) return;
        plugin.getLogger().info("[tools] " + toolId + " " + type + " (" + context + ")");
    }
}

