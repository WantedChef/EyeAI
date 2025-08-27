package chef.sheesh.eyeAI.data.model;

import java.util.Map;
import java.util.UUID;

/**
 * Record of a player transaction (economy, purchases, etc.)
 */
public class TransactionRecord {

    private UUID transactionId;
    private String transactionType; // "purchase", "sale", "reward", "penalty", etc.
    private double amount;
    private String currency;
    private String description;
    private long timestamp;
    private Map<String, Object> metadata;

    // Default constructor
    public TransactionRecord() {
        this.transactionId = UUID.randomUUID();
        this.timestamp = System.currentTimeMillis();
    }

    // Constructor with all fields
    public TransactionRecord(UUID transactionId, String transactionType, double amount, 
                            String currency, String description, long timestamp, 
                            Map<String, Object> metadata) {
        this.transactionId = transactionId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.timestamp = timestamp;
        this.metadata = metadata;
    }

    // Builder pattern implementation
    public static class TransactionRecordBuilder {
        private UUID transactionId;
        private String transactionType;
        private double amount;
        private String currency;
        private String description;
        private long timestamp;
        private Map<String, Object> metadata;

        public TransactionRecordBuilder transactionId(UUID transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public TransactionRecordBuilder transactionType(String transactionType) {
            this.transactionType = transactionType;
            return this;
        }

        public TransactionRecordBuilder amount(double amount) {
            this.amount = amount;
            return this;
        }

        public TransactionRecordBuilder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public TransactionRecordBuilder description(String description) {
            this.description = description;
            return this;
        }

        public TransactionRecordBuilder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public TransactionRecordBuilder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public TransactionRecord build() {
            return new TransactionRecord(transactionId, transactionType, amount, currency, description, timestamp, metadata);
        }
    }

    public static TransactionRecordBuilder builder() {
        return new TransactionRecordBuilder();
    }

    // Getters and Setters
    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    /**
     * Create a purchase transaction
     */
    public static TransactionRecord createPurchase(double amount, String currency, String item) {
        return TransactionRecord.builder()
            .transactionId(UUID.randomUUID())
            .transactionType("purchase")
            .amount(amount)
            .currency(currency)
            .description("Purchased: " + item)
            .timestamp(System.currentTimeMillis())
            .metadata(Map.of("item", item))
            .build();
    }

    /**
     * Create a reward transaction
     */
    public static TransactionRecord createReward(double amount, String currency, String reason) {
        return TransactionRecord.builder()
            .transactionId(UUID.randomUUID())
            .transactionType("reward")
            .amount(amount)
            .currency(currency)
            .description("Reward: " + reason)
            .timestamp(System.currentTimeMillis())
            .metadata(Map.of("reason", reason))
            .build();
    }

    /**
     * Create a penalty transaction
     */
    public static TransactionRecord createPenalty(double amount, String currency, String reason) {
        return TransactionRecord.builder()
            .transactionId(UUID.randomUUID())
            .transactionType("penalty")
            .amount(-Math.abs(amount)) // Always negative for penalties
            .currency(currency)
            .description("Penalty: " + reason)
            .timestamp(System.currentTimeMillis())
            .metadata(Map.of("reason", reason))
            .build();
    }
}
