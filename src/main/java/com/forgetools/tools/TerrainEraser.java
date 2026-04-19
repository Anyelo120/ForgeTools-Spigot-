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

import java.util.*;

public class TerrainEraser extends CustomTool {

    private final int radius;
    private final Set<Material> filterBlocks;

    public TerrainEraser(ForgeToolsPlugin plugin, ConfigurationSection cfg) {
        super(plugin, "terrain_eraser", cfg);
        this.radius = cfg.getInt("radius", 4);
        this.filterBlocks = new HashSet<>();
        for (String s : cfg.getStringList("filter_blocks")) {
            try { filterBlocks.add(Material.valueOf(s.toUpperCase())); } catch (Exception ignored) {}
        }
    }

    @Override
    public void onRightClick(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!canActivate(player, item)) return;

        Location center = player.getLocation();
        World world = center.getWorld();
        if (world == null) return;

        int cx = center.getBlockX();
        int cz = center.getBlockZ();
        int count = 0;
        int max = plugin.getConfig().getInt("settings.max_blocks_per_tick", 25);

        for (int dx = -radius; dx <= radius && count < max; dx++) {
            for (int dz = -radius; dz <= radius && count < max; dz++) {
                Block top = BlockUtil.getHighestSolidBlock(world, cx + dx, cz + dz);
                if (top.getType().isAir()) continue;
                if (!filterBlocks.isEmpty() && !filterBlocks.contains(top.getType())) continue;
                if (!plugin.getProtectionManager().canBuild(player, top)) continue;
                plugin.getCoreProtectLogger().logRemoval(player, top);
                top.breakNaturally(item);
                spawnParticle(top);
                count++;
            }
        }

        if (count > 0 && plugin.getConfig().getBoolean("settings.sounds_enabled", true)) {
            player.playSound(player.getLocation(), Sound.BLOCK_GRASS_BREAK, 1.0f, 0.9f);
        }

        postAction(player, item);
    }

    private void spawnParticle(Block b) {
        if (!plugin.getConfig().getBoolean("settings.particles_enabled", true)) return;
        b.getWorld().spawnParticle(Particle.BLOCK, b.getLocation().add(0.5, 1.0, 0.5),
                10, 0.5, 0.1, 0.5, 0.02, b.getBlockData());
    }
}
