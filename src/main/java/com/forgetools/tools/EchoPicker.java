package com.forgetools.tools;

import com.forgetools.ForgeToolsPlugin;
import com.forgetools.api.CustomTool;
import com.forgetools.util.BlockUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EchoPicker extends CustomTool {

    private final int floodRadius;
    private final int maxEchoBlocks;
    private final Map<UUID, Material> lastBroken = new HashMap<>();

    public EchoPicker(ForgeToolsPlugin plugin, ConfigurationSection cfg) {
        super(plugin, "echo_picker", cfg);
        this.floodRadius = cfg.getInt("flood_radius", 3);
        this.maxEchoBlocks = Math.min(cfg.getInt("max_echo_blocks", 25),
                plugin.getConfig().getInt("settings.max_blocks_per_tick", 25));
    }

    @Override
    public boolean handlesBlockBreak() { return true; }

    @Override
    public void onRightClick(PlayerInteractEvent event, Player player, ItemStack item) {}

    @Override
    public void onBlockBreak(BlockBreakEvent event, Player player, ItemStack item) {
        Block target = event.getBlock();
        Material type = target.getType();
        int mode = getMode(item);

        if (mode == 0) {
            lastBroken.put(player.getUniqueId(), type);
            return;
        }

        if (!canActivate(player, item)) return;

        Material remembered = lastBroken.getOrDefault(player.getUniqueId(), type);
        if (type != remembered) return;

        event.setCancelled(true);

        List<Block> connected = BlockUtil.floodFillSameType(target, type, floodRadius, maxEchoBlocks);
        for (Block b : connected) {
            if (!plugin.getProtectionManager().canBuild(player, b)) continue;
            plugin.getCoreProtectLogger().logRemoval(player, b);
            b.breakNaturally(item);
            spawnParticle(b);
        }

        lastBroken.put(player.getUniqueId(), type);

        if (plugin.getConfig().getBoolean("settings.sounds_enabled", true)) {
            player.playSound(player.getLocation(), Sound.BLOCK_SCULK_SENSOR_CLICKING, 0.8f, 1.3f);
        }

        postAction(player, item);
    }

    private void spawnParticle(Block b) {
        if (!plugin.getConfig().getBoolean("settings.particles_enabled", true)) return;
        b.getWorld().spawnParticle(Particle.SONIC_BOOM, b.getLocation().add(0.5, 0.5, 0.5),
                1, 0.1, 0.1, 0.1, 0);
    }
}
