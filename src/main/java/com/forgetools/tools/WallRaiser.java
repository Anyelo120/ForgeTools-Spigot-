package com.forgetools.tools;

import com.forgetools.ForgeToolsPlugin;
import com.forgetools.api.CustomTool;
import com.forgetools.util.BlockUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WallRaiser extends CustomTool {

    private final int height;
    private final int width;

    public WallRaiser(ForgeToolsPlugin plugin, ConfigurationSection cfg) {
        super(plugin, "wall_raiser", cfg);
        this.height = cfg.getInt("height", 5);
        this.width = cfg.getInt("width", 5);
    }

    @Override
    public void onRightClick(PlayerInteractEvent event, Player player, ItemStack item) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block clicked = event.getClickedBlock();
        BlockFace clickedFace = event.getBlockFace();
        if (clicked == null || clickedFace == null) return;
        if (!canActivate(player, item)) return;

        Material mat = BlockUtil.getMaterialFromHotbar(player);
        if (mat == null) {
            player.sendMessage(ChatColor.RED + "No placeable block found in hotbar.");
            return;
        }

        int mode = getMode(item);
        List<Block> wallBlocks = buildWallBlocks(clicked, clickedFace, mode);

        int count = 0;
        int max = plugin.getConfig().getInt("settings.max_blocks_per_tick", 25);
        for (Block b : wallBlocks) {
            if (count >= max) break;
            if (!b.getType().isAir()) continue;
            if (!plugin.getProtectionManager().canBuild(player, b)) continue;
            if (!BlockUtil.consumeMaterialFromInventory(player, mat)) break;
            plugin.getCoreProtectLogger().logPlacement(player, b);
            b.setType(mat, true);
            spawnParticle(b);
            count++;
        }

        if (count > 0 && plugin.getConfig().getBoolean("settings.sounds_enabled", true)) {
            player.playSound(player.getLocation(), Sound.BLOCK_STONE_PLACE, 1.0f, 0.85f);
        }

        postAction(player, item);
    }

    private List<Block> buildWallBlocks(Block base, BlockFace normal, int mode) {
        List<Block> result = new ArrayList<>();
        int halfWidth = width / 2;

        BlockFace extrude = opposite(normal);
        BlockFace lateral = BlockUtil.getPerpendicularFace(normal);

        if (mode == 0) {
            for (int w = -halfWidth; w <= halfWidth; w++) {
                Block column = base.getRelative(lateral, w);
                for (int h = 0; h < height; h++) {
                    result.add(column.getRelative(BlockFace.UP, h));
                }
            }
        } else {
            for (int w = 0; w < width; w++) {
                Block column = base.getRelative(extrude, w);
                for (int h = 0; h < height; h++) {
                    result.add(column.getRelative(BlockFace.UP, h));
                }
            }
        }
        return result;
    }

    private BlockFace opposite(BlockFace face) {
        return switch (face) {
            case NORTH -> BlockFace.SOUTH;
            case SOUTH -> BlockFace.NORTH;
            case EAST -> BlockFace.WEST;
            case WEST -> BlockFace.EAST;
            case UP -> BlockFace.DOWN;
            case DOWN -> BlockFace.UP;
            default -> face;
        };
    }

    private void spawnParticle(Block b) {
        if (!plugin.getConfig().getBoolean("settings.particles_enabled", true)) return;
        b.getWorld().spawnParticle(Particle.BLOCK, b.getLocation().add(0.5, 0.5, 0.5),
                5, 0.3, 0.3, 0.3, b.getBlockData());
    }
}
