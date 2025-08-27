package chef.sheesh.eyeAI.core.ml.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

/**
 * Metrics collector for ML training and inference.
 * Collects various performance metrics and logs them periodically.
 */
public class MLMetricsCollector {
    private static final Logger log = LoggerFactory.getLogger(MLMetricsCollector.class);
    
    // Action selection metrics
    private final AtomicLong totalActions = new AtomicLong(0);
    private final AtomicLong explorationActions = new AtomicLong(0);
    private final AtomicLong exploitationActions = new AtomicLong(0);
    
    // Reward metrics
    private final DoubleAdder totalRewards = new DoubleAdder();
    private final LongAdder rewardCount = new LongAdder();
    
    // Training metrics
    private final AtomicLong trainingSteps = new AtomicLong(0);
    private final DoubleAdder totalLoss = new DoubleAdder();
    private final LongAdder lossCount = new LongAdder();
    
    // Social metrics
    private final DoubleAdder totalRelationshipValue = new DoubleAdder();
    private final LongAdder relationshipCount = new LongAdder();
    
    // Cache for per-agent metrics
    private final ConcurrentHashMap<String, AgentMetrics> agentMetrics = new ConcurrentHashMap<>();
    
    public void recordActionSelection(boolean isExploration) {
        totalActions.incrementAndGet();
        if (isExploration) {
            explorationActions.incrementAndGet();
        } else {
            exploitationActions.incrementAndGet();
        }
    }
    
    public void recordReward(double reward) {
        totalRewards.add(reward);
        rewardCount.increment();
    }
    
    public void recordTrainingStep(double loss) {
        trainingSteps.incrementAndGet();
        totalLoss.add(loss);
        lossCount.increment();
    }
    
    public void recordRelationship(String agent1, String agent2, double relationshipValue) {
        // Update totals for relationship values
        totalRelationshipValue.add(relationshipValue);
        relationshipCount.increment();
        
        // Record per-agent metrics
        agentMetrics.computeIfAbsent(agent1, k -> new AgentMetrics()).recordRelationship(relationshipValue);
        agentMetrics.computeIfAbsent(agent2, k -> new AgentMetrics()).recordRelationship(relationshipValue);
    }
    
    public void logMetrics() {
        log.info("=== ML Metrics Report ===");
        log.info("Total Actions: {}", totalActions.get());
        log.info("Exploration Actions: {}", explorationActions.get());
        log.info("Exploitation Actions: {}", exploitationActions.get());
        if (totalActions.get() > 0) {
            double explorationRate = (double) explorationActions.get() / totalActions.get();
            log.info("Exploration Rate: {}%", String.format("%.2f", explorationRate * 100));
        }
        
        long rewardsN = rewardCount.sum();
        if (rewardsN > 0) {
            double avgReward = totalRewards.sum() / rewardsN;
            log.info("Average Reward: {}", String.format("%.4f", avgReward));
        }
        
        log.info("Training Steps: {}", trainingSteps.get());
        long lossN = lossCount.sum();
        if (lossN > 0) {
            double avgLoss = totalLoss.sum() / lossN;
            log.info("Average Loss: {}", String.format("%.6f", avgLoss));
        }
        
        long relN = relationshipCount.sum();
        if (relN > 0) {
            double avgRel = totalRelationshipValue.sum() / relN;
            log.info("Average Relationship Value: {}", String.format("%.4f", avgRel));
        }
        
        // Log per-agent metrics
        agentMetrics.forEach((agentId, metrics) ->
                log.info("Agent {} - Average Relationship: {}", agentId, String.format("%.4f", metrics.getAverageRelationship()))
        );
        log.info("========================");
    }
    
    /**
     * Inner class to track metrics per agent.
     */
    private static class AgentMetrics {
        private final DoubleAdder totalRelationshipValue = new DoubleAdder();
        private final LongAdder relationshipCount = new LongAdder();
        
        public void recordRelationship(double relationshipValue) {
            totalRelationshipValue.add(relationshipValue);
            relationshipCount.increment();
        }
        
        public double getAverageRelationship() {
            long n = relationshipCount.sum();
            if (n == 0) {
                return 0.0;
            }
            return totalRelationshipValue.sum() / n;
        }
    }
}
