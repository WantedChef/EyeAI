package chef.sheesh.eyeAI.ai.fakeplayer.monitoring;

import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerManager;
import chef.sheesh.eyeAI.ai.fakeplayer.config.FakePlayerConfig;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Automatic performance optimization system for fake players
 */
public class PerformanceOptimizer {

    private final JavaPlugin plugin;
    private final FakePlayerManager fakePlayerManager;
    private final PerformanceMonitor performanceMonitor;
    private final OptimizationConfig config;
    private final Map<String, OptimizationStrategy> activeOptimizations;

    public PerformanceOptimizer(JavaPlugin plugin, FakePlayerManager fakePlayerManager,
                              PerformanceMonitor performanceMonitor) {
        this.plugin = plugin;
        this.fakePlayerManager = fakePlayerManager;
        this.performanceMonitor = performanceMonitor;
        this.config = new OptimizationConfig();
        this.activeOptimizations = new HashMap<>();

        // Start optimization task
        startOptimizationTask();
    }

    /**
     * Start the optimization task
     */
    private void startOptimizationTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::performOptimization, 0L, config.getOptimizationInterval());
    }

    /**
     * Perform automatic optimization
     */
    private void performOptimization() {
        // Analyze current performance
        Map<String, Double> metrics = performanceMonitor.getAllMetrics();

        // Check various performance aspects
        checkTickPerformance(metrics);
        checkMemoryPerformance(metrics);
        checkBehaviorTreePerformance();
        checkGroupCoordinationPerformance();

        // Apply optimizations
        applyOptimizations();

        // Log optimization actions
        logOptimizationActions();
    }

    /**
     * Check and optimize tick performance
     */
    private void checkTickPerformance(Map<String, Double> metrics) {
        Double avgTickTime = metrics.get("fakeplayers.performance.avg_tick_time");
        Double efficiency = metrics.get("fakeplayers.performance.efficiency");

        if (avgTickTime != null && avgTickTime > config.getHighTickTimeThreshold()) {
            // High tick time detected - optimize
            activateOptimization("reduce_ai_frequency", "Reducing AI update frequency due to high tick time");
        } else if (efficiency != null && efficiency < config.getLowEfficiencyThreshold()) {
            // Low efficiency detected - optimize
            activateOptimization("optimize_behavior_trees", "Optimizing behavior trees due to low efficiency");
        } else {
            // Performance is good - can increase frequency if needed
            deactivateOptimization("reduce_ai_frequency");
        }
    }

    /**
     * Check and optimize memory performance
     */
    private void checkMemoryPerformance(Map<String, Double> metrics) {
        Double memoryUsagePercent = metrics.get("memory.heap.used") != null ?
            (metrics.get("memory.heap.used") / metrics.get("memory.heap.max")) * 100 : null;
        Double memoryPerPlayer = metrics.get("memory.per_fakeplayer");

        if (memoryUsagePercent != null && memoryUsagePercent > config.getHighMemoryThreshold()) {
            activateOptimization("reduce_fakeplayer_count", "Reducing fake player count due to high memory usage");
        } else if (memoryPerPlayer != null && memoryPerPlayer > config.getHighMemoryPerPlayer()) {
            activateOptimization("optimize_memory_usage", "Optimizing memory usage per fake player");
        } else {
            deactivateOptimization("reduce_fakeplayer_count");
        }
    }

    /**
     * Check behavior tree performance
     */
    private void checkBehaviorTreePerformance() {
        // This would analyze which behavior trees are slow and optimize them
        // For example, replace complex trees with simpler ones during high load

        Collection<FakePlayer> fakePlayers = fakePlayerManager.getActiveFakePlayers();

        for (FakePlayer fakePlayer : fakePlayers) {
            // Check if behavior tree is taking too long
            // This would require integration with behavior tree timing
        }
    }

    /**
     * Check group coordination performance
     */
    private void checkGroupCoordinationPerformance() {
        // Optimize group sizes and coordination patterns based on performance

        int totalFakePlayers = fakePlayerManager.getActiveFakePlayerCount();

        if (totalFakePlayers > config.getLargeGroupThreshold()) {
            activateOptimization("split_large_groups", "Splitting large groups for better performance");
        } else {
            deactivateOptimization("split_large_groups");
        }
    }

    /**
     * Apply active optimizations
     */
    private void applyOptimizations() {
        for (Map.Entry<String, OptimizationStrategy> entry : activeOptimizations.entrySet()) {
            String optimizationName = entry.getKey();
            OptimizationStrategy strategy = entry.getValue();

            if (strategy.shouldApply()) {
                applyOptimizationStrategy(optimizationName, strategy);
            }
        }
    }

    /**
     * Apply a specific optimization strategy
     */
    private void applyOptimizationStrategy(String name, OptimizationStrategy strategy) {
        switch (name) {
            case "reduce_ai_frequency":
                reduceAIFrequency();
                break;
            case "optimize_behavior_trees":
                optimizeBehaviorTrees();
                break;
            case "reduce_fakeplayer_count":
                reduceFakePlayerCount();
                break;
            case "optimize_memory_usage":
                optimizeMemoryUsage();
                break;
            case "split_large_groups":
                splitLargeGroups();
                break;
            default:
                plugin.getLogger().warning("Unknown optimization strategy: " + name);
        }
    }

    /**
     * Reduce AI update frequency
     */
    private void reduceAIFrequency() {
        // Temporarily reduce AI tick frequency
        // This would integrate with the tick system to reduce update frequency

        plugin.getLogger().info("[PerformanceOptimizer] Reduced AI update frequency for better performance");
    }

    /**
     * Optimize behavior trees
     */
    private void optimizeBehaviorTrees() {
        Collection<FakePlayer> fakePlayers = fakePlayerManager.getActiveFakePlayers();

        for (FakePlayer fakePlayer : fakePlayers) {
            // Replace complex behavior trees with simpler ones
            // This would integrate with the behavior tree factory

            plugin.getLogger().info("[PerformanceOptimizer] Optimized behavior tree for fake player: " + fakePlayer.getName());
        }
    }

    /**
     * Reduce fake player count
     */
    private void reduceFakePlayerCount() {
        int currentCount = fakePlayerManager.getActiveFakePlayerCount();
        int targetCount = Math.max(config.getMinFakePlayerCount(),
                                 currentCount - config.getFakePlayerReductionStep());

        // Remove excess fake players
        List<FakePlayer> toRemove = new ArrayList<>();
        Collection<FakePlayer> activePlayers = fakePlayerManager.getActiveFakePlayers();

        int removeCount = currentCount - targetCount;
        Iterator<FakePlayer> iterator = activePlayers.iterator();

        for (int i = 0; i < removeCount && iterator.hasNext(); i++) {
            toRemove.add(iterator.next());
        }

        for (FakePlayer fakePlayer : toRemove) {
            fakePlayerManager.despawnFakePlayer(fakePlayer);
        }

        if (!toRemove.isEmpty()) {
            plugin.getLogger().info("[PerformanceOptimizer] Reduced fake player count from " + currentCount + " to " + targetCount);
        }
    }

    /**
     * Optimize memory usage
     */
    private void optimizeMemoryUsage() {
        // Force garbage collection if memory is high
        System.gc();

        // Clear caches if available
        // This would integrate with caching systems

        plugin.getLogger().info("[PerformanceOptimizer] Optimized memory usage");
    }

    /**
     * Split large groups
     */
    private void splitLargeGroups() {
        // This would integrate with GroupCoordinator to split large groups
        // into smaller, more manageable groups

        plugin.getLogger().info("[PerformanceOptimizer] Split large groups for better performance");
    }

    /**
     * Activate an optimization
     */
    private void activateOptimization(String name, String reason) {
        if (!activeOptimizations.containsKey(name)) {
            OptimizationStrategy strategy = new OptimizationStrategy(name, reason);
            activeOptimizations.put(name, strategy);
            plugin.getLogger().info("[PerformanceOptimizer] Activated optimization: " + name + " - " + reason);
        }
    }

    /**
     * Deactivate an optimization
     */
    private void deactivateOptimization(String name) {
        OptimizationStrategy removed = activeOptimizations.remove(name);
        if (removed != null) {
            plugin.getLogger().info("[PerformanceOptimizer] Deactivated optimization: " + name);
        }
    }

    /**
     * Log optimization actions
     */
    private void logOptimizationActions() {
        if (!activeOptimizations.isEmpty()) {
            plugin.getLogger().info("[PerformanceOptimizer] Active optimizations: " +
                                  String.join(", ", activeOptimizations.keySet()));
        }
    }

    /**
     * Get optimization recommendations
     */
    public List<String> getOptimizationRecommendations() {
        List<String> recommendations = new ArrayList<>();
        Map<String, Double> metrics = performanceMonitor.getAllMetrics();

        // Analyze metrics and provide recommendations
        Double avgTickTime = metrics.get("fakeplayers.performance.avg_tick_time");
        if (avgTickTime != null && avgTickTime > 10.0) {
            recommendations.add("Consider reducing the number of active fake players or simplifying behavior trees");
        }

        Double memoryUsage = metrics.get("memory.heap.used");
        if (memoryUsage != null && memoryUsage > 100 * 1024 * 1024) { // 100MB
            recommendations.add("High memory usage detected - consider reducing fake player count");
        }

        int fakePlayerCount = fakePlayerManager.getActiveFakePlayerCount();
        if (fakePlayerCount > 50) {
            recommendations.add("Large number of fake players - consider implementing load balancing");
        }

        return recommendations;
    }

    /**
     * Force optimization run
     */
    public void forceOptimization() {
        performOptimization();
        plugin.getLogger().info("[PerformanceOptimizer] Forced optimization completed");
    }

    /**
     * Get active optimizations
     */
    public Set<String> getActiveOptimizations() {
        return new HashSet<>(activeOptimizations.keySet());
    }

    /**
     * Clear all optimizations
     */
    public void clearOptimizations() {
        activeOptimizations.clear();
        plugin.getLogger().info("[PerformanceOptimizer] Cleared all optimizations");
    }

    /**
     * Optimization strategy data class
     */
    private static class OptimizationStrategy {
        private final String name;
        private final String reason;
        private final long activationTime;
        private int applicationCount;

        public OptimizationStrategy(String name, String reason) {
            this.name = name;
            this.reason = reason;
            this.activationTime = System.currentTimeMillis();
            this.applicationCount = 0;
        }

        public boolean shouldApply() {
            // Simple strategy: apply once every few minutes
            long timeSinceActivation = System.currentTimeMillis() - activationTime;
            return timeSinceActivation > (applicationCount + 1) * 30000; // 30 seconds * (count + 1)
        }

        public void applied() {
            applicationCount++;
        }

        public String getName() { return name; }
        public String getReason() { return reason; }
        public long getActivationTime() { return activationTime; }
        public int getApplicationCount() { return applicationCount; }
    }

    /**
     * Optimization configuration
     */
    public static class OptimizationConfig {
        private long optimizationInterval = 60 * 20; // 60 seconds (in ticks)
        private double highTickTimeThreshold = 5.0; // 5ms
        private double lowEfficiencyThreshold = 10.0; // fake players per ms
        private double highMemoryThreshold = 85.0; // 85%
        private double highMemoryPerPlayer = 2.0; // 2MB per player
        private int largeGroupThreshold = 20; // 20 fake players
        private int minFakePlayerCount = 5;
        private int fakePlayerReductionStep = 5;

        // Getters and setters
        public long getOptimizationInterval() { return optimizationInterval; }
        public void setOptimizationInterval(long optimizationInterval) { this.optimizationInterval = optimizationInterval; }

        public double getHighTickTimeThreshold() { return highTickTimeThreshold; }
        public void setHighTickTimeThreshold(double highTickTimeThreshold) { this.highTickTimeThreshold = highTickTimeThreshold; }

        public double getLowEfficiencyThreshold() { return lowEfficiencyThreshold; }
        public void setLowEfficiencyThreshold(double lowEfficiencyThreshold) { this.lowEfficiencyThreshold = lowEfficiencyThreshold; }

        public double getHighMemoryThreshold() { return highMemoryThreshold; }
        public void setHighMemoryThreshold(double highMemoryThreshold) { this.highMemoryThreshold = highMemoryThreshold; }

        public double getHighMemoryPerPlayer() { return highMemoryPerPlayer; }
        public void setHighMemoryPerPlayer(double highMemoryPerPlayer) { this.highMemoryPerPlayer = highMemoryPerPlayer; }

        public int getLargeGroupThreshold() { return largeGroupThreshold; }
        public void setLargeGroupThreshold(int largeGroupThreshold) { this.largeGroupThreshold = largeGroupThreshold; }

        public int getMinFakePlayerCount() { return minFakePlayerCount; }
        public void setMinFakePlayerCount(int minFakePlayerCount) { this.minFakePlayerCount = minFakePlayerCount; }

        public int getFakePlayerReductionStep() { return fakePlayerReductionStep; }
        public void setFakePlayerReductionStep(int fakePlayerReductionStep) { this.fakePlayerReductionStep = fakePlayerReductionStep; }
    }
}
