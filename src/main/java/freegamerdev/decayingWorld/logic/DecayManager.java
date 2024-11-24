package freegamerdev.decayingWorld.logic;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

public class DecayManager {

    private final JavaPlugin plugin;
    private final Connection databaseConnection;

    private long decay_time = 1 * 60 * 1000; //time when decay starts affection blocks
    private double random_decay_chance = 0.8; //chance where a block is decaying after the time is up
    private int batch_size = 100; //max amount of block changes per cycle

    public DecayManager(JavaPlugin plugin, Connection databaseConnection) {
        this.plugin = plugin;
        this.databaseConnection = databaseConnection;
    }

    public void startDecayTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            String query = "SELECT * FROM placed_blocks WHERE placed_at < ?";
            try (PreparedStatement stmt = databaseConnection.prepareStatement(query)) {
                stmt.setLong(1, System.currentTimeMillis() - decay_time);

                ResultSet rs = stmt.executeQuery();
                int processed = 0;
                while (rs.next() && processed < batch_size) {
                    String worldName = rs.getString("world");
                    int x = rs.getInt("x");
                    int y = rs.getInt("y");
                    int z = rs.getInt("z");

                    World world = Bukkit.getWorld(worldName);
                    if (world != null) {
                        Block block = world.getBlockAt(x, y, z);

                        if (ThreadLocalRandom.current().nextDouble() < 0.8) { // Adjust the percentage as needed
                            block.setType(Material.AIR);
                            world.spawnParticle(Particle.LARGE_SMOKE, block.getLocation().add(0.5, 0.5, 0.5), 10);
                            world.playSound(block.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.0f);
                        }
                    }

                    try (PreparedStatement deleteStmt = databaseConnection.prepareStatement(
                            "DELETE FROM placed_blocks WHERE id = ?")) {
                        deleteStmt.setInt(1, rs.getInt("id"));
                        deleteStmt.executeUpdate();
                    }

                    processed++;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, 0L, 20L * 60);
    }

    public long getDecay_time() {
        return decay_time;
    }

    public void setDecay_time(long decay_time) {
        this.decay_time = decay_time;
    }

    public double getRandom_decay_chance() {
        return random_decay_chance;
    }

    public void setRandom_decay_chance(double random_decay_chance) {
        this.random_decay_chance = random_decay_chance;
    }
}
