package me.HeyAwesomePeople.ChestGenerators;


import org.bukkit.Bukkit;

import java.sql.SQLException;

public class MySQLMethods {
    private static ChestGenerators plugin = ChestGenerators.instance;

    public void createTables() {
        java.sql.PreparedStatement statement;
        try {
            statement = plugin.sql.openConnection().
                    prepareStatement("CREATE TABLE IF NOT EXISTS chests (Location tinytext, ToAdd int)");
            statement.executeUpdate();
        } catch (SQLException sqlE) {
            sqlE.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
