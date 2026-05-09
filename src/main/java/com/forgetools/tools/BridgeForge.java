package com.forgetools.tools;

import com.forgetools.ForgeToolsPlugin;
import com.forgetools.api.CustomTool;
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

    private static final double MOVE_THRESHOLD = 0.06;

    private final Map<UUID, double[]>  lastPositions = new HashMap<>();
    private final Map<UUID, BlockFace> lookDirections = new HashMap<>();
    private BukkitTask task;

    public BridgeForge(ForgeToolsPlugin plugin, ConfigurationSection cfg) {
        super(plugin, "bridge_forge", cfg);
    }

    private void startTask() {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    tickPlayer(player);
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    @Override
    public void onRegister() {
        startTask();
    }

    @Override
    public void onUnregister() {
        if (task != null && !task.isCancelled()) task.cancel();
        task = null;
        lastPositions.clear();
        lookDirections.clear();
    }

    private void tickPlayer(Player player) {
        if (!player.isSneaking()) {
            clearState(player.getUniqueId());
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!plugin.getToolRegistry().isToolItem(item)) {
            clearState(player.getUniqueId());
            return;
        }
        CustomTool tool = plugin.getToolRegistry().getToolFromItem(item);
        if (tool == null || !"bridge_forge".equals(tool.getId())) {
            clearState(player.getUniqueId());
            return;
        }

        UUID uuid = player.getUniqueId();

        BlockFace lookFace = getHorizontalFace(player);
        lookDirections.put(uuid, lookFace);

        double px = player.getLocation().getX();
        double pz = player.getLocation().getZ();
        double[] last = lastPositions.get(uuid);

        if (last != null) {
            double dx = px - last[0];
            double dz = pz - last[1];
            double dist = Math.sqrt(dx * dx + dz * dz);

            if (dist >= MOVE_THRESHOLD && isMovingTowardFace(dx, dz, lookFace)) {
                tryPlaceBlock(player, item);
            }
        }

        lastPositions.put(uuid, new double[]{px, pz});
    }

    private boolean isMovingTowardFace(double dx, double dz, BlockFace face) {
        return switch (face) {
            case NORTH -> dz < -0.01;
            case SOUTH -> dz > 0.01;
            case EAST  -> dx > 0.01;
            case WEST  -> dx < -0.01;
            default    -> true;
        };
    }

    private void tryPlaceBlock(Player player, ItemStack item) {
        if (!canActivate(player, item)) return;

        Location feet = player.getLocation();
        Block feetBlock = feet.getBlock();
        Block placeAt = feetBlock.getRelative(BlockFace.DOWN);

        if (!placeAt.getType().isAir()) return;
        if (!plugin.getProtectionManager().canBuild(player, placeAt)) {
            plugin.getLangManager().send(player, "general.protected_region", 3000L);
            return;
        }

        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand == null || offHand.getType().isAir() || !offHand.getType().isBlock()) {
            plugin.getLangManager().send(player, "bridge_forge.no_block_in_hand", 3000L);
            return;
        }
        Material mat = offHand.getType();

        plugin.getCoreProtectLogger().logPlacement(player, placeAt);
        placeAt.setType(mat, true);
        offHand.setAmount(offHand.getAmount() - 1);
        player.getInventory().setItemInOffHand(offHand.getAmount() > 0 ? offHand : null);

        spawnParticle(placeAt);
        postAction(player, item);
        player.getInventory().setItemInMainHand(item);
    }

    @Override
    public void onRightClick(PlayerInteractEvent event, Player player, ItemStack item) {
        clearState(player.getUniqueId());
    }

    @Override
    public boolean handlesMove() { return false; }

    @Override
    public void onMove(PlayerMoveEvent event, Player player, ItemStack item) {}

    private BlockFace getHorizontalFace(Player player) {
        float yaw = ((player.getLocation().getYaw() % 360) + 360) % 360;
        if (yaw < 45 || yaw >= 315) return BlockFace.SOUTH;
        if (yaw < 135)              return BlockFace.WEST;
        if (yaw < 225)              return BlockFace.NORTH;
        return BlockFace.EAST;
    }

    private void clearState(UUID uuid) {
        lastPositions.remove(uuid);
        lookDirections.remove(uuid);
    }

    private void spawnParticle(Block b) {
        if (!plugin.getConfig().getBoolean("settings.particles_enabled", true)) return;
        b.getWorld().spawnParticle(Particle.BLOCK, b.getLocation().add(0.5, 1.1, 0.5),
                6, 0.3, 0.05, 0.3, b.getBlockData());
    }
}
