package com.forgetools.lang;

import com.forgetools.ForgeToolsPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LangManager {

    private final ForgeToolsPlugin plugin;
    private final Map<String, String> messages = new HashMap<>();
    private String loadedLang;

    public LangManager(ForgeToolsPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        messages.clear();
        String lang = plugin.getConfig().getString("settings.language", "es");
        this.loadedLang = lang;

        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists()) langDir.mkdirs();

        File langFile = new File(langDir, lang + ".yml");
        if (!langFile.exists()) {
            saveDefaultLang(lang, langFile);
        }

        FileConfiguration defaults = loadFromJar(lang);
        FileConfiguration custom = YamlConfiguration.loadConfiguration(langFile);

        if (defaults != null) {
            custom.setDefaults(defaults);
        }

        for (String key : custom.getKeys(true)) {
            if (custom.isString(key)) {
                messages.put(key, custom.getString(key));
            }
        }

        plugin.getLogger().info("Language loaded: " + lang + " (" + messages.size() + " keys)");
    }

    private void saveDefaultLang(String lang, File target) {
        InputStream stream = plugin.getResource("lang/" + lang + ".yml");
        if (stream != null) {
            plugin.saveResource("lang/" + lang + ".yml", false);
        }
    }

    private FileConfiguration loadFromJar(String lang) {
        InputStream stream = plugin.getResource("lang/" + lang + ".yml");
        if (stream == null) {
            plugin.getLogger().warning("No bundled lang file for: " + lang + ". Trying 'es' fallback.");
            stream = plugin.getResource("lang/es.yml");
        }
        if (stream == null) return null;
        return YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    public String get(String key, Object... args) {
        String raw = messages.getOrDefault(key, "&c[Missing: " + key + "]");
        raw = org.bukkit.ChatColor.translateAlternateColorCodes('&', raw);
        for (int i = 0; i < args.length - 1; i += 2) {
            raw = raw.replace("{" + args[i] + "}", String.valueOf(args[i + 1]));
        }
        return raw;
    }

    public String getLoadedLang() { return loadedLang; }
}
