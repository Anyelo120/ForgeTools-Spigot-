package com.forgetools.listener;

import com.forgetools.ForgeToolsPlugin;
import com.forgetools.api.CustomTool;
import com.forgetools.registry.ToolRegistry;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class EnergyChargeListener implements Listener {

    private static final Map<Material, Integer> CHARGE_VALUES = Map.of(
            Material.GLOWSTONE_DUST,    15,
            Material.IRON_INGOT,        20,
            Material.BLAZE_ROD,         30,
            Material.GOLD_INGOT,        35,
            Material.DIAMOND,           50,
            Material.NETHERITE_INGOT,   75
    );

    private final ForgeToolsPlugin plugin;
    private final ToolRegistry registry;

    public EnergyChargeListener(ForgeToolsPlugin plugin, ToolRegistry registry) {
        this.plugin = plugin;
        this.registry = registry;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.OFF_HAND) return;
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        Player player    = event.getPlayer();
        ItemStack main   = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        if (!registry.isToolItem(main)) return;
        Integer chargeAmount = CHARGE_VALUES.get(offHand.getType());
        if (chargeAmount == null) return;

        CustomTool tool = registry.getToolFromItem(main);
        if (tool == null) return;

        int currentEnergy = plugin.getEnergyManager().getEnergy(player, main, tool);
        if (currentEnergy >= tool.getMaxEnergy()) {
            player.sendMessage(plugin.getLangManager().get("energy.full"));
            return;
        }

        event.setCancelled(true);

        offHand.setAmount(offHand.getAmount() - 1);
        player.getInventory().setItemInOffHand(offHand);

        plugin.getEnergyManager().refillEnergy(player, main, tool, chargeAmount);
        int newEnergy = plugin.getEnergyManager().getEnergy(player, main, tool);

        player.sendMessage(plugin.getLangManager().get(
                "energy.charged",
                "amount",  chargeAmount,
                "current", newEnergy,
                "max",     tool.getMaxEnergy()));

        if (plugin.getConfig().getBoolean("settings.sounds_enabled", true)) {
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_HIT, 0.8f, 1.3f);
        }
        if (plugin.getConfig().getBoolean("settings.particles_enabled", true)) {
            player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK,
                    player.getLocation().add(0, 1.2, 0), 12, 0.3, 0.3, 0.3, 0.05);
        }
    }
}
