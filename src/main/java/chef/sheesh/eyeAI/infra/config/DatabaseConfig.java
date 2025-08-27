package chef.sheesh.eyeAI.infra.config;

import chef.sheesh.eyeAI.infra.data.DataStore;
import chef.sheesh.eyeAI.infra.data.H2Provider;
import chef.sheesh.eyeAI.infra.data.PostgresProvider;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Database configuration that supports multiple database providers
 */
public class DatabaseConfig {

    private final JavaPlugin plugin;
    private final ConfigurationManager configManager;
    private DataStore dataStore;
    private String providerType;

    public DatabaseConfig(JavaPlugin plugin, ConfigurationManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    /**
     * Initialize the database provider based on configuration
     */
    public void initialize() {
        FileConfiguration config = configManager.getConfig();
        this.providerType = config.getString(ConfigKeys.STORAGE_PROVIDER, "H2").toUpperCase();

        plugin.getLogger().info("Initializing database provider: " + providerType);

        try {
            switch (providerType) {
                case "H2" -> {
                    this.dataStore = new H2Provider(plugin, configManager);
                }
                case "POSTGRES" -> {
                    this.dataStore = new PostgresProvider(plugin, configManager);
                }
                default -> {
                    plugin.getLogger().warning("Unknown database provider '" + providerType + "', falling back to H2");
                    this.dataStore = new H2Provider(plugin, configManager);
                    this.providerType = "H2";
                }
            }

            // Connect to the database
            dataStore.connect();

            plugin.getLogger().info("Database provider '" + providerType + "' initialized successfully");
            testConnection();

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize database provider '" + providerType + "': " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    /**
     * Test database connection
     */
    private void testConnection() {
        try {
            // Try to save and load a test value
            String testKey = "connection_test_" + System.currentTimeMillis();
            String testValue = "test_value";

            dataStore.saveData(testKey, testValue);
            Object retrievedValue = dataStore.loadData(testKey);

            if (testValue.equals(retrievedValue)) {
                plugin.getLogger().info("Database connection test successful");
            } else {
                plugin.getLogger().warning("Database connection test failed - data mismatch");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Database connection test failed: " + e.getMessage());
            throw new RuntimeException("Failed to establish database connection", e);
        }
    }

    /**
     * Get the data store
     */
    public DataStore getDataStore() {
        return dataStore;
    }

    /**
     * Get the current provider type
     */
    public String getProviderType() {
        return providerType;
    }

    /**
     * Shutdown the database provider
     */
    public void shutdown() {
        if (dataStore != null) {
            dataStore.disconnect();
            plugin.getLogger().info("Database provider '" + providerType + "' shut down");
        }
    }

    /**
     * Get database connection metrics
     */
    public DatabaseMetrics getMetrics() {
        if (dataStore == null) {
            return new DatabaseMetrics(0, 0, 0, 0);
        }

        // For embedded databases, we don't have connection pools
        // This is a simplified metrics implementation
        boolean isConnected = false;
        if (dataStore instanceof H2Provider h2Provider) {
            isConnected = h2Provider.isConnected();
        } else if (dataStore instanceof PostgresProvider pgProvider) {
            isConnected = pgProvider.isConnected();
        }

        return new DatabaseMetrics(
            isConnected ? 1 : 0,  // total connections
            isConnected ? 1 : 0,  // active connections
            isConnected ? 0 : 0,  // idle connections
            0  // threads awaiting connection
        );
    }

    /**
     * Database metrics record
     */
    public record DatabaseMetrics(
        int totalConnections,
        int activeConnections,
        int idleConnections,
        int threadsAwaitingConnection
    ) {}
}
