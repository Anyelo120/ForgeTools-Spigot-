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
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ToolInteractListener implements Listener {

    private final ForgeToolsPlugin plugin;
    private final ToolRegistry registry;
    private final ProtectionManager protection;
    private final CoreProtectLogger logger;

    public ToolInteractListener(ForgeToolsPlugin plugin, ToolRegistry registry,
                                ProtectionManager protection, CoreProtectLogger logger) {
        this.plugin = plugin;
        this.registry = registry;
        this.protection = protection;
        this.logger = logger;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Action action = event.getAction();
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!registry.isToolItem(item)) return;

        CustomTool tool = registry.getToolFromItem(item);
        if (tool == null) return;

        event.setCancelled(true);

        if (player.isSneaking()) {
            tool.onModeSwitch(player, item);
            player.getInventory().setItemInMainHand(item);
            return;
        }

        tool.onRightClick(event, player, item);
        player.getInventory().setItemInMainHand(item);
    }
}
