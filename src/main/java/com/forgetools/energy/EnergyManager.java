package com.forgetools.energy;

import com.forgetools.ForgeToolsPlugin;
import com.forgetools.Keys;
import com.forgetools.api.CustomTool;
import com.forgetools.registry.ToolRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Two-rate energy system:
 *
 *  FAST task  (energy_regen_interval_ticks, default 4t):
 *    Increments an in-memory counter only. Zero packets, zero ItemMeta writes.
 *
 *  SLOW task  (energy_sync_interval_ticks, default 60t):
 *    Flushes in-memory values → PDC + rebuilds lore only when value changed.
 *    One SET_SLOT packet per changed slot, every ~3 seconds.
 *
 *  On tool use  (consumeEnergy / refillEnergy):
 *    Immediate PDC + lore write for that slot only.
 *
 *  On player quit:
 *    Full flush so energy persists across restarts.
 *
 *  Slot 36 = off-hand.
 */
public class EnergyManager implements Listener {

    private final ForgeToolsPlugin plugin;
    private final ToolRegistry registry;

    private final Map<UUID, Map<Integer, Integer>> cache = new HashMap<>();

    private BukkitTask regenTask;
    private BukkitTask syncTask;

    public EnergyManager(ForgeToolsPlugin plugin, ToolRegistry registry) {
        this.plugin = plugin;
        this.registry = registry;
    }

    public void start() {
        long regenInterval = plugin.getConfig().getLong("settings.energy_regen_interval_ticks", 4L);
        long syncInterval  = plugin.getConfig().getLong("settings.energy_sync_interval_ticks", 60L);
        int  regenAmount   = plugin.getConfig().getInt("settings.energy_regen_amount", 1);

        regenTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    regenInMemory(player, regenAmount);
                }
            }
        }.runTaskTimer(plugin, regenInterval, regenInterval);

        syncTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    flushToInventory(player);
                }
            }
        }.runTaskTimer(plugin, syncInterval, syncInterval);

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void stop() {
        if (regenTask != null && !regenTask.isCancelled()) regenTask.cancel();
        if (syncTask  != null && !syncTask.isCancelled())  syncTask.cancel();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            flushToPdc(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        flushToPdc(event.getPlayer());
        cache.remove(event.getPlayer().getUniqueId());
    }

    // ── Public API ────────────────────────────────────────────────────

    public int getEnergy(Player player, ItemStack item, CustomTool tool) {
        int slot = findSlot(player, item);
        if (slot == -1) return readPdc(item, tool);
        return cache
                .computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                .computeIfAbsent(slot, k -> readPdc(item, tool));
    }

    public void consumeEnergy(Player player, ItemStack item, CustomTool tool, int amount) {
        int slot = findSlot(player, item);
        if (slot == -1) return;
        Map<Integer, Integer> playerCache = cache.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        int current = playerCache.computeIfAbsent(slot, k -> readPdc(item, tool));
        int newVal = Math.max(0, current - amount);
        playerCache.put(slot, newVal);
        flushSlot(player, item, tool, slot, newVal);
    }

    public void refillEnergy(Player player, ItemStack item, CustomTool tool, int amount) {
        int slot = findSlot(player, item);
        if (slot == -1) return;
        Map<Integer, Integer> playerCache = cache.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        int current = playerCache.computeIfAbsent(slot, k -> readPdc(item, tool));
        int newVal = Math.min(tool.getMaxEnergy(), current + amount);
        playerCache.put(slot, newVal);
        flushSlot(player, item, tool, slot, newVal);
    }

    // ── Internal ──────────────────────────────────────────────────────

    private void regenInMemory(Player player, int amount) {
        Map<Integer, Integer> playerCache = cache.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        for (int i = 0; i < 37; i++) {
            ItemStack item = (i == 36)
                    ? player.getInventory().getItemInOffHand()
                    : player.getInventory().getItem(i);
            if (!registry.isToolItem(item)) continue;
            CustomTool tool = registry.getToolFromItem(item);
            if (tool == null) continue;
            int current = playerCache.computeIfAbsent(i, k -> readPdc(item, tool));
            if (current < tool.getMaxEnergy()) {
                playerCache.put(i, Math.min(tool.getMaxEnergy(), current + amount));
            }
        }
    }

    private void flushToInventory(Player player) {
        Map<Integer, Integer> playerCache = cache.get(player.getUniqueId());
        if (playerCache == null || playerCache.isEmpty()) return;

        for (Map.Entry<Integer, Integer> entry : playerCache.entrySet()) {
            int slot   = entry.getKey();
            int energy = entry.getValue();

            ItemStack item = (slot == 36)
                    ? player.getInventory().getItemInOffHand()
                    : player.getInventory().getItem(slot);

            if (!registry.isToolItem(item)) continue;
            CustomTool tool = registry.getToolFromItem(item);
            if (tool == null) continue;

            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            int pdcEnergy = meta.getPersistentDataContainer()
                    .getOrDefault(Keys.ENERGY, PersistentDataType.INTEGER, tool.getMaxEnergy());

            if (pdcEnergy == energy) continue; // sin cambio → no packet

            int mode = meta.getPersistentDataContainer()
                    .getOrDefault(Keys.MODE, PersistentDataType.INTEGER, 0);
            int uses = meta.getPersistentDataContainer()
                    .getOrDefault(Keys.USES_REMAINING, PersistentDataType.INTEGER, tool.getMaxUses());

            meta.getPersistentDataContainer().set(Keys.ENERGY, PersistentDataType.INTEGER, energy);
            tool.buildLoreInMeta(meta, energy, mode, uses);
            item.setItemMeta(meta);

            if (slot == 36) player.getInventory().setItemInOffHand(item);
            else            player.getInventory().setItem(slot, item);
        }
    }

    private void flushToPdc(Player player) {
        Map<Integer, Integer> playerCache = cache.get(player.getUniqueId());
        if (playerCache == null) return;
        for (Map.Entry<Integer, Integer> entry : playerCache.entrySet()) {
            int slot   = entry.getKey();
            int energy = entry.getValue();
            ItemStack item = (slot == 36)
                    ? player.getInventory().getItemInOffHand()
                    : player.getInventory().getItem(slot);
            if (!registry.isToolItem(item)) continue;
            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;
            meta.getPersistentDataContainer().set(Keys.ENERGY, PersistentDataType.INTEGER, energy);
            item.setItemMeta(meta);
            if (slot == 36) player.getInventory().setItemInOffHand(item);
            else            player.getInventory().setItem(slot, item);
        }
    }

    private void flushSlot(Player player, ItemStack item, CustomTool tool, int slot, int energy) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        int mode = meta.getPersistentDataContainer()
                .getOrDefault(Keys.MODE, PersistentDataType.INTEGER, 0);
        int uses = meta.getPersistentDataContainer()
                .getOrDefault(Keys.USES_REMAINING, PersistentDataType.INTEGER, tool.getMaxUses());
        meta.getPersistentDataContainer().set(Keys.ENERGY, PersistentDataType.INTEGER, energy);
        tool.buildLoreInMeta(meta, energy, mode, uses);
        item.setItemMeta(meta);
        if (slot == 36) player.getInventory().setItemInOffHand(item);
        else            player.getInventory().setItem(slot, item);
    }

    private int findSlot(Player player, ItemStack item) {
        ItemStack main = player.getInventory().getItemInMainHand();
        if (main.isSimilar(item)) return player.getInventory().getHeldItemSlot();
        ItemStack off = player.getInventory().getItemInOffHand();
        if (off.isSimilar(item)) return 36;
        for (int i = 0; i < 36; i++) {
            ItemStack slot = player.getInventory().getItem(i);
            if (slot != null && slot.isSimilar(item)) return i;
        }
        return -1;
    }

    private int readPdc(ItemStack item, CustomTool tool) {
        if (item == null || !item.hasItemMeta()) return tool.getMaxEnergy();
        return item.getItemMeta().getPersistentDataContainer()
                .getOrDefault(Keys.ENERGY, PersistentDataType.INTEGER, tool.getMaxEnergy());
    }
}
