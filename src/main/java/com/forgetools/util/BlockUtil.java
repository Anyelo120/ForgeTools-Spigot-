package com.forgetools.util;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

public final class BlockUtil {

    private BlockUtil() {}

    public static Block getTargetBlock(Player player, int maxDistance) {
        return player.getTargetBlockExact(maxDistance, FluidCollisionMode.NEVER);
    }

    public static BlockFace getPrimaryFace(Vector direction) {
        double x = direction.getX();
        double y = direction.getY();
        double z = direction.getZ();
        double absX = Math.abs(x);
        double absY = Math.abs(y);
        double absZ = Math.abs(z);
        if (absY > absX && absY > absZ) return y > 0 ? BlockFace.UP : BlockFace.DOWN;
        if (absX >= absZ) return x > 0 ? BlockFace.EAST : BlockFace.WEST;
        return z > 0 ? BlockFace.SOUTH : BlockFace.NORTH;
    }

    public static List<Block> getLineBlocks(Block start, BlockFace direction, int length) {
        List<Block> result = new ArrayList<>(length);
        Block current = start;
        for (int i = 0; i < length; i++) {
            result.add(current);
            current = current.getRelative(direction);
        }
        return result;
    }

    public static List<Block> getSphereBlocks(Location center, int radius) {
        List<Block> result = new ArrayList<>();
        World world = center.getWorld();
        if (world == null) return result;
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        double r2 = (double) radius * radius;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + y * y + z * z <= r2) {
                        result.add(world.getBlockAt(cx + x, cy + y, cz + z));
                    }
                }
            }
        }
        return result;
    }

    public static Block getHighestSolidBlock(World world, int x, int z) {
        int y = world.getHighestBlockYAt(x, z);
        Block b = world.getBlockAt(x, y, z);
        while (y > world.getMinHeight() && (b.getType().isAir() || !b.getType().isSolid())) {
            y--;
            b = world.getBlockAt(x, y, z);
        }
        return b;
    }

    public static boolean isFloating(Block block) {
        if (block.getType().isAir()) return false;
        Block below = block.getRelative(BlockFace.DOWN);
        return below.getType().isAir() || !below.getType().isSolid();
    }

    public static List<Block> floodFillSameType(Block origin, Material type, int maxRadius, int maxBlocks) {
        List<Block> result = new ArrayList<>();
        Set<Block> visited = new HashSet<>();
        Deque<Block> queue = new ArrayDeque<>();
        queue.add(origin);
        visited.add(origin);
        int ox = origin.getX();
        int oy = origin.getY();
        int oz = origin.getZ();
        int r2 = maxRadius * maxRadius;
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};
        while (!queue.isEmpty() && result.size() < maxBlocks) {
            Block current = queue.poll();
            if (current.getType() != type) continue;
            result.add(current);
            for (BlockFace face : faces) {
                Block neighbor = current.getRelative(face);
                if (visited.contains(neighbor)) continue;
                int dx = neighbor.getX() - ox;
                int dy = neighbor.getY() - oy;
                int dz = neighbor.getZ() - oz;
                if (dx * dx + dy * dy + dz * dz > r2) continue;
                visited.add(neighbor);
                if (neighbor.getType() == type) queue.add(neighbor);
            }
        }
        return result;
    }

    public static List<Block> getCrossPattern(Location center, int radius) {
        List<Block> result = new ArrayList<>();
        World world = center.getWorld();
        if (world == null) return result;
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        result.add(world.getBlockAt(cx, cy, cz));
        for (int r = 1; r <= radius; r++) {
            result.add(world.getBlockAt(cx + r, cy, cz));
            result.add(world.getBlockAt(cx - r, cy, cz));
            result.add(world.getBlockAt(cx, cy, cz + r));
            result.add(world.getBlockAt(cx, cy, cz - r));
        }
        return result;
    }

    public static BlockFace getPerpendicularFace(BlockFace face) {
        return switch (face) {
            case NORTH, SOUTH -> BlockFace.EAST;
            case EAST, WEST -> BlockFace.NORTH;
            case UP, DOWN -> BlockFace.NORTH;
            default -> BlockFace.EAST;
        };
    }

    public static Material getMaterialFromHotbar(Player player) {
        org.bukkit.inventory.ItemStack offHand = player.getInventory().getItemInOffHand();
        if (!offHand.getType().isAir() && offHand.getType().isBlock()) {
            return offHand.getType();
        }
        for (int i = 0; i < 9; i++) {
            org.bukkit.inventory.ItemStack slot = player.getInventory().getItem(i);
            if (slot != null && !slot.getType().isAir() && slot.getType().isBlock()) {
                return slot.getType();
            }
        }
        return null;
    }

    public static boolean consumeMaterialFromInventory(Player player, Material material) {
        org.bukkit.inventory.ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand.getType() == material) {
            offHand.setAmount(offHand.getAmount() - 1);
            player.getInventory().setItemInOffHand(offHand);
            return true;
        }
        for (int i = 0; i < 9; i++) {
            org.bukkit.inventory.ItemStack slot = player.getInventory().getItem(i);
            if (slot != null && slot.getType() == material) {
                slot.setAmount(slot.getAmount() - 1);
                player.getInventory().setItem(i, slot);
                return true;
            }
        }
        return false;
    }
}
