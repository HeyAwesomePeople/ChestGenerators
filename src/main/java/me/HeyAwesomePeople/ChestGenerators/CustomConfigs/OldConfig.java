package me.HeyAwesomePeople.ChestGenerators.CustomConfigs;

import me.HeyAwesomePeople.ChestGenerators.ChestGenerators;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;

public class OldConfig {
    private ChestGenerators plugin = ChestGenerators.instance;

    private FileConfiguration customConfig = null;
    private File customConfigFile = null;

    public void reloadOldConfig() {
        if (customConfigFile == null) {
            customConfigFile = new File("plugins/ConfigurableChestGenerators", "chests.yml");
        }
        customConfig = YamlConfiguration.loadConfiguration(customConfigFile);

        InputStream defConfigStream = plugin.getResource("plugins" + File.separator + "ConfigurableChestGenerators" + File.separator + "chests.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            customConfig.setDefaults(defConfig);
        }
    }

    public FileConfiguration getOldConfig() {
        if (customConfig == null) {
            reloadOldConfig();
        }
        return customConfig;
    }

}
