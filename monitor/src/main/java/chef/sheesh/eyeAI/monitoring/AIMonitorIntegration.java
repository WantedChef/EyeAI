package chef.sheesh.eyeAI.monitoring;

import chef.sheesh.eyeAI.core.ml.MLManager;
import chef.sheesh.eyeAI.core.ml.TrainingManager;
import chef.sheesh.eyeAI.core.ml.MLModelPersistenceManager;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Integration example showing how to use the Ultimate AI Monitor
 * This demonstrates the complete setup and usage of the monitoring system
 */
@Slf4j
public class AIMonitorIntegration {

    private UltimateAIMonitor ultimateMonitor;

    /**
     * Initialize the monitoring system in your main plugin class
     */
    public void initializeMonitoring(JavaPlugin plugin, MLManager mlManager,
                                   TrainingManager trainingManager,
                                   MLModelPersistenceManager persistenceManager) {

        log.info("🚀 Initializing Ultimate AI Monitor...");

        // Create the ultimate monitoring system
        ultimateMonitor = new UltimateAIMonitor(
            plugin,
            mlManager,
            trainingManager,
            persistenceManager
        );

        log.info("✅ Ultimate AI Monitor initialized successfully!");
        log.info("🌐 Access the monitoring dashboard at: http://localhost:8080");
        log.info("🔌 REST API available at: http://localhost:8080/api/v1/");
        log.info("📊 WebSocket real-time updates available at: ws://localhost:8080/ws");
    }

    /**
     * Example: Get comprehensive system status
     */
    public void logSystemStatus() {
        if (ultimateMonitor == null) {
            log.warn("Monitor not initialized!");
            return;
        }

        SystemOverview overview = ultimateMonitor.getSystemOverview();

        log.info("=== AI SYSTEM STATUS ===");
        log.info("Training Active: {}", overview.getAiStatus().isTrainingActive());
        log.info("Learning Enabled: {}", overview.getAiStatus().isLearningEnabled());
        log.info("Training Progress: {:.1f}%", overview.getAiStatus().getTrainingProgress() * 100);
        log.info("Experiences Processed: {}", overview.getAiStatus().getTotalExperiencesProcessed());
        log.info("Buffer Size: {}", overview.getAiStatus().getExperienceBufferSize());
        log.info("System Healthy: {}", overview.getAiStatus().isSystemHealthy());

        log.info("=== DATABASE STATUS ===");
        log.info("Total Models: {}", overview.getDatabaseStatus().getTotalModels());
        log.info("Storage Used: {} MB", overview.getDatabaseStatus().getStorageUsedMB());
        log.info("Database Healthy: {}", overview.getDatabaseStatus().isHealthy());

        log.info("=== PERFORMANCE METRICS ===");
        log.info("Memory Usage: {} MB", overview.getSystemResources().getMemoryUsageMB());
        log.info("CPU Usage: {:.1f}%", overview.getSystemResources().getCpuUsagePercent());
        log.info("Training Batches/sec: {:.2f}", overview.getPerformanceMetrics().getTrainingBatchesPerSecond());

        log.info("=== ACTIVE ALERTS ===");
        overview.getAlerts().forEach(alert ->
            log.warn("ALERT [{}]: {}", alert.getSeverity(), alert.getMessage())
        );
    }

    /**
     * Example: Get detailed AI statistics
     */
    public void logDetailedAIStats() {
        if (ultimateMonitor == null) {
            log.warn("Monitor not initialized!");
            return;
        }

        AIDetailedStats detailedStats = ultimateMonitor.getAIDetailedStats();

        log.info("=== DETAILED AI STATISTICS ===");
        if (detailedStats.getTrainingMetrics() != null) {
            log.info("Current Training Step: {}", detailedStats.getTrainingMetrics().getCurrentStep());
            log.info("Training Loss: {:.4f}", detailedStats.getTrainingMetrics().getLoss());
            log.info("Training Accuracy: {:.2f}%", detailedStats.getTrainingMetrics().getAccuracy() * 100);
        }

        if (detailedStats.getMlStatistics() != null) {
            log.info("Total Experiences: {}", detailedStats.getMlStatistics().getTotalExperiencesProcessed());
            log.info("Average Reward: {:.4f}", detailedStats.getMlStatistics().getAverageReward());
        }

        if (detailedStats.getExperienceBufferStats() != null) {
            log.info("Buffer Utilization: {:.1f}%",
                detailedStats.getExperienceBufferStats().getUtilizationPercent());
            log.info("Samples per Second: {:.2f}",
                detailedStats.getExperienceBufferStats().getSamplesPerSecond());
        }
    }

    /**
     * Example: Access database content
     */
    public void inspectDatabaseContent() {
        if (ultimateMonitor == null) {
            log.warn("Monitor not initialized!");
            return;
        }

        DatabaseContent dbContent = ultimateMonitor.getDatabaseContent();

        log.info("=== DATABASE CONTENT ===");
        log.info("Total AI Models: {}", dbContent.getAiModels().size());
        log.info("Training Sessions: {}", dbContent.getTrainingData().size());
        log.info("Persisted Model Files: {}", dbContent.getPersistedModels().length);
        log.info("Total Storage: {} MB", dbContent.getTotalSizeBytes() / (1024.0 * 1024.0));

        // Show recent models
        dbContent.getAiModels().stream().limit(3).forEach(model ->
            log.info("Model: {} (v{}) - Accuracy: {:.1f}%",
                model.getModelName(), model.getModelVersion(), model.getAccuracy() * 100)
        );
    }

    /**
     * Example: Control monitoring system
     */
    public void controlMonitoring(boolean enable) {
        if (ultimateMonitor == null) {
            log.warn("Monitor not initialized!");
            return;
        }

        ultimateMonitor.setMonitoringEnabled(enable);
        log.info("Monitoring {}", enable ? "enabled" : "disabled");
    }

    /**
     * Example: Handle plugin shutdown
     */
    public void shutdownMonitoring() {
        if (ultimateMonitor != null) {
            log.info("Shutting down AI monitoring system...");
            ultimateMonitor.shutdown();
            ultimateMonitor = null;
            log.info("✅ AI monitoring system shutdown complete");
        }
    }

    /**
     * Example: Command handler integration
     * This shows how to integrate monitoring commands into your plugin
     */
    public void handleMonitoringCommand(String[] args) {
        if (args.length == 0) {
            logSystemStatus();
            return;
        }

        switch (args[0].toLowerCase()) {
            case "status":
                logSystemStatus();
                break;
            case "detailed":
                logDetailedAIStats();
                break;
            case "database":
                inspectDatabaseContent();
                break;
            case "enable":
                controlMonitoring(true);
                break;
            case "disable":
                controlMonitoring(false);
                break;
            case "alerts":
                if (ultimateMonitor != null) {
                    ultimateMonitor.getAlertingSystem().getActiveAlerts().forEach(alert ->
                        log.info("Alert [{}]: {}", alert.getSeverity(), alert.getMessage())
                    );
                }
                break;
            default:
                log.info("Available commands: status, detailed, database, enable, disable, alerts");
        }
    }

    /**
     * Example: Integration with existing AI training workflow
     */
    public void onTrainingStart() {
        if (ultimateMonitor != null) {
            log.info("🎯 Training started - monitoring system active");
            // The monitoring system will automatically track training progress
        }
    }

    public void onTrainingComplete() {
        if (ultimateMonitor != null) {
            log.info("🏁 Training completed - generating final report");
            logDetailedAIStats();
        }
    }

    public void onModelSaved() {
        if (ultimateMonitor != null) {
            log.info("💾 Model saved - database monitor will update automatically");
            // The database monitor will automatically detect the new model
        }
    }

    /**
     * Get the ultimate monitor instance for advanced usage
     */
    public UltimateAIMonitor getUltimateMonitor() {
        return ultimateMonitor;
    }
}
