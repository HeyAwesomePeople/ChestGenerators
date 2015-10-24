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

import java.util.Random;

public class Chests {
    private ChestGenerators plugin = ChestGenerators.instance;

    public Location location;
    public ChestGeneratorType type;
    public Integer interval;
    public BukkitTask task = null;

    public Chests(Location l, ChestGeneratorType type) {
        this.location = l;
        this.type = type;
        this.interval = type.regen;
        save();

        startTask();
    }


    public void createTask(final long delay, final long interval) {
        Bukkit.broadcastMessage("Started task! " + delay + " : " + interval);
        task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            public void run() {
                long startTime = System.nanoTime();
                FileConfiguration config = plugin.chestConfig.getCustomConfig();
                if (config.contains("chest." + type.configName)) {
                    for (String s : config.getConfigurationSection("chest." + type.configName).getKeys(false)) {
                        config.set("chest." + type.configName + s, config.getInt("chest." + type.configName + s) + type.amount);
                    }
                    plugin.chestConfig.saveCustomConfig();
                }
                long endTime = System.nanoTime();
                long durations = (endTime - startTime);
                Bukkit.broadcastMessage("Time to up the config: " + durations / 100000 + "ms");
            }
        }, delay, interval);
    }

    public void stopTask() {
        this.task.cancel();
    }

    public void startTask() {
        createTask(nextLong(), this.interval);
    }

    long nextLong() {
        long leftLimit = 1L;
        long rightLimit = 20L;
        return leftLimit + (long) (Math.random() * (rightLimit - leftLimit));
    }

    public void updateChest(Chest c) {
        Random r = new Random();
        int value = r.nextInt(type.items.size());
        ItemStack i = type.items.get(value);
        c.getBlockInventory().addItem(i);
        c.update();
    }

    public void save() {
        FileConfiguration config = plugin.chestConfig.getCustomConfig();
        config.set("chest." + type.configName + Utils.locationToString(location), 0);
        plugin.chestConfig.saveCustomConfig();
    }

    public void remove() {
        type.chests.remove(this);
        stopTask();

        FileConfiguration config = plugin.chestConfig.getCustomConfig();
        config.set("chest." + type.configName + Utils.locationToString(location), null);
        plugin.chestConfig.saveCustomConfig();

    }
}
