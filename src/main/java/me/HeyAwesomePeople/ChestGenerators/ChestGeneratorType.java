package me.HeyAwesomePeople.ChestGenerators;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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

    public ChestGeneratorType(String configName, Integer amount, String name, List<ItemStack> items, Integer regen, List<String> lore) {
        this.configName = configName;
        this.name = name;
        this.amount = amount;
        this.items = items;
        this.regen = regen;
        this.lore = lore;

        loadChests();
    }

    public void loadChests() {
        FileConfiguration config = plugin.chestConfig.getCustomConfig();
        for (String s : config.getConfigurationSection("chest." + configName).getKeys(false)) {
            chests.add(new Chests(Utils.stringToLocation(s), this));
        }
    }

    public ItemStack getChest() {
        ItemStack i = new ItemStack(Material.CHEST);
        ItemMeta im = i.getItemMeta();

        im.setLore(this.lore);
        im.setDisplayName(this.name);

        i.setItemMeta(im);
        return i;
    }

    public boolean isChestThis(ItemStack i) {
        if (!i.getType().equals(Material.CHEST)) return false;
        return i.getItemMeta().getLore().equals(this.lore);
    }

    public Chests getChestAtLocation(Location l) {
        for (Chests ches : chests) {
            if (ches.location == l) return ches;
        }
        return null;
    }

    public boolean isChestBlockThis(Chest chest) {
        Bukkit.broadcastMessage("Chest Inventory Name: " + chest.getBlockInventory().getName());
        Bukkit.broadcastMessage("Chest Inventory Title: " + chest.getBlockInventory().getTitle());
        if (chest.getBlockInventory().getName().equalsIgnoreCase(this.name) && chest.getBlockInventory().getTitle().equalsIgnoreCase(this.name)) {
            return true;
        }
        return false;
    }

}
