package com.forgetools;

import com.forgetools.command.ForgeToolsCommand;
import com.forgetools.energy.EnergyManager;
import com.forgetools.lang.LangManager;
import com.forgetools.listener.EnergyChargeListener;
import com.forgetools.listener.ToolBreakListener;
import com.forgetools.listener.ToolInteractListener;
import com.forgetools.listener.ToolMoveListener;
import com.forgetools.logging.CoreProtectLogger;
import com.forgetools.protection.ProtectionManager;
import com.forgetools.registry.ToolRegistry;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class ForgeToolsPlugin extends JavaPlugin {

    private static ForgeToolsPlugin instance;

    private LangManager langManager;
    private ToolRegistry toolRegistry;
    private EnergyManager energyManager;
    private ProtectionManager protectionManager;
    private CoreProtectLogger coreProtectLogger;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        saveResourceIfAbsent("tools.yml");
        saveResourceIfAbsent("lang/es.yml");

        Keys.init(this);

        langManager = new LangManager(this);
        langManager.load();

        protectionManager = new ProtectionManager(this);
        coreProtectLogger = new CoreProtectLogger(this);

        toolRegistry = new ToolRegistry(this);
        toolRegistry.load();

        energyManager = new EnergyManager(this, toolRegistry);
        energyManager.start();

        getServer().getPluginManager().registerEvents(
                new ToolInteractListener(this, toolRegistry, protectionManager, coreProtectLogger), this);
        getServer().getPluginManager().registerEvents(
                new ToolBreakListener(this, toolRegistry, protectionManager, coreProtectLogger), this);
        getServer().getPluginManager().registerEvents(
                new ToolMoveListener(this, toolRegistry, protectionManager, coreProtectLogger), this);
        getServer().getPluginManager().registerEvents(
                new EnergyChargeListener(this, toolRegistry), this);
        getServer().getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler
            public void onQuit(org.bukkit.event.player.PlayerQuitEvent e) {
                langManager.clearThrottle(e.getPlayer().getUniqueId());
            }
        }, this);

        ForgeToolsCommand cmd = new ForgeToolsCommand(this, toolRegistry);
        Objects.requireNonNull(getCommand("forgetools")).setExecutor(cmd);
        Objects.requireNonNull(getCommand("forgetools")).setTabCompleter(cmd);

        getLogger().info("ForgeTools v" + getDescription().getVersion() + " enabled — "
                + toolRegistry.getToolCount() + " tools loaded [lang=" + langManager.getLoadedLang() + "]");
    }

    @Override
    public void onDisable() {
        if (energyManager != null) energyManager.stop();
        getLogger().info("ForgeTools disabled.");
    }

    public void fullReload() {
        reloadConfig();
        langManager.load();
        if (energyManager != null) energyManager.stop();
        toolRegistry.load();
        energyManager = new EnergyManager(this, toolRegistry);
        energyManager.start();
        // Re-register EnergyManager as listener is handled internally in start()
    }

    private void saveResourceIfAbsent(String path) {
        java.io.File f = new java.io.File(getDataFolder(), path);
        if (!f.exists()) saveResource(path, false);
    }

    public static ForgeToolsPlugin getInstance()         { return instance; }
    public LangManager getLangManager()                  { return langManager; }
    public ToolRegistry getToolRegistry()                { return toolRegistry; }
    public EnergyManager getEnergyManager()              { return energyManager; }
    public ProtectionManager getProtectionManager()      { return protectionManager; }
    public CoreProtectLogger getCoreProtectLogger()      { return coreProtectLogger; }
}
