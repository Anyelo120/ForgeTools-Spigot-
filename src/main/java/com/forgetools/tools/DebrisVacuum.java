package com.forgetools.tools;

import com.forgetools.ForgeToolsPlugin;
import com.forgetools.api.CustomTool;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

public class DebrisVacuum extends CustomTool {

    private final int maxDistance;
    private final Set<Material> debrisTypes;

    public DebrisVacuum(ForgeToolsPlugin plugin, ConfigurationSection cfg) {
        super(plugin, "debris_vacuum", cfg);
        this.maxDistance = cfg.getInt("max_distance", 6);
        this.debrisTypes = new HashSet<>();
        for (String s : cfg.getStringList("debris_types")) {
            try { debrisTypes.add(Material.valueOf(s.toUpperCase())); } catch (Exception ignored) {}
        }
    }

    @Override
    public void onRightClick(PlayerInteractEvent event, Player player, ItemStack item) {
        if (!canActivate(player, item)) return;

        List<Block> targets = collectDebrisInRay(player);
        if (targets.isEmpty()) return;

        int count = 0;
        int max = plugin.getConfig().getInt("settings.max_blocks_per_tick", 25);
        List<ItemStack> overflow = new ArrayList<>();

        for (Block b : targets) {
            if (count >= max) break;
            if (!plugin.getProtectionManager().canBuild(player, b)) {
                spawnBlockedParticle(b);
                continue;
            }
            Collection<ItemStack> drops = b.getDrops(item);
            plugin.getCoreProtectLogger().logRemoval(player, b);
            b.setType(Material.AIR, false);
            for (ItemStack drop : drops) {
                Map<Integer, ItemStack> leftover = player.getInventory().addItem(drop);
                overflow.addAll(leftover.values());
            }
            spawnSuckParticle(b);
            count++;
        }

        for (ItemStack leftover : overflow) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        }

        if (count > 0 && plugin.getConfig().getBoolean("settings.sounds_enabled", true)) {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.8f);
        }

        postAction(player, item);
    }

    private List<Block> collectDebrisInRay(Player player) {
        List<Block> result = new ArrayList<>();
        Location eye = player.getEyeLocation();
        Vector dir = eye.getDirection().normalize();

        for (int i = 1; i <= maxDistance; i++) {
            Location loc = eye.clone().add(dir.clone().multiply(i));
            Block b = loc.getBlock();
            if (b.getType().isAir()) continue;
            if (debrisTypes.contains(b.getType())) {
                result.add(b);
            } else if (b.getType().isSolid()) {
                break;
            }
        }
        return result;
    }

    private void spawnSuckParticle(Block b) {
        if (!plugin.getConfig().getBoolean("settings.particles_enabled", true)) return;
        b.getWorld().spawnParticle(Particle.PORTAL, b.getLocation().add(0.5, 0.5, 0.5),
                8, 0.3, 0.3, 0.3, 0.1);
    }

    private void spawnBlockedParticle(Block b) {
        if (!plugin.getConfig().getBoolean("settings.particles_enabled", true)) return;
        b.getWorld().spawnParticle(Particle.SMOKE, b.getLocation().add(0.5, 0.5, 0.5),
                5, 0.2, 0.2, 0.2, 0.02);
    }
}
