package chef.sheesh.eyeAI.ai.agents;

import chef.sheesh.eyeAI.ai.behavior.BehaviorTreeFactory;
import chef.sheesh.eyeAI.ai.behavior.IBehaviorTree;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Example class demonstrating how to use the Agent system.
 * This class provides sample code for creating and managing different types of agents.
 */
public class AgentExamples {

    private final AgentManager agentManager;
    private final BehaviorTreeFactory behaviorTreeFactory;

    public AgentExamples(AgentManager agentManager) {
        this.agentManager = agentManager;
        this.behaviorTreeFactory = new BehaviorTreeFactory();
    }

    /**
     * Example: Create a simple combat agent
     */
    public IAgent createSimpleCombatAgent(Location location) {
        return agentManager.createCombatAgent("CombatGuard", location);
    }

    /**
     * Example: Create a patrol agent with custom waypoints
     */
    public IAgent createPatrolAgentWithWaypoints(Location center, World world) {
        List<Location> waypoints = new ArrayList<>();

        // Create a square patrol route
        double radius = 10.0;
        for (int i = 0; i < 4; i++) {
            double angle = (Math.PI * 2 * i) / 4;
            double x = center.getX() + Math.cos(angle) * radius;
            double z = center.getZ() + Math.sin(angle) * radius;
            waypoints.add(new Location(world, x, center.getY(), z));
        }

        return agentManager.createPatrolAgent("PatrolGuard", center, waypoints);
    }

    /**
     * Example: Create a custom agent with specific behavior
     */
    public IAgent createCustomAgent(Location location) {
        // Create a custom behavior tree
        IBehaviorTree customBehavior = behaviorTreeFactory.createDynamicPriorityTree();

        // Create custom configuration
        AgentConfig config = AgentConfig.builder("CustomAgent", IAgent.AgentType.CUSTOM)
            .behaviorTree(customBehavior)
            .maxHealth(150.0)
            .movementSpeed(1.1)
            .attackRange(4.0)
            .detectionRange(20.0)
            .canRespawn(true)
            .respawnDelay(20000)
            .property("aggression_level", 7)
            .property("special_ability", "teleport")
            .build();

        return agentManager.createAgent(config, location);
    }

    /**
     * Example: Create a boss agent
     */
    public IAgent createBossAgent(Location location) {
        AgentFactory factory = new AgentFactory();
        IAgent boss = factory.createBossAgent("BossAgent");

        // Spawn the boss
        if (boss.spawn(location)) {
            return boss;
        }

        return null;
    }

    /**
     * Example: Set up a defensive perimeter with multiple agents
     */
    public List<IAgent> createDefensivePerimeter(Location center, int agentCount) {
        List<IAgent> agents = new ArrayList<>();
        World world = center.getWorld();

        for (int i = 0; i < agentCount; i++) {
            // Position agents in a circle around the center
            double angle = (Math.PI * 2 * i) / agentCount;
            double radius = 15.0;
            double x = center.getX() + Math.cos(angle) * radius;
            double z = center.getZ() + Math.sin(angle) * radius;
            Location agentLocation = new Location(world, x, center.getY(), z);

            // Create a guard agent
            IAgent guard = agentManager.createAgent(
                AgentConfig.builder("Guard" + i, IAgent.AgentType.GUARD)
                    .spawnLocation(agentLocation)
                    .maxHealth(120.0)
                    .attackRange(4.0)
                    .detectionRange(20.0)
                    .build(),
                agentLocation
            );

            if (guard != null) {
                agents.add(guard);
            }
        }

        return agents;
    }

    /**
     * Example: Demonstrate agent management operations
     */
    public void demonstrateAgentManagement() {
        // Get all agents
        var allAgents = agentManager.getAllAgents();
        System.out.println("Total agents: " + allAgents.size());

        // Get agents by type
        var combatAgents = agentManager.getAgentsByType(IAgent.AgentType.COMBAT);
        System.out.println("Combat agents: " + combatAgents.size());

        // Get stats
        var stats = agentManager.getStats();
        System.out.println("Active agents: " + stats.getActiveAgents());
        System.out.println("Dead agents: " + stats.getDeadAgents());

        // Find agents near a location (would need a location parameter)
        // var nearbyAgents = agentManager.getAgentsNearLocation(someLocation, 50.0);
    }

    /**
     * Example: Create a complex scenario with multiple agent types
     */
    public void createComplexScenario(Location center) {
        World world = center.getWorld();

        // Create a boss in the center
        createBossAgent(center);

        // Create guards around the boss
        createDefensivePerimeter(center, 6);

        // Create patrol agents in the outer perimeter
        for (int i = 0; i < 3; i++) {
            double angle = (Math.PI * 2 * i) / 3;
            double radius = 30.0;
            double x = center.getX() + Math.cos(angle) * radius;
            double z = center.getZ() + Math.sin(angle) * radius;
            Location patrolLocation = new Location(world, x, center.getY(), z);

            createPatrolAgentWithWaypoints(patrolLocation, world);
        }

        // Create some scout agents
        for (int i = 0; i < 2; i++) {
            double x = center.getX() + (Math.random() - 0.5) * 40;
            double z = center.getZ() + (Math.random() - 0.5) * 40;
            Location scoutLocation = new Location(world, x, center.getY(), z);

            AgentFactory factory = new AgentFactory();
            IAgent scout = factory.createScoutAgent("Scout" + i);
            scout.spawn(scoutLocation);
        }
    }
}
