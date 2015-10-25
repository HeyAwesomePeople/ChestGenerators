package me.HeyAwesomePeople.ChestGenerators;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.util.*;

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
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[ParallaxGens] Loaded " + a + " generators!");
    }

    public boolean doesConfigHaveGenerator(String s) {
        return plugin.genConfig.getCustomConfig().contains("generators." + s);
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


    public void addChests(HashMap<Location, ChestGeneratorType> map) {
        StringBuilder mysqlS = new StringBuilder("INSERT INTO chests (Location, Generator, ToAdd) VALUES ");
        StringBuilder dataDump = new StringBuilder("");

        int runs = 0;


        java.sql.PreparedStatement statement;
        try {
            statement = plugin.sql.openConnection().prepareStatement("INSERT INTO chests (Location, Generator, ToAdd) VALUES (?,?,?)");
            for (Map.Entry<Location, ChestGeneratorType> entry : map.entrySet()) {
                Location           loc  = entry.getKey();
                ChestGeneratorType type = entry.getValue();
                type.chests.add(new Chests(loc, type, 0));

                statement.setString(1, Utils.locationToString(loc));
                statement.setString(2, type.configName);
                statement.setInt(3, 0);

                statement.addBatch();
            }

            statement.executeBatch();
        } catch (BatchUpdateException e) {
            int[] i = e.getUpdateCounts();
            Bukkit.getConsoleSender().sendMessage("Values: " + i);
            e.printStackTrace();
        } catch (SQLException sqlE) {
            sqlE.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.print(mysqlS);
        PrintWriter writer = null;
        PrintWriter writer2 = null;
        try {
            writer = new PrintWriter("plugins/ParallaxGens/mysqlDump.txt", "UTF-8");
            writer2 = new PrintWriter("plugins/ParallaxGens/dataDump.txt", "UTF-8");
            writer.println(mysqlS);
            writer2.println(dataDump);
            writer.close();
            writer2.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

}
