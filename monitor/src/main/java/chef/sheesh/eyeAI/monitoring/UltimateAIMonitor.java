package chef.sheesh.eyeAI.monitoring;

import chef.sheesh.eyeAI.core.ml.MLManager;
import chef.sheesh.eyeAI.core.ml.TrainingManager;
import chef.sheesh.eyeAI.core.ml.MLModelPersistenceManager;
import chef.sheesh.eyeAI.data.model.AIModel;
import chef.sheesh.eyeAI.data.model.TrainingData;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Ultimate AI Monitoring System - The Best Monitor Ever
 * Comprehensive real-time monitoring of all AI operations, database, and system resources
 */
@Slf4j
public class UltimateAIMonitor {

    private final JavaPlugin plugin;
    private final MLManager mlManager;
    private final TrainingManager trainingManager;
    private final MLModelPersistenceManager persistenceManager;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final AtomicBoolean monitoringEnabled = new AtomicBoolean(true);

    // Monitoring Components
    private final AISystemMonitor aiSystemMonitor;
    private final DatabaseMonitor databaseMonitor;
    private final PerformanceMonitor performanceMonitor;
    private final ResourceMonitor resourceMonitor;
    private final WebDashboard webDashboard;
    private final RESTAPI restApi;
    private final AlertingSystem alertingSystem;

    public UltimateAIMonitor(JavaPlugin plugin, MLManager mlManager,
                           TrainingManager trainingManager,
                           MLModelPersistenceManager persistenceManager) {
        this.plugin = plugin;
        this.mlManager = mlManager;
        this.trainingManager = trainingManager;
        this.persistenceManager = persistenceManager;

        // Initialize all monitoring components
        this.aiSystemMonitor = new AISystemMonitor(mlManager, trainingManager);
        this.databaseMonitor = new DatabaseMonitor(persistenceManager, plugin.getDataFolder());
        this.performanceMonitor = new PerformanceMonitor();
        this.resourceMonitor = new ResourceMonitor();
        this.webDashboard = new WebDashboard(this, plugin);
        this.restApi = new RESTAPI(this, plugin);
        this.alertingSystem = new AlertingSystem(plugin);

        log.info("🚀 Ultimate AI Monitor initialized - The Best Monitor Ever!");
        startMonitoring();
    }

    /**
     * Start all monitoring systems
     */
    private void startMonitoring() {
        // AI System monitoring - every 1 second
        scheduler.scheduleAtFixedRate(this::monitorAISystem, 0, 1, TimeUnit.SECONDS);

        // Database monitoring - every 30 seconds
        scheduler.scheduleAtFixedRate(this::monitorDatabase, 0, 30, TimeUnit.SECONDS);

        // Performance monitoring - every 5 seconds
        scheduler.scheduleAtFixedRate(this::monitorPerformance, 0, 5, TimeUnit.SECONDS);

        // Resource monitoring - every 10 seconds
        scheduler.scheduleAtFixedRate(this::monitorResources, 0, 10, TimeUnit.SECONDS);

        // Web dashboard updates - every 2 seconds
        scheduler.scheduleAtFixedRate(this::updateWebDashboard, 0, 2, TimeUnit.SECONDS);

        log.info("📊 All monitoring systems started successfully!");
    }

    /**
     * Monitor AI system status in real-time
     */
    private void monitorAISystem() {
        try {
            AISystemStatus status = aiSystemMonitor.collectStatus();
            alertingSystem.checkAlerts(status);
            webDashboard.updateAISystemStatus(status);
        } catch (Exception e) {
            log.error("Error monitoring AI system", e);
        }
    }

    /**
     * Monitor database operations and model persistence
     */
    private void monitorDatabase() {
        try {
            DatabaseStatus dbStatus = databaseMonitor.collectStatus();
            alertingSystem.checkDatabaseAlerts(dbStatus);
            webDashboard.updateDatabaseStatus(dbStatus);
        } catch (Exception e) {
            log.error("Error monitoring database", e);
        }
    }

    /**
     * Monitor performance metrics
     */
    private void monitorPerformance() {
        try {
            PerformanceMetrics metrics = performanceMonitor.collectMetrics();
            alertingSystem.checkPerformanceAlerts(metrics);
            webDashboard.updatePerformanceMetrics(metrics);
        } catch (Exception e) {
            log.error("Error monitoring performance", e);
        }
    }

    /**
     * Monitor system resources
     */
    private void monitorResources() {
        try {
            SystemResources resources = resourceMonitor.collectResources();
            alertingSystem.checkResourceAlerts(resources);
            webDashboard.updateSystemResources(resources);
        } catch (Exception e) {
            log.error("Error monitoring resources", e);
        }
    }

    /**
     * Update web dashboard with latest data
     */
    private void updateWebDashboard() {
        try {
            webDashboard.refreshAll();
        } catch (Exception e) {
            log.error("Error updating web dashboard", e);
        }
    }

    /**
     * Get comprehensive system overview
     */
    public SystemOverview getSystemOverview() {
        return SystemOverview.builder()
                .aiStatus(aiSystemMonitor.getCurrentStatus())
                .databaseStatus(databaseMonitor.getCurrentStatus())
                .performanceMetrics(performanceMonitor.getCurrentMetrics())
                .systemResources(resourceMonitor.getCurrentResources())
                .alerts(alertingSystem.getActiveAlerts())
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * Get detailed AI statistics
     */
    public AIDetailedStats getAIDetailedStats() {
        return AIDetailedStats.builder()
                .trainingMetrics(trainingManager != null ? trainingManager.evaluate() : null)
                .mlStatistics(mlManager.getStatistics())
                .modelStats(databaseMonitor.getModelStatistics())
                .experienceBufferStats(aiSystemMonitor.getExperienceBufferStats())
                .build();
    }

    /**
     * Get database content for viewing
     */
    public DatabaseContent getDatabaseContent() {
        return DatabaseContent.builder()
                .aiModels(databaseMonitor.getAllAIModels())
                .trainingData(databaseMonitor.getAllTrainingData())
                .persistedModels(persistenceManager.listModelFiles())
                .modelConfigurations(databaseMonitor.getModelConfigurations())
                .build();
    }

    /**
     * Enable/disable monitoring
     */
    public void setMonitoringEnabled(boolean enabled) {
        monitoringEnabled.set(enabled);
        if (enabled) {
            log.info("▶️ AI Monitoring enabled");
        } else {
            log.info("⏸️ AI Monitoring disabled");
        }
    }

    /**
     * Shutdown monitoring system
     */
    public void shutdown() {
        log.info("🛑 Shutting down Ultimate AI Monitor...");
        monitoringEnabled.set(false);
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        webDashboard.shutdown();
        restApi.shutdown();
        log.info("✅ Ultimate AI Monitor shutdown complete");
    }

    // Getters for individual components
    public AISystemMonitor getAiSystemMonitor() { return aiSystemMonitor; }
    public DatabaseMonitor getDatabaseMonitor() { return databaseMonitor; }
    public PerformanceMonitor getPerformanceMonitor() { return performanceMonitor; }
    public ResourceMonitor getResourceMonitor() { return resourceMonitor; }
    public WebDashboard getWebDashboard() { return webDashboard; }
    public RESTAPI getRestApi() { return restApi; }
    public AlertingSystem getAlertingSystem() { return alertingSystem; }
}
