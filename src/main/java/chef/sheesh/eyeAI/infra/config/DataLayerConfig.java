package chef.sheesh.eyeAI.infra.config;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Central configuration manager for all data layer components
 */
public class DataLayerConfig {

    private final JavaPlugin plugin;

    private final ConfigurationManager configurationManager;
    private final DatabaseConfig databaseConfig;
    private final RedisConfig redisConfig;
    private final CacheConfig cacheConfig;

    private boolean initialized = false;

    public DataLayerConfig(JavaPlugin plugin, ConfigurationManager configurationManager) {
        this.plugin = plugin;
        this.configurationManager = configurationManager;
        this.databaseConfig = new DatabaseConfig(plugin, configurationManager);
        this.redisConfig = new RedisConfig(plugin, configurationManager);
        this.cacheConfig = new CacheConfig(plugin, configurationManager);
    }

    /**
     * Initialize all data layer components
     */
    public void initialize() {
        if (initialized) {
            plugin.getLogger().warning("Data layer already initialized");
            return;
        }

        plugin.getLogger().info("Initializing data layer components...");

        try {
            // Initialize database first
            databaseConfig.initialize();

            // Initialize Redis
            redisConfig.initialize();

            // Initialize caching
            cacheConfig.initialize();

            initialized = true;
            plugin.getLogger().info("Data layer initialization completed successfully");

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize data layer: " + e.getMessage());
            throw new RuntimeException("Data layer initialization failed", e);
        }
    }

    /**
     * Shutdown all data layer components
     */
    public void shutdown() {
        if (!initialized) {
            return;
        }

        plugin.getLogger().info("Shutting down data layer components...");

        try {
            cacheConfig.clearAllCaches();
            redisConfig.shutdown();
            databaseConfig.shutdown();

            initialized = false;
            plugin.getLogger().info("Data layer shutdown completed");

        } catch (Exception e) {
            plugin.getLogger().severe("Error during data layer shutdown: " + e.getMessage());
        }
    }

    /**
     * Get database configuration
     */
    public DatabaseConfig getDatabaseConfig() {
        return databaseConfig;
    }

    /**
     * Get Redis configuration
     */
    public RedisConfig getRedisConfig() {
        return redisConfig;
    }

    /**
     * Get cache configuration
     */
    public CacheConfig getCacheConfig() {
        return cacheConfig;
    }

    /**
     * Get configuration manager
     */
    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    /**
     * Check if data layer is initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Get comprehensive health status
     */
    public DataLayerHealth getHealthStatus() {
        if (!initialized) {
            return new DataLayerHealth(false, "Not initialized", null, null, null);
        }

        try {
            var dbMetrics = databaseConfig.getMetrics();
            var redisMetrics = redisConfig.getMetrics();
            var cacheStats = cacheConfig.getCacheStatistics();

            boolean healthy = dbMetrics.activeConnections() > 0 && redisMetrics.activeConnections() >= 0;

            return new DataLayerHealth(
                healthy,
                healthy ? "All systems operational" : "One or more systems unhealthy",
                dbMetrics,
                redisMetrics,
                cacheStats
            );

        } catch (Exception e) {
            return new DataLayerHealth(false, "Health check failed: " + e.getMessage(), null, null, null);
        }
    }

    /**
     * Data layer health status record
     */
    public record DataLayerHealth(
        boolean healthy,
        String status,
        DatabaseConfig.DatabaseMetrics databaseMetrics,
        RedisConfig.RedisMetrics redisMetrics,
        CacheConfig.CacheStatistics cacheStatistics
    ) {}
}
