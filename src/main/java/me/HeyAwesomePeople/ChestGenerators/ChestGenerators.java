package me.HeyAwesomePeople.ChestGenerators;

import me.HeyAwesomePeople.ChestGenerators.CustomConfigs.ChestConfig;
import me.HeyAwesomePeople.ChestGenerators.CustomConfigs.GeneratorConfig;
import me.HeyAwesomePeople.ChestGenerators.CustomConfigs.OldConfig;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;


public class ChestGenerators extends JavaPlugin {
    public static ChestGenerators instance;

    public Methods methods;
    public ChestConfig chestConfig;
    public GeneratorConfig genConfig;
    public OldConfig oldConfig;

    private File fileconfig = new File(this.getDataFolder() + File.separator + "config.yml");
    private File filechests = new File(this.getDataFolder() + File.separator + "chests.yml");
    private File filegenerators = new File(this.getDataFolder() + File.separator + "generators.yml");

    public HashMap<String, ChestGeneratorType> generators = new HashMap<String, ChestGeneratorType>();

    @Override
    public void onEnable() {
        instance = this;

        World templateworld = this.getServer().createWorld(new WorldCreator("ASkyBlock"));

        methods = new Methods();
        chestConfig = new ChestConfig();
        genConfig = new GeneratorConfig();
        oldConfig = new OldConfig();

        createFiles();
        methods.loadChestGenerators();

        if (!getConfig().getBoolean("convertedOldChests")) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Converting old chests..." + ChatColor.RED + "This may take a few minutes");
            convertOldChests();
        }

        Bukkit.getServer().getPluginManager().registerEvents(new ChestListeners(), this);

        // methods.cleanChests();
    }

    public void createFiles() {
        if (!fileconfig.exists()) {
            this.saveDefaultConfig();
        }

        if (!filechests.exists()) {
            chestConfig.getCustomConfig().set("chest.irongenerator.234_54_45_world", 42);
            chestConfig.saveCustomConfig();
        }

        if (!filegenerators.exists()) {
            List<String> list  = new ArrayList<String>();
            list.add("&7Generated Item&8: &aIron");
            list.add("&7Generation Time&8: &a30 Seconds");
            genConfig.getCustomConfig().set("generators.irongenerator.name", "Iron Generator!");
            genConfig.getCustomConfig().set("generators.irongenerator.tickRate", 20);
            genConfig.getCustomConfig().set("generators.irongenerator.quantity", 1);
            genConfig.getCustomConfig().set("generators.irongenerator.item", "IRON_INGOT");
            genConfig.getCustomConfig().set("generators.irongenerator.lore", list);
            genConfig.saveCustomConfig();
        }
    }

    public void convertOldChests() {
        FileConfiguration config = oldConfig.getOldConfig();
        for (String s : config.getKeys(false)) {
            String generator = config.getString(s).toLowerCase();
            String[] split = s.split("_");

            if (Bukkit.getWorld(split[3]) == null) {
                continue;
            }

            Location l = new Location(Bukkit.getWorld(split[3]), Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));
            if (generators.containsKey(generator)) {
                generators.get(generator).chests.add(new Chests(l, generators.get(generator)));
            }
        }
        getConfig().set("convertedOldChests", true);
        saveConfig();
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[ChestGenerators] Converted Old Chests!");
    }

    @Override
    public void onDisable() {
        this.reloadConfig();
    }

    public boolean onCommand(final CommandSender sender, Command cmd,
                             String commandLabel, final String[] args) {
        if (commandLabel.equalsIgnoreCase("chestgenerators")) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.BLUE + "== Chest Generators! ==");
                sender.sendMessage(ChatColor.AQUA + "/chestgenerators give <name> <chestgenerator>");
            } else {
                if (args[0].equalsIgnoreCase("give")) {
                    String name = args[1];
                    if (Bukkit.getPlayer(args[1]) == null) {
                        sender.sendMessage(ChatColor.RED + "Player not online.");
                        return false;
                    }
                    Player p = Bukkit.getPlayer(args[1]);
                    String chestgenerator = args[2];
                    if (!methods.doesConfigHaveGenerator(args[2])) {
                        sender.sendMessage(ChatColor.RED + "That chest generator is not valid!");
                        return false;
                    }
                    if (!generators.containsKey(name.toLowerCase())) {
                        sender.sendMessage(ChatColor.RED + "Chest generator was unable to be accessed. Available generators: " + Arrays.toString(generators.keySet().toArray()));
                        return false;
                    }
                    int count = 0;
                    for (ItemStack i : p.getInventory()) {
                        if (i == null) count++;
                    }
                    if (count > 0) {
                        p.getInventory().addItem(generators.get(name).getChest());
                    } else {
                        p.getWorld().dropItem(p.getLocation(), generators.get(name).getChest());
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Invalid subcommand!");
                }
            }
        }
        return false;
    }

}
