package com.forgetools.tools;

import com.forgetools.ForgeToolsPlugin;
import com.forgetools.api.CustomTool;
import com.forgetools.util.BlockUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ChunkHammer extends CustomTool {

    private final int radius;
    private final List<Material> allowedBlocks;

    public ChunkHammer(ForgeToolsPlugin plugin, ConfigurationSection cfg) {
        super(plugin, "chunk_hammer", cfg);
        this.radius = cfg.getInt("radius", 3);
        this.allowedBlocks = cfg.getStringList("allowed_blocks").stream()
                .map(s -> { try { return Material.valueOf(s.toUpperCase()); } catch (Exception e) { return null; } })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Override
    public void onRightClick(PlayerInteractEvent event, Player player, ItemStack item) {
        Block target = BlockUtil.getTargetBlock(player, radius + 2);
        if (target == null || target.getType().isAir()) return;
        if (!canActivate(player, item)) return;

        int mode = getMode(item);
        if (mode == 0) {
            handleLineBreak(player, item, target);
        } else {
            handleFlatClean(player, item, target);
        }

        postAction(player, item);
    }

    private void handleLineBreak(Player player, ItemStack item, Block target) {
        BlockFace face = BlockUtil.getPrimaryFace(player.getEyeLocation().getDirection());
        Material type = target.getType();

        List<Block> line = new ArrayList<>();
        Block current = target;
        for (int i = 0; i < radius; i++) {
            if (current.getType() != type) break;
            line.add(current);
            current = current.getRelative(face);
        }

        int broken = 0;
        for (Block b : line) {
            if (broken >= plugin.getConfig().getInt("settings.max_blocks_per_tick", 25)) break;
            if (!allowedBlocks.isEmpty() && !allowedBlocks.contains(b.getType())) continue;
            if (!plugin.getProtectionManager().canBuild(player, b)) continue;
            plugin.getCoreProtectLogger().logRemoval(player, b);
            b.breakNaturally(item);
            spawnParticle(b);
            broken++;
        }

        if (plugin.getConfig().getBoolean("settings.sounds_enabled", true)) {
            player.playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 0.9f, 0.85f);
        }
    }

    private void handleFlatClean(Player player, ItemStack item, Block target) {
        World world = target.getWorld();
        int cx = target.getX();
        int cz = target.getZ();
        int count = 0;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (count >= plugin.getConfig().getInt("settings.max_blocks_per_tick", 25)) break;
                Block top = BlockUtil.getHighestSolidBlock(world, cx + dx, cz + dz);
                if (top.getType().isAir()) continue;
                if (!allowedBlocks.isEmpty() && !allowedBlocks.contains(top.getType())) continue;
                if (!plugin.getProtectionManager().canBuild(player, top)) continue;
                plugin.getCoreProtectLogger().logRemoval(player, top);
                top.breakNaturally(item);
                spawnParticle(top);
                count++;
            }
        }

        if (plugin.getConfig().getBoolean("settings.sounds_enabled", true)) {
            player.playSound(player.getLocation(), Sound.BLOCK_GRAVEL_BREAK, 0.9f, 1.0f);
        }
    }

    private void spawnParticle(Block b) {
        if (!plugin.getConfig().getBoolean("settings.particles_enabled", true)) return;
        b.getWorld().spawnParticle(Particle.BLOCK, b.getLocation().add(0.5, 0.5, 0.5),
                12, 0.3, 0.3, 0.3, 0.05, b.getBlockData());
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event, Player player, ItemStack item) {}
}
