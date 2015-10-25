package me.HeyAwesomePeople.ChestGenerators;

import me.HeyAwesomePeople.ChestGenerators.CustomConfigs.GeneratorConfig;
import me.HeyAwesomePeople.ChestGenerators.CustomConfigs.OldConfig;
import me.HeyAwesomePeople.ChestGenerators.mysql.MySQL;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class ChestGenerators extends JavaPlugin {
    public static ChestGenerators instance;

    public MySQLMethods mysqlmethods;
    public Methods methods;
    public GeneratorConfig genConfig;
    public OldConfig oldConfig;

    private File fileconfig = new File(this.getDataFolder() + File.separator + "config.yml");
    private File filegenerators = new File(this.getDataFolder() + File.separator + "generators.yml");

    public HashMap<String, ChestGeneratorType> generators = new HashMap<String, ChestGeneratorType>();

    public Boolean connect = true;
    public MySQL sql;
	public Connection c;

    @Override
    public void onEnable() {
        instance = this;

        World templateworld = this.getServer().createWorld(new WorldCreator("ASkyBlock"));

        mysqlmethods = new MySQLMethods();

        mySql();

        methods = new Methods();
        genConfig = new GeneratorConfig();
        oldConfig = new OldConfig();

        createFiles();
        methods.loadChestGenerators();

        if (!getConfig().getBoolean("convertedOldChests")) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[ParallaxGens] Converting old chests..." + ChatColor.RED + "This will take a while.");
            convertOldChests();
        }

        Bukkit.getServer().getPluginManager().registerEvents(new ChestListeners(), this);

        // methods.cleanChests(); TODO
    }

    public void mySql() {
        if (connect) {
            sql = new MySQL(this, getConfig().getString("mysql.host"), getConfig().getString("mysql.port"),
                    getConfig().getString("mysql.database"), getConfig().getString("mysql.user"), getConfig().getString("mysql.password"));
            attemptMySQLConnection();
        }
    }

    public void attemptMySQLConnection() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.BLUE + "[ParallaxGens] Attempting to connect to MySQL... This may take up to 10 seconds.");
        try {
            c = sql.openConnection();
            connect = true;
            Bukkit.getConsoleSender().sendMessage(ChatColor.BLUE + "[ParallaxGens] Successfully connected to MySQL.");
            mysqlmethods.createTables();
        } catch (SQLException e) {
            e.printStackTrace();
            connect = false;
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[ParallaxGens] MySQL Exception Error! Full functionality will not work until problem is resolved.");
        } catch (ClassNotFoundException c) {
            c.printStackTrace();
            connect = false;
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[ParallaxGens] MySQL ClassNotFound Error! Full functionality will not work until problem is resolved.");
        }
    }

    public void createFiles() {
        if (!fileconfig.exists()) {
            this.saveDefaultConfig();
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
        int count = 0;
        for (String s : config.getKeys(false)) {
            String generator = config.getString(s).toLowerCase();
            String[] split = s.split("_");

            if (Bukkit.getWorld(split[3]) == null) {
                continue;
            }

            Location l = new Location(Bukkit.getWorld(split[3]), Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));
            if (generators.containsKey(generator)) {
                count++;
                generators.get(generator).addNewChest(new Chests(l, generators.get(generator), 0));
            }
        }
        getConfig().set("convertedOldChests", true);
        saveConfig();
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[ParallaxGens] Converted Old Chests! " + count);
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
                    Player p = Bukkit.getPlayer(name);
                    String chestgenerator = args[2];
                    if (!methods.doesConfigHaveGenerator(args[2])) {
                        sender.sendMessage(ChatColor.RED + "That chest generator is not valid!");
                        return false;
                    }
                    if (!generators.containsKey(chestgenerator.toLowerCase())) {
                        sender.sendMessage(ChatColor.RED + "Chest generator was unable to be accessed. Available generators: " + Arrays.toString(generators.keySet().toArray()));
                        return false;
                    }
                    int count = 0;
                    for (ItemStack i : p.getInventory()) {
                        if (i == null) count++;
                    }
                    if (count > 0) {
                        p.getInventory().addItem(generators.get(chestgenerator).getChest());
                    } else {
                        p.getWorld().dropItem(p.getLocation(), generators.get(chestgenerator).getChest());
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Invalid subcommand!");
                }
            }
        }
        return false;
    }

}
