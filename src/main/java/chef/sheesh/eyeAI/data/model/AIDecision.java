package chef.sheesh.eyeAI.data.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

/**
 * AI Decision record for logging and analytics
 */
public class AIDecision {

    @NotNull private String decisionId;
    private UUID playerId;
    private String decisionType; // "combat_action", "movement_decision", "resource_allocation", "player_interaction"
    private JsonNode inputData;
    private JsonNode outputData;
    private double confidence;
    private long timestamp;
    private String modelUsed;
    private Map<String, Double> factors;
    private boolean wasSuccessful;
    private String feedback;

    // Default constructor
    public AIDecision() {
        this.decisionId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.wasSuccessful = false;
    }

    // Constructor with all fields
    public AIDecision(@NotNull String decisionId, UUID playerId, String decisionType, 
                     JsonNode inputData, JsonNode outputData, double confidence, 
                     long timestamp, String modelUsed, Map<String, Double> factors, 
                     boolean wasSuccessful, String feedback) {
        this.decisionId = decisionId;
        this.playerId = playerId;
        this.decisionType = decisionType;
        this.inputData = inputData;
        this.outputData = outputData;
        this.confidence = confidence;
        this.timestamp = timestamp;
        this.modelUsed = modelUsed;
        this.factors = factors;
        this.wasSuccessful = wasSuccessful;
        this.feedback = feedback;
    }

    // Builder pattern implementation
    public static class AIDecisionBuilder {
        private String decisionId;
        private UUID playerId;
        private String decisionType;
        private JsonNode inputData;
        private JsonNode outputData;
        private double confidence;
        private long timestamp;
        private String modelUsed;
        private Map<String, Double> factors;
        private boolean wasSuccessful;
        private String feedback;

        public AIDecisionBuilder decisionId(String decisionId) {
            this.decisionId = decisionId;
            return this;
        }

        public AIDecisionBuilder playerId(UUID playerId) {
            this.playerId = playerId;
            return this;
        }

        public AIDecisionBuilder decisionType(String decisionType) {
            this.decisionType = decisionType;
            return this;
        }

        public AIDecisionBuilder inputData(JsonNode inputData) {
            this.inputData = inputData;
            return this;
        }

        public AIDecisionBuilder outputData(JsonNode outputData) {
            this.outputData = outputData;
            return this;
        }

        public AIDecisionBuilder confidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        public AIDecisionBuilder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public AIDecisionBuilder modelUsed(String modelUsed) {
            this.modelUsed = modelUsed;
            return this;
        }

        public AIDecisionBuilder factors(Map<String, Double> factors) {
            this.factors = factors;
            return this;
        }

        public AIDecisionBuilder wasSuccessful(boolean wasSuccessful) {
            this.wasSuccessful = wasSuccessful;
            return this;
        }

        public AIDecisionBuilder feedback(String feedback) {
            this.feedback = feedback;
            return this;
        }

        public AIDecision build() {
            return new AIDecision(decisionId, playerId, decisionType, inputData, outputData, 
                                confidence, timestamp, modelUsed, factors, wasSuccessful, feedback);
        }
    }

    public static AIDecisionBuilder builder() {
        return new AIDecisionBuilder();
    }

    // Getters and Setters
    public String getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(String decisionId) {
        this.decisionId = decisionId;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public String getDecisionType() {
        return decisionType;
    }

    public void setDecisionType(String decisionType) {
        this.decisionType = decisionType;
    }

    public JsonNode getInputData() {
        return inputData;
    }

    public void setInputData(JsonNode inputData) {
        this.inputData = inputData;
    }

    public JsonNode getOutputData() {
        return outputData;
    }

    public void setOutputData(JsonNode outputData) {
        this.outputData = outputData;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getModelUsed() {
        return modelUsed;
    }

    public void setModelUsed(String modelUsed) {
        this.modelUsed = modelUsed;
    }

    public Map<String, Double> getFactors() {
        return factors;
    }

    public void setFactors(Map<String, Double> factors) {
        this.factors = factors;
    }

    public boolean isWasSuccessful() {
        return wasSuccessful;
    }

    public void setWasSuccessful(boolean wasSuccessful) {
        this.wasSuccessful = wasSuccessful;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    /**
     * Create a new AI decision
     */
    public static AIDecision createDecision(UUID playerId, String decisionType, String modelUsed,
                                           JsonNode inputData, JsonNode outputData, double confidence) {
        return AIDecision.builder()
            .decisionId(UUID.randomUUID().toString())
            .playerId(playerId)
            .decisionType(decisionType)
            .inputData(inputData)
            .outputData(outputData)
            .confidence(confidence)
            .timestamp(System.currentTimeMillis())
            .modelUsed(modelUsed)
            .factors(Map.of())
            .wasSuccessful(false) // Will be updated later
            .feedback(null)
            .build();
    }

    /**
     * Mark decision as successful
     */
    public void markSuccessful() {
        this.wasSuccessful = true;
    }

    /**
     * Mark decision as failed
     */
    public void markFailed() {
        this.wasSuccessful = false;
    }

    /**
     * Add feedback to the decision
     */
    public void addFeedback(String feedback) {
        this.feedback = feedback;
    }

    /**
     * Add factor that influenced the decision
     */
    public void addFactor(String factorName, double weight) {
        if (factors == null) {
            factors = new java.util.HashMap<>();
        }
        factors.put(factorName, weight);
    }

    /**
     * Get primary factor (highest weight)
     */
    public String getPrimaryFactor() {
        if (factors == null || factors.isEmpty()) {
            return null;
        }

        return factors.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    /**
     * Check if decision was made with high confidence
     */
    public boolean isHighConfidence() {
        return confidence >= 0.8;
    }

    /**
     * Check if decision was made with low confidence
     */
    public boolean isLowConfidence() {
        return confidence < 0.5;
    }

    /**
     * Get decision age in milliseconds
     */
    public long getDecisionAge() {
        return System.currentTimeMillis() - timestamp;
    }

    /**
     * Get decision age in seconds
     */
    public double getDecisionAgeSeconds() {
        return getDecisionAge() / 1000.0;
    }
}
