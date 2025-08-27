package chef.sheesh.eyeAI.core.ml;

import chef.sheesh.eyeAI.core.ml.features.FeatureEngineer;
import chef.sheesh.eyeAI.core.ml.ga.GAOptimizer;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.core.sim.SimExperience;
import chef.sheesh.eyeAI.infra.config.ConfigurationManager;
import chef.sheesh.eyeAI.infra.events.EventBus;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * High-level ML Service providing easy access to all ML functionality.
 * Acts as a facade for the ML system.
 */
public final class MLService {

    private final MLManager mlManager;
    private final EventBus eventBus;
    private final ConfigurationManager config;

    // Service configuration
    private boolean autoTraining = true;
    private int defaultBatchSize = 32;
    private long predictionCacheExpiryMs = 5000; // 5 seconds

    // Prediction cache
    private final Map<String, CachedPrediction> predictionCache = new HashMap<>();

    public MLService(EventBus eventBus, ConfigurationManager config) {
        this.eventBus = eventBus;
        this.config = config;
        this.mlManager = new MLManager(eventBus, config);
    }

    /**
     * Initialize the ML service
     */
    public CompletableFuture<Void> initialize() {
        return CompletableFuture.runAsync(() -> {
            mlManager.initializeMLComponents();
            eventBus.post(new MLServiceInitializedEvent());
        });
    }

    /**
     * Shutdown the ML service
     */
    public void shutdown() {
        MLManager.MLModels models = mlManager.exportModels();
        mlManager.reset();
        eventBus.post(new MLServiceShutdownEvent(models));
    }

    // Prediction Methods

    /**
     * Predict next location for a player
     */
    public Location predictPlayerLocation(Player player) {
        String cacheKey = "location_" + player.getUniqueId();
        CachedPrediction cached = predictionCache.get(cacheKey);

        if (cached != null && !cached.isExpired()) {
            return cached.location;
        }

        Location predicted = mlManager.predictNextLocation(player);
        predictionCache.put(cacheKey, new CachedPrediction(predicted));

        return predicted;
    }

    /**
     * Predict best action for a fake player
     */
    public int predictBestAction(FakePlayer fakePlayer, long stateHash, int maxActions) {
        return mlManager.getBestAction(stateHash, maxActions);
    }

    /**
     * Get action recommendation with confidence
     */
    public ActionRecommendation getActionRecommendation(FakePlayer fakePlayer, long stateHash, int maxActions) {
        int bestAction = mlManager.getBestAction(stateHash, maxActions);

        // Calculate confidence based on Q-value difference
        double[] qValues = mlManager.getStatistics().qStats.qTable.getOrDefault(stateHash, new double[maxActions]);
        double bestQValue = Double.NEGATIVE_INFINITY;
        double secondBestQValue = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < Math.min(qValues.length, maxActions); i++) {
            if (qValues[i] > bestQValue) {
                secondBestQValue = bestQValue;
                bestQValue = qValues[i];
            } else if (qValues[i] > secondBestQValue) {
                secondBestQValue = qValues[i];
            }
        }

        double confidence = bestQValue - secondBestQValue;
        confidence = Math.max(0.0, Math.min(1.0, confidence / 10.0)); // Normalize to 0-1

        return new ActionRecommendation(bestAction, confidence, bestQValue);
    }

    // Training Methods

    /**
     * Add experience from fake player
     */
    public void addPlayerExperience(FakePlayer fakePlayer, SimExperience experience) {
        mlManager.addPlayerExperience(fakePlayer, experience);
    }

    /**
     * Add raw experience data
     */
    public void addExperience(SimExperience experience) {
        mlManager.addExperience(experience);
    }

    /**
     * Train models on batch
     */
    public CompletableFuture<MLManager.MLTrainingResult> trainBatch(int batchSize) {
        mlManager.setBatchSize(batchSize);
        return mlManager.trainOnBatchAsync();
    }

    /**
     * Run genetic algorithm evolution
     */
    public CompletableFuture<GAOptimizer.GAEvolutionResult> evolveGA() {
        return mlManager.evolveGAAsync();
    }

    // Feature Engineering Methods

    /**
     * Extract features from fake player
     */
    public double[] extractFeatures(FakePlayer fakePlayer) {
        return FeatureEngineer.createComprehensiveFeatures(fakePlayer);
    }

    /**
     * Extract movement features only
     */
    public double[] extractMovementFeatures(FakePlayer fakePlayer) {
        return FeatureEngineer.extractMovementFeatures(fakePlayer);
    }

    /**
     * Extract combat features only
     */
    public double[] extractCombatFeatures(FakePlayer fakePlayer) {
        return FeatureEngineer.extractCombatFeatures(fakePlayer);
    }

    // Configuration Methods

    /**
     * Enable/disable auto training
     */
    public void setAutoTraining(boolean enabled) {
        this.autoTraining = enabled;
        mlManager.setLearningEnabled(enabled);
    }

    /**
     * Set default batch size
     */
    public void setDefaultBatchSize(int batchSize) {
        this.defaultBatchSize = Math.max(1, batchSize);
    }

    /**
     * Set prediction cache expiry time
     */
    public void setPredictionCacheExpiry(long expiryMs) {
        this.predictionCacheExpiryMs = expiryMs;
        predictionCache.clear(); // Clear cache with new expiry
    }

    // Statistics and Monitoring

    /**
     * Get comprehensive ML statistics
     */
    public MLManager.MLStatistics getStatistics() {
        return mlManager.getStatistics();
    }

    /**
     * Get service health status
     */
    public MLServiceHealth getHealth() {
        MLManager.MLStatistics stats = getStatistics();

        boolean isHealthy = stats.totalTrainingBatches > 0 &&
                          stats.qStats.updateCount > 0 &&
                          !Double.isNaN(stats.averageReward);

        String status = isHealthy ? "HEALTHY" : "DEGRADED";

        return new MLServiceHealth(
            status,
            stats.totalExperiencesProcessed,
            stats.totalTrainingBatches,
            stats.averageReward,
            predictionCache.size()
        );
    }

    /**
     * Clear prediction cache
     */
    public void clearPredictionCache() {
        predictionCache.clear();
    }

    /**
     * Reset all ML models
     */
    public void reset() {
        mlManager.reset();
        predictionCache.clear();
        eventBus.post(new MLServiceResetEvent());
    }

    // Utility Methods

    /**
     * Convert state to hash for Q-learning
     */
    public long stateToHash(FakePlayer fakePlayer) {
        Location loc = fakePlayer.getLocation();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        // Simple hash combining position and health
        long hash = ((long) x & 0x3FF) |
                   (((long) y & 0xFF) << 10) |
                   (((long) z & 0x3FF) << 18) |
                   (((long) (fakePlayer.getHealth() * 10) & 0xFF) << 26);

        return hash;
    }

    /**
     * Get available action count for current context
     */
    public int getAvailableActions(FakePlayer fakePlayer) {
        // This would depend on your game's action system
        // Basic actions: move, attack, use item, flee
        return 10; // Placeholder
    }

    // Event Classes

    public static class MLServiceInitializedEvent {
        public final long timestamp = System.currentTimeMillis();
    }

    public static class MLServiceShutdownEvent {
        public final MLManager.MLModels finalModels;
        public final long timestamp = System.currentTimeMillis();

        public MLServiceShutdownEvent(MLManager.MLModels models) {
            this.finalModels = models;
        }
    }

    public static class MLServiceResetEvent {
        public final long timestamp = System.currentTimeMillis();
    }

    // Data Classes

    public static class ActionRecommendation {
        public final int action;
        public final double confidence;
        public final double qValue;

        public ActionRecommendation(int action, double confidence, double qValue) {
            this.action = action;
            this.confidence = confidence;
            this.qValue = qValue;
        }

        @Override
        public String toString() {
            return String.format("ActionRecommendation{action=%d, confidence=%.3f, qValue=%.3f}",
                               action, confidence, qValue);
        }
    }

    public static class MLServiceHealth {
        public final String status;
        public final long experiencesProcessed;
        public final long trainingBatches;
        public final double averageReward;
        public final int cacheSize;

        public MLServiceHealth(String status, long experiencesProcessed, long trainingBatches,
                             double averageReward, int cacheSize) {
            this.status = status;
            this.experiencesProcessed = experiencesProcessed;
            this.trainingBatches = trainingBatches;
            this.averageReward = averageReward;
            this.cacheSize = cacheSize;
        }

        @Override
        public String toString() {
            return String.format("MLServiceHealth{status=%s, experiences=%d, batches=%d, avgReward=%.3f, cacheSize=%d}",
                               status, experiencesProcessed, trainingBatches, averageReward, cacheSize);
        }
    }

    // Cached prediction for performance
    private class CachedPrediction {
        final Location location;
        final long timestamp;

        CachedPrediction(Location location) {
            this.location = location;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > predictionCacheExpiryMs;
        }
    }
}
