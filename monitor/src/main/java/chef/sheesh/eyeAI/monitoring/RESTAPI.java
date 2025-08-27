package chef.sheesh.eyeAI.monitoring;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.java.JavaPlugin;
import spark.Spark;

import java.util.List;

import static spark.Spark.*;

/**
 * REST API for the Ultimate AI Monitor
 */
@Slf4j
public class RESTAPI {

    private final UltimateAIMonitor monitor;
    private final JavaPlugin plugin;
    private final Gson gson = new Gson();

    public RESTAPI(UltimateAIMonitor monitor, JavaPlugin plugin) {
        this.monitor = monitor;
        this.plugin = plugin;
        initializeAPI();
        log.info("🔌 REST API initialized");
    }

    /**
     * Initialize REST API endpoints
     */
    private void initializeAPI() {
        // CORS support
        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        });

        options("/*", (request, response) -> {
            response.status(200);
            return "";
        });

        // API endpoints
        get("/api/v1/status", (req, res) -> {
            res.type("application/json");
            return gson.toJson(monitor.getSystemOverview());
        });

        get("/api/v1/ai-system/status", (req, res) -> {
            res.type("application/json");
            return gson.toJson(monitor.getAiSystemMonitor().getCurrentStatus());
        });

        get("/api/v1/database/status", (req, res) -> {
            res.type("application/json");
            return gson.toJson(monitor.getDatabaseMonitor().getCurrentStatus());
        });

        get("/api/v1/performance/metrics", (req, res) -> {
            res.type("application/json");
            return gson.toJson(monitor.getPerformanceMonitor().getCurrentMetrics());
        });

        get("/api/v1/resources/system", (req, res) -> {
            res.type("application/json");
            return gson.toJson(monitor.getResourceMonitor().getCurrentResources());
        });

        get("/api/v1/ai-system/detailed", (req, res) -> {
            res.type("application/json");
            return gson.toJson(monitor.getAIDetailedStats());
        });

        get("/api/v1/database/content", (req, res) -> {
            res.type("application/json");
            return gson.toJson(monitor.getDatabaseContent());
        });

        get("/api/v1/alerts/active", (req, res) -> {
            res.type("application/json");
            return gson.toJson(monitor.getAlertingSystem().getActiveAlerts());
        });

        post("/api/v1/alerts/:alertId/acknowledge", (req, res) -> {
            String alertId = req.params(":alertId");
            boolean success = monitor.getAlertingSystem().acknowledgeAlert(alertId);
            res.type("application/json");
            return gson.toJson(new APIResponse(success, success ? "Alert acknowledged" : "Alert not found"));
        });

        get("/api/v1/monitoring/enable", (req, res) -> {
            monitor.setMonitoringEnabled(true);
            res.type("application/json");
            return gson.toJson(new APIResponse(true, "Monitoring enabled"));
        });

        get("/api/v1/monitoring/disable", (req, res) -> {
            monitor.setMonitoringEnabled(false);
            res.type("application/json");
            return gson.toJson(new APIResponse(true, "Monitoring disabled"));
        });

        // Health check endpoint
        get("/api/v1/health", (req, res) -> {
            res.type("application/json");
            SystemOverview overview = monitor.getSystemOverview();
            boolean healthy = overview.getAiStatus().isSystemHealthy() &&
                            overview.getDatabaseStatus().isHealthy();

            return gson.toJson(new HealthResponse(
                healthy ? "healthy" : "unhealthy",
                overview.getTimestamp(),
                healthy ? "All systems operational" : "Issues detected"
            ));
        });

        // Metrics endpoint for monitoring systems
        get("/api/v1/metrics", (req, res) -> {
            res.type("text/plain");
            return generateMetricsOutput();
        });

        log.info("📋 REST API endpoints configured");
    }

    /**
     * Generate Prometheus-style metrics output
     */
    private String generateMetricsOutput() {
        SystemOverview overview = monitor.getSystemOverview();
        StringBuilder metrics = new StringBuilder();

        // AI System metrics
        metrics.append("# AI System Metrics\n");
        metrics.append(String.format("ai_system_training_active %d\n",
            overview.getAiStatus().isTrainingActive() ? 1 : 0));
        metrics.append(String.format("ai_system_learning_enabled %d\n",
            overview.getAiStatus().isLearningEnabled() ? 1 : 0));
        metrics.append(String.format("ai_system_training_progress %.2f\n",
            overview.getAiStatus().getTrainingProgress()));
        metrics.append(String.format("ai_system_experiences_processed %d\n",
            overview.getAiStatus().getTotalExperiencesProcessed()));
        metrics.append(String.format("ai_system_buffer_size %d\n",
            overview.getAiStatus().getExperienceBufferSize()));

        // Performance metrics
        metrics.append("# Performance Metrics\n");
        metrics.append(String.format("performance_memory_usage_mb %d\n",
            overview.getSystemResources().getMemoryUsageMB()));
        metrics.append(String.format("performance_cpu_usage_percent %.2f\n",
            overview.getSystemResources().getCpuUsagePercent()));

        // Database metrics
        metrics.append("# Database Metrics\n");
        metrics.append(String.format("database_total_models %d\n",
            overview.getDatabaseStatus().getTotalModels()));
        metrics.append(String.format("database_storage_used_mb %d\n",
            overview.getDatabaseStatus().getStorageUsedMB()));
        metrics.append(String.format("database_healthy %d\n",
            overview.getDatabaseStatus().isHealthy() ? 1 : 0));

        return metrics.toString();
    }

    /**
     * Shutdown the REST API
     */
    public void shutdown() {
        Spark.stop();
        log.info("🛑 REST API shutdown complete");
    }

    // Data classes for API responses
    public static class APIResponse {
        private final boolean success;
        private final String message;

        public APIResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }

    public static class HealthResponse {
        private final String status;
        private final long timestamp;
        private final String message;

        public HealthResponse(String status, long timestamp, String message) {
            this.status = status;
            this.timestamp = timestamp;
            this.message = message;
        }

        public String getStatus() { return status; }
        public long getTimestamp() { return timestamp; }
        public String getMessage() { return message; }
    }
}
