package me.HeyAwesomePeople.ChestGenerators;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;

public class ChestRunnable {
    private ChestGenerators plugin = ChestGenerators.instance;

    public Location location;
    public ChestGeneratorType type;
    public Integer interval;
    public BukkitTask task = null;

    public ChestRunnable(Location l, Integer interval, ChestGeneratorType type) {
        this.location = l;
        this.type = type;
        this.interval = interval;
    }

    public void createTask(final long delay, final long interval) {
        task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            public void run() {
                FileConfiguration config = plugin.chestConfig.getCustomConfig();
                if (config.contains("chest." + type.configName)) {
                    for (String s : config.getConfigurationSection("chest." + type.configName).getKeys(false)) {
                        config.set("chest." + type.configName + s, config.getInt("chest." + type.configName + s) + type.amount);
                    }
                    plugin.chestConfig.saveCustomConfig();
                }
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

}
