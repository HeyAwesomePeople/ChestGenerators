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

    /**
     * ******** REPEATING TASK *************
     */
    public void createTask(final long delay, final long interval) {
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            public void run() {
                System.out.print(name + ":Chests: " + chests.size());
                if (chests.isEmpty()) {
                    return;
                }

                StringBuilder mysqlS = new StringBuilder("INSERT INTO chests (Location, Generator, ToAdd) VALUES ");
                int runs = 0;
                for (final Chests c : chests) {
                    if (runs == chests.size() - 1) {
                        c.increase();
                        mysqlS.append("('" + Utils.locationToString(c.location) + "','" + configName + "','" + c.amountThatCanBeAdded + "')");
                        break;
                    }
                    c.increase();
                    mysqlS.append("('" + Utils.locationToString(c.location) + "','" + configName + "','" + c.amountThatCanBeAdded + "'), ");
                    runs++;
                }
                final String state = mysqlS.toString();
                final int runF = runs;

                /* TRUNCATE Chests then insert into MySQL */
                java.sql.PreparedStatement statement;
                try {
                    statement = plugin.sql.openConnection().prepareStatement("TRUNCATE chests");
                    statement.executeUpdate();
                    statement = plugin.sql.openConnection().prepareStatement(state);
                    statement.executeUpdate();
                } catch (SQLException sqlE) {
                    sqlE.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }


            }
        }, delay, interval);
    }

    public void stopTask() {
        this.task.cancel();
    }

    public void startTask() {
        createTask(nextLong(), this.regen);
    }

    /**
     * ******** ADD NEW CHEST INTO THE MYSQL *************
     */
    public void addNewChest(final Chests n) {
        Bukkit.getConsoleSender().sendMessage("added new chest");
        chests.add(n);

        java.sql.PreparedStatement statement;
        try {
            statement = plugin.sql.openConnection().prepareStatement("INSERT INTO chests (Location, Generator, ToAdd)" +
                    " VALUES ('" + Utils.locationToString(n.location) + "','" + configName + "','" + n.amountThatCanBeAdded + "')");
            statement.executeUpdate();

        } catch (SQLException sqlE) {
            sqlE.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ******** LOAD CHESTS FROM MYSQL *************
     */
    public void loadChests() {
        Bukkit.getConsoleSender().sendMessage("loaded chests");
        int count = 0;
        java.sql.PreparedStatement statement;
        try {
            statement = plugin.sql.openConnection().prepareStatement("SELECT * FROM chests WHERE Generator=?");
            statement.setString(1, configName);
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                count++;
                chests.add(new Chests(Utils.stringToLocation(result.getString(1)), this, Integer.parseInt(result.getString(3))));
            }
        } catch (SQLException sqlE) {
            sqlE.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[ChestGenerators] Loaded " + count + " " + this.name + ChatColor.GREEN + " chests from MySQL!");
    }

    /**
     * ******** GET THIS GENERATORS CHEST ITEM *************
     */
    public ItemStack getChest() {
        ItemStack i = new ItemStack(Material.CHEST);
        ItemMeta im = i.getItemMeta();

        im.setLore(this.lore);
        im.setDisplayName(this.name);

        i.setItemMeta(im);
        return i;
    }

    /**
     * ******** CLEANUP UNUSED CHESTS *************
     */
    public void cleanChests() {
        Bukkit.getConsoleSender().sendMessage("cleaning chests");
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

    long nextLong() {
        long leftLimit = 1L;
        long rightLimit = 20L;
        return leftLimit + (long) (Math.random() * (rightLimit - leftLimit));
    }

}
