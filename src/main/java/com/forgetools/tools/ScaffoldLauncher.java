package com.forgetools.tools;

import com.forgetools.ForgeToolsPlugin;
import com.forgetools.api.CustomTool;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class ScaffoldLauncher extends CustomTool {

    private final int maxDistance;
    private final int autoRemoveTicks;

    public ScaffoldLauncher(ForgeToolsPlugin plugin, ConfigurationSection cfg) {
        super(plugin, "scaffold_launcher", cfg);
        this.maxDistance = cfg.getInt("max_distance", 8);
        this.autoRemoveTicks = cfg.getInt("auto_remove_ticks", 600);
    }

    @Override
    public void onRightClick(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!canActivate(player, item)) return;

        int mode = getMode(item);
        List<Block> placed = (mode == 0) ? launchVertical(player, item) : launchHorizontal(player, item);

        if (placed.isEmpty()) return;

        scheduleRemoval(placed);
        postAction(player, item);

        if (plugin.getConfig().getBoolean("settings.sounds_enabled", true)) {
            player.playSound(player.getLocation(), Sound.BLOCK_SCAFFOLDING_PLACE, 1.0f, 1.1f);
        }
    }

    private List<Block> launchVertical(Player player, ItemStack item) {
        List<Block> placed = new ArrayList<>();
        Block start = player.getLocation().getBlock();

        for (int i = 1; i <= maxDistance; i++) {
            Block target = start.getRelative(BlockFace.UP, i);
            if (!target.getType().isAir()) break;
            if (!plugin.getProtectionManager().canBuild(player, target)) break;
            if (!hasScaffolding(player)) break;
            consumeScaffolding(player);
            plugin.getCoreProtectLogger().logPlacement(player, target);
            target.setType(Material.SCAFFOLDING, true);
            spawnParticle(target);
            placed.add(target);
        }
        return placed;
    }

    private List<Block> launchHorizontal(Player player, ItemStack item) {
        List<Block> placed = new ArrayList<>();
        Location eye = player.getEyeLocation();
        BlockFace face = getHorizontalFace(player);

        Block start = player.getLocation().getBlock();
        for (int i = 1; i <= maxDistance; i++) {
            Block target = start.getRelative(face, i);
            if (!target.getType().isAir()) break;
            if (!plugin.getProtectionManager().canBuild(player, target)) break;
            if (!hasScaffolding(player)) break;
            consumeScaffolding(player);
            plugin.getCoreProtectLogger().logPlacement(player, target);
            target.setType(Material.SCAFFOLDING, true);
            spawnParticle(target);
            placed.add(target);
        }
        return placed;
    }

    private boolean hasScaffolding(Player player) {
        for (ItemStack slot : player.getInventory().getContents()) {
            if (slot != null && slot.getType() == Material.SCAFFOLDING && slot.getAmount() > 0) return true;
        }
        return false;
    }

    private void consumeScaffolding(Player player) {
        for (ItemStack slot : player.getInventory().getContents()) {
            if (slot != null && slot.getType() == Material.SCAFFOLDING && slot.getAmount() > 0) {
                slot.setAmount(slot.getAmount() - 1);
                return;
            }
        }
    }

    private BlockFace getHorizontalFace(Player player) {
        float yaw = player.getLocation().getYaw();
        yaw = ((yaw % 360) + 360) % 360;
        if (yaw < 45 || yaw >= 315) return BlockFace.SOUTH;
        if (yaw < 135) return BlockFace.WEST;
        if (yaw < 225) return BlockFace.NORTH;
        return BlockFace.EAST;
    }

    private void scheduleRemoval(List<Block> blocks) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Block b : blocks) {
                    if (b.getType() == Material.SCAFFOLDING) {
                        b.setType(Material.AIR, false);
                        spawnParticle(b);
                    }
                }
            }
        }.runTaskLater(plugin, autoRemoveTicks);
    }

    private void spawnParticle(Block b) {
        if (!plugin.getConfig().getBoolean("settings.particles_enabled", true)) return;
        b.getWorld().spawnParticle(Particle.CLOUD, b.getLocation().add(0.5, 0.5, 0.5),
                4, 0.2, 0.2, 0.2, 0.01);
    }
}
