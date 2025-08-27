package chef.sheesh.eyeAI.data.model;

import com.fasterxml.jackson.databind.JsonNode;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Training data structure for AI model training
 */
public class TrainingData {

    @NotNull private String dataId;
    @NotNull private String datasetName;
    private String dataType; // "player_behavior", "combat_data", "movement_patterns", "decision_patterns"
    private byte[] compressedData;
    private byte[] rawData; // Uncompressed data for processing
    private JsonNode metadata;
    private long collectedAt;
    private int sampleCount;
    private Map<String, Object> features;
    private Object labels; // Can be various types depending on the model
    private boolean isProcessed;
    private String preprocessingStatus; // "pending", "processing", "completed", "failed"

    // Default constructor
    public TrainingData() {
        this.collectedAt = System.currentTimeMillis();
        this.sampleCount = 1;
        this.features = Map.of();
        this.isProcessed = false;
        this.preprocessingStatus = "pending";
    }

    // Constructor with all fields
    public TrainingData(@NotNull String dataId, @NotNull String datasetName, String dataType, 
                       byte[] compressedData, byte[] rawData, JsonNode metadata, long collectedAt, 
                       int sampleCount, Map<String, Object> features, Object labels, 
                       boolean isProcessed, String preprocessingStatus) {
        this.dataId = dataId;
        this.datasetName = datasetName;
        this.dataType = dataType;
        this.compressedData = compressedData;
        this.rawData = rawData;
        this.metadata = metadata;
        this.collectedAt = collectedAt;
        this.sampleCount = sampleCount;
        this.features = features;
        this.labels = labels;
        this.isProcessed = isProcessed;
        this.preprocessingStatus = preprocessingStatus;
    }

    // Builder pattern implementation
    public static class TrainingDataBuilder {
        private String dataId;
        private String datasetName;
        private String dataType;
        private byte[] compressedData;
        private byte[] rawData;
        private JsonNode metadata;
        private long collectedAt;
        private int sampleCount;
        private Map<String, Object> features;
        private Object labels;
        private boolean isProcessed;
        private String preprocessingStatus;

        public TrainingDataBuilder dataId(String dataId) {
            this.dataId = dataId;
            return this;
        }

        public TrainingDataBuilder datasetName(String datasetName) {
            this.datasetName = datasetName;
            return this;
        }

        public TrainingDataBuilder dataType(String dataType) {
            this.dataType = dataType;
            return this;
        }

        public TrainingDataBuilder compressedData(byte[] compressedData) {
            this.compressedData = compressedData;
            return this;
        }

        public TrainingDataBuilder rawData(byte[] rawData) {
            this.rawData = rawData;
            return this;
        }

        public TrainingDataBuilder metadata(JsonNode metadata) {
            this.metadata = metadata;
            return this;
        }

        public TrainingDataBuilder collectedAt(long collectedAt) {
            this.collectedAt = collectedAt;
            return this;
        }

        public TrainingDataBuilder sampleCount(int sampleCount) {
            this.sampleCount = sampleCount;
            return this;
        }

        public TrainingDataBuilder features(Map<String, Object> features) {
            this.features = features;
            return this;
        }

        public TrainingDataBuilder labels(Object labels) {
            this.labels = labels;
            return this;
        }

        public TrainingDataBuilder isProcessed(boolean isProcessed) {
            this.isProcessed = isProcessed;
            return this;
        }

        public TrainingDataBuilder preprocessingStatus(String preprocessingStatus) {
            this.preprocessingStatus = preprocessingStatus;
            return this;
        }

        public TrainingData build() {
            return new TrainingData(dataId, datasetName, dataType, compressedData, rawData, 
                                 metadata, collectedAt, sampleCount, features, labels, 
                                 isProcessed, preprocessingStatus);
        }
    }

    public static TrainingDataBuilder builder() {
        return new TrainingDataBuilder();
    }

    // Getters and Setters
    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public byte[] getCompressedData() {
        return compressedData;
    }

    public void setCompressedData(byte[] compressedData) {
        this.compressedData = compressedData;
    }

    public byte[] getRawData() {
        return rawData;
    }

    public void setRawData(byte[] rawData) {
        this.rawData = rawData;
    }

    public JsonNode getMetadata() {
        return metadata;
    }

    public void setMetadata(JsonNode metadata) {
        this.metadata = metadata;
    }

    public long getCollectedAt() {
        return collectedAt;
    }

    public void setCollectedAt(long collectedAt) {
        this.collectedAt = collectedAt;
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(int sampleCount) {
        this.sampleCount = sampleCount;
    }

    public Map<String, Object> getFeatures() {
        return features;
    }

    public void setFeatures(Map<String, Object> features) {
        this.features = features;
    }

    public Object getLabels() {
        return labels;
    }

    public void setLabels(Object labels) {
        this.labels = labels;
    }

    public boolean isProcessed() {
        return isProcessed;
    }

    public void setProcessed(boolean processed) {
        isProcessed = processed;
    }

    public String getPreprocessingStatus() {
        return preprocessingStatus;
    }

    public void setPreprocessingStatus(String preprocessingStatus) {
        this.preprocessingStatus = preprocessingStatus;
    }

    /**
     * Create training data from raw data
     */
    public static TrainingData createFromRawData(String dataId, String datasetName, String dataType,
                                                byte[] rawData, Map<String, Object> features, Object labels) {
        return TrainingData.builder()
            .dataId(dataId)
            .datasetName(datasetName)
            .dataType(dataType)
            .rawData(rawData)
            .compressedData(null) // Will be set during storage
            .metadata(null)
            .collectedAt(System.currentTimeMillis())
            .sampleCount(1)
            .features(features != null ? features : Map.of())
            .labels(labels)
            .isProcessed(false)
            .preprocessingStatus("pending")
            .build();
    }

    /**
     * Mark data as processed
     */
    public void markAsProcessed() {
        this.isProcessed = true;
        this.preprocessingStatus = "completed";
    }

    /**
     * Mark data processing as failed
     */
    public void markAsFailed(String reason) {
        this.isProcessed = false;
        this.preprocessingStatus = "failed";
        // Update metadata with failure reason
        if (metadata == null) {
            metadata = com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        }
        ((com.fasterxml.jackson.databind.node.ObjectNode) metadata).put("failure_reason", reason);
    }

    /**
     * Get data size in MB
     */
    public double getDataSizeMB() {
        if (compressedData != null) {
            return (double) compressedData.length / (1024 * 1024);
        } else if (rawData != null) {
            return (double) rawData.length / (1024 * 1024);
        }
        return 0.0;
    }

    /**
     * Check if data is ready for training
     */
    public boolean isReadyForTraining() {
        return isProcessed &&
               preprocessingStatus.equals("completed") &&
               (compressedData != null || rawData != null) &&
               sampleCount > 0;
    }

    /**
     * Add metadata
     */
    public void addMetadata(String key, String value) {
        if (metadata == null) {
            metadata = com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        }
        ((com.fasterxml.jackson.databind.node.ObjectNode) metadata).put(key, value);
    }

    /**
     * Get metadata value
     */
    public String getMetadataValue(String key) {
        return metadata != null && metadata.has(key) ? metadata.get(key).asText() : null;
    }
}
