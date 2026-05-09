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

public class PhantomPlacer extends CustomTool {

    private final int maxDistance;

    public PhantomPlacer(ForgeToolsPlugin plugin, ConfigurationSection cfg) {
        super(plugin, "phantom_placer", cfg);
        this.maxDistance = cfg.getInt("max_distance", 15);
    }

    @Override
    public void onRightClick(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!canActivate(player, item)) return;

        Material mat = BlockUtil.getMaterialFromHotbar(player);
        if (mat == null) {
            plugin.getLangManager().send(player, "phantom_placer.no_block_in_hand", 2000L);
            return;
        }

        Block target = player.getTargetBlockExact(maxDistance, FluidCollisionMode.NEVER);
        if (target == null) {
            plugin.getLangManager().send(player, "phantom_placer.no_target", 2000L, "max", maxDistance);
            return;
        }

        List<Block> line = buildPlacementLine(player, target, mat);
        if (line.isEmpty()) return;

        int count = 0;
        for (Block b : line) {
            if (!BlockUtil.consumeMaterialFromInventory(player, mat)) break;
            if (!plugin.getProtectionManager().canBuild(player, b)) continue;
            plugin.getCoreProtectLogger().logPlacement(player, b);
            b.setType(mat, true);
            spawnParticle(b);
            count++;
        }

        if (count > 0 && plugin.getConfig().getBoolean("settings.sounds_enabled", true)) {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.6f, 1.5f);
            player.playSound(target.getLocation(), Sound.BLOCK_STONE_PLACE, 0.8f, 1.0f);
        }

        postAction(player, item);
    }

    private List<Block> buildPlacementLine(Player player, Block target, Material mat) {
        List<Block> result = new ArrayList<>();
        Location eye = player.getEyeLocation();
        Location targetLoc = target.getLocation().add(0.5, 0.5, 0.5);

        double distance = eye.distance(targetLoc);
        if (distance < 1) return result;

        double dx = (targetLoc.getX() - eye.getX()) / distance;
        double dy = (targetLoc.getY() - eye.getY()) / distance;
        double dz = (targetLoc.getZ() - eye.getZ()) / distance;

        Block lastSolidAir = null;
        int max = plugin.getConfig().getInt("settings.max_blocks_per_tick", 25);

        for (int i = 1; i <= Math.min((int) distance + 1, maxDistance) && result.size() < max; i++) {
            Block b = eye.clone().add(dx * i, dy * i, dz * i).getBlock();
            if (b.getType().isAir()) {
                lastSolidAir = b;
            } else {
                if (lastSolidAir != null && lastSolidAir.getRelative(BlockFace.DOWN).getType().isSolid()) {
                    result.add(lastSolidAir);
                    lastSolidAir = null;
                }
            }
        }

        if (lastSolidAir != null && lastSolidAir.getRelative(BlockFace.DOWN).getType().isSolid()) {
            result.add(lastSolidAir);
        }

        return result;
    }

    private void spawnParticle(Block b) {
        if (!plugin.getConfig().getBoolean("settings.particles_enabled", true)) return;
        b.getWorld().spawnParticle(Particle.DRAGON_BREATH, b.getLocation().add(0.5, 0.5, 0.5),
                6, 0.3, 0.3, 0.3, 0.02);
    }
}
