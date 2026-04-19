package com.forgetools.tools;

import com.forgetools.ForgeToolsPlugin;
import com.forgetools.api.CustomTool;
import com.forgetools.util.SmeltMap;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ResourceRecycler extends CustomTool {

    private final double smeltChance;
    private final Random rng = new Random();

    public ResourceRecycler(ForgeToolsPlugin plugin, ConfigurationSection cfg) {
        super(plugin, "resource_recycler", cfg);
        this.smeltChance = cfg.getDouble("smelt_chance", 0.5);
    }

    @Override
    public boolean handlesBlockBreak() { return true; }

    @Override
    public void onRightClick(PlayerInteractEvent event, Player player, ItemStack item) {}

    @Override
    public void onBlockBreak(BlockBreakEvent event, Player player, ItemStack item) {
        if (!canActivate(player, item)) return;

        Block block = event.getBlock();
        Material blockType = block.getType();

        event.setDropItems(false);
        event.setCancelled(false);

        Collection<ItemStack> drops = block.getDrops(item);
        plugin.getCoreProtectLogger().logRemoval(player, block);

        Map<Integer, ItemStack> overflow = new HashMap<>();

        for (ItemStack drop : drops) {
            Material smelted = SmeltMap.getSmeltResult(drop.getType());
            if (smelted != null && rng.nextDouble() < smeltChance) {
                ItemStack smeltedStack = new ItemStack(smelted, drop.getAmount());
                overflow.putAll(player.getInventory().addItem(smeltedStack));
                spawnSmeltParticle(block);
            } else {
                overflow.putAll(player.getInventory().addItem(drop));
            }
        }

        for (ItemStack leftover : overflow.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        }

        if (plugin.getConfig().getBoolean("settings.sounds_enabled", true)) {
            player.playSound(player.getLocation(), Sound.BLOCK_FURNACE_FIRE_CRACKLE, 0.7f, 1.2f);
        }

        postAction(player, item);
    }

    private void spawnSmeltParticle(Block b) {
        if (!plugin.getConfig().getBoolean("settings.particles_enabled", true)) return;
        b.getWorld().spawnParticle(Particle.FLAME, b.getLocation().add(0.5, 0.5, 0.5),
                6, 0.3, 0.3, 0.3, 0.05);
        b.getWorld().spawnParticle(Particle.LAVA, b.getLocation().add(0.5, 0.5, 0.5),
                2, 0.2, 0.2, 0.2, 0);
    }
}
