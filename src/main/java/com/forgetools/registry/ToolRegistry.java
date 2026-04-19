package com.forgetools.registry;

import com.forgetools.ForgeToolsPlugin;
import com.forgetools.Keys;
import com.forgetools.api.CustomTool;
import com.forgetools.tools.*;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.*;
import java.util.function.BiFunction;
import java.util.logging.Level;

public class ToolRegistry {

    private final ForgeToolsPlugin plugin;
    private final Map<String, CustomTool> tools = new LinkedHashMap<>();
    private FileConfiguration toolsConfig;

    private static final Map<String, BiFunction<ForgeToolsPlugin, ConfigurationSection, CustomTool>> FACTORIES = new LinkedHashMap<>();

    static {
        FACTORIES.put("chunk_hammer", ChunkHammer::new);
        FACTORIES.put("debris_vacuum", DebrisVacuum::new);
        FACTORIES.put("terrain_eraser", TerrainEraser::new);
        FACTORIES.put("bridge_forge", BridgeForge::new);
        FACTORIES.put("scaffold_launcher", ScaffoldLauncher::new);
        FACTORIES.put("fill_pulse", FillPulse::new);
        FACTORIES.put("wall_raiser", WallRaiser::new);
        FACTORIES.put("echo_picker", EchoPicker::new);
        FACTORIES.put("gravity_anchor", GravityAnchor::new);
        FACTORIES.put("light_weaver", LightWeaver::new);
        FACTORIES.put("resource_recycler", ResourceRecycler::new);
        FACTORIES.put("auto_level_shovel", AutoLevelShovel::new);
        FACTORIES.put("claim_polisher", ClaimPolisher::new);
        FACTORIES.put("phantom_placer", PhantomPlacer::new);
        FACTORIES.put("biome_stitcher", BiomeStitcher::new);
    }

    public ToolRegistry(ForgeToolsPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        tools.clear();
        File toolsFile = new File(plugin.getDataFolder(), "tools.yml");
        if (!toolsFile.exists()) {
            plugin.saveResource("tools.yml", false);
        }
        toolsConfig = YamlConfiguration.loadConfiguration(toolsFile);
        for (Map.Entry<String, BiFunction<ForgeToolsPlugin, ConfigurationSection, CustomTool>> entry : FACTORIES.entrySet()) {
            String id = entry.getKey();
            ConfigurationSection section = toolsConfig.getConfigurationSection(id);
            if (section == null) {
                plugin.getLogger().warning("No config section found for tool: " + id + " — skipping.");
                continue;
            }
            try {
                CustomTool tool = entry.getValue().apply(plugin, section);
                tools.put(id, tool);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load tool: " + id, e);
            }
        }
    }

    public CustomTool getTool(String id) {
        return tools.get(id);
    }

    public CustomTool getToolFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        String id = item.getItemMeta().getPersistentDataContainer()
                .get(Keys.TOOL_ID, PersistentDataType.STRING);
        if (id == null) return null;
        return tools.get(id);
    }

    public boolean isToolItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer()
                .has(Keys.TOOL_ID, PersistentDataType.STRING);
    }

    public ItemStack createToolItem(String id) {
        CustomTool tool = tools.get(id);
        if (tool == null) return null;
        ItemStack item = new ItemStack(tool.getBaseMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        meta.setDisplayName(tool.getDisplayName());
        meta.setUnbreakable(true);
        meta.getPersistentDataContainer().set(Keys.TOOL_ID, PersistentDataType.STRING, id);
        meta.getPersistentDataContainer().set(Keys.ENERGY, PersistentDataType.INTEGER, tool.getMaxEnergy());
        meta.getPersistentDataContainer().set(Keys.MODE, PersistentDataType.INTEGER, 0);
        meta.getPersistentDataContainer().set(Keys.USES_REMAINING, PersistentDataType.INTEGER, tool.getMaxUses());
        item.setItemMeta(meta);
        tool.updateLore(item);
        return item;
    }

    public Collection<CustomTool> getAllTools() {
        return Collections.unmodifiableCollection(tools.values());
    }

    public Set<String> getAllToolIds() {
        return Collections.unmodifiableSet(tools.keySet());
    }

    public int getToolCount() {
        return tools.size();
    }
}
