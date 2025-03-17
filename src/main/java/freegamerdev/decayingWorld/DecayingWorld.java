package freegamerdev.decayingWorld;

import freegamerdev.decayingWorld.listeners.BlockPlaceListener;
import freegamerdev.decayingWorld.logic.DecayManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DecayingWorld extends JavaPlugin {

    private Connection databaseConnection;

    @Override
    public void onEnable() {
        initDatabase();
        BlockPlaceListener listener = new BlockPlaceListener(databaseConnection);
        getServer().getPluginManager().registerEvents(listener, this);

        DecayManager decayManager = new DecayManager(this, databaseConnection);
        decayManager.startDecayTask();

        Bukkit.getLogger().info("WorldDecay has been initialized.");
    }

    @Override
    public void onDisable() {
        if (databaseConnection != null) {
            try {
                databaseConnection.close();
                getLogger().info("Database connection for the DecayingWorld Plugin closed.");
            } catch (SQLException e) {
                getLogger().severe("Error closing database connection: " + e.getMessage());
            }
        }
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
                        "placed_at INTEGER NOT NULL, " +
                        "number_of_survived_rolls INTEGER DEFAULT 0" +
                        ");");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
