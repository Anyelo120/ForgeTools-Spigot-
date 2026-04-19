package com.forgetools.tools;

import com.forgetools.ForgeToolsPlugin;
import com.forgetools.api.CustomTool;
import com.forgetools.util.BlockUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AutoLevelShovel extends CustomTool {

    private final int radius;
    private final int maxDelta;

    public AutoLevelShovel(ForgeToolsPlugin plugin, ConfigurationSection cfg) {
        super(plugin, "auto_level_shovel", cfg);
        this.radius = cfg.getInt("radius", 2);
        this.maxDelta = cfg.getInt("max_delta", 1);
    }

    @Override
    public void onRightClick(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!canActivate(player, item)) return;

        int targetY = (int) Math.floor(player.getEyeLocation().getY()) - 1;
        World world = player.getWorld();
        Location center = player.getLocation();
        int cx = center.getBlockX();
        int cz = center.getBlockZ();

        int max = plugin.getConfig().getInt("settings.max_blocks_per_tick", 25);
        List<Block> toBreak = new ArrayList<>();
        List<Block> toFill = new ArrayList<>();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                Block top = BlockUtil.getHighestSolidBlock(world, cx + dx, cz + dz);
                int topY = top.getY();
                int delta = topY - targetY;

                if (delta > 0 && delta <= maxDelta) {
                    for (int y = targetY + 1; y <= topY; y++) {
                        Block b = world.getBlockAt(cx + dx, y, cz + dz);
                        if (!b.getType().isAir()) toBreak.add(b);
                    }
                } else if (delta < 0 && Math.abs(delta) <= maxDelta) {
                    for (int y = topY + 1; y <= targetY; y++) {
                        Block b = world.getBlockAt(cx + dx, y, cz + dz);
                        if (b.getType().isAir()) toFill.add(b);
                    }
                }
            }
        }

        Material fillMat = BlockUtil.getMaterialFromHotbar(player);
        int count = 0;

        for (Block b : toBreak) {
            if (count >= max) break;
            if (!plugin.getProtectionManager().canBuild(player, b)) continue;
            plugin.getCoreProtectLogger().logRemoval(player, b);
            b.breakNaturally(item);
            spawnBreakParticle(b);
            count++;
        }

        if (fillMat != null) {
            for (Block b : toFill) {
                if (count >= max) break;
                if (!plugin.getProtectionManager().canBuild(player, b)) continue;
                if (!BlockUtil.consumeMaterialFromInventory(player, fillMat)) break;
                plugin.getCoreProtectLogger().logPlacement(player, b);
                b.setType(fillMat, true);
                spawnFillParticle(b);
                count++;
            }
        }

        if (count > 0 && plugin.getConfig().getBoolean("settings.sounds_enabled", true)) {
            player.playSound(player.getLocation(), Sound.BLOCK_GRAVEL_PLACE, 1.0f, 0.95f);
        }

        postAction(player, item);
    }

    private void spawnBreakParticle(Block b) {
        if (!plugin.getConfig().getBoolean("settings.particles_enabled", true)) return;
        b.getWorld().spawnParticle(Particle.BLOCK, b.getLocation().add(0.5, 1.0, 0.5),
                8, 0.4, 0.1, 0.4, b.getBlockData());
    }

    private void spawnFillParticle(Block b) {
        if (!plugin.getConfig().getBoolean("settings.particles_enabled", true)) return;
        b.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, b.getLocation().add(0.5, 1.0, 0.5),
                3, 0.3, 0.1, 0.3, 0);
    }
}
