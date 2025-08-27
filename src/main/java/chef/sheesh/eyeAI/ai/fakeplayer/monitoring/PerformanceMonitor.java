package chef.sheesh.eyeAI.ai.fakeplayer.monitoring;

import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Comprehensive performance monitoring system for fake players
 */
public class PerformanceMonitor {

    private final JavaPlugin plugin;
    private final FakePlayerManager fakePlayerManager;
    private final Map<String, Metric> metrics;
    private final AtomicLong totalTicks;
    private final AtomicLong totalProcessingTime;
    private final ThreadLocal<Stopwatch> stopwatch;
    private final PerformanceConfig config;

    public PerformanceMonitor(JavaPlugin plugin, FakePlayerManager fakePlayerManager) {
        this.plugin = plugin;
        this.fakePlayerManager = fakePlayerManager;
        this.metrics = new ConcurrentHashMap<>();
        this.totalTicks = new AtomicLong(0);
        this.totalProcessingTime = new AtomicLong(0);
        this.stopwatch = ThreadLocal.withInitial(Stopwatch::new);
        this.config = new PerformanceConfig();

        // Start monitoring task
        startMonitoring();
    }

    /**
     * Start the monitoring system
     */
    private void startMonitoring() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateMetrics();
                checkPerformanceThresholds();
                generateOptimizationRecommendations();
            }
        }.runTaskTimer(plugin, 0L, config.getUpdateInterval());
    }

    /**
     * Update performance metrics
     */
    private void updateMetrics() {
        long currentTime = System.currentTimeMillis();

        // Update system metrics
        updateSystemMetrics();

        // Update fake player metrics
        updateFakePlayerMetrics();

        // Update behavior tree metrics
        updateBehaviorTreeMetrics();

        // Update memory metrics
        updateMemoryMetrics();

        // Calculate derived metrics
        calculateDerivedMetrics();
    }

    /**
     * Update system-level metrics
     */
    private void updateSystemMetrics() {
        // CPU usage (approximate)
        double cpuUsage = getCpuUsage();
        recordMetric("system.cpu.usage", cpuUsage);

        // TPS (ticks per second)
        double tps = getServerTPS();
        recordMetric("system.server.tps", tps);

        // Online players
        int onlinePlayers = plugin.getServer().getOnlinePlayers().size();
        recordMetric("system.players.online", onlinePlayers);
    }

    /**
     * Update fake player specific metrics
     */
    private void updateFakePlayerMetrics() {
        Collection<FakePlayer> fakePlayers = fakePlayerManager.getActiveFakePlayers();
        int activeCount = fakePlayers.size();

        recordMetric("fakeplayers.active.count", activeCount);
        recordMetric("fakeplayers.total.count", fakePlayerManager.getActiveFakePlayerCount());

        // Health distribution
        int healthy = 0, damaged = 0, critical = 0;
        for (FakePlayer fp : fakePlayers) {
            double healthRatio = fp.getHealth() / 20.0; // Assuming max health of 20
            if (healthRatio > 0.6) {
                healthy++;
            } else if (healthRatio > 0.3) {
                damaged++;
            } else {
                critical++;
            }
        }

        recordMetric("fakeplayers.health.healthy", healthy);
        recordMetric("fakeplayers.health.damaged", damaged);
        recordMetric("fakeplayers.health.critical", critical);

        // State distribution
        Map<String, Integer> stateCounts = new HashMap<>();
        for (FakePlayer fp : fakePlayers) {
            String state = fp.getState().name().toLowerCase();
            stateCounts.put(state, stateCounts.getOrDefault(state, 0) + 1);
        }

        for (Map.Entry<String, Integer> entry : stateCounts.entrySet()) {
            recordMetric("fakeplayers.state." + entry.getKey(), entry.getValue());
        }

        // Performance per fake player
        double avgTickTime = getAverageTickTime();
        recordMetric("fakeplayers.performance.avg_tick_time", avgTickTime);

        // Movement metrics
        double avgDistancePerTick = calculateAverageDistancePerTick();
        recordMetric("fakeplayers.movement.avg_distance_per_tick", avgDistancePerTick);
    }

    /**
     * Update behavior tree performance metrics
     */
    private void updateBehaviorTreeMetrics() {
        // Behavior tree execution times
        // This would integrate with behavior tree system to track execution times

        // Success/failure rates
        // This would track behavior tree success rates

        // Most used behavior trees
        // This would track which behavior trees are most frequently used
    }

    /**
     * Update memory usage metrics
     */
    private void updateMemoryMetrics() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();

        recordMetric("memory.heap.used", heapUsage.getUsed() / 1024.0 / 1024.0); // MB
        recordMetric("memory.heap.committed", heapUsage.getCommitted() / 1024.0 / 1024.0);
        recordMetric("memory.heap.max", heapUsage.getMax() / 1024.0 / 1024.0);

        recordMetric("memory.nonheap.used", nonHeapUsage.getUsed() / 1024.0 / 1024.0);
        recordMetric("memory.nonheap.committed", nonHeapUsage.getCommitted() / 1024.0 / 1024.0);

        // Garbage collection metrics
        long gcTime = getGarbageCollectionTime();
        recordMetric("memory.gc.total_time", gcTime);
    }

    /**
     * Calculate derived metrics
     */
    private void calculateDerivedMetrics() {
        // Performance efficiency (fake players per tick time)
        int activeCount = fakePlayerManager.getActiveFakePlayerCount();
        double avgTickTime = getAverageTickTime();

        if (activeCount > 0 && avgTickTime > 0) {
            double efficiency = activeCount / avgTickTime;
            recordMetric("fakeplayers.performance.efficiency", efficiency);
        }

        // Memory per fake player
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();

        if (activeCount > 0) {
            double memoryPerPlayer = usedMemory / (double) activeCount / 1024.0 / 1024.0; // MB per player
            recordMetric("memory.per_fakeplayer", memoryPerPlayer);
        }

        // Health efficiency (how well fake players stay healthy)
        Collection<FakePlayer> fakePlayers = fakePlayerManager.getActiveFakePlayers();
        if (!fakePlayers.isEmpty()) {
            double avgHealth = fakePlayers.stream()
                .mapToDouble(FakePlayer::getHealth)
                .average()
                .orElse(0.0);

            recordMetric("fakeplayers.health.average", avgHealth);
            recordMetric("fakeplayers.health.efficiency", avgHealth / 20.0); // Assuming max health of 20
        }
    }

    /**
     * Check performance thresholds and alert if needed
     */
    private void checkPerformanceThresholds() {
        // Check tick time threshold
        double avgTickTime = getAverageTickTime();
        if (avgTickTime > config.getMaxTickTimeThreshold()) {
            alertPerformanceIssue("High average tick time: " + String.format("%.2fms", avgTickTime));
        }

        // Check memory usage threshold
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        double memoryUsagePercent = (double) memoryBean.getHeapMemoryUsage().getUsed() /
                                   memoryBean.getHeapMemoryUsage().getMax() * 100;

        if (memoryUsagePercent > config.getMaxMemoryUsageThreshold()) {
            alertPerformanceIssue("High memory usage: " + String.format("%.1f%%", memoryUsagePercent));
        }

        // Check fake player count threshold
        int activeCount = fakePlayerManager.getActiveFakePlayerCount();
        if (activeCount > config.getMaxFakePlayerThreshold()) {
            alertPerformanceIssue("High fake player count: " + activeCount);
        }

        // Check TPS threshold
        double tps = getServerTPS();
        if (tps < config.getMinTpsThreshold()) {
            alertPerformanceIssue("Low server TPS: " + String.format("%.1f", tps));
        }
    }

    /**
     * Generate optimization recommendations
     */
    private void generateOptimizationRecommendations() {
        List<String> recommendations = new ArrayList<>();

        // Analyze tick time
        double avgTickTime = getAverageTickTime();
        if (avgTickTime > 10.0) {
            recommendations.add("Consider reducing AI update frequency or optimizing behavior trees");
        }

        // Analyze memory usage
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        double memoryUsagePercent = (double) memoryBean.getHeapMemoryUsage().getUsed() /
                                   memoryBean.getHeapMemoryUsage().getMax() * 100;

        if (memoryUsagePercent > 80) {
            recommendations.add("High memory usage detected - consider reducing fake player count or clearing caches");
        }

        // Analyze fake player efficiency
        int activeCount = fakePlayerManager.getActiveFakePlayerCount();
        if (activeCount > 50) {
            recommendations.add("Large number of fake players - consider implementing grouping or load balancing");
        }

        // Analyze behavior tree performance
        // This would analyze which behavior trees are slow

        if (!recommendations.isEmpty()) {
            plugin.getLogger().info("[PerformanceMonitor] Optimization Recommendations:");
            for (String recommendation : recommendations) {
                plugin.getLogger().info("  - " + recommendation);
            }
        }
    }

    /**
     * Record a metric value
     */
    public void recordMetric(String name, double value) {
        metrics.computeIfAbsent(name, k -> new Metric()).record(value);
    }

    /**
     * Get metric value
     */
    public Optional<Double> getMetric(String name) {
        Metric metric = metrics.get(name);
        return metric != null ? Optional.of(metric.getAverage()) : Optional.empty();
    }

    /**
     * Get all metrics
     */
    public Map<String, Double> getAllMetrics() {
        Map<String, Double> result = new HashMap<>();
        for (Map.Entry<String, Metric> entry : metrics.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getAverage());
        }
        return result;
    }

    /**
     * Start timing an operation
     */
    public void startTiming(String operation) {
        stopwatch.get().start(operation);
    }

    /**
     * Stop timing an operation
     */
    public void stopTiming(String operation) {
        long duration = stopwatch.get().stop(operation);
        if (duration > 0) {
            recordMetric("operation." + operation + ".duration", duration);
        }
    }

    /**
     * Alert about performance issues
     */
    private void alertPerformanceIssue(String message) {
        plugin.getLogger().warning("[PerformanceMonitor] PERFORMANCE ISSUE: " + message);

        // Could also send to administrators, write to log file, etc.
    }

    /**
     * Get CPU usage (approximate)
     */
    private double getCpuUsage() {
        // This is a simplified CPU usage calculation
        // In a real implementation, you might use JMX or other profiling tools
        return 0.0; // Placeholder
    }

    /**
     * Get server TPS
     */
    private double getServerTPS() {
        // This would integrate with server TPS monitoring
        // For now, return an estimate
        return 20.0; // Placeholder - assume 20 TPS
    }

    /**
     * Get average tick time
     */
    private double getAverageTickTime() {
        long ticks = totalTicks.get();
        long totalTime = totalProcessingTime.get();
        return ticks > 0 ? (double) totalTime / ticks : 0.0;
    }

    /**
     * Calculate average distance traveled per tick
     */
    private double calculateAverageDistancePerTick() {
        // This would calculate based on fake player movement data
        return 0.1; // Placeholder
    }

    /**
     * Get garbage collection time
     */
    private long getGarbageCollectionTime() {
        // This would query garbage collection MXBeans
        return 0; // Placeholder
    }

    /**
     * Get performance report
     */
    public String getPerformanceReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== FakePlayer Performance Report ===\n");
        report.append("Active Fake Players: ").append(fakePlayerManager.getActiveFakePlayerCount()).append("\n");
        report.append("Average Tick Time: ").append(String.format("%.2fms", getAverageTickTime())).append("\n");

        // Memory usage
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
        long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
        double memoryPercent = (double) usedMemory / maxMemory * 100;
        report.append("Memory Usage: ").append(String.format("%.1fMB (%.1f%%)", usedMemory / 1024.0 / 1024.0, memoryPercent)).append("\n");

        // Top metrics
        report.append("\nTop Metrics:\n");
        metrics.entrySet().stream()
            .sorted((e1, e2) -> Double.compare(e2.getValue().getAverage(), e1.getValue().getAverage()))
            .limit(10)
            .forEach(entry -> {
                report.append("  ").append(entry.getKey())
                      .append(": ").append(String.format("%.2f", entry.getValue().getAverage())).append("\n");
            });

        return report.toString();
    }

    /**
     * Reset all metrics
     */
    public void resetMetrics() {
        metrics.clear();
        totalTicks.set(0);
        totalProcessingTime.set(0);
    }

    /**
     * Metric data class
     */
    private static class Metric {
        private double sum = 0.0;
        private long count = 0;
        private double min = Double.MAX_VALUE;
        private double max = Double.MIN_VALUE;

        public void record(double value) {
            sum += value;
            count++;
            min = Math.min(min, value);
            max = Math.max(max, value);
        }

        public double getAverage() {
            return count > 0 ? sum / count : 0.0;
        }

        public double getMin() { return min; }
        public double getMax() { return max; }
        public long getCount() { return count; }
        public double getSum() { return sum; }
    }

    /**
     * Stopwatch for timing operations
     */
    private static class Stopwatch {
        private final Map<String, Long> startTimes = new HashMap<>();

        public void start(String operation) {
            startTimes.put(operation, System.nanoTime());
        }

        public long stop(String operation) {
            Long startTime = startTimes.remove(operation);
            if (startTime != null) {
                return (System.nanoTime() - startTime) / 1_000_000; // Convert to milliseconds
            }
            return -1;
        }
    }

    /**
     * Performance configuration
     */
    public static class PerformanceConfig {
        private long updateInterval = 60 * 20; // 60 seconds (in ticks)
        private double maxTickTimeThreshold = 5.0; // 5ms
        private double maxMemoryUsageThreshold = 85.0; // 85%
        private int maxFakePlayerThreshold = 100;
        private double minTpsThreshold = 18.0; // 18 TPS

        // Getters and setters
        public long getUpdateInterval() { return updateInterval; }
        public void setUpdateInterval(long updateInterval) { this.updateInterval = updateInterval; }

        public double getMaxTickTimeThreshold() { return maxTickTimeThreshold; }
        public void setMaxTickTimeThreshold(double maxTickTimeThreshold) { this.maxTickTimeThreshold = maxTickTimeThreshold; }

        public double getMaxMemoryUsageThreshold() { return maxMemoryUsageThreshold; }
        public void setMaxMemoryUsageThreshold(double maxMemoryUsageThreshold) { this.maxMemoryUsageThreshold = maxMemoryUsageThreshold; }

        public int getMaxFakePlayerThreshold() { return maxFakePlayerThreshold; }
        public void setMaxFakePlayerThreshold(int maxFakePlayerThreshold) { this.maxFakePlayerThreshold = maxFakePlayerThreshold; }

        public double getMinTpsThreshold() { return minTpsThreshold; }
        public void setMinTpsThreshold(double minTpsThreshold) { this.minTpsThreshold = minTpsThreshold; }
    }
}
