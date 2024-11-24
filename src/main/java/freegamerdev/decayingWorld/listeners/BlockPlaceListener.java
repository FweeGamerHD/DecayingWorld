package freegamerdev.decayingWorld.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BlockPlaceListener implements Listener {

    private final Connection databaseConnection;

    public BlockPlaceListener(Connection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        String query = "INSERT INTO placed_blocks (world, x, y, z, type, placed_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = databaseConnection.prepareStatement(query)) {
            stmt.setString(1, block.getWorld().getName());
            stmt.setInt(2, block.getX());
            stmt.setInt(3, block.getY());
            stmt.setInt(4, block.getZ());
            stmt.setString(5, block.getType().name());
            stmt.setLong(6, System.currentTimeMillis()); // Current timestamp
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        String query = "DELETE FROM placed_blocks WHERE world = ? AND x = ? AND y = ? AND z = ?";
        try (PreparedStatement stmt = databaseConnection.prepareStatement(query)) {
            stmt.setString(1, block.getWorld().getName());
            stmt.setInt(2, block.getX());
            stmt.setInt(3, block.getY());
            stmt.setInt(4, block.getZ());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
