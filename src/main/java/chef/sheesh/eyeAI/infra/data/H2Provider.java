package chef.sheesh.eyeAI.infra.data;

import chef.sheesh.eyeAI.infra.config.ConfigKeys;
import chef.sheesh.eyeAI.infra.config.ConfigurationManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

/**
 * H2 Database Provider for embedded data storage
 */
public final class H2Provider implements DataStore {

    private final JavaPlugin plugin;
    private final ConfigurationManager configManager;
    private final ObjectMapper objectMapper;
    private Connection connection;
    private String databaseUrl;

    public H2Provider(JavaPlugin plugin, ConfigurationManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }

    @Override
    public void connect() {
        try {
            // Load H2 driver
            Class.forName("org.h2.Driver");

            // Build database URL
            String dbFile = configManager.getString(ConfigKeys.STORAGE_H2_FILE, "plugins/ChefAI/data/chefai");
            File dbPath = new File(plugin.getDataFolder().getParentFile(), dbFile);

            // Ensure parent directory exists
            File parentDir = dbPath.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            this.databaseUrl = "jdbc:h2:" + dbPath.getAbsolutePath() + ";AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1";

            // Connect to database
            this.connection = DriverManager.getConnection(databaseUrl);
            this.connection.setAutoCommit(true);

            // Create tables
            createTables();

            plugin.getLogger().info("H2 database connected successfully at: " + dbPath.getAbsolutePath());

        } catch (ClassNotFoundException e) {
            plugin.getLogger().log(Level.SEVERE, "H2 driver not found", e);
            throw new RuntimeException("Failed to load H2 driver", e);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to connect to H2 database", e);
            throw new RuntimeException("Failed to connect to H2 database", e);
        }
    }

    @Override
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("H2 database disconnected successfully");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Error disconnecting from H2 database", e);
        }
    }

    @Override
    public void saveData(String key, Object data) {
        if (connection == null) {
            plugin.getLogger().warning("Cannot save data: H2 connection is null");
            return;
        }

        String sql = """
            MERGE INTO data_store (data_key, data_value, data_type, created_at, updated_at)
            VALUES (?, ?, ?, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP())
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, key);
            stmt.setString(2, objectMapper.writeValueAsString(data));
            stmt.setString(3, data.getClass().getSimpleName());
            stmt.executeUpdate();

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save data for key: " + key, e);
        }
    }

    @Override
    public Object loadData(String key) {
        if (connection == null) {
            plugin.getLogger().warning("Cannot load data: H2 connection is null");
            return null;
        }

        String sql = "SELECT data_value, data_type FROM data_store WHERE data_key = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, key);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String jsonValue = rs.getString("data_value");
                String dataType = rs.getString("data_type");

                // Basic type mapping - can be extended for specific classes
                switch (dataType) {
                    case "PlayerData":
                        return objectMapper.readValue(jsonValue, chef.sheesh.eyeAI.data.model.PlayerData.class);
                    case "AIModel":
                        return objectMapper.readValue(jsonValue, chef.sheesh.eyeAI.data.model.AIModel.class);
                    case "TrainingData":
                        return objectMapper.readValue(jsonValue, chef.sheesh.eyeAI.data.model.TrainingData.class);
                    case "TransactionRecord":
                        return objectMapper.readValue(jsonValue, chef.sheesh.eyeAI.data.model.TransactionRecord.class);
                    case "AIDecision":
                        return objectMapper.readValue(jsonValue, chef.sheesh.eyeAI.data.model.AIDecision.class);
                    default:
                        return objectMapper.readValue(jsonValue, Object.class);
                }
            }

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load data for key: " + key, e);
        }

        return null;
    }

    /**
     * Create database tables if they don't exist
     */
    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Main data store table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS data_store (
                    data_key VARCHAR(255) PRIMARY KEY,
                    data_value TEXT NOT NULL,
                    data_type VARCHAR(100) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP()
                )
                """);

            // Player data table (for complex queries)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS player_data (
                    player_id VARCHAR(36) PRIMARY KEY,
                    player_name VARCHAR(16) NOT NULL,
                    level INTEGER DEFAULT 1,
                    experience BIGINT DEFAULT 0,
                    kills INTEGER DEFAULT 0,
                    deaths INTEGER DEFAULT 0,
                    kdr DOUBLE DEFAULT 0.0,
                    assists INTEGER DEFAULT 0,
                    wins INTEGER DEFAULT 0,
                    losses INTEGER DEFAULT 0,
                    balance DOUBLE DEFAULT 0.0,
                    tokens INTEGER DEFAULT 0,
                    scoreboard_enabled BOOLEAN DEFAULT TRUE,
                    preferred_language VARCHAR(5) DEFAULT 'en',
                    first_join BIGINT NOT NULL,
                    last_seen BIGINT NOT NULL,
                    playtime BIGINT DEFAULT 0,
                    afk_time BIGINT DEFAULT 0,
                    average_session_length DOUBLE DEFAULT 0.0,
                    sessions_count INTEGER DEFAULT 0,
                    last_ip_address VARCHAR(45),
                    failed_login_attempts INTEGER DEFAULT 0,
                    last_failed_login BIGINT DEFAULT 0
                )
                """);

            // AI models table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS ai_models (
                    model_id VARCHAR(255) PRIMARY KEY,
                    model_name VARCHAR(255) NOT NULL,
                    model_type VARCHAR(50) NOT NULL,
                    model_data TEXT NOT NULL,
                    version VARCHAR(20) NOT NULL,
                    accuracy DOUBLE DEFAULT 0.0,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP()
                )
                """);

            // Training data table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS training_data (
                    data_id VARCHAR(255) PRIMARY KEY,
                    model_id VARCHAR(255),
                    input_data TEXT NOT NULL,
                    output_data TEXT,
                    reward DOUBLE DEFAULT 0.0,
                    timestamp BIGINT NOT NULL,
                    FOREIGN KEY (model_id) REFERENCES ai_models(model_id) ON DELETE SET NULL
                )
                """);

            // AI decisions table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS ai_decisions (
                    decision_id VARCHAR(255) PRIMARY KEY,
                    model_id VARCHAR(255) NOT NULL,
                    input_state TEXT NOT NULL,
                    action_taken TEXT NOT NULL,
                    reward_received DOUBLE DEFAULT 0.0,
                    next_state TEXT,
                    timestamp BIGINT NOT NULL,
                    FOREIGN KEY (model_id) REFERENCES ai_models(model_id) ON DELETE CASCADE
                )
                """);

            // Transaction records table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    transaction_id VARCHAR(255) PRIMARY KEY,
                    player_id VARCHAR(36) NOT NULL,
                    transaction_type VARCHAR(50) NOT NULL,
                    amount DOUBLE NOT NULL,
                    currency_type VARCHAR(10) DEFAULT 'COINS',
                    description VARCHAR(255),
                    timestamp BIGINT NOT NULL,
                    FOREIGN KEY (player_id) REFERENCES player_data(player_id) ON DELETE CASCADE
                )
                """);

            plugin.getLogger().info("Database tables created successfully");
        }
    }

    /**
     * Check if database is connected
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Get database connection (for advanced operations)
     */
    public Connection getConnection() {
        return connection;
    }
}
