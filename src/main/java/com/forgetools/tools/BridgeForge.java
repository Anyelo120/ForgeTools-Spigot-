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
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BridgeForge extends CustomTool {

    private final int maxLength;
    private final Map<UUID, Integer> blockCounters = new HashMap<>();

    public BridgeForge(ForgeToolsPlugin plugin, ConfigurationSection cfg) {
        super(plugin, "bridge_forge", cfg);
        this.maxLength = cfg.getInt("max_length", 12);
    }

    @Override
    public boolean handlesMove() { return true; }

    @Override
    public void onRightClick(PlayerInteractEvent event, Player player, ItemStack item) {
        blockCounters.remove(player.getUniqueId());
        player.sendMessage(ChatColor.GOLD + "Bridge counter reset.");
    }

    @Override
    public void onMove(PlayerMoveEvent event, Player player, ItemStack item) {
        if (!player.isSneaking()) return;
        if (!canActivate(player, item)) return;

        int placed = blockCounters.getOrDefault(player.getUniqueId(), 0);
        if (placed >= maxLength) return;

        Block below = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
        Block twoBelow = below.getRelative(BlockFace.DOWN);

        if (!below.getType().isAir() && !isPassable(below)) return;

        Material mat = BlockUtil.getMaterialFromHotbar(player);
        if (mat == null) return;

        Block target = below.getType().isAir() ? below : twoBelow;
        if (!target.getType().isAir()) return;
        if (!plugin.getProtectionManager().canBuild(player, target)) return;
        if (!BlockUtil.consumeMaterialFromInventory(player, mat)) return;

        plugin.getCoreProtectLogger().logPlacement(player, target);
        target.setType(mat, true);
        blockCounters.put(player.getUniqueId(), placed + 1);

        if (getMode(item) == 1) {
            placeRails(player, item, target);
        }

        spawnParticle(target);
        postAction(player, item);
    }

    private void placeRails(Player player, ItemStack item, Block base) {
        BlockFace[] sides = {BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH};
        BlockFace dir = BlockUtil.getPrimaryFace(player.getEyeLocation().getDirection());
        BlockFace left = rotateLeft(dir);
        BlockFace right = rotateRight(dir);

        for (BlockFace side : new BlockFace[]{left, right}) {
            Block rail = base.getRelative(side).getRelative(BlockFace.UP);
            if (!rail.getType().isAir()) continue;
            if (!plugin.getProtectionManager().canBuild(player, rail)) continue;
            if (!BlockUtil.consumeMaterialFromInventory(player, Material.OAK_FENCE)) continue;
            plugin.getCoreProtectLogger().logPlacement(player, rail);
            rail.setType(Material.OAK_FENCE, true);
        }
    }

    private boolean isPassable(Block b) {
        return b.getType().isAir() || !b.getType().isSolid();
    }

    private BlockFace rotateLeft(BlockFace face) {
        return switch (face) {
            case NORTH -> BlockFace.WEST;
            case WEST -> BlockFace.SOUTH;
            case SOUTH -> BlockFace.EAST;
            default -> BlockFace.NORTH;
        };
    }

    private BlockFace rotateRight(BlockFace face) {
        return switch (face) {
            case NORTH -> BlockFace.EAST;
            case EAST -> BlockFace.SOUTH;
            case SOUTH -> BlockFace.WEST;
            default -> BlockFace.NORTH;
        };
    }

    private void spawnParticle(Block b) {
        if (!plugin.getConfig().getBoolean("settings.particles_enabled", true)) return;
        b.getWorld().spawnParticle(Particle.BLOCK, b.getLocation().add(0.5, 1.1, 0.5),
                6, 0.3, 0.05, 0.3, b.getBlockData());
    }
}
