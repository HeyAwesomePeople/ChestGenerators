package me.HeyAwesomePeople.ChestGenerators.CustomConfigs;

import me.HeyAwesomePeople.ChestGenerators.ChestGenerators;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

public class GeneratorConfig {

    private ChestGenerators plugin = ChestGenerators.instance;

    private FileConfiguration customConfig = null;
    private File customConfigFile = null;

    public void reloadCustomConfig() {
        if (customConfigFile == null) {
            customConfigFile = new File(plugin.getDataFolder(), "generators.yml");
        }
        customConfig = YamlConfiguration.loadConfiguration(customConfigFile);

        // Look for defaults in the jar
        InputStream defConfigStream = plugin.getResource("generators.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            customConfig.setDefaults(defConfig);
        }
    }

    public FileConfiguration getCustomConfig() {
        if (customConfig == null) {
            reloadCustomConfig();
        }
        return customConfig;
    }

    public void saveCustomConfig() {
        if (customConfig == null || customConfigFile == null) {
            return;
        }
        try {
            getCustomConfig().save(customConfigFile);
        } catch (IOException ex) {
           plugin.getLogger().log(Level.SEVERE, "Could not save config to " + customConfigFile, ex);
        }
    }

}
