package com.forgetools.tools;

import com.forgetools.ForgeToolsPlugin;
import com.forgetools.api.CustomTool;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.Chunk;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ClaimPolisher extends CustomTool {

    private final Map<UUID, Material> playerFilters = new HashMap<>();

    public ClaimPolisher(ForgeToolsPlugin plugin, ConfigurationSection cfg) {
        super(plugin, "claim_polisher", cfg);
    }

    public void setFilter(UUID playerId, Material material) {
        playerFilters.put(playerId, material);
    }

    public Material getFilter(UUID playerId) {
        return playerFilters.get(playerId);
    }

    @Override
    public void onRightClick(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!canActivate(player, item)) return;

        Material filter = playerFilters.get(player.getUniqueId());
        if (filter == null) {
            plugin.getLangManager().send(player, "claim_polisher.no_filter", 3000L);
            return;
        }

        Chunk chunk = player.getLocation().getChunk();
        World world = player.getWorld();
        int baseX = chunk.getX() * 16;
        int baseZ = chunk.getZ() * 16;
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight();

        List<Block> targets = collectTargets(world, baseX, baseZ, minY, maxY, filter);

        if (targets.isEmpty()) {
            plugin.getLangManager().send(player, "claim_polisher.none_found", 2000L, "material", filter.name());
            return;
        }

        int perTick = plugin.getConfig().getInt("settings.max_blocks_per_tick", 25);
        int[] index = {0};
        int[] removed = {0};

        new BukkitRunnable() {
            @Override
            public void run() {
                int end = Math.min(index[0] + perTick, targets.size());
                while (index[0] < end) {
                    Block b = targets.get(index[0]++);
                    if (b.getType() != filter) continue;
                    if (!plugin.getProtectionManager().canBuild(player, b)) continue;
                    plugin.getCoreProtectLogger().logRemoval(player, b);
                    b.breakNaturally(item);
                    spawnParticle(b);
                    removed[0]++;
                }
                if (index[0] >= targets.size()) {
                    plugin.getLangManager().send(player, "claim_polisher.done", 1000L,
                            "count", removed[0], "material", filter.name());
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);

        postAction(player, item);

        if (plugin.getConfig().getBoolean("settings.sounds_enabled", true)) {
            player.playSound(player.getLocation(), Sound.ITEM_HOE_TILL, 1.0f, 0.8f);
        }
    }

    private List<Block> collectTargets(World world, int baseX, int baseZ, int minY, int maxY, Material filter) {
        List<Block> result = new ArrayList<>();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y < maxY; y++) {
                    Block b = world.getBlockAt(baseX + x, y, baseZ + z);
                    if (b.getType() == filter) result.add(b);
                }
            }
        }
        return result;
    }

    private void spawnParticle(Block b) {
        if (!plugin.getConfig().getBoolean("settings.particles_enabled", true)) return;
        b.getWorld().spawnParticle(Particle.SWEEP_ATTACK, b.getLocation().add(0.5, 0.5, 0.5),
                1, 0, 0, 0, 0);
    }
}
