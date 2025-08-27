package chef.sheesh.eyeAI.infra.cache;

/**
 * Configuration class for caching layers
 */
public class CacheConfig {

    // Player cache settings
    private final long playerCacheMaxSize;
    private final long playerCacheExpireMinutes;

    // AI Model cache settings
    private final long aiModelCacheMaxSize;
    private final long aiModelCacheExpireHours;

    // Training data cache settings
    private final long trainingDataCacheMaxSize;
    private final long trainingDataCacheExpireHours;

    // General cache settings
    private final boolean cacheEnabled;
    private final boolean metricsEnabled;

    public CacheConfig(long playerCacheMaxSize, long playerCacheExpireMinutes,
                      long aiModelCacheMaxSize, long aiModelCacheExpireHours,
                      long trainingDataCacheMaxSize, long trainingDataCacheExpireHours,
                      boolean cacheEnabled, boolean metricsEnabled) {
        this.playerCacheMaxSize = playerCacheMaxSize;
        this.playerCacheExpireMinutes = playerCacheExpireMinutes;
        this.aiModelCacheMaxSize = aiModelCacheMaxSize;
        this.aiModelCacheExpireHours = aiModelCacheExpireHours;
        this.trainingDataCacheMaxSize = trainingDataCacheMaxSize;
        this.trainingDataCacheExpireHours = trainingDataCacheExpireHours;
        this.cacheEnabled = cacheEnabled;
        this.metricsEnabled = metricsEnabled;
    }

    // Getters
    public long getPlayerCacheMaxSize() { return playerCacheMaxSize; }
    public long getPlayerCacheExpireMinutes() { return playerCacheExpireMinutes; }
    public long getAiModelCacheMaxSize() { return aiModelCacheMaxSize; }
    public long getAiModelCacheExpireHours() { return aiModelCacheExpireHours; }
    public long getTrainingDataCacheMaxSize() { return trainingDataCacheMaxSize; }
    public long getTrainingDataCacheExpireHours() { return trainingDataCacheExpireHours; }
    public boolean isCacheEnabled() { return cacheEnabled; }
    public boolean isMetricsEnabled() { return metricsEnabled; }

    /**
     * Create default cache configuration
     */
    public static CacheConfig createDefault() {
        return new CacheConfig(
            10000L,    // playerCacheMaxSize
            30L,       // playerCacheExpireMinutes
            100L,      // aiModelCacheMaxSize
            1L,        // aiModelCacheExpireHours
            500L,      // trainingDataCacheMaxSize
            2L,        // trainingDataCacheExpireHours
            true,      // cacheEnabled
            true       // metricsEnabled
        );
    }

    /**
     * Create cache configuration from values
     */
    public static CacheConfig create(long playerMaxSize, long playerExpireMinutes,
                                   long aiModelMaxSize, long aiModelExpireHours,
                                   long trainingDataMaxSize, long trainingDataExpireHours) {
        return new CacheConfig(
            playerMaxSize,
            playerExpireMinutes,
            aiModelMaxSize,
            aiModelExpireHours,
            trainingDataMaxSize,
            trainingDataExpireHours,
            true,
            true
        );
    }
}
