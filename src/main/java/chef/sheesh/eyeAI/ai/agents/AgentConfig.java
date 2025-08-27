package chef.sheesh.eyeAI.ai.agents;

import chef.sheesh.eyeAI.ai.behavior.IBehaviorTree;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for agents that defines their behavior and properties.
 */
public class AgentConfig {

    private String name;
    private IAgent.AgentType type;
    private IBehaviorTree behaviorTree;
    private Location spawnLocation;
    private double maxHealth = 100.0;
    private double movementSpeed = 1.0;
    private double attackRange = 3.0;
    private double detectionRange = 16.0;
    private boolean canRespawn = true;
    private long respawnDelay = 30000; // 30 seconds
    private Map<String, Object> customProperties;

    public AgentConfig() {
        this.customProperties = new HashMap<>();
    }

    public AgentConfig(String name, IAgent.AgentType type) {
        this();
        this.name = name;
        this.type = type;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public IAgent.AgentType getType() {
        return type;
    }

    public void setType(IAgent.AgentType type) {
        this.type = type;
    }

    public IBehaviorTree getBehaviorTree() {
        return behaviorTree;
    }

    public void setBehaviorTree(IBehaviorTree behaviorTree) {
        this.behaviorTree = behaviorTree;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(double maxHealth) {
        this.maxHealth = maxHealth;
    }

    public double getMovementSpeed() {
        return movementSpeed;
    }

    public void setMovementSpeed(double movementSpeed) {
        this.movementSpeed = movementSpeed;
    }

    public double getAttackRange() {
        return attackRange;
    }

    public void setAttackRange(double attackRange) {
        this.attackRange = attackRange;
    }

    public double getDetectionRange() {
        return detectionRange;
    }

    public void setDetectionRange(double detectionRange) {
        this.detectionRange = detectionRange;
    }

    public boolean canRespawn() {
        return canRespawn;
    }

    public void setCanRespawn(boolean canRespawn) {
        this.canRespawn = canRespawn;
    }

    public long getRespawnDelay() {
        return respawnDelay;
    }

    public void setRespawnDelay(long respawnDelay) {
        this.respawnDelay = respawnDelay;
    }

    public Object getProperty(String key) {
        return customProperties.get(key);
    }

    public void setProperty(String key, Object value) {
        customProperties.put(key, value);
    }

    public Map<String, Object> getCustomProperties() {
        return new HashMap<>(customProperties);
    }

    /**
     * Create a builder for fluent configuration
     */
    public static Builder builder(String name, IAgent.AgentType type) {
        return new Builder(name, type);
    }

    /**
     * Builder class for creating AgentConfig instances
     */
    public static class Builder {
        private final AgentConfig config;

        public Builder(String name, IAgent.AgentType type) {
            this.config = new AgentConfig(name, type);
        }

        public Builder behaviorTree(IBehaviorTree behaviorTree) {
            config.setBehaviorTree(behaviorTree);
            return this;
        }

        public Builder spawnLocation(Location location) {
            config.setSpawnLocation(location);
            return this;
        }

        public Builder maxHealth(double health) {
            config.setMaxHealth(health);
            return this;
        }

        public Builder movementSpeed(double speed) {
            config.setMovementSpeed(speed);
            return this;
        }

        public Builder attackRange(double range) {
            config.setAttackRange(range);
            return this;
        }

        public Builder detectionRange(double range) {
            config.setDetectionRange(range);
            return this;
        }

        public Builder canRespawn(boolean canRespawn) {
            config.setCanRespawn(canRespawn);
            return this;
        }

        public Builder respawnDelay(long delay) {
            config.setRespawnDelay(delay);
            return this;
        }

        public Builder property(String key, Object value) {
            config.setProperty(key, value);
            return this;
        }

        public AgentConfig build() {
            return config;
        }
    }
}
