package chef.sheesh.eyeAI.ai.fakeplayer.config;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Configuration class for fake player properties and behaviors
 */
public class FakePlayerConfig {

    // Basic Properties
    private double defaultHealth = 20.0;
    private double maxHealth = 20.0;
    private double movementSpeed = 1.0;
    private double fleeSpeed = 2.0;
    private double combatSpeed = 1.2;

    // AI Behavior Settings
    private double detectionRange = 16.0;
    private double attackRange = 3.0;
    private double fleeRange = 15.0;
    private double safeDistance = 20.0;
    private long attackCooldown = 1000; // milliseconds
    private long fleeTimeout = 15000; // milliseconds
    private long patrolWaitTime = 4000; // milliseconds
    private double healthFleeThreshold = 6.0; // 30% of max health
    private double healthCriticalThreshold = 3.0; // 15% of max health

    // Persistence Settings
    private boolean enablePersistence = true;
    private long saveInterval = 300000; // 5 minutes
    private boolean saveStatistics = true;
    private boolean loadOnStartup = true;

    // Visual Settings
    private boolean enableVisualNpc = true;
    private String defaultSkinTexture = "ewogICJ0aW1lc3RhbXAiIDogMTY0ODIyNDY4MzQ3MywKICAicHJvZmlsZUlkIiA6ICIwZjJlODU4NWJkNGU0Yjk0YTc5ZmJmNzYxNWJjZWVkZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJ5emZyX3N3ZWVwIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzFmNDFkZDM4YWU4NDcwNGY4ZGQ3NzE3MzBiYjYxODZkODM4ZjA5MmNkNzJkNzc4YmNhNmI1ZGQ4YjE1N2U5OTQiCiAgICB9CiAgfQp9";
    private String defaultSkinSignature = "T6gJZA9h9g7V4B6oP9L8xZ7F3R1M5N9P2Q4S8W6E0Y2U7I5O3A1C9V8B4X7Z2";
    private boolean showNametag = true;
    private boolean showHealthBar = false;
    private int visibilityRange = 50;

    // Performance Settings
    private int maxFakePlayers = 100;
    private int tickInterval = 1; // ticks between AI updates
    private int packetUpdateRate = 20; // packets per second max
    private long statisticsUpdateInterval = 60000; // 1 minute
    private boolean enablePerformanceMonitoring = true;

    // Behavior Tree Settings
    private String defaultBehaviorTree = "advanced_combat";
    private boolean enableBehaviorTreeDebug = false;
    private int maxBehaviorTreeDepth = 10;
    private long behaviorTreeTimeout = 5000; // milliseconds

    // Combat Settings
    private double damageMultiplier = 1.0;
    private double defenseMultiplier = 1.0;
    private boolean enableFriendlyFire = false;
    private boolean enablePvp = true;
    private double knockbackResistance = 0.0;

    // Pathfinding Settings
    private boolean enableAdvancedPathfinding = false;
    private double pathfindingStepSize = 2.0;
    private int maxPathfindingSteps = 50;
    private boolean avoidWater = true;
    private boolean avoidLava = true;
    private boolean avoidCliffs = true;

    // Interaction Settings
    private boolean allowPlayerInteraction = true;
    private boolean enableChatResponse = true;
    private String interactionMessage = "§e[NPC] §f%name%§7: Hello! I'm an AI-controlled entity.";
    private boolean enableTrading = false;
    private boolean enableQuests = false;

    // Spawn Settings
    private boolean enableAutoSpawn = false;
    private int autoSpawnCount = 5;
    private int autoSpawnRadius = 50;
    private boolean spawnInGroups = false;
    private int groupSize = 3;

    // Debug Settings
    private boolean enableDebugLogging = false;
    private boolean showMovementPath = false;
    private boolean showDetectionRange = false;
    private boolean enableMetrics = true;

    public FakePlayerConfig() {
        // Default constructor with default values
    }

    public FakePlayerConfig(ConfigurationSection config) {
        loadFromConfig(config);
    }

    /**
     * Load configuration from a ConfigurationSection
     */
    public void loadFromConfig(ConfigurationSection config) {
        if (config == null) {
            return;
        }

        // Basic Properties
        defaultHealth = config.getDouble("defaultHealth", defaultHealth);
        maxHealth = config.getDouble("maxHealth", maxHealth);
        movementSpeed = config.getDouble("movementSpeed", movementSpeed);
        fleeSpeed = config.getDouble("fleeSpeed", fleeSpeed);
        combatSpeed = config.getDouble("combatSpeed", combatSpeed);

        // AI Behavior Settings
        detectionRange = config.getDouble("detectionRange", detectionRange);
        attackRange = config.getDouble("attackRange", attackRange);
        fleeRange = config.getDouble("fleeRange", fleeRange);
        safeDistance = config.getDouble("safeDistance", safeDistance);
        attackCooldown = config.getLong("attackCooldown", attackCooldown);
        fleeTimeout = config.getLong("fleeTimeout", fleeTimeout);
        patrolWaitTime = config.getLong("patrolWaitTime", patrolWaitTime);
        healthFleeThreshold = config.getDouble("healthFleeThreshold", healthFleeThreshold);
        healthCriticalThreshold = config.getDouble("healthCriticalThreshold", healthCriticalThreshold);

        // Persistence Settings
        enablePersistence = config.getBoolean("enablePersistence", enablePersistence);
        saveInterval = config.getLong("saveInterval", saveInterval);
        saveStatistics = config.getBoolean("saveStatistics", saveStatistics);
        loadOnStartup = config.getBoolean("loadOnStartup", loadOnStartup);

        // Visual Settings
        enableVisualNpc = config.getBoolean("enableVisualNpc", enableVisualNpc);
        defaultSkinTexture = config.getString("defaultSkinTexture", defaultSkinTexture);
        defaultSkinSignature = config.getString("defaultSkinSignature", defaultSkinSignature);
        showNametag = config.getBoolean("showNametag", showNametag);
        showHealthBar = config.getBoolean("showHealthBar", showHealthBar);
        visibilityRange = config.getInt("visibilityRange", visibilityRange);

        // Performance Settings
        maxFakePlayers = config.getInt("maxFakePlayers", maxFakePlayers);
        tickInterval = config.getInt("tickInterval", tickInterval);
        packetUpdateRate = config.getInt("packetUpdateRate", packetUpdateRate);
        statisticsUpdateInterval = config.getLong("statisticsUpdateInterval", statisticsUpdateInterval);
        enablePerformanceMonitoring = config.getBoolean("enablePerformanceMonitoring", enablePerformanceMonitoring);

        // Behavior Tree Settings
        defaultBehaviorTree = config.getString("defaultBehaviorTree", defaultBehaviorTree);
        enableBehaviorTreeDebug = config.getBoolean("enableBehaviorTreeDebug", enableBehaviorTreeDebug);
        maxBehaviorTreeDepth = config.getInt("maxBehaviorTreeDepth", maxBehaviorTreeDepth);
        behaviorTreeTimeout = config.getLong("behaviorTreeTimeout", behaviorTreeTimeout);

        // Combat Settings
        damageMultiplier = config.getDouble("damageMultiplier", damageMultiplier);
        defenseMultiplier = config.getDouble("defenseMultiplier", defenseMultiplier);
        enableFriendlyFire = config.getBoolean("enableFriendlyFire", enableFriendlyFire);
        enablePvp = config.getBoolean("enablePvp", enablePvp);
        knockbackResistance = config.getDouble("knockbackResistance", knockbackResistance);

        // Pathfinding Settings
        enableAdvancedPathfinding = config.getBoolean("enableAdvancedPathfinding", enableAdvancedPathfinding);
        pathfindingStepSize = config.getDouble("pathfindingStepSize", pathfindingStepSize);
        maxPathfindingSteps = config.getInt("maxPathfindingSteps", maxPathfindingSteps);
        avoidWater = config.getBoolean("avoidWater", avoidWater);
        avoidLava = config.getBoolean("avoidLava", avoidLava);
        avoidCliffs = config.getBoolean("avoidCliffs", avoidCliffs);

        // Interaction Settings
        allowPlayerInteraction = config.getBoolean("allowPlayerInteraction", allowPlayerInteraction);
        enableChatResponse = config.getBoolean("enableChatResponse", enableChatResponse);
        interactionMessage = config.getString("interactionMessage", interactionMessage);
        enableTrading = config.getBoolean("enableTrading", enableTrading);
        enableQuests = config.getBoolean("enableQuests", enableQuests);

        // Spawn Settings
        enableAutoSpawn = config.getBoolean("enableAutoSpawn", enableAutoSpawn);
        autoSpawnCount = config.getInt("autoSpawnCount", autoSpawnCount);
        autoSpawnRadius = config.getInt("autoSpawnRadius", autoSpawnRadius);
        spawnInGroups = config.getBoolean("spawnInGroups", spawnInGroups);
        groupSize = config.getInt("groupSize", groupSize);

        // Debug Settings
        enableDebugLogging = config.getBoolean("enableDebugLogging", enableDebugLogging);
        showMovementPath = config.getBoolean("showMovementPath", showMovementPath);
        showDetectionRange = config.getBoolean("showDetectionRange", showDetectionRange);
        enableMetrics = config.getBoolean("enableMetrics", enableMetrics);
    }

    /**
     * Save configuration to a ConfigurationSection
     */
    public void saveToConfig(ConfigurationSection config) {
        // Basic Properties
        config.set("defaultHealth", defaultHealth);
        config.set("maxHealth", maxHealth);
        config.set("movementSpeed", movementSpeed);
        config.set("fleeSpeed", fleeSpeed);
        config.set("combatSpeed", combatSpeed);

        // AI Behavior Settings
        config.set("detectionRange", detectionRange);
        config.set("attackRange", attackRange);
        config.set("fleeRange", fleeRange);
        config.set("safeDistance", safeDistance);
        config.set("attackCooldown", attackCooldown);
        config.set("fleeTimeout", fleeTimeout);
        config.set("patrolWaitTime", patrolWaitTime);
        config.set("healthFleeThreshold", healthFleeThreshold);
        config.set("healthCriticalThreshold", healthCriticalThreshold);

        // Persistence Settings
        config.set("enablePersistence", enablePersistence);
        config.set("saveInterval", saveInterval);
        config.set("saveStatistics", saveStatistics);
        config.set("loadOnStartup", loadOnStartup);

        // Visual Settings
        config.set("enableVisualNpc", enableVisualNpc);
        config.set("defaultSkinTexture", defaultSkinTexture);
        config.set("defaultSkinSignature", defaultSkinSignature);
        config.set("showNametag", showNametag);
        config.set("showHealthBar", showHealthBar);
        config.set("visibilityRange", visibilityRange);

        // Performance Settings
        config.set("maxFakePlayers", maxFakePlayers);
        config.set("tickInterval", tickInterval);
        config.set("packetUpdateRate", packetUpdateRate);
        config.set("statisticsUpdateInterval", statisticsUpdateInterval);
        config.set("enablePerformanceMonitoring", enablePerformanceMonitoring);

        // Behavior Tree Settings
        config.set("defaultBehaviorTree", defaultBehaviorTree);
        config.set("enableBehaviorTreeDebug", enableBehaviorTreeDebug);
        config.set("maxBehaviorTreeDepth", maxBehaviorTreeDepth);
        config.set("behaviorTreeTimeout", behaviorTreeTimeout);

        // Combat Settings
        config.set("damageMultiplier", damageMultiplier);
        config.set("defenseMultiplier", defenseMultiplier);
        config.set("enableFriendlyFire", enableFriendlyFire);
        config.set("enablePvp", enablePvp);
        config.set("knockbackResistance", knockbackResistance);

        // Pathfinding Settings
        config.set("enableAdvancedPathfinding", enableAdvancedPathfinding);
        config.set("pathfindingStepSize", pathfindingStepSize);
        config.set("maxPathfindingSteps", maxPathfindingSteps);
        config.set("avoidWater", avoidWater);
        config.set("avoidLava", avoidLava);
        config.set("avoidCliffs", avoidCliffs);

        // Interaction Settings
        config.set("allowPlayerInteraction", allowPlayerInteraction);
        config.set("enableChatResponse", enableChatResponse);
        config.set("interactionMessage", interactionMessage);
        config.set("enableTrading", enableTrading);
        config.set("enableQuests", enableQuests);

        // Spawn Settings
        config.set("enableAutoSpawn", enableAutoSpawn);
        config.set("autoSpawnCount", autoSpawnCount);
        config.set("autoSpawnRadius", autoSpawnRadius);
        config.set("spawnInGroups", spawnInGroups);
        config.set("groupSize", groupSize);

        // Debug Settings
        config.set("enableDebugLogging", enableDebugLogging);
        config.set("showMovementPath", showMovementPath);
        config.set("showDetectionRange", showDetectionRange);
        config.set("enableMetrics", enableMetrics);
    }

    // Getters and setters for all properties
    public double getDefaultHealth() { return defaultHealth; }
    public void setDefaultHealth(double defaultHealth) { this.defaultHealth = defaultHealth; }

    public double getMaxHealth() { return maxHealth; }
    public void setMaxHealth(double maxHealth) { this.maxHealth = maxHealth; }

    public double getMovementSpeed() { return movementSpeed; }
    public void setMovementSpeed(double movementSpeed) { this.movementSpeed = movementSpeed; }

    public double getFleeSpeed() { return fleeSpeed; }
    public void setFleeSpeed(double fleeSpeed) { this.fleeSpeed = fleeSpeed; }

    public double getCombatSpeed() { return combatSpeed; }
    public void setCombatSpeed(double combatSpeed) { this.combatSpeed = combatSpeed; }

    public double getDetectionRange() { return detectionRange; }
    public void setDetectionRange(double detectionRange) { this.detectionRange = detectionRange; }

    public double getAttackRange() { return attackRange; }
    public void setAttackRange(double attackRange) { this.attackRange = attackRange; }

    public double getFleeRange() { return fleeRange; }
    public void setFleeRange(double fleeRange) { this.fleeRange = fleeRange; }

    public double getSafeDistance() { return safeDistance; }
    public void setSafeDistance(double safeDistance) { this.safeDistance = safeDistance; }

    public long getAttackCooldown() { return attackCooldown; }
    public void setAttackCooldown(long attackCooldown) { this.attackCooldown = attackCooldown; }

    public long getFleeTimeout() { return fleeTimeout; }
    public void setFleeTimeout(long fleeTimeout) { this.fleeTimeout = fleeTimeout; }

    public long getPatrolWaitTime() { return patrolWaitTime; }
    public void setPatrolWaitTime(long patrolWaitTime) { this.patrolWaitTime = patrolWaitTime; }

    public double getHealthFleeThreshold() { return healthFleeThreshold; }
    public void setHealthFleeThreshold(double healthFleeThreshold) { this.healthFleeThreshold = healthFleeThreshold; }

    public double getHealthCriticalThreshold() { return healthCriticalThreshold; }
    public void setHealthCriticalThreshold(double healthCriticalThreshold) { this.healthCriticalThreshold = healthCriticalThreshold; }

    public boolean isEnablePersistence() { return enablePersistence; }
    public void setEnablePersistence(boolean enablePersistence) { this.enablePersistence = enablePersistence; }

    public long getSaveInterval() { return saveInterval; }
    public void setSaveInterval(long saveInterval) { this.saveInterval = saveInterval; }

    public boolean isSaveStatistics() { return saveStatistics; }
    public void setSaveStatistics(boolean saveStatistics) { this.saveStatistics = saveStatistics; }

    public boolean isLoadOnStartup() { return loadOnStartup; }
    public void setLoadOnStartup(boolean loadOnStartup) { this.loadOnStartup = loadOnStartup; }

    public boolean isEnableVisualNpc() { return enableVisualNpc; }
    public void setEnableVisualNpc(boolean enableVisualNpc) { this.enableVisualNpc = enableVisualNpc; }

    public String getDefaultSkinTexture() { return defaultSkinTexture; }
    public void setDefaultSkinTexture(String defaultSkinTexture) { this.defaultSkinTexture = defaultSkinTexture; }

    public String getDefaultSkinSignature() { return defaultSkinSignature; }
    public void setDefaultSkinSignature(String defaultSkinSignature) { this.defaultSkinSignature = defaultSkinSignature; }

    public boolean isShowNametag() { return showNametag; }
    public void setShowNametag(boolean showNametag) { this.showNametag = showNametag; }

    public boolean isShowHealthBar() { return showHealthBar; }
    public void setShowHealthBar(boolean showHealthBar) { this.showHealthBar = showHealthBar; }

    public int getVisibilityRange() { return visibilityRange; }
    public void setVisibilityRange(int visibilityRange) { this.visibilityRange = visibilityRange; }

    public int getMaxFakePlayers() { return maxFakePlayers; }
    public void setMaxFakePlayers(int maxFakePlayers) { this.maxFakePlayers = maxFakePlayers; }

    public int getTickInterval() { return tickInterval; }
    public void setTickInterval(int tickInterval) { this.tickInterval = tickInterval; }

    public int getPacketUpdateRate() { return packetUpdateRate; }
    public void setPacketUpdateRate(int packetUpdateRate) { this.packetUpdateRate = packetUpdateRate; }

    public long getStatisticsUpdateInterval() { return statisticsUpdateInterval; }
    public void setStatisticsUpdateInterval(long statisticsUpdateInterval) { this.statisticsUpdateInterval = statisticsUpdateInterval; }

    public boolean isEnablePerformanceMonitoring() { return enablePerformanceMonitoring; }
    public void setEnablePerformanceMonitoring(boolean enablePerformanceMonitoring) { this.enablePerformanceMonitoring = enablePerformanceMonitoring; }

    public String getDefaultBehaviorTree() { return defaultBehaviorTree; }
    public void setDefaultBehaviorTree(String defaultBehaviorTree) { this.defaultBehaviorTree = defaultBehaviorTree; }

    public boolean isEnableBehaviorTreeDebug() { return enableBehaviorTreeDebug; }
    public void setEnableBehaviorTreeDebug(boolean enableBehaviorTreeDebug) { this.enableBehaviorTreeDebug = enableBehaviorTreeDebug; }

    public int getMaxBehaviorTreeDepth() { return maxBehaviorTreeDepth; }
    public void setMaxBehaviorTreeDepth(int maxBehaviorTreeDepth) { this.maxBehaviorTreeDepth = maxBehaviorTreeDepth; }

    public long getBehaviorTreeTimeout() { return behaviorTreeTimeout; }
    public void setBehaviorTreeTimeout(long behaviorTreeTimeout) { this.behaviorTreeTimeout = behaviorTreeTimeout; }

    public double getDamageMultiplier() { return damageMultiplier; }
    public void setDamageMultiplier(double damageMultiplier) { this.damageMultiplier = damageMultiplier; }

    public double getDefenseMultiplier() { return defenseMultiplier; }
    public void setDefenseMultiplier(double defenseMultiplier) { this.defenseMultiplier = defenseMultiplier; }

    public boolean isEnableFriendlyFire() { return enableFriendlyFire; }
    public void setEnableFriendlyFire(boolean enableFriendlyFire) { this.enableFriendlyFire = enableFriendlyFire; }

    public boolean isEnablePvp() { return enablePvp; }
    public void setEnablePvp(boolean enablePvp) { this.enablePvp = enablePvp; }

    public double getKnockbackResistance() { return knockbackResistance; }
    public void setKnockbackResistance(double knockbackResistance) { this.knockbackResistance = knockbackResistance; }

    public boolean isEnableAdvancedPathfinding() { return enableAdvancedPathfinding; }
    public void setEnableAdvancedPathfinding(boolean enableAdvancedPathfinding) { this.enableAdvancedPathfinding = enableAdvancedPathfinding; }

    public double getPathfindingStepSize() { return pathfindingStepSize; }
    public void setPathfindingStepSize(double pathfindingStepSize) { this.pathfindingStepSize = pathfindingStepSize; }

    public int getMaxPathfindingSteps() { return maxPathfindingSteps; }
    public void setMaxPathfindingSteps(int maxPathfindingSteps) { this.maxPathfindingSteps = maxPathfindingSteps; }

    public boolean isAvoidWater() { return avoidWater; }
    public void setAvoidWater(boolean avoidWater) { this.avoidWater = avoidWater; }

    public boolean isAvoidLava() { return avoidLava; }
    public void setAvoidLava(boolean avoidLava) { this.avoidLava = avoidLava; }

    public boolean isAvoidCliffs() { return avoidCliffs; }
    public void setAvoidCliffs(boolean avoidCliffs) { this.avoidCliffs = avoidCliffs; }

    public boolean isAllowPlayerInteraction() { return allowPlayerInteraction; }
    public void setAllowPlayerInteraction(boolean allowPlayerInteraction) { this.allowPlayerInteraction = allowPlayerInteraction; }

    public boolean isEnableChatResponse() { return enableChatResponse; }
    public void setEnableChatResponse(boolean enableChatResponse) { this.enableChatResponse = enableChatResponse; }

    public String getInteractionMessage() { return interactionMessage; }
    public void setInteractionMessage(String interactionMessage) { this.interactionMessage = interactionMessage; }

    public boolean isEnableTrading() { return enableTrading; }
    public void setEnableTrading(boolean enableTrading) { this.enableTrading = enableTrading; }

    public boolean isEnableQuests() { return enableQuests; }
    public void setEnableQuests(boolean enableQuests) { this.enableQuests = enableQuests; }

    public boolean isEnableAutoSpawn() { return enableAutoSpawn; }
    public void setEnableAutoSpawn(boolean enableAutoSpawn) { this.enableAutoSpawn = enableAutoSpawn; }

    public int getAutoSpawnCount() { return autoSpawnCount; }
    public void setAutoSpawnCount(int autoSpawnCount) { this.autoSpawnCount = autoSpawnCount; }

    public int getAutoSpawnRadius() { return autoSpawnRadius; }
    public void setAutoSpawnRadius(int autoSpawnRadius) { this.autoSpawnRadius = autoSpawnRadius; }

    public boolean isSpawnInGroups() { return spawnInGroups; }
    public void setSpawnInGroups(boolean spawnInGroups) { this.spawnInGroups = spawnInGroups; }

    public int getGroupSize() { return groupSize; }
    public void setGroupSize(int groupSize) { this.groupSize = groupSize; }

    public boolean isEnableDebugLogging() { return enableDebugLogging; }
    public void setEnableDebugLogging(boolean enableDebugLogging) { this.enableDebugLogging = enableDebugLogging; }

    public boolean isShowMovementPath() { return showMovementPath; }
    public void setShowMovementPath(boolean showMovementPath) { this.showMovementPath = showMovementPath; }

    public boolean isShowDetectionRange() { return showDetectionRange; }
    public void setShowDetectionRange(boolean showDetectionRange) { this.showDetectionRange = showDetectionRange; }

    public boolean isEnableMetrics() { return enableMetrics; }
    public void setEnableMetrics(boolean enableMetrics) { this.enableMetrics = enableMetrics; }

    /**
     * Create a copy of this configuration
     */
    public FakePlayerConfig copy() {
        FakePlayerConfig copy = new FakePlayerConfig();
        copy.defaultHealth = this.defaultHealth;
        copy.maxHealth = this.maxHealth;
        copy.movementSpeed = this.movementSpeed;
        copy.fleeSpeed = this.fleeSpeed;
        copy.combatSpeed = this.combatSpeed;
        copy.detectionRange = this.detectionRange;
        copy.attackRange = this.attackRange;
        copy.fleeRange = this.fleeRange;
        copy.safeDistance = this.safeDistance;
        copy.attackCooldown = this.attackCooldown;
        copy.fleeTimeout = this.fleeTimeout;
        copy.patrolWaitTime = this.patrolWaitTime;
        copy.healthFleeThreshold = this.healthFleeThreshold;
        copy.healthCriticalThreshold = this.healthCriticalThreshold;
        copy.enablePersistence = this.enablePersistence;
        copy.saveInterval = this.saveInterval;
        copy.saveStatistics = this.saveStatistics;
        copy.loadOnStartup = this.loadOnStartup;
        copy.enableVisualNpc = this.enableVisualNpc;
        copy.defaultSkinTexture = this.defaultSkinTexture;
        copy.defaultSkinSignature = this.defaultSkinSignature;
        copy.showNametag = this.showNametag;
        copy.showHealthBar = this.showHealthBar;
        copy.visibilityRange = this.visibilityRange;
        copy.maxFakePlayers = this.maxFakePlayers;
        copy.tickInterval = this.tickInterval;
        copy.packetUpdateRate = this.packetUpdateRate;
        copy.statisticsUpdateInterval = this.statisticsUpdateInterval;
        copy.enablePerformanceMonitoring = this.enablePerformanceMonitoring;
        copy.defaultBehaviorTree = this.defaultBehaviorTree;
        copy.enableBehaviorTreeDebug = this.enableBehaviorTreeDebug;
        copy.maxBehaviorTreeDepth = this.maxBehaviorTreeDepth;
        copy.behaviorTreeTimeout = this.behaviorTreeTimeout;
        copy.damageMultiplier = this.damageMultiplier;
        copy.defenseMultiplier = this.defenseMultiplier;
        copy.enableFriendlyFire = this.enableFriendlyFire;
        copy.enablePvp = this.enablePvp;
        copy.knockbackResistance = this.knockbackResistance;
        copy.enableAdvancedPathfinding = this.enableAdvancedPathfinding;
        copy.pathfindingStepSize = this.pathfindingStepSize;
        copy.maxPathfindingSteps = this.maxPathfindingSteps;
        copy.avoidWater = this.avoidWater;
        copy.avoidLava = this.avoidLava;
        copy.avoidCliffs = this.avoidCliffs;
        copy.allowPlayerInteraction = this.allowPlayerInteraction;
        copy.enableChatResponse = this.enableChatResponse;
        copy.interactionMessage = this.interactionMessage;
        copy.enableTrading = this.enableTrading;
        copy.enableQuests = this.enableQuests;
        copy.enableAutoSpawn = this.enableAutoSpawn;
        copy.autoSpawnCount = this.autoSpawnCount;
        copy.autoSpawnRadius = this.autoSpawnRadius;
        copy.spawnInGroups = this.spawnInGroups;
        copy.groupSize = this.groupSize;
        copy.enableDebugLogging = this.enableDebugLogging;
        copy.showMovementPath = this.showMovementPath;
        copy.showDetectionRange = this.showDetectionRange;
        copy.enableMetrics = this.enableMetrics;
        return copy;
    }

    @Override
    public String toString() {
        return "FakePlayerConfig{" +
            "defaultHealth=" + defaultHealth +
            ", maxHealth=" + maxHealth +
            ", movementSpeed=" + movementSpeed +
            ", enablePersistence=" + enablePersistence +
            ", enableVisualNpc=" + enableVisualNpc +
            ", maxFakePlayers=" + maxFakePlayers +
            ", defaultBehaviorTree='" + defaultBehaviorTree + '\'' +
            '}';
    }
}
