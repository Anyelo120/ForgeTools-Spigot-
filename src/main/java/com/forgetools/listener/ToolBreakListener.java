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
    private final ProtectionManager protection;
    private final CoreProtectLogger logger;

    public ToolBreakListener(ForgeToolsPlugin plugin, ToolRegistry registry,
                             ProtectionManager protection, CoreProtectLogger logger) {
        this.plugin = plugin;
        this.registry = registry;
        this.protection = protection;
        this.logger = logger;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!registry.isToolItem(item)) return;

        CustomTool tool = registry.getToolFromItem(item);
        if (tool == null || !tool.handlesBlockBreak()) return;

        tool.onBlockBreak(event, player, item);
        player.getInventory().setItemInMainHand(item);
    }
}
