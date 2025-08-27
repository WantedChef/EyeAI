package chef.sheesh.eyeAI.infra.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;

/**
 * Multi-level caching configuration
 */
public class CacheConfig {

    private final JavaPlugin plugin;
    private final ConfigurationManager configManager;

    private Cache<String, Object> playerCache;
    private Cache<String, Object> aiModelCache;
    private Cache<String, Object> trainingDataCache;
    private Cache<String, Object> decisionCache;

    public CacheConfig(JavaPlugin plugin, ConfigurationManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    /**
     * Initialize all cache layers
     */
    public void initialize() {
        FileConfiguration config = configManager.getConfig();

        if (!config.getBoolean(ConfigKeys.CACHE_ENABLED, true)) {
            plugin.getLogger().info("Caching is disabled");
            return;
        }

        initializeL1Caches(config);
        plugin.getLogger().info("Multi-level caching initialized successfully");
    }

    /**
     * Initialize L1 (Memory) caches with Caffeine
     */
    private void initializeL1Caches(FileConfiguration config) {
        // Get cache configuration from new YAML structure
        boolean cacheEnabled = config.getBoolean("cache.enabled", true);
        if (!cacheEnabled) {
            plugin.getLogger().info("L1 caching is disabled in configuration");
            return;
        }

        int maxSize = config.getInt("cache.l1.maxSize", 10000);
        int expireMinutes = config.getInt("cache.l1.expireMinutes", 30);
        boolean metricsEnabled = config.getBoolean("cache.metrics.enabled", true);

        Caffeine<Object, Object> builder = Caffeine.newBuilder()
            .maximumSize(maxSize)
            .expireAfterWrite(Duration.ofMinutes(expireMinutes))
            .expireAfterAccess(Duration.ofMinutes(expireMinutes / 2))
            .removalListener((key, value, cause) ->
                plugin.getLogger().fine("L1 cache entry removed: " + key + " cause: " + cause));

        if (metricsEnabled) {
            builder.recordStats();
        }

        // Player data cache
        this.playerCache = builder.build();
        plugin.getLogger().fine("Player cache initialized with max size: " + maxSize);

        // AI Model cache
        this.aiModelCache = Caffeine.newBuilder()
            .maximumSize(100)  // Fixed size for AI models
            .expireAfterWrite(Duration.ofHours(1))
            .expireAfterAccess(Duration.ofMinutes(30))
            .recordStats()
            .build();
        plugin.getLogger().fine("AI Model cache initialized");

        // Training data cache
        this.trainingDataCache = Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(Duration.ofHours(2))
            .expireAfterAccess(Duration.ofHours(1))
            .recordStats()
            .build();
        plugin.getLogger().fine("Training data cache initialized");

        // Decision cache
        this.decisionCache = Caffeine.newBuilder()
            .maximumSize(5000)
            .expireAfterWrite(Duration.ofDays(7))
            .expireAfterAccess(Duration.ofDays(1))
            .recordStats()
            .build();
        plugin.getLogger().fine("Decision cache initialized");
    }

    /**
     * Get player cache
     */
    public Cache<String, Object> getPlayerCache() {
        return playerCache;
    }

    /**
     * Get AI model cache
     */
    public Cache<String, Object> getAiModelCache() {
        return aiModelCache;
    }

    /**
     * Get training data cache
     */
    public Cache<String, Object> getTrainingDataCache() {
        return trainingDataCache;
    }

    /**
     * Get decision cache
     */
    public Cache<String, Object> getDecisionCache() {
        return decisionCache;
    }

    /**
     * Clear all caches
     */
    public void clearAllCaches() {
        if (playerCache != null) {
            playerCache.invalidateAll();
        }
        if (aiModelCache != null) {
            aiModelCache.invalidateAll();
        }
        if (trainingDataCache != null) {
            trainingDataCache.invalidateAll();
        }
        if (decisionCache != null) {
            decisionCache.invalidateAll();
        }
        plugin.getLogger().info("All caches cleared");
    }

    /**
     * Get cache statistics
     */
    public CacheStatistics getCacheStatistics() {
        return new CacheStatistics(
            getCacheStats(playerCache, "player"),
            getCacheStats(aiModelCache, "ai_model"),
            getCacheStats(trainingDataCache, "training_data"),
            getCacheStats(decisionCache, "decision")
        );
    }

    private CacheStats getCacheStats(Cache<String, Object> cache, String name) {
        if (cache == null) {
            return new CacheStats(name, 0, 0, 0, 0, 0, 0);
        }

        var stats = cache.stats();
        return new CacheStats(
            name,
            cache.estimatedSize(),
            stats.hitCount(),
            stats.missCount(),
            stats.hitRate(),
            stats.loadCount(),
            stats.evictionCount()
        );
    }

    /**
     * Cache statistics record
     */
    public record CacheStatistics(
        CacheStats playerCache,
        CacheStats aiModelCache,
        CacheStats trainingDataCache,
        CacheStats decisionCache
    ) {}

    /**
     * Individual cache statistics
     */
    public record CacheStats(
        String cacheName,
        long size,
        long hitCount,
        long missCount,
        double hitRate,
        long loadCount,
        long evictionCount
    ) {}
}
