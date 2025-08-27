package chef.sheesh.eyeAI.core.ml.testing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * A/B testing framework for ML algorithms.
 * Allows testing different configurations of ML algorithms and comparing their performance.
 */
public class ABTester {
    private static final Logger log = LoggerFactory.getLogger(ABTester.class);
    
    private final Map<String, TestGroup> testGroups = new ConcurrentHashMap<>();
    private final String defaultGroup;
    
    public ABTester(String defaultGroupName) {
        this.defaultGroup = defaultGroupName;
        // Initialize default group
        testGroups.put(defaultGroupName, new TestGroup(defaultGroupName, 1.0));
    }
    
    /**
     * Add a test group with a specific configuration.
     * @param name The name of the test group
     * @param weight The relative weight of this group (for random assignment)
     */
    public void addTestGroup(String name, double weight) {
        testGroups.put(name, new TestGroup(name, weight));
        log.info("Added A/B test group: {} with weight: {}", name, weight);
    }
    
    /**
     * Randomly assign an agent to a test group based on weights.
     * @param agentId The ID of the agent
     * @return The name of the assigned test group
     */
    public String assignAgentToGroup(String agentId) {
        // Calculate total weight
        double totalWeight = testGroups.values().stream()
                .mapToDouble(TestGroup::getWeight)
                .sum();
        
        // Randomly select a group based on weights
        double randomValue = ThreadLocalRandom.current().nextDouble() * totalWeight;
        double cumulativeWeight = 0.0;
        
        for (TestGroup group : testGroups.values()) {
            cumulativeWeight += group.getWeight();
            if (randomValue <= cumulativeWeight) {
                group.addAgent(agentId);
                log.info("Assigned agent {} to test group {}", agentId, group.getName());
                return group.getName();
            }
        }
        
        // Fallback to default group
        TestGroup defaultGroupObj = testGroups.get(defaultGroup);
        defaultGroupObj.addAgent(agentId);
        log.info("Assigned agent {} to default test group {}", agentId, defaultGroup);
        return defaultGroup;
    }
    
    /**
     * Record a metric for a specific agent in their assigned group.
     * @param agentId The ID of the agent
     * @param metricName The name of the metric
     * @param value The value of the metric
     */
    public void recordMetric(String agentId, String metricName, double value) {
        // Find the group this agent belongs to
        for (TestGroup group : testGroups.values()) {
            if (group.containsAgent(agentId)) {
                group.recordMetric(metricName, value);
                return;
            }
        }
        
        // If not found, record in default group
        testGroups.get(defaultGroup).recordMetric(metricName, value);
    }
    
    /**
     * Get the average value of a metric across all agents in a group.
     * @param groupName The name of the test group
     * @param metricName The name of the metric
     * @return The average value of the metric, or 0.0 if not found
     */
    public double getAverageMetric(String groupName, String metricName) {
        TestGroup group = testGroups.get(groupName);
        if (group == null) {
            return 0.0;
        }
        return group.getAverageMetric(metricName);
    }
    
    /**
     * Log a comparison report of all test groups for a specific metric.
     * @param metricName The name of the metric to compare
     */
    public void logComparisonReport(String metricName) {
        log.info("=== A/B Test Comparison Report for {} ===", metricName);
        
        // Collect all groups with this metric
        List<TestGroup> groupsWithMetric = new ArrayList<>();
        for (TestGroup group : testGroups.values()) {
            if (group.hasMetric(metricName)) {
                groupsWithMetric.add(group);
            }
        }
        
        if (groupsWithMetric.isEmpty()) {
            log.info("No data available for metric {}", metricName);
            return;
        }
        
        // Sort groups by metric value (descending)
        groupsWithMetric.sort((g1, g2) -> Double.compare(g2.getAverageMetric(metricName), g1.getAverageMetric(metricName)));
        
        // Log comparison
        for (TestGroup group : groupsWithMetric) {
            double avg = group.getAverageMetric(metricName);
            log.info("Group {}: Average {} = {}", group.getName(), metricName, String.format("%.4f", avg));
        }
        
        log.info("========================");
    }
    
    /**
     * Inner class representing a test group.
     */
    private static class TestGroup {
        private final String name;
        private final double weight;
        private final List<String> agents = new ArrayList<>();
        private final Map<String, Metric> metrics = new ConcurrentHashMap<>();
        
        public TestGroup(String name, double weight) {
            this.name = name;
            this.weight = weight;
        }
        
        public String getName() {
            return name;
        }
        
        public double getWeight() {
            return weight;
        }
        
        public void addAgent(String agentId) {
            agents.add(agentId);
        }
        
        public boolean containsAgent(String agentId) {
            return agents.contains(agentId);
        }
        
        public void recordMetric(String metricName, double value) {
            metrics.computeIfAbsent(metricName, k -> new Metric()).addValue(value);
        }
        
        public double getAverageMetric(String metricName) {
            Metric metric = metrics.get(metricName);
            if (metric == null) {
                return 0.0;
            }
            return metric.getAverage();
        }
        
        public boolean hasMetric(String metricName) {
            return metrics.containsKey(metricName) && metrics.get(metricName).getCount() > 0;
        }
    }
    
    /**
     * Inner class representing a metric.
     */
    private static class Metric {
        private final DoubleAdder totalValue = new DoubleAdder();
        private final LongAdder count = new LongAdder();
        
        public void addValue(double value) {
            totalValue.add(value);
            count.increment();
        }
        
        public double getAverage() {
            long currentCount = count.longValue();
            if (currentCount == 0) {
                return 0.0;
            }
            return totalValue.doubleValue() / currentCount;
        }
        
        public long getCount() {
            return count.longValue();
        }
    }
}
