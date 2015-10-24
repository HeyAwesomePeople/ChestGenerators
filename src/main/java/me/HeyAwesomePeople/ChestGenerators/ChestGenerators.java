package me.HeyAwesomePeople.ChestGenerators;

import me.HeyAwesomePeople.ChestGenerators.CustomConfigs.ChestConfig;
import me.HeyAwesomePeople.ChestGenerators.CustomConfigs.GeneratorConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;


public class ChestGenerators extends JavaPlugin {
    public static ChestGenerators instance;

    public Methods methods;
    public ChestConfig chestConfig;
    public GeneratorConfig genConfig;

    private File fileconfig = new File(this.getDataFolder() + File.separator + "config.yml");
    private File filechests = new File(this.getDataFolder() + File.separator + "chests.yml");
    private File filegenerators = new File(this.getDataFolder() + File.separator + "generators.yml");

    public HashMap<String, ChestGeneratorType> generators = new HashMap<String, ChestGeneratorType>();

    @Override
    public void onEnable() {
        instance = this;
        methods = new Methods();
        chestConfig = new ChestConfig();
        genConfig = new GeneratorConfig();

        createFiles();
        methods.loadChestGenerators();

        Bukkit.getServer().getPluginManager().registerEvents(new ChestListeners(), this);

    }

    public void createFiles() {
        if (!fileconfig.exists()) {
            this.saveDefaultConfig();
        }

        if (!filechests.exists()) {
            chestConfig.getCustomConfig().set("chest.irongenerator.234-543-45-WorldName", 42);
            chestConfig.saveCustomConfig();
        }

        if (!filegenerators.exists()) {
            genConfig.getCustomConfig().set("generators.irongenerator.name", "Iron Generator!");
            genConfig.saveCustomConfig();
        }
    }

    @Override
    public void onDisable() {

    }

    //TODO commands to get chests, commands to edit

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
                    String chestgenerator = args[2];
                    if (!methods.doesConfigHaveGenerator(args[2])) {
                        sender.sendMessage(ChatColor.RED + "That chest generator is not valid!");
                        return false;
                    }
                    if (generators.containsKey(name)) {
                        Bukkit.getPlayer(args[1]).getInventory().addItem(generators.get(name).getChest());
                    }
                }
            }
        }
        return false;
    }

}
