package chef.sheesh.eyeAI.data;

import chef.sheesh.eyeAI.data.model.PlayerData;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

/**
 * Handles async database operations for player data
 */
public class PlayerDataHandler implements AutoCloseable {
    
    private final Plugin plugin;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private Connection connection;
    
    public PlayerDataHandler(Plugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Initialize database connection
     */
    public void initialize() {
        CompletableFuture.runAsync(() -> {
            try {
                // Try to load H2 JDBC driver - multiple fallback methods
                try {
                    Class.forName("org.h2.Driver");
                    plugin.getLogger().info("H2 driver loaded via Class.forName");
                } catch (ClassNotFoundException e1) {
                    try {
                        // Try using DriverManager's automatic loading
                        DriverManager.getDriver("jdbc:h2:");
                        plugin.getLogger().info("H2 driver loaded via DriverManager");
                    } catch (SQLException e2) {
                        // Try loading the driver class directly from the classpath
                        try {
                            Class<?> driverClass = getClass().getClassLoader().loadClass("org.h2.Driver");
                            // Instantiate the driver to register it with DriverManager
                            driverClass.getDeclaredConstructor().newInstance();
                            plugin.getLogger().info("H2 driver loaded and registered via instantiation");
                        } catch (Exception e3) {
                            // Try loading from relocated package (shaded JAR)
                            try {
                                Class<?> relocatedDriverClass = getClass().getClassLoader().loadClass("chef.sheesh.eyeAI.libs.h2.org.h2.Driver");
                                relocatedDriverClass.getDeclaredConstructor().newInstance();
                                plugin.getLogger().info("H2 driver loaded from relocated package");
                            } catch (Exception e4) {
                                // Final fallback: try ServiceLoader mechanism
                                try {
                                    ServiceLoader<Driver> serviceLoader = ServiceLoader.load(Driver.class);
                                    for (Driver driver : serviceLoader) {
                                        if (driver.getClass().getName().contains("h2")) {
                                            DriverManager.registerDriver(driver);
                                            plugin.getLogger().info("H2 driver loaded via ServiceLoader");
                                            break;
                                        }
                                    }
                                } catch (Exception e5) {
                                    // Last resort: try direct connection (might auto-load driver)
                                    try {
                                        // Just try to get a connection - if H2 is available, this might work
                                        String testUrl = "jdbc:h2:mem:test";
                                        Connection testConn = DriverManager.getConnection(testUrl);
                                        testConn.close();
                                        plugin.getLogger().info("H2 driver loaded via direct connection test");
                                    } catch (SQLException e6) {
                                        plugin.getLogger().log(Level.SEVERE, "H2 JDBC driver not found in any location", e6);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }

                // Ensure data folder exists
                java.io.File dataFolder = new java.io.File(plugin.getDataFolder(), "data");
                if (!dataFolder.exists()) {
                    dataFolder.mkdirs();
                    plugin.getLogger().info("Created data directory: " + dataFolder.getAbsolutePath());
                }

                // Use H2 for simplicity (can be changed to PostgreSQL)
                String dbPath = dataFolder.getAbsolutePath() + "/playerdata";
                String url = "jdbc:h2:" + dbPath + ";AUTO_SERVER=TRUE";

                this.connection = DriverManager.getConnection(url);
                createTables();
                plugin.getLogger().info("Database initialized successfully");

            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to initialize database", e);
            }
        }, executor);
    }
    
    /**
     * Get player data asynchronously
     */
    @NotNull
    public CompletableFuture<PlayerData> getPlayerData(@NotNull UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM player_data WHERE player_id = ?")) {
                
                stmt.setString(1, playerId.toString());
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    return PlayerData.builder()
                        .playerId(playerId)
                        .playerName(rs.getString("player_name"))
                        .level(rs.getInt("level"))
                        .experience(rs.getLong("experience"))
                        .kills(rs.getInt("kills"))
                        .deaths(rs.getInt("deaths"))
                        .balance(rs.getDouble("balance"))
                        .tokens(rs.getInt("tokens"))
                        .scoreboardEnabled(rs.getBoolean("scoreboard_enabled"))
                        .preferredLanguage(rs.getString("preferred_language"))
                        .firstJoin(rs.getLong("first_join"))
                        .lastSeen(rs.getLong("last_seen"))
                        .playtime(rs.getLong("playtime"))
                        .build();
                } else {
                    // Return default data for new player
                    return createDefaultPlayerData(playerId);
                }
                
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to get player data for " + playerId, e);
                return createDefaultPlayerData(playerId);
            }
        }, executor);
    }
    
    /**
     * Save player data asynchronously
     */
    @NotNull
    public CompletableFuture<Void> savePlayerData(@NotNull PlayerData playerData) {
        return CompletableFuture.runAsync(() -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                "MERGE INTO player_data (player_id, player_name, level, experience, kills, deaths, " +
                "balance, tokens, scoreboard_enabled, preferred_language, first_join, last_seen, playtime) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                
                stmt.setString(1, playerData.getPlayerId().toString());
                stmt.setString(2, playerData.getPlayerName());
                stmt.setInt(3, playerData.getLevel());
                stmt.setLong(4, playerData.getExperience());
                stmt.setInt(5, playerData.getKills());
                stmt.setInt(6, playerData.getDeaths());
                stmt.setDouble(7, playerData.getBalance());
                stmt.setInt(8, playerData.getTokens());
                stmt.setBoolean(9, playerData.isScoreboardEnabled());
                stmt.setString(10, playerData.getPreferredLanguage());
                stmt.setLong(11, playerData.getFirstJoin());
                stmt.setLong(12, playerData.getLastSeen());
                stmt.setLong(13, playerData.getPlaytime());
                
                stmt.executeUpdate();
                
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save player data for " + playerData.getPlayerId(), e);
            }
        }, executor);
    }
    
    /**
     * Create default player data
     */
    @NotNull
    private PlayerData createDefaultPlayerData(@NotNull UUID playerId) {
        long now = System.currentTimeMillis();
        return PlayerData.builder()
            .playerId(playerId)
            .playerName("Unknown")
            .level(1)
            .experience(0)
            .kills(0)
            .deaths(0)
            .balance(0.0)
            .tokens(0)
            .scoreboardEnabled(true)
            .preferredLanguage("en")
            .firstJoin(now)
            .lastSeen(now)
            .playtime(0)
            .build();
    }
    
    /**
     * Create database tables
     */
    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS player_data (
                    player_id VARCHAR(36) PRIMARY KEY,
                    player_name VARCHAR(16) NOT NULL,
                    level INTEGER DEFAULT 1,
                    experience BIGINT DEFAULT 0,
                    kills INTEGER DEFAULT 0,
                    deaths INTEGER DEFAULT 0,
                    balance DOUBLE DEFAULT 0.0,
                    tokens INTEGER DEFAULT 0,
                    scoreboard_enabled BOOLEAN DEFAULT TRUE,
                    preferred_language VARCHAR(5) DEFAULT 'en',
                    first_join BIGINT NOT NULL,
                    last_seen BIGINT NOT NULL,
                    playtime BIGINT DEFAULT 0
                )
                """);
        }
    }
    
    @Override
    public void close() {
        try {
            executor.shutdown();
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            plugin.getLogger().info("Database connection closed");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Error closing database connection", e);
        }
    }
}
