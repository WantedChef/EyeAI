package chef.sheesh.eyeAI.monitoring;

import chef.sheesh.eyeAI.core.ml.MLManager;
import chef.sheesh.eyeAI.core.ml.TrainingManager;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

/**
 * AI System Monitor - Real-time monitoring of AI operations
 */
@Slf4j
public class AISystemMonitor {

    private final MLManager mlManager;
    private final TrainingManager trainingManager;
    private final AtomicLong lastUpdateTime = new AtomicLong(System.currentTimeMillis());

    // Current status
    private volatile AISystemStatus currentStatus = new AISystemStatus();

    public AISystemMonitor(MLManager mlManager, TrainingManager trainingManager) {
        this.mlManager = mlManager;
        this.trainingManager = trainingManager;
    }

    /**
     * Collect current AI system status
     */
    public AISystemStatus collectStatus() {
        AISystemStatus status = new AISystemStatus();
        status.setTimestamp(System.currentTimeMillis());

        // ML Manager status
        if (mlManager != null) {
            MLManager.MLStatistics stats = mlManager.getStatistics();
            status.setLearningEnabled(mlManager.isLearningEnabled());
            status.setTotalExperiencesProcessed(stats.totalExperiencesProcessed);
            status.setTotalTrainingBatches(stats.totalTrainingBatches);
            status.setAverageReward(stats.averageReward);
            status.setExperienceBufferSize(stats.experienceBufferSize);
            status.setQTableSize(stats.qStats.qTable.size());
            status.setQTableUpdateCount(stats.qStats.updateCount);
        }

        // Training Manager status
        if (trainingManager != null) {
            status.setTrainingActive(trainingManager.isTrainingActive());
            status.setCurrentTrainingStep(trainingManager.getCurrentTrainingStep());
            status.setMaxTrainingSteps(trainingManager.getMaxTrainingSteps());
            status.setTrainingProgress(calculateTrainingProgress());
            status.setBatchSize(trainingManager.getBatchSize());
            status.setNumEnvironments(trainingManager.getNumEnvironments());
            status.setLearningRate(trainingManager.getLearningRate());
            status.setUseMultiAgent(trainingManager.isUseMultiAgent());
        }

        // Performance metrics
        status.setExperiencesPerSecond(calculateExperiencesPerSecond());
        status.setTrainingBatchesPerSecond(calculateTrainingBatchesPerSecond());
        status.setAverageTrainingTime(calculateAverageTrainingTime());
        status.setMemoryUsageMB(getMemoryUsageMB());

        // Health status
        status.setSystemHealthy(isSystemHealthy());
        status.setLastUpdateTime(lastUpdateTime.get());
        status.setUptimeSeconds((System.currentTimeMillis() - getStartTime()) / 1000);

        currentStatus = status;
        lastUpdateTime.set(System.currentTimeMillis());
        return status;
    }

    /**
     * Get experience buffer statistics
     */
    public ExperienceBufferStats getExperienceBufferStats() {
        if (mlManager == null) {
            return new ExperienceBufferStats();
        }

        return ExperienceBufferStats.builder()
                .currentSize(mlManager.getStatistics().experienceBufferSize)
                .maxCapacity(mlManager.getExperienceBufferCapacity())
                .utilizationPercent(calculateBufferUtilization())
                .samplesPerSecond(calculateSamplesPerSecond())
                .averagePriority(calculateAveragePriority())
                .oldestExperienceAge(calculateOldestExperienceAge())
                .newestExperienceAge(calculateNewestExperienceAge())
                .build();
    }

    /**
     * Check if AI system is healthy
     */
    private boolean isSystemHealthy() {
        if (mlManager == null || trainingManager == null) {
            return false;
        }

        // Check for obvious issues
        MLManager.MLStatistics stats = mlManager.getStatistics();
        if (stats.experienceBufferSize > mlManager.getExperienceBufferCapacity() * 0.95) {
            return false; // Buffer nearly full
        }

        if (trainingManager.isTrainingActive() && trainingManager.getCurrentTrainingStep() == 0) {
            return false; // Training stuck
        }

        return true;
    }

    private double calculateTrainingProgress() {
        if (trainingManager == null || trainingManager.getMaxTrainingSteps() == 0) return 0.0;
        return (double) trainingManager.getCurrentTrainingStep() / trainingManager.getMaxTrainingSteps();
    }

    private double calculateExperiencesPerSecond() {
        // Implementation would track experiences over time windows
        return 0.0; // Placeholder
    }

    private double calculateTrainingBatchesPerSecond() {
        // Implementation would track batches over time windows
        return 0.0; // Placeholder
    }

    private double calculateAverageTrainingTime() {
        // Implementation would track training times
        return 0.0; // Placeholder
    }

    private long getMemoryUsageMB() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
    }

    private long getStartTime() {
        return System.currentTimeMillis() - (System.nanoTime() / 1_000_000); // Approximation
    }

    private double calculateBufferUtilization() {
        if (mlManager == null) return 0.0;
        return (double) mlManager.getStatistics().experienceBufferSize / mlManager.getExperienceBufferCapacity();
    }

    private double calculateSamplesPerSecond() {
        // Implementation would track sampling rate
        return 0.0; // Placeholder
    }

    private double calculateAveragePriority() {
        // Implementation would calculate from priority queue
        return 0.0; // Placeholder
    }

    private long calculateOldestExperienceAge() {
        // Implementation would track experience timestamps
        return 0L; // Placeholder
    }

    private long calculateNewestExperienceAge() {
        // Implementation would track experience timestamps
        return 0L; // Placeholder
    }

    public AISystemStatus getCurrentStatus() {
        return currentStatus;
    }

    @Data
    public static class AISystemStatus {
        private long timestamp;
        private boolean learningEnabled;
        private long totalExperiencesProcessed;
        private long totalTrainingBatches;
        private double averageReward;
        private long experienceBufferSize;
        private int qTableSize;
        private long qTableUpdateCount;
        private boolean trainingActive;
        private int currentTrainingStep;
        private int maxTrainingSteps;
        private double trainingProgress;
        private int batchSize;
        private int numEnvironments;
        private double learningRate;
        private boolean useMultiAgent;
        private double experiencesPerSecond;
        private double trainingBatchesPerSecond;
        private double averageTrainingTime;
        private long memoryUsageMB;
        private boolean systemHealthy;
        private long lastUpdateTime;
        private long uptimeSeconds;
    }

    @Data
    @Builder
    public static class ExperienceBufferStats {
        private long currentSize;
        private long maxCapacity;
        private double utilizationPercent;
        private double samplesPerSecond;
        private double averagePriority;
        private long oldestExperienceAge;
        private long newestExperienceAge;
    }
}
