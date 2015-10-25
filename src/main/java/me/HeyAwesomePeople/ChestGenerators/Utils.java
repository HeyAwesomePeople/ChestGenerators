package me.HeyAwesomePeople.ChestGenerators;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Utils {

    public static String convertColor(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static String locationToString(Location loc) {
        String s = "";
        s += loc.getBlockX();
        s += "_";
        s += loc.getBlockY();
        s += "_";
        s += loc.getBlockZ();
        s += "_";
        s += loc.getWorld().getName();
        return s;
    }

    public static Location stringToLocation(String s) {
        String[] l = s.split("_");// 0,x 1,y 2,z 3,worldmame
        if (Bukkit.getWorld(l[3]) == null) {
            return null;
        }
        return new Location(Bukkit.getWorld(l[3]), (double) Integer.parseInt(l[0]), (double) Integer.parseInt(l[1]), (double) Integer.parseInt(l[2]));
    }

}
