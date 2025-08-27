package chef.sheesh.eyeAI.infra.cache;

import chef.sheesh.eyeAI.data.model.AIModel;
import chef.sheesh.eyeAI.data.model.PlayerData;
import chef.sheesh.eyeAI.data.model.TrainingData;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.util.UUID;

/**
 * L1 Cache Manager using Caffeine for high-performance memory caching
 */
public class L1CacheManager {

    private final JavaPlugin plugin;
    private final Cache<String, PlayerData> playerCache;
    private final Cache<String, AIModel> aiModelCache;
    private final Cache<String, TrainingData> trainingDataCache;

    public L1CacheManager(JavaPlugin plugin, CacheConfig config) {
        this.plugin = plugin;

        // Player data cache - most frequently accessed
        this.playerCache = Caffeine.newBuilder()
            .maximumSize(config.getPlayerCacheMaxSize())
            .expireAfterWrite(Duration.ofMinutes(config.getPlayerCacheExpireMinutes()))
            .expireAfterAccess(Duration.ofMinutes(config.getPlayerCacheExpireMinutes() / 2))
            .recordStats()
            .removalListener((key, value, cause) ->
                plugin.getLogger().fine("Player cache entry removed: " + key + " cause: " + cause))
            .build();

        // AI Model cache - moderately accessed
        this.aiModelCache = Caffeine.newBuilder()
            .maximumSize(config.getAiModelCacheMaxSize())
            .expireAfterWrite(Duration.ofHours(config.getAiModelCacheExpireHours()))
            .expireAfterAccess(Duration.ofMinutes(30))
            .recordStats()
            .removalListener((key, value, cause) ->
                plugin.getLogger().fine("AI Model cache entry removed: " + key + " cause: " + cause))
            .build();

        // Training data cache - less frequently accessed
        this.trainingDataCache = Caffeine.newBuilder()
            .maximumSize(config.getTrainingDataCacheMaxSize())
            .expireAfterWrite(Duration.ofHours(config.getTrainingDataCacheExpireHours()))
            .expireAfterAccess(Duration.ofHours(1))
            .recordStats()
            .removalListener((key, value, cause) ->
                plugin.getLogger().fine("Training data cache entry removed: " + key + " cause: " + cause))
            .build();

        plugin.getLogger().info("L1 Cache Manager initialized successfully");
    }

    // Player Data Operations
    public PlayerData getPlayerData(UUID playerId) {
        String key = "player:" + playerId.toString();
        return playerCache.getIfPresent(key);
    }

    public void cachePlayerData(PlayerData playerData) {
        String key = "player:" + playerData.getPlayerId().toString();
        playerCache.put(key, playerData);
    }

    public void invalidatePlayerData(UUID playerId) {
        String key = "player:" + playerId.toString();
        playerCache.invalidate(key);
    }

    // AI Model Operations
    public AIModel getAIModel(String modelId) {
        String key = "aimodel:" + modelId;
        return aiModelCache.getIfPresent(key);
    }

    public void cacheAIModel(AIModel aiModel) {
        String key = "aimodel:" + aiModel.getModelId();
        aiModelCache.put(key, aiModel);
    }

    public void invalidateAIModel(String modelId) {
        String key = "aimodel:" + modelId;
        aiModelCache.invalidate(key);
    }

    // Training Data Operations
    public TrainingData getTrainingData(String dataId) {
        String key = "training:" + dataId;
        return trainingDataCache.getIfPresent(key);
    }

    public void cacheTrainingData(TrainingData trainingData) {
        String key = "training:" + trainingData.getDataId();
        trainingDataCache.put(key, trainingData);
    }

    public void invalidateTrainingData(String dataId) {
        String key = "training:" + dataId;
        trainingDataCache.invalidate(key);
    }

    // Cache Management Operations
    public void clearAllCaches() {
        playerCache.invalidateAll();
        aiModelCache.invalidateAll();
        trainingDataCache.invalidateAll();
        plugin.getLogger().info("All L1 caches cleared");
    }

    public void clearPlayerCache() {
        playerCache.invalidateAll();
        plugin.getLogger().info("Player cache cleared");
    }

    public void clearAIModelCache() {
        aiModelCache.invalidateAll();
        plugin.getLogger().info("AI Model cache cleared");
    }

    public void clearTrainingDataCache() {
        trainingDataCache.invalidateAll();
        plugin.getLogger().info("Training data cache cleared");
    }

    // Cache Statistics
    public CacheStats getPlayerCacheStats() {
        var stats = playerCache.stats();
        return new CacheStats(
            playerCache.estimatedSize(),
            stats.hitCount(),
            stats.missCount(),
            stats.hitRate(),
            stats.loadCount(),
            stats.evictionCount()
        );
    }

    public CacheStats getAIModelCacheStats() {
        var stats = aiModelCache.stats();
        return new CacheStats(
            aiModelCache.estimatedSize(),
            stats.hitCount(),
            stats.missCount(),
            stats.hitRate(),
            stats.loadCount(),
            stats.evictionCount()
        );
    }

    public CacheStats getTrainingDataCacheStats() {
        var stats = trainingDataCache.stats();
        return new CacheStats(
            trainingDataCache.estimatedSize(),
            stats.hitCount(),
            stats.missCount(),
            stats.hitRate(),
            stats.loadCount(),
            stats.evictionCount()
        );
    }

    public record CacheStats(
        long size,
        long hitCount,
        long missCount,
        double hitRate,
        long loadCount,
        long evictionCount
    ) {}
}
