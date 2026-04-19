package com.forgetools.listener;

import com.forgetools.ForgeToolsPlugin;
import com.forgetools.api.CustomTool;
import com.forgetools.logging.CoreProtectLogger;
import com.forgetools.protection.ProtectionManager;
import com.forgetools.registry.ToolRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

public class ToolMoveListener implements Listener {

    private final ForgeToolsPlugin plugin;
    private final ToolRegistry registry;
    private final ProtectionManager protection;
    private final CoreProtectLogger logger;

    public ToolMoveListener(ForgeToolsPlugin plugin, ToolRegistry registry,
                            ProtectionManager protection, CoreProtectLogger logger) {
        this.plugin = plugin;
        this.registry = registry;
        this.protection = protection;
        this.logger = logger;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!event.hasChangedBlock()) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!registry.isToolItem(item)) return;

        CustomTool tool = registry.getToolFromItem(item);
        if (tool == null || !tool.handlesMove()) return;

        tool.onMove(event, player, item);
        player.getInventory().setItemInMainHand(item);
    }
}
