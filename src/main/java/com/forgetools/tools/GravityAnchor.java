package com.forgetools.tools;

import com.forgetools.ForgeToolsPlugin;
import com.forgetools.api.CustomTool;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GravityAnchor extends CustomTool {

    private final int radius;
    private static final Material[] EXCLUDED = {
            Material.BEDROCK, Material.OBSIDIAN, Material.END_PORTAL_FRAME,
            Material.BARRIER, Material.COMMAND_BLOCK, Material.CHAIN_COMMAND_BLOCK,
            Material.REPEATING_COMMAND_BLOCK
    };

    public GravityAnchor(ForgeToolsPlugin plugin, ConfigurationSection cfg) {
        super(plugin, "gravity_anchor", cfg);
        this.radius = cfg.getInt("radius", 4);
    }

    @Override
    public void onRightClick(PlayerInteractEvent event, Player player, ItemStack item) {
        Block target = player.getTargetBlockExact(radius + 2, FluidCollisionMode.NEVER);
        if (target == null || target.getType().isAir()) {
            target = player.getLocation().getBlock();
        }
        if (!canActivate(player, item)) return;

        List<Block> floaters = collectFloaters(target);
        if (floaters.isEmpty()) return;

        int max = plugin.getConfig().getInt("settings.max_blocks_per_tick", 25);
        int count = 0;

        for (Block b : floaters) {
            if (count >= max) break;
            if (!plugin.getProtectionManager().canBuild(player, b)) continue;
            if (isExcluded(b.getType())) continue;
            plugin.getCoreProtectLogger().logRemoval(player, b);
            Location center = b.getLocation().add(0.5, 0.5, 0.5);
            Material type = b.getType();
            b.setType(Material.AIR, false);
            FallingBlock falling = b.getWorld().spawnFallingBlock(center, b.getBlockData());
            falling.setDropItem(true);
            falling.setHurtEntities(false);
            count++;
        }

        if (plugin.getConfig().getBoolean("settings.sounds_enabled", true)) {
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.7f, 1.2f);
        }
        if (plugin.getConfig().getBoolean("settings.particles_enabled", true)) {
            target.getWorld().spawnParticle(Particle.EXPLOSION, target.getLocation().add(0.5, 0.5, 0.5),
                    3, radius * 0.5, 1.0, radius * 0.5, 0);
        }

        postAction(player, item);
    }

    private List<Block> collectFloaters(Block center) {
        List<Block> result = new ArrayList<>();
        World world = center.getWorld();
        int cx = center.getX(), cy = center.getY(), cz = center.getZ();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    Block b = world.getBlockAt(cx + dx, cy + dy, cz + dz);
                    if (!b.getType().isAir() && b.getType().isSolid() && isFloating(b)) {
                        result.add(b);
                    }
                }
            }
        }
        return result;
    }

    private boolean isFloating(Block block) {
        Block below = block.getRelative(BlockFace.DOWN);
        return below.getType().isAir() || !below.getType().isSolid();
    }

    private boolean isExcluded(Material m) {
        for (Material ex : EXCLUDED) if (m == ex) return true;
        return false;
    }
}
