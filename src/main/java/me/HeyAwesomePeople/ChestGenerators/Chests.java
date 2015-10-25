package me.HeyAwesomePeople.ChestGenerators;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.Random;

public class Chests {
    private ChestGenerators plugin = ChestGenerators.instance;

    public Location location;
    public ChestGeneratorType type;
    public Integer interval;

    public Integer amountThatCanBeAdded = 0;

    public Chests(Location l, ChestGeneratorType type, Integer a) {
        this.location = l;
        this.type = type;
        this.interval = type.regen;
        this.amountThatCanBeAdded = a;
    }

    public void increase() {
        amountThatCanBeAdded++;
    }

    public void updateChest(Chest c) {
        for (int valuea = 1; valuea <= amountThatCanBeAdded; valuea++) {
            Random r = new Random();
            int value = r.nextInt(type.items.size());
            ItemStack i = type.items.get(value);
            c.getBlockInventory().addItem(i);
        }
        amountThatCanBeAdded = 0;
        c.update();

        Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            public void run() {
                java.sql.PreparedStatement statement;
                try {
                    statement = plugin.sql.openConnection().prepareStatement("UPDATE chests SET ToAdd=? WHERE Location=?");
                    statement.setInt(1, 0);
                    statement.setString(2, Utils.locationToString(location));
                    statement.executeUpdate();
                } catch (SQLException sqlE) {
                    sqlE.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void remove() {
        type.chests.remove(this);

        Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            public void run() {
                java.sql.PreparedStatement statement;
                try {
                    statement = plugin.sql.openConnection().prepareStatement("DELETE FROM chests WHERE Location=?");
                    statement.setString(1, Utils.locationToString(location));
                    statement.executeUpdate();
                } catch (SQLException sqlE) {
                    sqlE.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
