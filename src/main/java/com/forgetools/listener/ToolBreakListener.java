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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class ToolBreakListener implements Listener {

    private final ForgeToolsPlugin plugin;
    private final ToolRegistry registry;
    
    public ToolBreakListener(ForgeToolsPlugin plugin, ToolRegistry registry,
                             ProtectionManager protection, CoreProtectLogger logger) {
        this.plugin = plugin;
        this.registry = registry;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!registry.isToolItem(item)) return;

        CustomTool tool = registry.getToolFromItem(item);

        if (tool == null || !tool.handlesBlockBreak()) {
            event.setCancelled(true);
            return;
        }

        if (event.isCancelled()) {
            plugin.getToolTelemetry().rejection(tool.getId(), "event_cancelled_by_third_party", "block_break");
            return;
        }

        try {
            tool.onBlockBreak(event, player, item);
            player.getInventory().setItemInMainHand(item);
        } catch (Exception ex) {
            event.setCancelled(true);
            plugin.getToolTelemetry().error(tool.getId(), ex, "block_break");
            plugin.getLangManager().send(player, "general.tool_failed", 1500L);
        }
    }
}
