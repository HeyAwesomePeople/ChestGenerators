package me.HeyAwesomePeople.ChestGenerators;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.sql.SQLException;
import java.util.Random;

public class Chests {
    private ChestGenerators plugin = ChestGenerators.instance;

    public Location location;
    public ChestGeneratorType type;
    public Integer interval;

    public Integer amountThatCanBeAdded = 0;

    public Chests(Location l, ChestGeneratorType type) {
        this.location = l;
        this.type = type;
        this.interval = type.regen;
        save();
    }

    public void increase() {
        amountThatCanBeAdded++;
    }

    public void updateChest(Chest c) {
        FileConfiguration config = plugin.chestConfig.getCustomConfig();
        for (int valuea = 1; valuea <= config.getInt("chest." + type.configName + "." + Utils.locationToString(location)); valuea++) {
            Random r = new Random();
            int value = r.nextInt(type.items.size());
            ItemStack i = type.items.get(value);
            c.getBlockInventory().addItem(i);
        }
        reset();
        c.update();
    }

    public void save() {
        FileConfiguration config = plugin.chestConfig.getCustomConfig();
        if (config.contains("chest." + type.configName + "." + Utils.locationToString(location))) return;
        config.set("chest." + type.configName + "." + Utils.locationToString(location), 0);
        plugin.chestConfig.saveCustomConfig();
    }

    public void reset() {
        FileConfiguration config = plugin.chestConfig.getCustomConfig();
        config.set("chest." + type.configName + "." + Utils.locationToString(location), 0);
        plugin.chestConfig.saveCustomConfig();
    }

    public void remove() {
        type.chests.remove(this);

        FileConfiguration config = plugin.chestConfig.getCustomConfig();
        config.set("chest." + type.configName + "." + Utils.locationToString(location), null);
        plugin.chestConfig.saveCustomConfig();

    }
}
