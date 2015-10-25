package me.HeyAwesomePeople.ChestGenerators;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.sql.ResultSet;
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
        // This ASync timer is a repeating task which runs on a seperate thread than the main one.
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            public void run() {
                if (chests.isEmpty()) {
                    return;
                }
                System.out.print("1");
                long startTime = System.nanoTime();

                String mysqlS = "INSERT INTO chests (Location, ToAdd) VALUES ";
                System.out.print("2");
                int runs = 0;
                for (final Chests c : chests) {
                    System.out.print("3");
                    if (runs == chests.size() - 1) {
                        System.out.print("3.1");
                        c.increase();
                        mysqlS += "('" + Utils.locationToString(c.location) + "','" + c.amountThatCanBeAdded + "')";
                        break;
                    }
                    System.out.print("3.01");
                    c.increase();
                    mysqlS += "('" + Utils.locationToString(c.location) + "','" + c.amountThatCanBeAdded + "'), ";
                    runs++;
                }
                System.out.print("4");
                final String state = mysqlS;
                final int runF = runs;

                System.out.print("5");
                java.sql.PreparedStatement statement;
                try {
                    System.out.print("6");
                    statement = plugin.sql.openConnection().prepareStatement("TRUNCATE chests");
                    statement.executeUpdate();
                    System.out.print("7");
                    statement = plugin.sql.openConnection().prepareStatement(state);
                    statement.executeUpdate();
                    System.out.print("8");
                } catch (SQLException sqlE) {
                    sqlE.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                System.out.print("9");
                long stopTime = System.nanoTime();
                System.out.print("Timing for " + runF + " chests... " + (stopTime - startTime) / 1000000 + "ms!");
                System.out.print("10");
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

    public void addNewChest(final Chests n) {
        chests.add(n);
        Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            public void run() {
                java.sql.PreparedStatement statement;
                try {
                    statement = plugin.sql.openConnection().prepareStatement("INSERT INTO chests (Location, ToAdd) VALUES ('" + Utils.locationToString(n.location) + "', '" + n.amountThatCanBeAdded + "')");
                    statement.executeUpdate();
                } catch (SQLException sqlE) {
                    sqlE.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void loadChests() {
        int count = 0;
        java.sql.PreparedStatement statement;
        try {
            statement = plugin.sql.openConnection().prepareStatement("SELECT * FROM chests");
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                count++;
                chests.add(new Chests(Utils.stringToLocation(result.getString(1)), this, Integer.parseInt(result.getString(2))));
            }
        } catch (SQLException sqlE) {
            sqlE.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[ChestGenerators] Loaded " + count + " " + this.name + ChatColor.GREEN + " chests from MySQL!");
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
