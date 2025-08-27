package chef.sheesh.eyeAI.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Alerting System for AI Monitoring
 */
@Slf4j
public class AlertingSystem {

    private final Plugin plugin;
    private final List<Alert> activeAlerts = new CopyOnWriteArrayList<>();
    private final Map<String, AlertRule> alertRules = new ConcurrentHashMap<>();

    public AlertingSystem(JavaPlugin plugin) {
        this.plugin = plugin;
        initializeDefaultRules();
        log.info("🚨 Alerting System initialized");
    }

    /**
     * Initialize default alert rules
     */
    private void initializeDefaultRules() {
        // AI System alerts
        alertRules.put("ai_system_unhealthy", new AlertRule(
            "AI System Unhealthy",
            AlertSeverity.CRITICAL,
            "AI system is in an unhealthy state",
            "AI_SYSTEM"
        ));

        alertRules.put("training_stuck", new AlertRule(
            "Training Stuck",
            AlertSeverity.WARNING,
            "Training has not progressed for extended period",
            "AI_SYSTEM"
        ));

        alertRules.put("buffer_full", new AlertRule(
            "Experience Buffer Full",
            AlertSeverity.ERROR,
            "Experience buffer is at capacity",
            "AI_SYSTEM"
        ));

        // Database alerts
        alertRules.put("database_unhealthy", new AlertRule(
            "Database Unhealthy",
            AlertSeverity.CRITICAL,
            "Database is in an unhealthy state",
            "DATABASE"
        ));

        alertRules.put("storage_low", new AlertRule(
            "Storage Space Low",
            AlertSeverity.WARNING,
            "Available storage space is running low",
            "DATABASE"
        ));

        // Performance alerts
        alertRules.put("high_memory_usage", new AlertRule(
            "High Memory Usage",
            AlertSeverity.ERROR,
            "Memory usage is critically high",
            "PERFORMANCE"
        ));

        alertRules.put("high_cpu_usage", new AlertRule(
            "High CPU Usage",
            AlertSeverity.ERROR,
            "CPU usage is critically high",
            "PERFORMANCE"
        ));

        // Resource alerts
        alertRules.put("system_load_high", new AlertRule(
            "High System Load",
            AlertSeverity.WARNING,
            "System load average is very high",
            "RESOURCE"
        ));
    }

    /**
     * Check alerts for AI system status
     */
    public void checkAlerts(AISystemStatus status) {
        // AI system unhealthy
        if (!status.isSystemHealthy()) {
            raiseAlert("ai_system_unhealthy", "AI system is unhealthy", status.getTimestamp());
        } else {
            clearAlert("ai_system_unhealthy");
        }

        // Training stuck
        if (status.isTrainingActive() && status.getCurrentTrainingStep() == 0) {
            raiseAlert("training_stuck", "Training appears to be stuck", status.getTimestamp());
        } else {
            clearAlert("training_stuck");
        }

        // Buffer full
        if (status.getExperienceBufferSize() > 90000) { // 90% capacity
            raiseAlert("buffer_full", "Experience buffer is nearly full", status.getTimestamp());
        } else {
            clearAlert("buffer_full");
        }
    }

    /**
     * Check alerts for database status
     */
    public void checkDatabaseAlerts(DatabaseStatus status) {
        // Database unhealthy
        if (!status.isHealthy()) {
            raiseAlert("database_unhealthy", "Database is unhealthy", status.getTimestamp());
        } else {
            clearAlert("database_unhealthy");
        }

        // Storage low
        if (status.getStorageUsedMB() > 500) { // 500MB threshold
            raiseAlert("storage_low", "Storage usage is high", status.getTimestamp());
        } else {
            clearAlert("storage_low");
        }
    }

    /**
     * Check alerts for performance metrics
     */
    public void checkPerformanceAlerts(PerformanceMetrics metrics) {
        // High memory usage
        if (metrics.getMemoryUsageMB() > 1024) { // 1GB threshold
            raiseAlert("high_memory_usage",
                String.format("Memory usage is %.0f MB", metrics.getMemoryUsageMB()),
                metrics.getTimestamp());
        } else {
            clearAlert("high_memory_usage");
        }

        // High CPU usage
        if (metrics.getCpuUsagePercent() > 90) {
            raiseAlert("high_cpu_usage",
                String.format("CPU usage is %.1f%%", metrics.getCpuUsagePercent()),
                metrics.getTimestamp());
        } else {
            clearAlert("high_cpu_usage");
        }
    }

    /**
     * Check alerts for system resources
     */
    public void checkResourceAlerts(SystemResources resources) {
        // High system load
        if (resources.getSystemLoadAverage() > Runtime.getRuntime().availableProcessors() * 1.5) {
            raiseAlert("system_load_high",
                String.format("System load is %.2f", resources.getSystemLoadAverage()),
                resources.getTimestamp());
        } else {
            clearAlert("system_load_high");
        }
    }

    /**
     * Raise an alert
     */
    private void raiseAlert(String alertId, String message, long timestamp) {
        // Check if alert already exists
        boolean alertExists = activeAlerts.stream()
                .anyMatch(alert -> alert.getId().equals(alertId) && !alert.isAcknowledged());

        if (!alertExists) {
            AlertRule rule = alertRules.get(alertId);
            if (rule != null) {
                Alert alert = new Alert(
                    alertId,
                    rule.getSeverity().toString(),
                    message,
                    rule.getCategory(),
                    timestamp,
                    false,
                    rule.getDescription()
                );

                activeAlerts.add(alert);
                log.warn("🚨 ALERT RAISED: {} - {}", alert.getSeverity(), alert.getMessage());

                // Send to plugin logger if available
                if (plugin != null) {
                    plugin.getLogger().warning(String.format("[ALERT] %s: %s",
                        alert.getSeverity(), alert.getMessage()));
                }
            }
        }
    }

    /**
     * Clear an alert
     */
    private void clearAlert(String alertId) {
        activeAlerts.removeIf(alert -> alert.getId().equals(alertId));
    }

    /**
     * Acknowledge an alert
     */
    public boolean acknowledgeAlert(String alertId) {
        for (Alert alert : activeAlerts) {
            if (alert.getId().equals(alertId)) {
                alert.setAcknowledged(true);
                log.info("✅ Alert acknowledged: {}", alertId);
                return true;
            }
        }
        return false;
    }

    /**
     * Get all active alerts
     */
    public List<Alert> getActiveAlerts() {
        return activeAlerts.stream()
                .filter(alert -> !alert.isAcknowledged())
                .collect(Collectors.toList());
    }

    /**
     * Get all alerts (acknowledged and unacknowledged)
     */
    public List<Alert> getAllAlerts() {
        return List.copyOf(activeAlerts);
    }

    /**
     * Clear all acknowledged alerts
     */
    public void clearAcknowledgedAlerts() {
        activeAlerts.removeIf(Alert::isAcknowledged);
        log.info("🧹 Cleared acknowledged alerts");
    }

    /**
     * Get alert statistics
     */
    public AlertStatistics getAlertStatistics() {
        long totalAlerts = activeAlerts.size();
        long acknowledgedAlerts = activeAlerts.stream().mapToLong(alert -> alert.isAcknowledged() ? 1 : 0).sum();
        long criticalAlerts = activeAlerts.stream().mapToLong(alert -> "CRITICAL".equals(alert.getSeverity()) ? 1 : 0).sum();
        long errorAlerts = activeAlerts.stream().mapToLong(alert -> "ERROR".equals(alert.getSeverity()) ? 1 : 0).sum();
        long warningAlerts = activeAlerts.stream().mapToLong(alert -> "WARNING".equals(alert.getSeverity()) ? 1 : 0).sum();

        return AlertStatistics.builder()
                .totalAlerts(totalAlerts)
                .acknowledgedAlerts(acknowledgedAlerts)
                .criticalAlerts(criticalAlerts)
                .errorAlerts(errorAlerts)
                .warningAlerts(warningAlerts)
                .build();
    }

    // Data classes
    @Data
    @Builder
    public static class AlertRule {
        private String name;
        private AlertSeverity severity;
        private String description;
        private String category;
    }

    public enum AlertSeverity {
        INFO, WARNING, ERROR, CRITICAL
    }

    @Data
    @Builder
    public static class AlertStatistics {
        private long totalAlerts;
        private long acknowledgedAlerts;
        private long criticalAlerts;
        private long errorAlerts;
        private long warningAlerts;
    }
}
