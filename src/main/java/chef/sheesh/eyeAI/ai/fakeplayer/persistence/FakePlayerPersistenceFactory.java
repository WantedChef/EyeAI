package chef.sheesh.eyeAI.ai.fakeplayer.persistence;

import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Factory for creating FakePlayerPersistence instances
 */
public class FakePlayerPersistenceFactory {

    private static FakePlayerPersistence instance;

    /**
     * Create or get the persistence instance
     */
    public static FakePlayerPersistence getInstance(JavaPlugin plugin, FakePlayerManager fakePlayerManager) {
        if (instance == null) {
            instance = createPersistence(plugin, fakePlayerManager);
        }
        return instance;
    }

    /**
     * Create a new persistence instance based on configuration
     */
    private static FakePlayerPersistence createPersistence(JavaPlugin plugin, FakePlayerManager fakePlayerManager) {
        // Try database persistence first
        try {
            Connection connection = createDatabaseConnection(plugin);
            return new DatabaseFakePlayerPersistence(connection, fakePlayerManager);
        } catch (Exception e) {
            plugin.getLogger().warning("Database persistence failed, falling back to file-based persistence: " + e.getMessage());
        }

        // Fallback to file-based persistence
        return new FileFakePlayerPersistence(plugin.getDataFolder());
    }

    /**
     * Create database connection
     */
    private static Connection createDatabaseConnection(JavaPlugin plugin) throws SQLException {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File databaseFile = new File(dataFolder, "fake_players.db");
        String url = "jdbc:sqlite:" + databaseFile.getAbsolutePath();

        Connection connection = DriverManager.getConnection(url);
        connection.setAutoCommit(true);

        plugin.getLogger().info("Connected to SQLite database for fake player persistence");

        return connection;
    }

    /**
     * Force recreation of the persistence instance (useful for testing)
     */
    public static void resetInstance() {
        instance = null;
    }
}
