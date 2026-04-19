package com.forgetools.tools;

import com.forgetools.ForgeToolsPlugin;
import com.forgetools.api.CustomTool;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BiomeStitcher extends CustomTool {

    private final int sectionSize;
    private final List<Biome> biomeList = new ArrayList<>();

    public BiomeStitcher(ForgeToolsPlugin plugin, ConfigurationSection cfg) {
        super(plugin, "biome_stitcher", cfg);
        this.sectionSize = cfg.getInt("section_size", 16);

        for (String name : cfg.getStringList("mode_names")) {
            try {
                biomeList.add(Biome.valueOf(name.toUpperCase()));
            } catch (Exception ignored) {}
        }
        if (biomeList.isEmpty()) biomeList.add(Biome.PLAINS);
    }

    @Override
    public void onRightClick(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!player.hasPermission("forgetools.use.biome_stitcher")) {
            player.sendMessage(ChatColor.RED + "No permission.");
            return;
        }
        if (!canActivate(player, item)) return;

        int mode = getMode(item);
        if (mode >= biomeList.size()) mode = 0;

        Biome targetBiome = biomeList.get(mode);
        Location loc = player.getLocation();
        World world = loc.getWorld();
        if (world == null) return;

        int cx = loc.getBlockX();
        int cy = loc.getBlockY();
        int cz = loc.getBlockZ();
        int half = sectionSize / 2;
        int minY = Math.max(world.getMinHeight(), cy - half);
        int maxY = Math.min(world.getMaxHeight() - 1, cy + half);

        int changed = 0;
        for (int x = cx - half; x < cx + half; x++) {
            for (int z = cz - half; z < cz + half; z++) {
                for (int y = minY; y <= maxY; y++) {
                    world.setBiome(x, y, z, targetBiome);
                    changed++;
                }
            }
        }

        world.refreshChunk(loc.getChunk().getX(), loc.getChunk().getZ());

        player.sendMessage(ChatColor.GREEN + "✔ Biome set to " + ChatColor.YELLOW + targetBiome.name()
                + ChatColor.GREEN + " for " + changed + " sections.");

        if (plugin.getConfig().getBoolean("settings.sounds_enabled", true)) {
            player.playSound(player.getLocation(), Sound.BLOCK_MOSS_PLACE, 1.0f, 0.8f);
        }
        if (plugin.getConfig().getBoolean("settings.particles_enabled", true)) {
            loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc.add(0, 1, 0),
                    30, half, 2, half, 0.1);
        }

        postAction(player, item);
    }
}
