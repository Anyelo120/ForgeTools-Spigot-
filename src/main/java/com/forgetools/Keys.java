package com.forgetools;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public final class Keys {

    public static NamespacedKey TOOL_ID;
    public static NamespacedKey ENERGY;
    public static NamespacedKey MODE;
    public static NamespacedKey USES_REMAINING;

    private Keys() {}

    public static void init(Plugin plugin) {
        TOOL_ID = new NamespacedKey(plugin, "tool_id");
        ENERGY = new NamespacedKey(plugin, "energy");
        MODE = new NamespacedKey(plugin, "mode");
        USES_REMAINING = new NamespacedKey(plugin, "uses_remaining");
    }
}
