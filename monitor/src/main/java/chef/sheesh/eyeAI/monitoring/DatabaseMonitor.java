package chef.sheesh.eyeAI.monitoring;

import chef.sheesh.eyeAI.core.ml.MLModelPersistenceManager;
import chef.sheesh.eyeAI.data.model.AIModel;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Database Monitor - Monitors database operations and model persistence
 */
@Slf4j
public class DatabaseMonitor {

    private final MLModelPersistenceManager persistenceManager;
    private final File dataFolder;
    private volatile DatabaseStatus currentStatus;

    public DatabaseMonitor(MLModelPersistenceManager persistenceManager, File dataFolder) {
        this.persistenceManager = persistenceManager;
        this.dataFolder = dataFolder;
    }

    /**
     * Collect current database status
     */
    public DatabaseStatus collectStatus() {
        DatabaseStatus status = new DatabaseStatus();
        status.setTimestamp(System.currentTimeMillis());

        try {
            // Count model files
            File[] modelFiles = persistenceManager.listModelFiles();
            status.setTotalModelFiles(modelFiles != null ? modelFiles.length : 0);

            // Calculate storage usage
            long totalSize = calculateDatabaseSize();
            status.setDatabaseSizeBytes(totalSize);
            status.setStorageUsedMB(totalSize / (1024 * 1024));

            // Find last backup time
            long lastBackupTime = findLastBackupTime();
            status.setLastBackupTime(lastBackupTime);

            // Health check
            status.setHealthy(performHealthCheck());

            // Get AI models count (placeholder - would integrate with actual model storage)
            status.setTotalModels(getAIModelsCount());

        } catch (Exception e) {
            log.error("Error collecting database status", e);
            status.setHealthy(false);
        }

        currentStatus = status;
        return status;
    }

    /**
     * Get database statistics
     */
    public ModelStats getModelStatistics() {
        return ModelStats.builder()
                .totalModels(getAIModelsCount())
                .activeModels(getActiveModelsCount())
                .totalStorageBytes(calculateDatabaseSize())
                .averageAccuracy(calculateAverageAccuracy())
                .build();
    }

    /**
     * Get all AI models (placeholder implementation)
     */
    public List<AIModelSummary> getAllAIModels() {
        List<AIModelSummary> models = new ArrayList<>();
        // Placeholder - in real implementation, this would query the actual model storage
        // For now, we'll create dummy data based on persisted model files
        File[] modelFiles = persistenceManager.listModelFiles();
        if (modelFiles != null) {
            for (File file : modelFiles) {
                models.add(AIModelSummary.builder()
                        .modelId(file.getName())
                        .modelName(file.getName().replace(".json", ""))
                        .modelType("PERSISTED_MODEL")
                        .modelVersion("1.0")
                        .accuracy(0.85) // Placeholder
                        .createdAt(file.lastModified())
                        .isActive(true)
                        .build());
            }
        }
        return models;
    }

    /**
     * Get all training data (placeholder implementation)
     */
    public List<TrainingDataSummary> getAllTrainingData() {
        List<TrainingDataSummary> trainingData = new ArrayList<>();
        // Placeholder - would integrate with actual training data storage
        return trainingData;
    }

    /**
     * Get model configurations
     */
    public List<ModelConfiguration> getModelConfigurations() {
        List<ModelConfiguration> configs = new ArrayList<>();
        // Placeholder - would read actual configuration files
        return configs;
    }

    /**
     * Calculate total database size
     */
    private long calculateDatabaseSize() {
        try {
            Path dataPath = dataFolder.toPath();
            if (!Files.exists(dataPath)) {
                return 0;
            }

            return Files.walk(dataPath)
                    .filter(Files::isRegularFile)
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (Exception e) {
                            return 0L;
                        }
                    })
                    .sum();
        } catch (Exception e) {
            log.error("Error calculating database size", e);
            return 0L;
        }
    }

    /**
     * Find last backup time
     */
    private long findLastBackupTime() {
        try {
            File modelsFolder = new File(dataFolder, "ml_models");
            if (!modelsFolder.exists()) {
                return 0;
            }

            File[] backupFiles = modelsFolder.listFiles((dir, name) -> name.startsWith("backup_"));
            if (backupFiles == null || backupFiles.length == 0) {
                return 0;
            }

            long latestBackup = 0;
            for (File backup : backupFiles) {
                if (backup.lastModified() > latestBackup) {
                    latestBackup = backup.lastModified();
                }
            }
            return latestBackup;
        } catch (Exception e) {
            log.error("Error finding last backup time", e);
            return 0;
        }
    }

    /**
     * Perform database health check
     */
    private boolean performHealthCheck() {
        try {
            // Check if data folder exists and is accessible
            if (!dataFolder.exists() || !dataFolder.canRead()) {
                return false;
            }

            // Check if models folder exists
            File modelsFolder = new File(dataFolder, "ml_models");
            if (!modelsFolder.exists()) {
                return false;
            }

            // Check if we can list model files
            File[] modelFiles = persistenceManager.listModelFiles();
            if (modelFiles == null) {
                return false;
            }

            // Check for any corrupted files (basic check)
            for (File file : modelFiles) {
                if (!file.exists() || file.length() == 0) {
                    log.warn("Found potentially corrupted model file: {}", file.getName());
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            log.error("Error performing database health check", e);
            return false;
        }
    }

    /**
     * Get AI models count (placeholder)
     */
    private int getAIModelsCount() {
        File[] modelFiles = persistenceManager.listModelFiles();
        return modelFiles != null ? modelFiles.length : 0;
    }

    /**
     * Get active models count (placeholder)
     */
    private int getActiveModelsCount() {
        // Placeholder - would check which models are currently active
        return getAIModelsCount(); // Assume all are active for now
    }

    /**
     * Calculate average accuracy (placeholder)
     */
    private double calculateAverageAccuracy() {
        // Placeholder - would calculate from actual model data
        return 0.85; // Dummy value
    }

    /**
     * Get current database status
     */
    public DatabaseStatus getCurrentStatus() {
        return currentStatus;
    }
}
