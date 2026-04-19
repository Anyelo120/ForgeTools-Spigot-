package com.forgetools.tools;

import com.forgetools.ForgeToolsPlugin;
import com.forgetools.api.CustomTool;
import com.forgetools.util.BlockUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class LightWeaver extends CustomTool {

    private final int radius;
    private final Material lightBlock;

    public LightWeaver(ForgeToolsPlugin plugin, ConfigurationSection cfg) {
        super(plugin, "light_weaver", cfg);
        this.radius = cfg.getInt("radius", 3);
        Material parsed = Material.SEA_LANTERN;
        try { parsed = Material.valueOf(cfg.getString("light_block", "SEA_LANTERN")); } catch (Exception ignored) {}
        this.lightBlock = parsed;
    }

    @Override
    public void onRightClick(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!canActivate(player, item)) return;

        int mode = getMode(item);
        Location center = player.getEyeLocation();
        Block target = player.getTargetBlockExact(radius + 3, FluidCollisionMode.NEVER);
        if (target != null) center = target.getLocation().add(0, 1, 0);

        List<Block> pattern = BlockUtil.getCrossPattern(center, radius);
        int max = plugin.getConfig().getInt("settings.max_blocks_per_tick", 25);
        int count = 0;

        for (Block b : pattern) {
            if (count >= max) break;
            if (!plugin.getProtectionManager().canBuild(player, b)) continue;

            if (mode == 0) {
                if (!b.getType().isAir()) continue;
                if (!BlockUtil.consumeMaterialFromInventory(player, lightBlock)) break;
                plugin.getCoreProtectLogger().logPlacement(player, b);
                b.setType(lightBlock, true);
                spawnPlaceParticle(b);
            } else {
                if (!isLightSource(b.getType())) continue;
                plugin.getCoreProtectLogger().logRemoval(player, b);
                b.breakNaturally();
                spawnRemoveParticle(b);
            }
            count++;
        }

        if (count > 0 && plugin.getConfig().getBoolean("settings.sounds_enabled", true)) {
            Sound sound = (mode == 0) ? Sound.BLOCK_AMETHYST_CLUSTER_PLACE : Sound.BLOCK_GLASS_BREAK;
            player.playSound(player.getLocation(), sound, 0.9f, mode == 0 ? 1.0f : 1.4f);
        }

        postAction(player, item);
    }

    private boolean isLightSource(Material m) {
        return switch (m) {
            case SEA_LANTERN, GLOWSTONE, LANTERN, SOUL_LANTERN,
                    SHROOMLIGHT, JACK_O_LANTERN, MAGMA_BLOCK,
                    TORCH, SOUL_TORCH, END_ROD -> true;
            default -> false;
        };
    }

    private void spawnPlaceParticle(Block b) {
        if (!plugin.getConfig().getBoolean("settings.particles_enabled", true)) return;
        b.getWorld().spawnParticle(Particle.END_ROD, b.getLocation().add(0.5, 0.5, 0.5),
                8, 0.4, 0.4, 0.4, 0.05);
    }

    private void spawnRemoveParticle(Block b) {
        if (!plugin.getConfig().getBoolean("settings.particles_enabled", true)) return;
        b.getWorld().spawnParticle(Particle.SMOKE, b.getLocation().add(0.5, 0.5, 0.5),
                6, 0.3, 0.3, 0.3, 0.02);
    }
}
