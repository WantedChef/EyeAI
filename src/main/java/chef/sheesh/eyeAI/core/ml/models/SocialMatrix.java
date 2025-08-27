package chef.sheesh.eyeAI.core.ml.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Social Dynamics Model for tracking relationships between agents/players.
 * Uses matrix updates with decay for forgetting mechanism.
 */
public class SocialMatrix {
    private static final Logger log = LoggerFactory.getLogger(SocialMatrix.class);
    
    // Relationship matrix: agent1 -> agent2 -> relationship value [-1.0 to 1.0]
    private final Map<UUID, Map<UUID, Double>> relationshipMatrix = new ConcurrentHashMap<>();
    
    // Configuration
    private final double decayRate;
    private final double maxRelationship;
    private final double minRelationship;
    private final long decayIntervalMs;
    
    // Scheduler for periodic decay
    private final ScheduledExecutorService decayScheduler = Executors.newSingleThreadScheduledExecutor();
    
    /**
     * Default constructor for tests with reasonable defaults.
     */
    public SocialMatrix() {
        this(0.05, 1.0, -1.0, 1000L);
    }

    public SocialMatrix(double decayRate, double maxRelationship, double minRelationship, long decayIntervalMs) {
        this.decayRate = decayRate;
        this.maxRelationship = maxRelationship;
        this.minRelationship = minRelationship;
        this.decayIntervalMs = decayIntervalMs;
        
        // Start periodic decay
        decayScheduler.scheduleAtFixedRate(this::applyDecay, decayIntervalMs, decayIntervalMs, TimeUnit.MILLISECONDS);
        log.info("SocialMatrix initialized with decay rate: {}, interval: {}ms", decayRate, decayIntervalMs);
    }

    /**
     * Apply a single decay step with the provided factor (0..1), used by tests.
     * factor represents the proportion to decay this step (e.g., 0.1 => 10% decay)
     */
    public void decayRelationships(double factor) {
        for (Map<UUID, Double> agentRelationships : relationshipMatrix.values()) {
            agentRelationships.replaceAll((target, value) -> {
                double newValue = value * (1.0 - factor);
                if (Math.abs(newValue) < 0.01) {
                    return null;
                }
                return newValue;
            });
            agentRelationships.values().removeIf(v -> v == null);
        }
        relationshipMatrix.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
    
    /**
     * Update relationship between two agents
     * @param agent1 First agent UUID
     * @param agent2 Second agent UUID
     * @param delta Change in relationship [-1.0 to 1.0]
     * @param bidirectional Whether to apply change to both directions
     */
    public void updateRelationship(UUID agent1, UUID agent2, double delta, boolean bidirectional) {
        if (agent1.equals(agent2)) {
            return; // No self-relationships
        }
        
        // Update agent1 -> agent2
        relationshipMatrix.computeIfAbsent(agent1, k -> new ConcurrentHashMap<>())
                .merge(agent2, delta, (oldVal, newVal) -> 
                    Math.max(minRelationship, Math.min(maxRelationship, oldVal + newVal)));
        
        if (bidirectional) {
            // Update agent2 -> agent1
            relationshipMatrix.computeIfAbsent(agent2, k -> new ConcurrentHashMap<>())
                    .merge(agent1, delta, (oldVal, newVal) -> 
                        Math.max(minRelationship, Math.min(maxRelationship, oldVal + newVal)));
        }
        
        log.debug("Updated relationship {} -> {}: delta={}, bidirectional={}", 
                agent1, agent2, delta, bidirectional);
    }

    /**
     * Convenience overload: defaults to bidirectional updates (backwards compatibility for tests)
     */
    public void updateRelationship(UUID agent1, UUID agent2, double delta) {
        updateRelationship(agent1, agent2, delta, true);
    }
    
    /**
     * Get relationship value between two agents
     * @param agent1 First agent UUID
     * @param agent2 Second agent UUID
     * @return Relationship value [-1.0 to 1.0], 0.0 if no relationship exists
     */
    public double getRelationship(UUID agent1, UUID agent2) {
        return relationshipMatrix.getOrDefault(agent1, new HashMap<>()).getOrDefault(agent2, 0.0);
    }
    
    /**
     * Get all relationships for a specific agent
     * @param agent Agent UUID
     * @return Map of target agents to relationship values
     */
    public Map<UUID, Double> getAgentRelationships(UUID agent) {
        return new HashMap<>(relationshipMatrix.getOrDefault(agent, new HashMap<>()));
    }
    
    /**
     * Apply decay to all relationships (forgetting mechanism)
     */
    private void applyDecay() {
        int decayedCount = 0;
        
        for (Map<UUID, Double> agentRelationships : relationshipMatrix.values()) {
            agentRelationships.replaceAll((target, value) -> {
                double newValue = value * (1.0 - decayRate);
                // Remove relationships that have decayed to near-zero
                if (Math.abs(newValue) < 0.01) {
                    return null; // Will be removed by replaceAll
                }
                return newValue;
            });
            
            // Remove null entries (decayed relationships)
            agentRelationships.values().removeIf(value -> value == null);
            decayedCount++;
        }
        
        // Remove empty agent entries
        relationshipMatrix.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        
        if (decayedCount > 0) {
            log.debug("Applied decay to {} agent relationships", decayedCount);
        }
    }
    
    /**
     * Record positive interaction (cooperation, help, etc.)
     */
    public void recordPositiveInteraction(UUID agent1, UUID agent2, double intensity) {
        updateRelationship(agent1, agent2, intensity * 0.1, true);
        log.debug("Positive interaction recorded: {} <-> {}, intensity: {}", agent1, agent2, intensity);
    }
    
    /**
     * Record negative interaction (combat, theft, etc.)
     */
    public void recordNegativeInteraction(UUID agent1, UUID agent2, double intensity) {
        updateRelationship(agent1, agent2, -intensity * 0.15, true);
        log.debug("Negative interaction recorded: {} <-> {}, intensity: {}", agent1, agent2, intensity);
    }
    
    /**
     * Get social influence factor for decision making
     * @param agent Agent making decision
     * @param target Target of potential action
     * @return Influence factor [-1.0 to 1.0] affecting action probability
     */
    public double getSocialInfluence(UUID agent, UUID target) {
        double relationship = getRelationship(agent, target);
        
        // Calculate influence based on relationship strength and social network
        double networkInfluence = 0.0;
        Map<UUID, Double> agentRelationships = getAgentRelationships(agent);
        
        for (Map.Entry<UUID, Double> entry : agentRelationships.entrySet()) {
            UUID intermediary = entry.getKey();
            double agentToIntermediary = entry.getValue();
            double intermediaryToTarget = getRelationship(intermediary, target);
            
            // Transitive influence: friend of friend is friend (weighted)
            networkInfluence += agentToIntermediary * intermediaryToTarget * 0.3;
        }
        
        return Math.max(-1.0, Math.min(1.0, relationship + networkInfluence));
    }

    /**
     * Calculate social influence with depth-limited transitive propagation.
     * Depth 0 returns direct relationship; depth 1 includes one-hop neighbors, etc.
     */
    public double calculateSocialInfluence(UUID agent, UUID target, int depth) {
        return Math.max(-1.0, Math.min(1.0, relationshipInfluence(agent, target, depth, new java.util.HashSet<>())));
    }

    private double relationshipInfluence(UUID agent, UUID target, int depth, java.util.Set<UUID> visited) {
        if (agent == null || target == null) {
            return 0.0;
        }
        if (depth < 0) {
            return 0.0;
        }
        visited.add(agent);

        double base = getRelationship(agent, target);
        if (depth == 0) {
            return base;
        }

        double transitive = 0.0;
        Map<UUID, Double> neighbors = relationshipMatrix.getOrDefault(agent, java.util.Collections.emptyMap());
        for (Map.Entry<UUID, Double> entry : neighbors.entrySet()) {
            UUID intermediary = entry.getKey();
            if (visited.contains(intermediary)) {
                continue;
            }
            double agentToIntermediary = entry.getValue();
            double intermediaryToTarget = getRelationship(intermediary, target);
            // Weight diminishes with each hop (0.3 per hop similar to getSocialInfluence)
            double hopWeight = 0.3;
            transitive += agentToIntermediary * intermediaryToTarget * hopWeight;
            transitive += 0.3 * relationshipInfluence(intermediary, target, depth - 1, new java.util.HashSet<>(visited));
        }

        return base + transitive;
    }
    
    /**
     * Get matrix statistics for monitoring
     */
    public SocialMatrixStats getStats() {
        int totalAgents = relationshipMatrix.size();
        int totalRelationships = relationshipMatrix.values().stream()
                .mapToInt(Map::size).sum();
        
        double avgRelationship = relationshipMatrix.values().stream()
                .flatMap(map -> map.values().stream())
                .mapToDouble(Double::doubleValue)
                .average().orElse(0.0);
        
        return new SocialMatrixStats(totalAgents, totalRelationships, avgRelationship);
    }
    
    /**
     * Shutdown the decay scheduler
     */
    public void shutdown() {
        decayScheduler.shutdown();
        log.info("SocialMatrix shutdown completed");
    }
    
    /**
     * Statistics record for social matrix monitoring
     */
    public record SocialMatrixStats(int totalAgents, int totalRelationships, double averageRelationship) {}
}
