package chef.sheesh.eyeAI.infra.data;

import chef.sheesh.eyeAI.infra.config.ConfigKeys;
import chef.sheesh.eyeAI.infra.config.ConfigurationManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;

/**
 * PostgreSQL Database Provider for external database storage
 */
public final class PostgresProvider implements DataStore {

    private final JavaPlugin plugin;
    private final ConfigurationManager configManager;
    private final ObjectMapper objectMapper;
    private Connection connection;
    private String databaseUrl;

    public PostgresProvider(JavaPlugin plugin, ConfigurationManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }

    @Override
    public void connect() {
        try {
            // Load PostgreSQL driver
            Class.forName("org.postgresql.Driver");

            // Get connection parameters
            String host = configManager.getString(ConfigKeys.STORAGE_POSTGRES_HOST, "localhost");
            int port = configManager.getInt(ConfigKeys.STORAGE_POSTGRES_PORT, 5432);
            String database = configManager.getString(ConfigKeys.STORAGE_POSTGRES_DATABASE, "chefai");
            String user = configManager.getString(ConfigKeys.STORAGE_POSTGRES_USER, "postgres");
            String password = configManager.getString(ConfigKeys.STORAGE_POSTGRES_PASSWORD, "");

            // Build connection URL
            this.databaseUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);

            // Set connection properties
            Properties props = new Properties();
            props.setProperty("user", user);
            if (!password.isEmpty()) {
                props.setProperty("password", password);
            }
            props.setProperty("ssl", "false");
            props.setProperty("sslmode", "disable");
            props.setProperty("connectTimeout", "10000");
            props.setProperty("socketTimeout", "30000");

            // Connect to database
            this.connection = DriverManager.getConnection(databaseUrl, props);
            this.connection.setAutoCommit(true);

            // Test connection
            try (PreparedStatement testStmt = connection.prepareStatement("SELECT 1")) {
                testStmt.executeQuery();
            }

            // Create tables
            createTables();

            plugin.getLogger().info("PostgreSQL database connected successfully to: " + host + ":" + port + "/" + database);

        } catch (ClassNotFoundException e) {
            plugin.getLogger().log(Level.SEVERE, "PostgreSQL driver not found", e);
            throw new RuntimeException("Failed to load PostgreSQL driver", e);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to connect to PostgreSQL database", e);
            throw new RuntimeException("Failed to connect to PostgreSQL database", e);
        }
    }

    @Override
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("PostgreSQL database disconnected successfully");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Error disconnecting from PostgreSQL database", e);
        }
    }

    @Override
    public void saveData(String key, Object data) {
        if (connection == null) {
            plugin.getLogger().warning("Cannot save data: PostgreSQL connection is null");
            return;
        }

        String sql = """
            INSERT INTO data_store (data_key, data_value, data_type, created_at, updated_at)
            VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (data_key) DO UPDATE SET
                data_value = EXCLUDED.data_value,
                data_type = EXCLUDED.data_type,
                updated_at = CURRENT_TIMESTAMP
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
            plugin.getLogger().warning("Cannot load data: PostgreSQL connection is null");
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
            // Enable UUID extension
            stmt.execute("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\"");

            // Main data store table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS data_store (
                    data_key VARCHAR(255) PRIMARY KEY,
                    data_value TEXT NOT NULL,
                    data_type VARCHAR(100) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
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
                    kdr DOUBLE PRECISION DEFAULT 0.0,
                    assists INTEGER DEFAULT 0,
                    wins INTEGER DEFAULT 0,
                    losses INTEGER DEFAULT 0,
                    balance DOUBLE PRECISION DEFAULT 0.0,
                    tokens INTEGER DEFAULT 0,
                    scoreboard_enabled BOOLEAN DEFAULT TRUE,
                    preferred_language VARCHAR(5) DEFAULT 'en',
                    first_join BIGINT NOT NULL,
                    last_seen BIGINT NOT NULL,
                    playtime BIGINT DEFAULT 0,
                    afk_time BIGINT DEFAULT 0,
                    average_session_length DOUBLE PRECISION DEFAULT 0.0,
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
                    accuracy DOUBLE PRECISION DEFAULT 0.0,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);

            // Training data table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS training_data (
                    data_id VARCHAR(255) PRIMARY KEY,
                    model_id VARCHAR(255),
                    input_data TEXT NOT NULL,
                    output_data TEXT,
                    reward DOUBLE PRECISION DEFAULT 0.0,
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
                    reward_received DOUBLE PRECISION DEFAULT 0.0,
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
                    amount DOUBLE PRECISION NOT NULL,
                    currency_type VARCHAR(10) DEFAULT 'COINS',
                    description VARCHAR(255),
                    timestamp BIGINT NOT NULL,
                    FOREIGN KEY (player_id) REFERENCES player_data(player_id) ON DELETE CASCADE
                )
                """);

            // Create indexes for better performance
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_player_data_name ON player_data(player_name)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_player_data_last_seen ON player_data(last_seen)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_ai_models_type ON ai_models(model_type)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_training_data_model ON training_data(model_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_transactions_player ON transactions(player_id)");

            plugin.getLogger().info("PostgreSQL database tables created successfully");
        }
    }

    /**
     * Check if database is connected
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(5);
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

    /**
     * Test database connection
     */
    public boolean testConnection() {
        if (connection == null) {
            return false;
        }

        try (PreparedStatement stmt = connection.prepareStatement("SELECT 1")) {
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Database connection test failed", e);
            return false;
        }
    }
}
