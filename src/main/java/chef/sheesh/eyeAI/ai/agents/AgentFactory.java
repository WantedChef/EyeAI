package chef.sheesh.eyeAI.ai.agents;

import chef.sheesh.eyeAI.ai.behavior.BehaviorTreeFactory;
import chef.sheesh.eyeAI.ai.behavior.IBehaviorTree;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;

/**
 * Factory class for creating agents with predefined configurations.
 */
public class AgentFactory {

    private final BehaviorTreeFactory behaviorTreeFactory;

    public AgentFactory() {
        this.behaviorTreeFactory = new BehaviorTreeFactory();
    }

    /**
     * Create an agent based on configuration
     */
    public IAgent createAgent(AgentConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("AgentConfig cannot be null");
        }

        IAgent.AgentType type = config.getType();
        if (type == null) {
            throw new IllegalArgumentException("AgentType cannot be null");
        }

        // Set default behavior tree if none provided
        if (config.getBehaviorTree() == null) {
            config.setBehaviorTree(createDefaultBehaviorTree(type));
        }

        switch (type) {
            case COMBAT:
                return new CombatAgent(config);
            case PATROL:
                return new PatrolAgent(config);
            case GUARD:
                return new GuardAgent(config);
            case SCOUT:
                return new ScoutAgent(config);
            case TRADER:
                return new TraderAgent(config);
            case CUSTOM:
                return new CustomAgent(config);
            default:
                throw new IllegalArgumentException("Unknown agent type: " + type);
        }
    }

    /**
     * Create a default behavior tree for the given agent type
     */
    private IBehaviorTree createDefaultBehaviorTree(IAgent.AgentType type) {
        switch (type) {
            case COMBAT:
                return behaviorTreeFactory.createAdvancedCombatTree();
            case PATROL:
                return behaviorTreeFactory.createPatrolTree();
            case GUARD:
                return behaviorTreeFactory.createAdvancedCombatTree(); // Guards use combat trees
            case SCOUT:
                return behaviorTreeFactory.createPatrolTree(); // Scouts use patrol trees as base
            case TRADER:
                return behaviorTreeFactory.createPatrolTree(); // Traders use patrol trees
            case CUSTOM:
                return behaviorTreeFactory.createDefaultCombatTree(); // Default fallback
            default:
                return behaviorTreeFactory.createDefaultCombatTree();
        }
    }

    /**
     * Create a quick combat agent with default settings
     */
    public IAgent createQuickCombatAgent(String name) {
        AgentConfig config = AgentConfig.builder(name, IAgent.AgentType.COMBAT)
            .maxHealth(100.0)
            .movementSpeed(1.0)
            .attackRange(3.0)
            .detectionRange(16.0)
            .canRespawn(true)
            .respawnDelay(30000)
            .build();

        return new CombatAgent(config);
    }

    /**
     * Create a quick patrol agent with default settings
     */
    public IAgent createQuickPatrolAgent(String name) {
        AgentConfig config = AgentConfig.builder(name, IAgent.AgentType.PATROL)
            .maxHealth(80.0)
            .movementSpeed(0.8)
            .detectionRange(12.0)
            .canRespawn(true)
            .respawnDelay(20000)
            .build();

        return new PatrolAgent(config);
    }

    /**
     * Create a guard agent with defensive behavior
     */
    public IAgent createGuardAgent(String name) {
        AgentConfig config = AgentConfig.builder(name, IAgent.AgentType.GUARD)
            .maxHealth(120.0)
            .movementSpeed(0.9)
            .attackRange(4.0)
            .detectionRange(20.0)
            .canRespawn(true)
            .respawnDelay(45000)
            .build();

        return new GuardAgent(config);
    }

    /**
     * Create a scout agent with high mobility
     */
    public IAgent createScoutAgent(String name) {
        AgentConfig config = AgentConfig.builder(name, IAgent.AgentType.SCOUT)
            .maxHealth(60.0)
            .movementSpeed(1.3)
            .attackRange(2.5)
            .detectionRange(25.0)
            .canRespawn(true)
            .respawnDelay(15000)
            .build();

        return new ScoutAgent(config);
    }

    /**
     * Create a trader agent with peaceful behavior
     */
    public IAgent createTraderAgent(String name) {
        AgentConfig config = AgentConfig.builder(name, IAgent.AgentType.TRADER)
            .maxHealth(70.0)
            .movementSpeed(0.7)
            .detectionRange(15.0)
            .canRespawn(true)
            .respawnDelay(60000)
            .build();

        return new TraderAgent(config);
    }

    /**
     * Create a custom agent with user-defined behavior
     */
    public IAgent createCustomAgent(String name, IBehaviorTree customBehavior) {
        AgentConfig config = AgentConfig.builder(name, IAgent.AgentType.CUSTOM)
            .behaviorTree(customBehavior)
            .maxHealth(100.0)
            .movementSpeed(1.0)
            .detectionRange(16.0)
            .canRespawn(true)
            .respawnDelay(30000)
            .build();

        return new CustomAgent(config);
    }

    /**
     * Create a boss agent with enhanced capabilities
     */
    public IAgent createBossAgent(String name) {
        AgentConfig config = AgentConfig.builder(name, IAgent.AgentType.COMBAT)
            .maxHealth(500.0)
            .movementSpeed(1.2)
            .attackRange(5.0)
            .detectionRange(30.0)
            .canRespawn(false) // Bosses don't respawn
            .build();

        // Create a more advanced behavior tree for the boss
        IBehaviorTree bossBehavior = behaviorTreeFactory.createDynamicPriorityTree();
        config.setBehaviorTree(bossBehavior);

        return new CombatAgent(config);
    }

    // Placeholder classes for agent types not yet implemented
    private static class GuardAgent extends CombatAgent {
        public GuardAgent(AgentConfig config) {
            super(config);
        }
    }

    private static class ScoutAgent extends PatrolAgent {
        public ScoutAgent(AgentConfig config) {
            super(config);
        }
    }

    private static class TraderAgent extends PatrolAgent {
        public TraderAgent(AgentConfig config) {
            super(config);
        }
    }

    private static class CustomAgent extends AbstractAgent {
        public CustomAgent(AgentConfig config) {
            super(config);
        }

        @Override
        protected IFakePlayer createFakePlayer(org.bukkit.Location location) {
            // Implementation would depend on your FakePlayer system
            return null;
        }

        @Override
        protected void onSpawn(org.bukkit.Location location) {
            // Custom spawn logic
        }

        @Override
        protected void onDespawn() {
            // Custom despawn logic
        }

        @Override
        protected void onTick(long deltaTime) {
            // Custom tick logic
        }

        @Override
        protected void onTickError(Exception e) {
            // Custom error handling
        }

        @Override
        protected void onReset() {
            // Custom reset logic
        }

        @Override
        protected void onDamage(double amount) {
            // Custom damage logic
        }

        @Override
        protected void onHeal(double amount) {
            // Custom heal logic
        }

        @Override
        protected void onDeath() {
            // Custom death logic
        }

        @Override
        protected void onBehaviorTreeResult(IBehaviorTree.ExecutionResult result) {
            // Custom behavior tree result handling
        }

        @Override
        public AgentType getType() {
            return AgentType.CUSTOM;
        }
    }
}
