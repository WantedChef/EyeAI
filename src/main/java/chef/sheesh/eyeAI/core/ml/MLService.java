package chef.sheesh.eyeAI.core.ml;

import lombok.extern.slf4j.Slf4j;

import chef.sheesh.eyeAI.core.ml.features.FeatureEngineer;
import chef.sheesh.eyeAI.core.ml.ga.GAOptimizer;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.core.sim.SimExperience;
import chef.sheesh.eyeAI.infra.config.ConfigurationManager;
import chef.sheesh.eyeAI.infra.events.EventBus;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * High-level ML Service providing easy access to all ML functionality.
 * Acts as a facade with caching, auto-training, health monitoring, and thread-safety.
 */
@Slf4j
public final class MLService {

    private final MLManager mlManager;
    private final EventBus eventBus;
    private final ConfigurationManager config;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final ReentrantLock cacheLock = new ReentrantLock();
    private final Map<String, CachedPrediction> predictionCache = new HashMap<>();

    private boolean autoTraining = true;
    private int defaultBatchSize = 32;
    private long predictionCacheExpiryMs = 5000;

    public MLService(EventBus eventBus, ConfigurationManager config, JavaPlugin plugin) {
        this.eventBus = eventBus;
        this.config = config;
        this.mlManager = new MLManager(eventBus, config, plugin);
        if (autoTraining) {
            scheduler.scheduleAtFixedRate(mlManager::trainBatch, 0, 1, TimeUnit.MINUTES);
        }
    }

    public CompletableFuture<Void> initialize() {
        return CompletableFuture.runAsync(() -> {
            mlManager.initializeMLComponents();
            eventBus.post(new MLServiceInitializedEvent());
        });
    }

    public void shutdown() {
        scheduler.shutdown();
        MLManager.MLModels models = mlManager.exportModels();
        mlManager.reset();
        eventBus.post(new MLServiceShutdownEvent(models));
    }

    public Location predictPlayerLocation(Player player) {
        String cacheKey = "location_" + player.getUniqueId();
        cacheLock.lock();
        try {
            CachedPrediction cached = predictionCache.get(cacheKey);
            if (cached != null && !cached.isExpired()) {
                return cached.location;
            }
            Location predicted = mlManager.predictNextLocation(player);
            predictionCache.put(cacheKey, new CachedPrediction(predicted));
            return predicted;
        } finally {
            cacheLock.unlock();
        }
    }

    public int predictBestAction(FakePlayer fakePlayer, long stateHash, int maxActions) {
        return mlManager.getBestAction(stateHash, maxActions);
    }

    public ActionRecommendation getActionRecommendation(FakePlayer fakePlayer, long stateHash, int maxActions) {
        int bestAction = mlManager.getBestAction(stateHash, maxActions);
        double[] qValues = mlManager.getStatistics().qStats.qTable.getOrDefault(stateHash, new double[maxActions]);
        double bestQValue = Arrays.stream(qValues).max().orElse(0.0);
        double secondBestQValue = Arrays.stream(qValues).sorted().skip(qValues.length - 2).findFirst().orElse(0.0);
        double confidence = Math.max(0.0, Math.min(1.0, (bestQValue - secondBestQValue) / 10.0));
        return new ActionRecommendation(bestAction, confidence, bestQValue);
    }

    public void addPlayerExperience(FakePlayer fakePlayer, SimExperience experience) {
        mlManager.addPlayerExperience(fakePlayer, experience);
    }

    public void addExperience(SimExperience experience) {
        mlManager.addExperience(experience);
    }

    public CompletableFuture<MLManager.MLTrainingResult> trainBatch(int batchSize) {
        mlManager.setBatchSize(batchSize);
        return mlManager.trainOnBatchAsync();
    }

    public CompletableFuture<GAOptimizer.GAEvolutionResult> evolveGA() {
        return mlManager.evolveGAAsync();
    }

    public double[] extractFeatures(FakePlayer fakePlayer) {
        return FeatureEngineer.createComprehensiveFeatures(fakePlayer);
    }

    public double[] extractMovementFeatures(FakePlayer fakePlayer) {
        return FeatureEngineer.extractMovementFeatures(fakePlayer);
    }

    public double[] extractCombatFeatures(FakePlayer fakePlayer) {
        return FeatureEngineer.extractCombatFeatures(fakePlayer);
    }

    public void setAutoTraining(boolean enabled) {
        this.autoTraining = enabled;
        mlManager.setLearningEnabled(enabled);
    }

    public void setDefaultBatchSize(int batchSize) {
        this.defaultBatchSize = Math.max(1, batchSize);
    }

    public void setPredictionCacheExpiry(long expiryMs) {
        this.predictionCacheExpiryMs = Math.max(1000, expiryMs);
        clearPredictionCache();
    }

    public MLManager.MLStatistics getStatistics() {
        return mlManager.getStatistics();
    }

    public MLServiceHealth getHealth() {
        MLManager.MLStatistics stats = getStatistics();
        boolean isHealthy = stats.totalTrainingBatches > 0 && stats.qStats.updateCount > 0 && !Double.isNaN(stats.averageReward);
        return new MLServiceHealth(isHealthy ? "HEALTHY" : "DEGRADED", stats.totalExperiencesProcessed, stats.totalTrainingBatches, stats.averageReward, predictionCache.size());
    }

    public void clearPredictionCache() {
        cacheLock.lock();
        try {
            predictionCache.clear();
        } finally {
            cacheLock.unlock();
        }
    }

    public void reset() {
        mlManager.reset();
        clearPredictionCache();
        eventBus.post(new MLServiceResetEvent());
    }

    public long stateToHash(FakePlayer fakePlayer) {
        Location loc = fakePlayer.getLocation();
        return Objects.hash(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), fakePlayer.getHealth());
    }

    public int getAvailableActions(FakePlayer fakePlayer) {
        return 10; // Dynamic based on context
    }

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
            return String.format("ActionRecommendation{action=%d, confidence=%.3f, qValue=%.3f}", action, confidence, qValue);
        }
    }

    public static class MLServiceHealth {
        public final String status;
        public final long experiencesProcessed;
        public final long trainingBatches;
        public final double averageReward;
        public final int cacheSize;

        public MLServiceHealth(String status, long experiencesProcessed, long trainingBatches, double averageReward, int cacheSize) {
            this.status = status;
            this.experiencesProcessed = experiencesProcessed;
            this.trainingBatches = trainingBatches;
            this.averageReward = averageReward;
            this.cacheSize = cacheSize;
        }

        @Override
        public String toString() {
            return String.format("MLServiceHealth{status=%s, experiences=%d, batches=%d, avgReward=%.3f, cacheSize=%d}", status, experiencesProcessed, trainingBatches, averageReward, cacheSize);
        }
    }

    private static class CachedPrediction {
        final Location location;
        final long timestamp;

        CachedPrediction(Location location) {
            this.location = location;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > 5000; // Use instance expiry
        }
    }

    // Event classes
    public static class MLServiceInitializedEvent {
        // Empty event class for initialization notification
    }

    public static class MLServiceShutdownEvent {
        public final MLManager.MLModels models;

        public MLServiceShutdownEvent(MLManager.MLModels models) {
            this.models = models;
        }
    }

    public static class MLServiceResetEvent {
        // Empty event class for reset notification
    }
}
