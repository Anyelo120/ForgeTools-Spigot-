package com.forgetools.api;

import com.forgetools.ForgeToolsPlugin;
import com.forgetools.Keys;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public abstract class CustomTool {

    protected final ForgeToolsPlugin plugin;
    protected final String id;
    protected final String displayName;
    protected final Material baseMaterial;
    protected final int maxEnergy;
    protected final int energyCost;
    protected final int cooldownTicks;
    protected final int maxUses;
    protected final List<String> modeNames;

    private final Map<UUID, Long> cooldownMap = new HashMap<>();

    protected CustomTool(ForgeToolsPlugin plugin, String id, ConfigurationSection cfg) {
        this.plugin = plugin;
        this.id = id;
        this.displayName = org.bukkit.ChatColor.translateAlternateColorCodes('&',
                cfg.getString("display_name", "&7" + id));
        this.baseMaterial = Material.valueOf(cfg.getString("base", "STICK").toUpperCase());
        this.maxEnergy    = cfg.getInt("energy_max", 100);
        this.energyCost   = cfg.getInt("energy_cost", 10);
        this.cooldownTicks = cfg.getInt("cooldown_ticks", 10);
        this.maxUses      = cfg.getInt("max_uses", 500);
        this.modeNames    = new ArrayList<>(cfg.getStringList("mode_names"));
        if (modeNames.isEmpty()) modeNames.add("Default");
    }

    // ── Abstract interface ────────────────────────────────────────────

    public abstract void onRightClick(PlayerInteractEvent event, Player player, ItemStack item);
    public void onBlockBreak(BlockBreakEvent event, Player player, ItemStack item) {}
    public void onMove(PlayerMoveEvent event, Player player, ItemStack item) {}
    public boolean handlesBlockBreak() { return false; }
    public boolean handlesMove()       { return false; }

    // ── Mode switch ───────────────────────────────────────────────────

    public void onModeSwitch(Player player, ItemStack item) {
        int current = getMode(item);
        int next    = (current + 1) % modeNames.size();
        setMode(item, next);
        player.sendMessage(plugin.getLangManager().get("mode.switched", "mode", modeNames.get(next)));
        if (plugin.getConfig().getBoolean("settings.sounds_enabled", true)) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
        }
    }

    // ── Lifecycle helpers  ────────────────────────────────────────────

    /**
     * Call at the top of every onRightClick / onBlockBreak.
     * Checks cooldown, energy (via in-memory cache), and permission.
     */
    protected boolean canActivate(Player player, ItemStack item) {
        long now  = System.currentTimeMillis();
        Long last = cooldownMap.get(player.getUniqueId());
        if (last != null && now - last < (long) cooldownTicks * 50L) {
            long remaining = ((last + (long) cooldownTicks * 50L) - now) / 1000L + 1L;
            plugin.getLangManager().send(player, "cooldown.active", 1500L, "seconds", remaining);
            return false;
        }
        if (!player.hasPermission("forgetools.use." + id)) {
            plugin.getLangManager().send(player, "general.no_permission", 3000L);
            return false;
        }
        int energy = plugin.getEnergyManager().getEnergy(player, item, this);
        if (energy < energyCost) {
            plugin.getLangManager().send(player, "energy.not_enough", 2000L,
                    "current", energy, "required", energyCost);
            return false;
        }
        return true;
    }

    /**
     * Call at the end of every successful tool use.
     * Consumes energy (immediate PDC write), decrements uses, handles tool break.
     */
    protected void postAction(Player player, ItemStack item) {
        cooldownMap.put(player.getUniqueId(), System.currentTimeMillis());

        if (item == null || !item.hasItemMeta()) return;

        plugin.getEnergyManager().consumeEnergy(player, item, this, energyCost);

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        int newUses = Math.max(0, meta.getPersistentDataContainer()
                .getOrDefault(Keys.USES_REMAINING, PersistentDataType.INTEGER, maxUses) - 1);

        if (newUses <= 0) {
            breakTool(player, item);
            return;
        }

        int mode   = meta.getPersistentDataContainer().getOrDefault(Keys.MODE, PersistentDataType.INTEGER, 0);
        int energy = plugin.getEnergyManager().getEnergy(player, item, this);
        meta.getPersistentDataContainer().set(Keys.USES_REMAINING, PersistentDataType.INTEGER, newUses);
        buildLoreInMeta(meta, energy, mode, newUses);
        item.setItemMeta(meta);
    }

    // ── Lore builder ─────────────────────────────────────────────────

    /**
     * Rebuilds lore in an already-fetched ItemMeta.
     * Caller is responsible for calling item.setItemMeta(meta) afterwards.
     */
    public void buildLoreInMeta(ItemMeta meta, int energy, int mode, int uses) {
        String modeName = (mode >= 0 && mode < modeNames.size()) ? modeNames.get(mode) : "?";
        List<String> lore = new ArrayList<>();
        lore.add(buildBar(plugin.getLangManager().get("energy.label"), energy, maxEnergy, org.bukkit.ChatColor.YELLOW));
        lore.add(org.bukkit.ChatColor.GRAY + plugin.getLangManager().get("mode.label")
                + ": " + org.bukkit.ChatColor.AQUA + modeName);
        lore.add(buildBar(plugin.getLangManager().get("durability.label"), uses, maxUses, usesColor(uses)));
        lore.add("");
        if (modeNames.size() > 1) {
            lore.add(org.bukkit.ChatColor.DARK_GRAY + plugin.getLangManager().get("mode.hint_switch"));
        }
        meta.setLore(lore);
    }

    public void updateLore(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        int energy = meta.getPersistentDataContainer().getOrDefault(Keys.ENERGY, PersistentDataType.INTEGER, maxEnergy);
        int mode   = meta.getPersistentDataContainer().getOrDefault(Keys.MODE, PersistentDataType.INTEGER, 0);
        int uses   = meta.getPersistentDataContainer().getOrDefault(Keys.USES_REMAINING, PersistentDataType.INTEGER, maxUses);
        buildLoreInMeta(meta, energy, mode, uses);
        item.setItemMeta(meta);
    }

    // ── Tool break ───────────────────────────────────────────────────

    protected void breakTool(Player player, ItemStack item) {
        if (plugin.getConfig().getBoolean("settings.sounds_enabled", true)) {
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 0.8f);
        }
        player.sendMessage(plugin.getLangManager().get("durability.broken", "tool", displayName));
        removeFromInventory(player);
    }

    private void removeFromInventory(Player player) {
        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < 36; i++) {
            ItemStack slot = inv.getItem(i);
            if (matchesThis(slot)) { inv.setItem(i, null); return; }
        }
        if (matchesThis(inv.getItemInOffHand())) { inv.setItemInOffHand(new ItemStack(Material.AIR)); return; }
    }

    private boolean matchesThis(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return false;
        return id.equals(stack.getItemMeta().getPersistentDataContainer()
                .get(Keys.TOOL_ID, PersistentDataType.STRING));
    }

    // ── PDC helpers ───────────────────────────────────────────────────

    public int getMode(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        return item.getItemMeta().getPersistentDataContainer()
                .getOrDefault(Keys.MODE, PersistentDataType.INTEGER, 0);
    }

    public void setMode(ItemStack item, int mode) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        int energy = meta.getPersistentDataContainer().getOrDefault(Keys.ENERGY, PersistentDataType.INTEGER, maxEnergy);
        int uses   = meta.getPersistentDataContainer().getOrDefault(Keys.USES_REMAINING, PersistentDataType.INTEGER, maxUses);
        meta.getPersistentDataContainer().set(Keys.MODE, PersistentDataType.INTEGER, mode);
        buildLoreInMeta(meta, energy, mode, uses);
        item.setItemMeta(meta);
    }

    public int getUsesRemaining(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return maxUses;
        return item.getItemMeta().getPersistentDataContainer()
                .getOrDefault(Keys.USES_REMAINING, PersistentDataType.INTEGER, maxUses);
    }

    // ── Lore utilities ────────────────────────────────────────────────

    private org.bukkit.ChatColor usesColor(int uses) {
        double ratio = (double) uses / maxUses;
        if (ratio > 0.5) return org.bukkit.ChatColor.GREEN;
        if (ratio > 0.2) return org.bukkit.ChatColor.YELLOW;
        return org.bukkit.ChatColor.RED;
    }

    private String buildBar(String label, int current, int max, org.bukkit.ChatColor color) {
        int filled = max > 0 ? (int) Math.round((double) current / max * 10) : 0;
        String bar = color + "█".repeat(Math.max(0, filled))
                + org.bukkit.ChatColor.DARK_GRAY + "░".repeat(Math.max(0, 10 - filled));
        return org.bukkit.ChatColor.GRAY + label + ": " + bar
                + org.bukkit.ChatColor.WHITE + " " + current + "/" + max;
    }

    // ── Getters ───────────────────────────────────────────────────────

    public String getId()              { return id; }
    public String getDisplayName()     { return displayName; }
    public Material getBaseMaterial()  { return baseMaterial; }
    public int getMaxEnergy()          { return maxEnergy; }
    public int getMaxUses()            { return maxUses; }
    public List<String> getModeNames() { return modeNames; }
}
