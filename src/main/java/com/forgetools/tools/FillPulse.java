package com.forgetools.tools;

import com.forgetools.ForgeToolsPlugin;
import com.forgetools.api.CustomTool;
import com.forgetools.util.BlockUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class FillPulse extends CustomTool {

    private final int radius;

    public FillPulse(ForgeToolsPlugin plugin, ConfigurationSection cfg) {
        super(plugin, "fill_pulse", cfg);
        this.radius = cfg.getInt("radius", 2);
    }

    @Override
    public void onRightClick(PlayerInteractEvent event, Player player, ItemStack item) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (!canActivate(player, item)) return;

        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand.getType().isAir() || !offHand.getType().isBlock()) {
            player.sendMessage(ChatColor.RED + "Hold a block in your off-hand to fill with.");
            return;
        }

        Material fillMaterial = offHand.getType();
        Location center = event.getClickedBlock().getLocation().add(0.5, 0.5, 0.5);
        List<Block> sphere = BlockUtil.getSphereBlocks(center, radius);

        int count = 0;
        int max = plugin.getConfig().getInt("settings.max_blocks_per_tick", 25);

        for (Block b : sphere) {
            if (count >= max) break;
            if (!b.getType().isAir()) continue;
            if (!plugin.getProtectionManager().canBuild(player, b)) continue;
            if (offHand.getAmount() <= 0) break;
            offHand.setAmount(offHand.getAmount() - 1);
            plugin.getCoreProtectLogger().logPlacement(player, b);
            b.setType(fillMaterial, true);
            spawnParticle(b);
            count++;
        }

        player.getInventory().setItemInOffHand(offHand);

        if (count > 0 && plugin.getConfig().getBoolean("settings.sounds_enabled", true)) {
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_PLACE, 1.0f, 0.9f);
            center.getWorld().spawnParticle(Particle.FLASH, center, 1, 0, 0, 0, 0);
        }

        postAction(player, item);
    }

    private void spawnParticle(Block b) {
        if (!plugin.getConfig().getBoolean("settings.particles_enabled", true)) return;
        b.getWorld().spawnParticle(Particle.END_ROD, b.getLocation().add(0.5, 0.5, 0.5),
                3, 0.2, 0.2, 0.2, 0.02);
    }
}
