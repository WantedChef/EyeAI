package chef.sheesh.eyeAI.data.model;

import com.fasterxml.jackson.databind.JsonNode;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * AI Model data structure for storing trained models and metadata
 */
public class AIModel {

    @NotNull private String modelId;
    @NotNull private String modelName;
    private String modelType; // "neural_network", "decision_tree", "genetic_algorithm", "reinforcement_learning"
    private String modelVersion;
    private byte[] modelData;
    private JsonNode modelConfiguration;
    private long createdAt;
    private long lastTrained;
    private double accuracy;
    private double precision;
    private double recall;
    private Map<String, Double> performanceMetrics;
    private boolean isActive;
    private String trainingStatus; // "idle", "training", "completed", "failed"

    // Default constructor
    public AIModel() {
        this.createdAt = System.currentTimeMillis();
        this.lastTrained = System.currentTimeMillis();
        this.accuracy = 0.0;
        this.precision = 0.0;
        this.recall = 0.0;
        this.performanceMetrics = Map.of();
        this.isActive = false;
        this.trainingStatus = "idle";
    }

    // Constructor with all fields
    public AIModel(@NotNull String modelId, @NotNull String modelName, String modelType, 
                  String modelVersion, byte[] modelData, JsonNode modelConfiguration, 
                  long createdAt, long lastTrained, double accuracy, double precision, 
                  double recall, Map<String, Double> performanceMetrics, boolean isActive, 
                  String trainingStatus) {
        this.modelId = modelId;
        this.modelName = modelName;
        this.modelType = modelType;
        this.modelVersion = modelVersion;
        this.modelData = modelData;
        this.modelConfiguration = modelConfiguration;
        this.createdAt = createdAt;
        this.lastTrained = lastTrained;
        this.accuracy = accuracy;
        this.precision = precision;
        this.recall = recall;
        this.performanceMetrics = performanceMetrics;
        this.isActive = isActive;
        this.trainingStatus = trainingStatus;
    }

    // Builder pattern implementation
    public static class AIModelBuilder {
        private String modelId;
        private String modelName;
        private String modelType;
        private String modelVersion;
        private byte[] modelData;
        private JsonNode modelConfiguration;
        private long createdAt;
        private long lastTrained;
        private double accuracy;
        private double precision;
        private double recall;
        private Map<String, Double> performanceMetrics;
        private boolean isActive;
        private String trainingStatus;

        public AIModelBuilder modelId(String modelId) {
            this.modelId = modelId;
            return this;
        }

        public AIModelBuilder modelName(String modelName) {
            this.modelName = modelName;
            return this;
        }

        public AIModelBuilder modelType(String modelType) {
            this.modelType = modelType;
            return this;
        }

        public AIModelBuilder modelVersion(String modelVersion) {
            this.modelVersion = modelVersion;
            return this;
        }

        public AIModelBuilder modelData(byte[] modelData) {
            this.modelData = modelData;
            return this;
        }

        public AIModelBuilder modelConfiguration(JsonNode modelConfiguration) {
            this.modelConfiguration = modelConfiguration;
            return this;
        }

        public AIModelBuilder createdAt(long createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public AIModelBuilder lastTrained(long lastTrained) {
            this.lastTrained = lastTrained;
            return this;
        }

        public AIModelBuilder accuracy(double accuracy) {
            this.accuracy = accuracy;
            return this;
        }

        public AIModelBuilder precision(double precision) {
            this.precision = precision;
            return this;
        }

        public AIModelBuilder recall(double recall) {
            this.recall = recall;
            return this;
        }

        public AIModelBuilder performanceMetrics(Map<String, Double> performanceMetrics) {
            this.performanceMetrics = performanceMetrics;
            return this;
        }

        public AIModelBuilder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public AIModelBuilder trainingStatus(String trainingStatus) {
            this.trainingStatus = trainingStatus;
            return this;
        }

        public AIModel build() {
            return new AIModel(modelId, modelName, modelType, modelVersion, modelData, 
                             modelConfiguration, createdAt, lastTrained, accuracy, precision, 
                             recall, performanceMetrics, isActive, trainingStatus);
        }
    }

    public static AIModelBuilder builder() {
        return new AIModelBuilder();
    }

    // Getters and Setters
    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public byte[] getModelData() {
        return modelData;
    }

    public void setModelData(byte[] modelData) {
        this.modelData = modelData;
    }

    public JsonNode getModelConfiguration() {
        return modelConfiguration;
    }

    public void setModelConfiguration(JsonNode modelConfiguration) {
        this.modelConfiguration = modelConfiguration;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastTrained() {
        return lastTrained;
    }

    public void setLastTrained(long lastTrained) {
        this.lastTrained = lastTrained;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public double getPrecision() {
        return precision;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public double getRecall() {
        return recall;
    }

    public void setRecall(double recall) {
        this.recall = recall;
    }

    public Map<String, Double> getPerformanceMetrics() {
        return performanceMetrics;
    }

    public void setPerformanceMetrics(Map<String, Double> performanceMetrics) {
        this.performanceMetrics = performanceMetrics;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public String getTrainingStatus() {
        return trainingStatus;
    }

    public void setTrainingStatus(String trainingStatus) {
        this.trainingStatus = trainingStatus;
    }

    /**
     * Create a new AI model
     */
    public static AIModel createNew(String modelId, String modelName, String modelType) {
        return AIModel.builder()
            .modelId(modelId)
            .modelName(modelName)
            .modelType(modelType)
            .modelVersion("1.0.0")
            .createdAt(System.currentTimeMillis())
            .accuracy(0.0)
            .precision(0.0)
            .recall(0.0)
            .performanceMetrics(Map.of())
            .isActive(false)
            .trainingStatus("idle")
            .build();
    }

    /**
     * Update model performance metrics
     */
    public void updateMetrics(double accuracy, double precision, double recall, Map<String, Double> additionalMetrics) {
        this.accuracy = accuracy;
        this.precision = precision;
        this.recall = recall;
        this.lastTrained = System.currentTimeMillis();

        if (additionalMetrics != null) {
            this.performanceMetrics = new java.util.HashMap<>(additionalMetrics);
        }
    }

    /**
     * Update training status
     */
    public void updateTrainingStatus(String status) {
        this.trainingStatus = status;
    }

    /**
     * Get F1 score (harmonic mean of precision and recall)
     */
    public double getF1Score() {
        if (precision + recall == 0) {
            return 0.0;
        }
        return 2 * (precision * recall) / (precision + recall);
    }

    /**
     * Check if model is ready for production use
     */
    public boolean isProductionReady() {
        return isActive &&
               accuracy > 0.8 &&
               trainingStatus.equals("completed") &&
               modelData != null &&
               modelData.length > 0;
    }

    /**
     * Get model size in MB
     */
    public double getModelSizeMB() {
        return modelData != null ? (double) modelData.length / (1024 * 1024) : 0.0;
    }
}
