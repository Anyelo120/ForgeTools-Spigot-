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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BridgeForge extends CustomTool {

    private static final double MOVE_THRESHOLD = 0.08;

    private final int maxLength;

    private final Map<UUID, Integer>  blockCounters  = new HashMap<>();
    private final Map<UUID, double[]> lastPositions  = new HashMap<>();

    private BukkitTask bridgeTask;

    public BridgeForge(ForgeToolsPlugin plugin, ConfigurationSection cfg) {
        super(plugin, "bridge_forge", cfg);
        this.maxLength = cfg.getInt("max_length", 12);
        startTask();
    }

    private void startTask() {
        bridgeTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    tickPlayer(player);
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private void tickPlayer(Player player) {
        if (!player.isSneaking()) {
            lastPositions.remove(player.getUniqueId());
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!plugin.getToolRegistry().isToolItem(item)) {
            lastPositions.remove(player.getUniqueId());
            return;
        }
        if (!"bridge_forge".equals(plugin.getToolRegistry().getToolFromItem(item) == null
                ? null : plugin.getToolRegistry().getToolFromItem(item).getId())) {
            lastPositions.remove(player.getUniqueId());
            return;
        }

        UUID uuid = player.getUniqueId();
        double px = player.getLocation().getX();
        double pz = player.getLocation().getZ();

        double[] last = lastPositions.get(uuid);
        if (last != null) {
            double dx = px - last[0];
            double dz = pz - last[1];
            double dist = Math.sqrt(dx * dx + dz * dz);

            if (dist >= MOVE_THRESHOLD) {
                tryPlaceBlock(player, item);
            }
        }

        lastPositions.put(uuid, new double[]{px, pz});
    }

    private void tryPlaceBlock(Player player, ItemStack item) {
        int placed = blockCounters.getOrDefault(player.getUniqueId(), 0);
        if (placed >= maxLength) {
            plugin.getLangManager().send(player, "bridge_forge.max_length_reached",
                    3000L, "max", maxLength);
            return;
        }

        if (!canActivate(player, item)) return;

        Location feet = player.getLocation();
        Block feetBlock   = feet.getBlock();
        Block belowFeet   = feetBlock.getRelative(BlockFace.DOWN);
        Block belowBelowFeet = belowFeet.getRelative(BlockFace.DOWN);

        Block target = null;
        if (belowFeet.getType().isAir() || isPassable(belowFeet)) {
            target = belowFeet;
        } else if (belowBelowFeet.getType().isAir() || isPassable(belowBelowFeet)) {
            target = belowBelowFeet;
        }

        if (target == null || (!target.getType().isAir() && !isPassable(target))) return;
        if (!plugin.getProtectionManager().canBuild(player, target)) {
            plugin.getLangManager().send(player, "general.protected_region", 3000L);
            return;
        }

        Material mat = BlockUtil.getMaterialFromHotbar(player);
        if (mat == null) {
            plugin.getLangManager().send(player, "bridge_forge.no_block_in_hand", 3000L);
            return;
        }
        if (!BlockUtil.consumeMaterialFromInventory(player, mat)) return;

        plugin.getCoreProtectLogger().logPlacement(player, target);
        target.setType(mat, true);
        blockCounters.put(player.getUniqueId(), placed + 1);

        if (getMode(item) == 1) placeRails(player, target);

        spawnParticle(target);
        postAction(player, item);
        player.getInventory().setItemInMainHand(item);
    }

    @Override
    public void onRightClick(PlayerInteractEvent event, Player player, ItemStack item) {
        blockCounters.remove(player.getUniqueId());
        lastPositions.remove(player.getUniqueId());
        plugin.getLangManager().send(player, "bridge_forge.counter_reset", 1000L);
    }

    @Override
    public boolean handlesMove() { return false; }

    @Override
    public void onMove(PlayerMoveEvent event, Player player, ItemStack item) {}

    private void placeRails(Player player, Block base) {
        BlockFace dir   = BlockUtil.getPrimaryFace(player.getEyeLocation().getDirection());
        BlockFace left  = rotateLeft(dir);
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
            case WEST  -> BlockFace.SOUTH;
            case SOUTH -> BlockFace.EAST;
            default    -> BlockFace.NORTH;
        };
    }

    private BlockFace rotateRight(BlockFace face) {
        return switch (face) {
            case NORTH -> BlockFace.EAST;
            case EAST  -> BlockFace.SOUTH;
            case SOUTH -> BlockFace.WEST;
            default    -> BlockFace.NORTH;
        };
    }

    private void spawnParticle(Block b) {
        if (!plugin.getConfig().getBoolean("settings.particles_enabled", true)) return;
        b.getWorld().spawnParticle(Particle.BLOCK, b.getLocation().add(0.5, 1.1, 0.5),
                6, 0.3, 0.05, 0.3, b.getBlockData());
    }
}
