package freegamerdev.decayingWorld;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public final class DecayingWorld extends JavaPlugin {

    private Connection databaseConnection;

    @Override
    public void onEnable() {
        initDatabase();
        BlockPlaceListener listener = new BlockPlaceListener(databaseConnection);
        getServer().getPluginManager().registerEvents(listener, this);

        Bukkit.getLogger().info("WorldDecay has been initialized.");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void initDatabase() {
        try {
            File databaseFile = new File(getDataFolder(), "blocks.db");
            if (!databaseFile.exists()) {
                getDataFolder().mkdirs();
                databaseFile.createNewFile();
            }
            databaseConnection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getPath());
            try (Statement stmt = databaseConnection.createStatement()) {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS placed_blocks (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "world TEXT NOT NULL, " +
                        "x INTEGER NOT NULL, " +
                        "y INTEGER NOT NULL, " +
                        "z INTEGER NOT NULL, " +
                        "type TEXT NOT NULL, " +
                        "placed_at INTEGER NOT NULL" +
                        ");");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
