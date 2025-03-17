package freegamerdev.decayingWorld.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;

public class BlockPlaceListener implements Listener {

    private final Connection databaseConnection;

    private static final Set<Material> DECAYABLE_BLOCKS = Set.of(
            // Wood-based blocks
            Material.OAK_PLANKS, Material.SPRUCE_PLANKS, Material.BIRCH_PLANKS,
            Material.JUNGLE_PLANKS, Material.ACACIA_PLANKS, Material.DARK_OAK_PLANKS,
            Material.MANGROVE_PLANKS, Material.CHERRY_PLANKS, Material.BAMBOO_PLANKS,

            Material.OAK_STAIRS, Material.SPRUCE_STAIRS, Material.BIRCH_STAIRS,
            Material.JUNGLE_STAIRS, Material.ACACIA_STAIRS, Material.DARK_OAK_STAIRS,
            Material.MANGROVE_STAIRS, Material.CHERRY_STAIRS, Material.BAMBOO_STAIRS,

            Material.OAK_SLAB, Material.SPRUCE_SLAB, Material.BIRCH_SLAB,
            Material.JUNGLE_SLAB, Material.ACACIA_SLAB, Material.DARK_OAK_SLAB,
            Material.MANGROVE_SLAB, Material.CHERRY_SLAB, Material.BAMBOO_SLAB,

            Material.OAK_FENCE, Material.SPRUCE_FENCE, Material.BIRCH_FENCE,
            Material.JUNGLE_FENCE, Material.ACACIA_FENCE, Material.DARK_OAK_FENCE,
            Material.MANGROVE_FENCE, Material.CHERRY_FENCE, Material.BAMBOO_FENCE,

            // Stone-based blocks
            Material.COBBLESTONE, Material.MOSSY_COBBLESTONE,
            Material.STONE_BRICKS, Material.CRACKED_STONE_BRICKS, Material.MOSSY_STONE_BRICKS,
            Material.SANDSTONE, Material.RED_SANDSTONE,
            Material.DEEPSLATE, Material.TUFF, Material.POLISHED_TUFF,
            Material.TUFF_BRICKS, Material.CRACKED_NETHER_BRICKS, Material.BASALT,
            Material.END_STONE_BRICKS, Material.POLISHED_TUFF_STAIRS, Material.POLISHED_TUFF_SLAB,
            Material.POLISHED_TUFF_WALL, Material.TUFF_STAIRS, Material.TUFF_SLAB,
            Material.TUFF_WALL, Material.TUFF_BRICK_STAIRS, Material.TUFF_BRICK_SLAB,
            Material.TUFF_BRICK_WALL, Material.CHISELED_TUFF,

            // Dirt & clay-like blocks
            Material.DIRT, Material.COARSE_DIRT,
            Material.MUD, Material.PACKED_MUD, Material.CLAY,

            // Nether blocks
            Material.NETHERRACK, Material.SOUL_SAND, Material.SOUL_SOIL, Material.BLACKSTONE,

            // Fragile & special blocks
            Material.GLASS, Material.GLASS_PANE,
            Material.WHITE_STAINED_GLASS, Material.ORANGE_STAINED_GLASS, Material.MAGENTA_STAINED_GLASS,
            Material.LIGHT_BLUE_STAINED_GLASS, Material.YELLOW_STAINED_GLASS, Material.LIME_STAINED_GLASS,
            Material.PINK_STAINED_GLASS, Material.GRAY_STAINED_GLASS, Material.LIGHT_GRAY_STAINED_GLASS,
            Material.CYAN_STAINED_GLASS, Material.PURPLE_STAINED_GLASS, Material.BLUE_STAINED_GLASS,
            Material.BROWN_STAINED_GLASS, Material.GREEN_STAINED_GLASS, Material.RED_STAINED_GLASS,
            Material.BLACK_STAINED_GLASS,

            Material.ICE, Material.PACKED_ICE,
            Material.BONE_BLOCK

            // Excluded: No plants, moss, fungi, or blocks needing support
            // Excluded: No doors, trapdoors, buttons, boats, or entities
    );



    public BlockPlaceListener(Connection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer(); //use for permissions to ignore placed block

        /**
         * Change this to only apply for certain types of blocks (e.g. stone, wood)
         */
        if (DECAYABLE_BLOCKS.contains(block.getType())) {
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
