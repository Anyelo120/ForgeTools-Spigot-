package com.forgetools.lang;

import com.forgetools.ForgeToolsPlugin;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LangManager {

    private final ForgeToolsPlugin plugin;
    private final Map<String, String> messages = new HashMap<>();
    private final Map<UUID, Map<String, Long>> throttleCache = new HashMap<>();
    private String loadedLang;

    public LangManager(ForgeToolsPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        messages.clear();
        throttleCache.clear();
        String lang = plugin.getConfig().getString("settings.language", "es");
        this.loadedLang = lang;

        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists()) langDir.mkdirs();

        File langFile = new File(langDir, lang + ".yml");
        if (!langFile.exists()) {
            InputStream stream = plugin.getResource("lang/" + lang + ".yml");
            if (stream != null) plugin.saveResource("lang/" + lang + ".yml", false);
        }

        FileConfiguration defaults = loadFromJar(lang);
        FileConfiguration custom = YamlConfiguration.loadConfiguration(langFile);
        if (defaults != null) custom.setDefaults(defaults);

        for (String key : custom.getKeys(true)) {
            if (custom.isString(key)) messages.put(key, custom.getString(key));
        }

        plugin.getLogger().info("Language loaded: " + lang + " (" + messages.size() + " keys)");
    }

    public String get(String key, Object... args) {
        String raw = messages.getOrDefault(key, "&c[Missing: " + key + "]");
        raw = ChatColor.translateAlternateColorCodes('&', raw);
        for (int i = 0; i + 1 < args.length; i += 2) {
            raw = raw.replace("{" + args[i] + "}", String.valueOf(args[i + 1]));
        }
        return raw;
    }

    public void send(Player player, String key, long cooldownMs, Object... args) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        Map<String, Long> playerMap = throttleCache.computeIfAbsent(uuid, k -> new HashMap<>());
        Long last = playerMap.get(key);
        if (last != null && now - last < cooldownMs) return;
        playerMap.put(key, now);
        player.sendMessage(get(key, args));
    }

    public void clearThrottle(UUID uuid) {
        throttleCache.remove(uuid);
    }

    private FileConfiguration loadFromJar(String lang) {
        InputStream stream = plugin.getResource("lang/" + lang + ".yml");
        if (stream == null) {
            plugin.getLogger().warning("No bundled lang file for: " + lang + ". Falling back to 'es'.");
            stream = plugin.getResource("lang/es.yml");
        }
        if (stream == null) return null;
        return YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    public String getLoadedLang() { return loadedLang; }
}
