package com.forgetools.protection;

import com.forgetools.ForgeToolsPlugin;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ProtectionManager {

    private final ForgeToolsPlugin plugin;
    private boolean worldGuardEnabled;
    private boolean griefPreventionEnabled;
    private boolean townyEnabled;

    private WorldGuardHook worldGuardHook;
    private GriefPreventionHook griefPreventionHook;
    private TownyHook townyHook;

    public ProtectionManager(ForgeToolsPlugin plugin) {
        this.plugin = plugin;
        init();
    }

    private void init() {
        if (plugin.getConfig().getBoolean("settings.check_worldguard", true)) {
            Plugin wg = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
            if (wg != null && wg.isEnabled()) {
                try {
                    worldGuardHook = new WorldGuardHook();
                    worldGuardEnabled = true;
                    plugin.getLogger().info("WorldGuard protection hooked.");
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to hook WorldGuard: " + e.getMessage());
                }
            }
        }
        if (plugin.getConfig().getBoolean("settings.check_griefprevention", true)) {
            Plugin gp = plugin.getServer().getPluginManager().getPlugin("GriefPrevention");
            if (gp != null && gp.isEnabled()) {
                try {
                    griefPreventionHook = new GriefPreventionHook();
                    griefPreventionEnabled = true;
                    plugin.getLogger().info("GriefPrevention protection hooked.");
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to hook GriefPrevention: " + e.getMessage());
                }
            }
        }
        if (plugin.getConfig().getBoolean("settings.check_towny", true)) {
            Plugin towny = plugin.getServer().getPluginManager().getPlugin("Towny");
            if (towny != null && towny.isEnabled()) {
                try {
                    townyHook = new TownyHook();
                    townyEnabled = true;
                    plugin.getLogger().info("Towny protection hooked.");
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to hook Towny: " + e.getMessage());
                }
            }
        }
    }

    public boolean canBuild(Player player, Block block) {
        if (player.isOp()) return true;
        Location loc = block.getLocation();
        if (worldGuardEnabled && worldGuardHook != null) {
            if (!worldGuardHook.canBuild(player, loc)) return false;
        }
        if (griefPreventionEnabled && griefPreventionHook != null) {
            if (!griefPreventionHook.canBuild(player, loc)) return false;
        }
        if (townyEnabled && townyHook != null) {
            if (!townyHook.canBuild(player, loc)) return false;
        }
        return true;
    }

    public boolean isWorldGuardEnabled() { return worldGuardEnabled; }
    public boolean isGriefPreventionEnabled() { return griefPreventionEnabled; }
    public boolean isTownyEnabled() { return townyEnabled; }
}
