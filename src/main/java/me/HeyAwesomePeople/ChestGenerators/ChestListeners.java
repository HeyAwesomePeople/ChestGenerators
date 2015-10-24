package me.HeyAwesomePeople.ChestGenerators;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class ChestListeners implements Listener {

    @EventHandler
    public void onPlayerPlaceChest(BlockPlaceEvent e) {
        if (e.getBlock().getType().equals(Material.CHEST)) return;
        if (e.getItemInHand().getType().equals(Material.CHEST)) {
            if (e.getItemInHand().getItemMeta().getLore().contains("")) {
            }
        }
    }
}
