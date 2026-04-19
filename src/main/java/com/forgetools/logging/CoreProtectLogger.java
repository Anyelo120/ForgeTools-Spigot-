package com.forgetools.logging;

import com.forgetools.ForgeToolsPlugin;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class CoreProtectLogger {

    private final ForgeToolsPlugin plugin;
    private CoreProtectAPI api;
    private boolean enabled;

    public CoreProtectLogger(ForgeToolsPlugin plugin) {
        this.plugin = plugin;
        init();
    }

    private void init() {
        if (!plugin.getConfig().getBoolean("settings.log_to_coreprotect", true)) return;
        Plugin cp = plugin.getServer().getPluginManager().getPlugin("CoreProtect");
        if (cp instanceof CoreProtect coreProtect) {
            CoreProtectAPI coreProtectAPI = coreProtect.getAPI();
            if (coreProtectAPI.isEnabled() && coreProtectAPI.APIVersion() >= 10) {
                api = coreProtectAPI;
                enabled = true;
                plugin.getLogger().info("CoreProtect logging hooked.");
            }
        }
    }

    public void logRemoval(Player player, Block block) {
        if (!enabled || api == null) return;
        api.logRemoval(player.getName(), block.getLocation(), block.getType(), block.getBlockData());
    }

    public void logPlacement(Player player, Block block) {
        if (!enabled || api == null) return;
        api.logPlacement(player.getName(), block.getLocation(), block.getType(), block.getBlockData());
    }

    public boolean isEnabled() { return enabled; }
}
