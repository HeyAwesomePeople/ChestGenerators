package me.HeyAwesomePeople.ChestGenerators;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChestGeneratorType {
    private ChestGenerators plugin = ChestGenerators.instance;

    public String configName;
    public String name;
    public Integer amount;
    public List<ItemStack> items = new ArrayList<ItemStack>();
    public Integer regen;
    public List<String> lore = new ArrayList<String>();
    public List<Chests> chests = new ArrayList<Chests>();

    public BukkitTask task = null;

    public ChestGeneratorType(String configName, Integer amount, String name, List<ItemStack> items, Integer regen, List<String> lore) {
        this.configName = configName;
        this.name = name;
        this.amount = amount;
        this.items = items;
        this.regen = regen;
        this.lore = lore;

        loadChests();
        startTask();
    }

    public void createTask(final long delay, final long interval) {
        task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            public void run() {

                String mysqlS = "INSERT INTO chests (Location, ToAdd) VALUES ";
                int runs = 0;
                for (Chests c : chests) {
                    if (runs == chests.size() - 1) {
                        mysqlS += "('" + Utils.locationToString(c.location) + "','" + c.amountThatCanBeAdded + "')";
                        break;
                    }
                    c.increase();
                    mysqlS += "('" + Utils.locationToString(c.location) + "','" + c.amountThatCanBeAdded + "'), ";
                    runs++;
                }

                final String state = mysqlS;

                Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                    public void run() {
                        java.sql.PreparedStatement statement;
                        try {
                            statement = plugin.sql.openConnection().prepareStatement(state);
                            statement.executeUpdate();
                        } catch (SQLException sqlE) {
                            sqlE.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });

                long startTime = System.nanoTime();
                int count = 0;
                // cleanChests();
                FileConfiguration config = plugin.chestConfig.getCustomConfig();
                if (config.contains("chest." + configName)) {
                    for (String s : config.getConfigurationSection("chest." + configName).getKeys(false)) {
                        config.set("chest." + configName + "." + s, config.getInt("chest." + configName + "." + s) + amount);
                        count++;
                    }
                    plugin.chestConfig.saveCustomConfig();
                }
                long endTime = System.nanoTime();
                Bukkit.broadcastMessage("Time it took to edit " + count + " chests in the config: " + (endTime - startTime) / 1000000 + "ms");
                // NOTE seems to take about 3ms
            }
        }, delay, interval);
    }

    public void stopTask() {
        this.task.cancel();
    }

    public void startTask() {
        createTask(nextLong(), this.regen);
    }

    long nextLong() {
        long leftLimit = 1L;
        long rightLimit = 20L;
        return leftLimit + (long) (Math.random() * (rightLimit - leftLimit));
    }

    public void loadChests() {
        FileConfiguration config = plugin.chestConfig.getCustomConfig();
        int count = 0;
        if (config.getConfigurationSection("chest." + configName) == null) {
            return;
        }
        for (String s : config.getConfigurationSection("chest." + configName).getKeys(false)) {
            chests.add(new Chests(Utils.stringToLocation(s), this));
            count++;
        }
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[ChestGenerators] Loaded " + count + " " + this.name + ChatColor.GREEN + " chests!");
    }

    public ItemStack getChest() {
        ItemStack i = new ItemStack(Material.CHEST);
        ItemMeta im = i.getItemMeta();

        im.setLore(this.lore);
        im.setDisplayName(this.name);

        i.setItemMeta(im);
        return i;
    }

    public void cleanChests() {
        for (Chests c : chests) {
            if (c.location.getWorld().getBlockAt(c.location).getState() == null) {
                c.remove();
                continue;
            }
            if (!c.location.getBlock().getType().equals(Material.CHEST)) {
                c.remove();
                continue;
            }
            Chest chest = (Chest) c.location.getWorld().getBlockAt(c.location).getState();
            if (!chest.getBlockInventory().getName().equalsIgnoreCase(this.name) || !chest.getBlockInventory().getTitle().equalsIgnoreCase(this.name)) {
                c.remove();
            }

        }
    }

    public boolean isChestThis(ItemStack i) {
        if (!i.getType().equals(Material.CHEST)) return false;
        return i.getItemMeta().getLore().equals(this.lore);
    }

    public Chests getChestAtLocation(Location l) {
        for (Chests ches : chests) {
            if (ches.location.equals(l)) return ches;
        }
        return null;
    }

    public boolean isChestBlockThis(Chest chest) {
        return chest.getBlockInventory().getName().equalsIgnoreCase(this.name) && chest.getBlockInventory().getTitle().equalsIgnoreCase(this.name);
    }

}
