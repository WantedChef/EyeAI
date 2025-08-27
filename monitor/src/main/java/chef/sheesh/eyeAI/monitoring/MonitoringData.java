package chef.sheesh.eyeAI.monitoring;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Data classes for the monitoring system
 */
public class MonitoringData {

    @Data
    @Builder
    public static class SystemOverview {
        private AISystemStatus aiStatus;
        private DatabaseStatus databaseStatus;
        private PerformanceMetrics performanceMetrics;
        private SystemResources systemResources;
        private List<Alert> alerts;
        private long timestamp;
    }

    @Data
    @Builder
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
    public static class DatabaseStatus {
        private long timestamp;
        private int totalModels;
        private long storageUsedMB;
        private long lastBackupTime;
        private boolean healthy;
        private int totalModelFiles;
        private long databaseSizeBytes;
    }

    @Data
    @Builder
    public static class PerformanceMetrics {
        private long timestamp;
        private double trainingBatchesPerSecond;
        private double averageTrainingTime;
        private double experiencesPerSecond;
        private long memoryUsageMB;
        private double cpuUsagePercent;
        private double diskReadBytesPerSecond;
        private double diskWriteBytesPerSecond;
    }

    @Data
    @Builder
    public static class SystemResources {
        private long timestamp;
        private long memoryUsageMB;
        private long maxMemoryMB;
        private double cpuUsagePercent;
        private double systemLoadAverage;
        private long diskUsedBytes;
        private long diskTotalBytes;
        private int threadCount;
        private long uptimeSeconds;
    }

    @Data
    @Builder
    public static class Alert {
        private String id;
        private String severity; // "INFO", "WARN", "ERROR", "CRITICAL"
        private String message;
        private String category; // "AI_SYSTEM", "DATABASE", "PERFORMANCE", "RESOURCE"
        private long timestamp;
        private boolean acknowledged;
        private String details;
    }

    @Data
    @Builder
    public static class AIDetailedStats {
        private TrainingMetrics trainingMetrics;
        private MLStatistics mlStatistics;
        private ModelStats modelStats;
        private ExperienceBufferStats experienceBufferStats;
    }

    @Data
    @Builder
    public static class TrainingMetrics {
        private int currentStep;
        private int maxSteps;
        private double progress;
        private double loss;
        private double accuracy;
        private long trainingTimeMs;
    }

    @Data
    @Builder
    public static class MLStatistics {
        private long totalExperiencesProcessed;
        private long totalTrainingBatches;
        private double averageReward;
        private long experienceBufferSize;
        private QStats qStats;
    }

    @Data
    @Builder
    public static class QStats {
        private long tableSize;
        private long updateCount;
        private double averageReward;
    }

    @Data
    @Builder
    public static class ModelStats {
        private int totalModels;
        private int activeModels;
        private long totalStorageBytes;
        private double averageAccuracy;
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

    @Data
    @Builder
    public static class DatabaseContent {
        private List<AIModelSummary> aiModels;
        private List<TrainingDataSummary> trainingData;
        private java.io.File[] persistedModels;
        private List<ModelConfiguration> modelConfigurations;
        private long totalSizeBytes;
    }

    @Data
    @Builder
    public static class AIModelSummary {
        private String modelId;
        private String modelName;
        private String modelType;
        private String modelVersion;
        private double accuracy;
        private long createdAt;
        private boolean isActive;
    }

    @Data
    @Builder
    public static class TrainingDataSummary {
        private String sessionId;
        private long duration;
        private double totalReward;
        private long experienceCount;
        private long timestamp;
    }

    @Data
    @Builder
    public static class ModelConfiguration {
        private String configId;
        private String configType;
        private String content;
        private long lastModified;
    }
}
