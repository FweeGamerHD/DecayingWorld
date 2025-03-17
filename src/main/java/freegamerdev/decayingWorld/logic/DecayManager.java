package freegamerdev.decayingWorld.logic;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class DecayManager {

    private final JavaPlugin plugin;
    private final Connection databaseConnection;

    private long decay_time = 1 * 60 * 1000; //time when decay starts affection blocks
    private double random_decay_chance = 0.2; //chance where a block is decaying after the time is up
    private int batch_size = 1000; //max amount of block changes per cycle
    private int number_of_survived_rolls = 3; //Some value I use to make sure some blocks can actually survive instead of eventually definitely decaying

    public DecayManager(JavaPlugin plugin, Connection databaseConnection) {
        this.plugin = plugin;
        this.databaseConnection = databaseConnection;
    }

    public void startDecayTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try (PreparedStatement stmt = databaseConnection.prepareStatement(
                        "SELECT id, world, x, y, z, number_of_survived_rolls FROM placed_blocks WHERE placed_at < ? AND number_of_survived_rolls < ? LIMIT ?")) {
                    stmt.setLong(1, System.currentTimeMillis() - decay_time);
                    stmt.setInt(2, number_of_survived_rolls);  // Add check for number_of_survived_rolls
                    stmt.setInt(3, batch_size);

                    ResultSet rs = stmt.executeQuery();
                    List<Block> blocksToDecay = new ArrayList<>();
                    List<Integer> idsToDelete = new ArrayList<>();

                    while (rs.next()) {
                        String worldName = rs.getString("world");
                        int x = rs.getInt("x"), y = rs.getInt("y"), z = rs.getInt("z");

                        World world = Bukkit.getWorld(worldName);
                        if (world != null) {
                            Block block = world.getBlockAt(x, y, z);
                            blocksToDecay.add(block);
                            idsToDelete.add(rs.getInt("id"));
                        }
                    }

                    /**
                     * Change this to an additional function to do specific things to a certain type of block
                     * First hardcoded, then with config to enable custom things :)
                     */
                    // Switch back to main thread for block updates
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        List<Integer> idsToDeleteAfterDecay = new ArrayList<>();  // Store IDs of blocks that decayed

                        for (Block block : blocksToDecay) {
                            if (ThreadLocalRandom.current().nextDouble() < random_decay_chance) {
                                block.setType(Material.AIR);
                                block.getWorld().spawnParticle(Particle.LARGE_SMOKE, block.getLocation().add(0.5, 0.5, 0.5), 10);
                                block.getWorld().playSound(block.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.0f);

                                // Add the block's ID to be deleted after decay
                                String query = "SELECT id FROM placed_blocks WHERE x = ? AND y = ? AND z = ? AND world = ?";
                                try (PreparedStatement stmt2 = databaseConnection.prepareStatement(query)) {
                                    stmt2.setInt(1, block.getX());
                                    stmt2.setInt(2, block.getY());
                                    stmt2.setInt(3, block.getZ());
                                    stmt2.setString(4, block.getWorld().getName());

                                    ResultSet rs2 = stmt2.executeQuery();
                                    if (rs2.next()) {
                                        idsToDeleteAfterDecay.add(rs2.getInt("id"));
                                    }
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                // Increase the "number_of_survived_rolls"
                                String updateQuery = "UPDATE placed_blocks SET number_of_survived_rolls = number_of_survived_rolls + 1 WHERE x = ? AND y = ? AND z = ? AND world = ?";
                                try (PreparedStatement stmt2 = databaseConnection.prepareStatement(updateQuery)) {
                                    stmt2.setInt(1, block.getX());
                                    stmt2.setInt(2, block.getY());
                                    stmt2.setInt(3, block.getZ());
                                    stmt2.setString(4, block.getWorld().getName());
                                    stmt2.executeUpdate();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        // Async deletion of blocks from the database (only those that decayed)
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                            try (PreparedStatement deleteStmt = databaseConnection.prepareStatement(
                                    "DELETE FROM placed_blocks WHERE id = ?")) {
                                for (int id : idsToDeleteAfterDecay) {
                                    deleteStmt.setInt(1, id);
                                    deleteStmt.executeUpdate();
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        });
                    });

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        }, 0L, 20L * 60 * 5); // Runs every 5 minutes
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

    public int getNumber_of_survived_rolls() {
        return number_of_survived_rolls;
    }

    public void setNumber_of_survived_rolls(int number_of_survived_rolls) {
        this.number_of_survived_rolls = number_of_survived_rolls;
    }
}
