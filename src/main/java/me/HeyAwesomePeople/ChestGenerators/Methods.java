package me.HeyAwesomePeople.ChestGenerators;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Methods {
    private ChestGenerators plugin = ChestGenerators.instance;

    /*
    Method cleanChests will search every config entry and check if the chest is there. If it isn't, remove it from the config.
    */
    public void cleanChests() {
        for (ChestGeneratorType gen : plugin.generators.values()) {
            gen.cleanChests();
        }
    }

    public void loadChestGenerators() {
        FileConfiguration config = plugin.genConfig.getCustomConfig();
        int a = 0;
        for (String s : config.getConfigurationSection("generators").getKeys(false)) {
            plugin.generators.put(s, new ChestGeneratorType(s.toLowerCase(), config.getInt("generators." + s + ".quantity"), Utils.convertColor(config.getString("generators." + s + ".name")),
                    getItems("generators." + s + ".item", config),
                    config.getInt("generators." + s + ".tickRate"),
                    config.getStringList("generators." + s + ".lore")));
            a++;
        }
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[ChestGenerators] Loaded " + a + " generators!");
    }

    public boolean doesConfigHaveGenerator(String s) {
        return plugin.chestConfig.getCustomConfig().contains("generators." + s);
    }

    public List<ItemStack> getItems(String s, FileConfiguration config) {
        List<ItemStack> items = new ArrayList<ItemStack>();
        if (!config.getString(s).contains(":")) {
            items.add(new ItemStack(Material.getMaterial(config.getString(s))));
            return items;
        }
        String[] strings = config.getString(s).split(":");
        for (String st : strings) {
            if (Material.getMaterial(st) == null) continue;
            items.add(new ItemStack(Material.getMaterial(st)));
        }
        return items;
    }


}
