package chef.sheesh.eyeAI.infra.data;

import chef.sheesh.eyeAI.infra.config.DatabaseConfig;
import chef.sheesh.eyeAI.infra.config.RedisConfig;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Comprehensive database health monitoring and diagnostics
 */
public class DatabaseHealthCheck {

    private final JavaPlugin plugin;
    private final DatabaseConfig databaseConfig;
    private final RedisConfig redisConfig;
    private volatile HealthStatus lastHealthStatus;
    private volatile long lastCheckTime;

    public DatabaseHealthCheck(JavaPlugin plugin, DatabaseConfig databaseConfig, RedisConfig redisConfig) {
        this.plugin = plugin;
        this.databaseConfig = databaseConfig;
        this.redisConfig = redisConfig;
    }

    /**
     * Perform comprehensive health check
     */
    public CompletableFuture<HealthStatus> checkHealth() {
        return CompletableFuture.supplyAsync(this::performHealthCheck)
                .orTimeout(10, TimeUnit.SECONDS)
                .exceptionally(throwable -> {
                    plugin.getLogger().log(Level.WARNING, "Health check timed out or failed", throwable);
                    return new HealthStatus(
                        HealthStatus.Status.UNHEALTHY,
                        "Health check failed: " + throwable.getMessage(),
                        null, null, null, null,
                        System.currentTimeMillis() - lastCheckTime
                    );
                })
                .whenComplete((status, throwable) -> {
                    if (throwable == null) {
                        lastHealthStatus = status;
                        lastCheckTime = System.currentTimeMillis();
                    }
                });
    }

    /**
     * Perform the actual health check
     */
    private HealthStatus performHealthCheck() {
        long startTime = System.currentTimeMillis();
        StringBuilder details = new StringBuilder();

        // Check database connectivity
        DatabaseHealth dbHealth = checkDatabaseHealth();
        details.append("Database: ").append(dbHealth.status).append(" (").append(dbHealth.responseTime).append("ms)");

        // Check Redis connectivity
        RedisHealth redisHealth = checkRedisHealth();
        details.append(", Redis: ").append(redisHealth.status).append(" (").append(redisHealth.responseTime).append("ms)");

        // Check cache performance
        CacheHealth cacheHealth = checkCacheHealth();
        details.append(", Cache: ").append(cacheHealth.status);

        // Determine overall status
        HealthStatus.Status overallStatus = determineOverallStatus(dbHealth, redisHealth, cacheHealth);
        long totalTime = System.currentTimeMillis() - startTime;

        return new HealthStatus(
            overallStatus,
            details.toString(),
            dbHealth,
            redisHealth,
            cacheHealth,
            generateRecommendations(dbHealth, redisHealth, cacheHealth),
            totalTime
        );
    }

    /**
     * Check database health
     */
    private DatabaseHealth checkDatabaseHealth() {
        long startTime = System.currentTimeMillis();

        try {
            chef.sheesh.eyeAI.infra.data.DataStore dataStore = databaseConfig.getDataStore();
            if (dataStore == null) {
                return new DatabaseHealth(DatabaseHealth.Status.DISCONNECTED, 0, "DataStore not initialized");
            }

            // Test basic connectivity with DataStore
            String testKey = "health_check_" + System.currentTimeMillis();
            String testValue = "ok";

            dataStore.saveData(testKey, testValue);
            Object retrieved = dataStore.loadData(testKey);

            if (testValue.equals(retrieved)) {
                long responseTime = System.currentTimeMillis() - startTime;
                return new DatabaseHealth(DatabaseHealth.Status.HEALTHY, responseTime, null);
            } else {
                return new DatabaseHealth(DatabaseHealth.Status.UNHEALTHY, 0, "Data consistency check failed");
            }

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return new DatabaseHealth(DatabaseHealth.Status.UNHEALTHY, responseTime, e.getMessage());
        }
    }

    /**
     * Check Redis health
     */
    private RedisHealth checkRedisHealth() {
        long startTime = System.currentTimeMillis();

        try {
            redisConfig.execute(jedis -> {
                String pong = jedis.ping();
                if (!"PONG".equals(pong)) {
                    throw new RuntimeException("Redis PING failed: " + pong);
                }
                return null;
            });

            long responseTime = System.currentTimeMillis() - startTime;
            return new RedisHealth(RedisHealth.Status.HEALTHY, responseTime, null);

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return new RedisHealth(RedisHealth.Status.UNHEALTHY, responseTime, e.getMessage());
        }
    }

    /**
     * Check cache health
     */
    private CacheHealth checkCacheHealth() {
        try {
            var metrics = redisConfig.getMetrics();
            var cacheManager = redisConfig.getL2CacheManager();

            // Check Redis memory usage
            if (metrics.usedMemoryBytes() > 100 * 1024 * 1024) { // 100MB
                return new CacheHealth(CacheHealth.Status.WARNING, "High memory usage: " + (metrics.usedMemoryBytes() / 1024 / 1024) + "MB");
            }

            // Check hit rates (using reflection to access methods)
            try {
                var cacheStats = cacheManager.getClass().getMethod("getCacheStats").invoke(cacheManager);
                double hitRate = (Double) cacheStats.getClass().getMethod("hitRate").invoke(cacheStats);
                if (hitRate < 0.5) {
                    return new CacheHealth(CacheHealth.Status.WARNING, "Low cache hit rate: " + String.format("%.2f%%", hitRate * 100));
                }
            } catch (Exception e) {
                // Ignore hit rate check if methods not available
            }

            return new CacheHealth(CacheHealth.Status.HEALTHY, "All cache metrics normal");

        } catch (Exception e) {
            return new CacheHealth(CacheHealth.Status.UNHEALTHY, "Cache check failed: " + e.getMessage());
        }
    }

    /**
     * Determine overall health status
     */
    private HealthStatus.Status determineOverallStatus(DatabaseHealth db, RedisHealth redis, CacheHealth cache) {
        if (db.status == DatabaseHealth.Status.UNHEALTHY || redis.status == RedisHealth.Status.UNHEALTHY) {
            return HealthStatus.Status.UNHEALTHY;
        }

        if (cache.status == CacheHealth.Status.UNHEALTHY) {
            return HealthStatus.Status.WARNING;
        }

        return HealthStatus.Status.HEALTHY;
    }

    /**
     * Generate health recommendations
     */
    private String generateRecommendations(DatabaseHealth db, RedisHealth redis, CacheHealth cache) {
        StringBuilder recommendations = new StringBuilder();

        if (db.status == DatabaseHealth.Status.UNHEALTHY) {
            recommendations.append("• Fix database connectivity issues\n");
        } else if (db.responseTime > 1000) {
            recommendations.append("• Database response time is high (").append(db.responseTime).append("ms) - consider optimization\n");
        }

        if (redis.status == RedisHealth.Status.UNHEALTHY) {
            recommendations.append("• Fix Redis connectivity issues\n");
        } else if (redis.responseTime > 500) {
            recommendations.append("• Redis response time is high (").append(redis.responseTime).append("ms) - consider optimization\n");
        }

        if (cache.status == CacheHealth.Status.WARNING) {
            recommendations.append("• ").append(cache.details).append(" - review cache configuration\n");
        }

        return recommendations.length() > 0 ? recommendations.toString() : "All systems operating normally";
    }

    /**
     * Get last health status
     */
    public HealthStatus getLastHealthStatus() {
        return lastHealthStatus;
    }

    /**
     * Get time since last check
     */
    public long getTimeSinceLastCheck() {
        return System.currentTimeMillis() - lastCheckTime;
    }

    // Health status records
    public record HealthStatus(
        Status status,
        String details,
        DatabaseHealth databaseHealth,
        RedisHealth redisHealth,
        CacheHealth cacheHealth,
        String recommendations,
        long checkDurationMs
    ) {
        public enum Status {
            HEALTHY("✅ Healthy"),
            WARNING("⚠️ Warning"),
            UNHEALTHY("❌ Unhealthy");

            private final String displayName;

            Status(String displayName) {
                this.displayName = displayName;
            }

            public String getDisplayName() {
                return displayName;
            }
        }
    }

    public record DatabaseHealth(
        Status status,
        long responseTime,
        String errorMessage
    ) {
        public enum Status {
            HEALTHY, UNHEALTHY, DISCONNECTED
        }
    }

    public record RedisHealth(
        Status status,
        long responseTime,
        String errorMessage
    ) {
        public enum Status {
            HEALTHY, UNHEALTHY, DISCONNECTED
        }
    }

    public record CacheHealth(
        Status status,
        String details
    ) {
        public enum Status {
            HEALTHY, WARNING, UNHEALTHY
        }
    }
}
