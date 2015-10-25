package me.HeyAwesomePeople.ChestGenerators;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ChestListeners implements Listener {
    private ChestGenerators plugin = ChestGenerators.instance;

    @EventHandler
    public void onPlayerPlaceChest(BlockPlaceEvent e) {
        if (!e.getBlock().getType().equals(Material.CHEST)) return;
        if (!e.getItemInHand().getType().equals(Material.CHEST)) return;
        if (!e.getItemInHand().hasItemMeta()) return;
        if (!e.getItemInHand().getItemMeta().hasDisplayName() || !e.getItemInHand().getItemMeta().hasLore()) return;
        ItemStack item = e.getItemInHand();
        for (ChestGeneratorType gen : plugin.generators.values()) {
            if (gen.isChestThis(item)) {
                gen.chests.add(new Chests(e.getBlock().getLocation(), gen));
                Bukkit.broadcastMessage("Created chest!");
                break;
            }
        }
    }

    @EventHandler
    public void onPlayerBreakChest(BlockBreakEvent e) {
        if (!e.getBlock().getType().equals(Material.CHEST)) return;
        if (e.getBlock().getState() == null) return;

        Chest c = (Chest) e.getBlock().getState();

        for (ChestGeneratorType gen : plugin.generators.values()) {
            if (gen.isChestBlockThis(c)) {
                if (gen.getChestAtLocation(e.getBlock().getLocation()) != null) {
                    gen.getChestAtLocation(e.getBlock().getLocation()).remove();

                    e.getBlock().getLocation().getWorld().dropItemNaturally(e.getBlock().getLocation(), gen.getChest());

                    Bukkit.broadcastMessage("Chest removed into item!");
                }
                break;
            }
        }
    }

    @EventHandler
    public void playerOpenedChestListener(PlayerInteractEvent e) {
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getClickedBlock().getType().equals(Material.CHEST)) {
            if (e.getClickedBlock().getState() == null) return;

            Chest c = (Chest) e.getClickedBlock().getState();

            for (ChestGeneratorType gen : plugin.generators.values()) {
                if (gen.isChestBlockThis(c)) {
                    if (e.getPlayer().getItemInHand().getType().equals(Material.SIGN)) return;
                    if (gen.getChestAtLocation(e.getClickedBlock().getLocation()) == null) return;
                    gen.getChestAtLocation(e.getClickedBlock().getLocation()).updateChest(c);
                }
            }
        }
    }

}
