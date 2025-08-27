package chef.sheesh.eyeAI.ai.agents;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manager class responsible for creating, managing, and updating all agents in the system.
 */
public class AgentManager {

    private final Map<String, IAgent> agents;
    private final Map<IAgent.AgentType, List<IAgent>> agentsByType;
    private final Map<World, List<IAgent>> agentsByWorld;
    private final AgentFactory agentFactory;
    private boolean isRunning;

    public AgentManager() {
        this.agents = new ConcurrentHashMap<>();
        this.agentsByType = new ConcurrentHashMap<>();
        this.agentsByWorld = new ConcurrentHashMap<>();
        this.agentFactory = new AgentFactory();
        this.isRunning = false;
    }

    /**
     * Start the agent manager
     */
    public void start() {
        if (isRunning) {
            return;
        }

        isRunning = true;
        // Start the main update loop
        startUpdateLoop();
    }

    /**
     * Stop the agent manager
     */
    public void stop() {
        if (!isRunning) {
            return;
        }

        isRunning = false;

        // Despawn all agents
        for (IAgent agent : agents.values()) {
            agent.despawn();
        }

        agents.clear();
        agentsByType.clear();
        agentsByWorld.clear();
    }

    /**
     * Create and spawn a new agent
     */
    public IAgent createAgent(AgentConfig config, Location location) {
        try {
            IAgent agent = agentFactory.createAgent(config);

            // Register the agent
            registerAgent(agent);

            // Spawn the agent
            if (agent.spawn(location)) {
                return agent;
            } else {
                // Spawn failed, unregister
                unregisterAgent(agent);
                return null;
            }

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Create a combat agent
     */
    public IAgent createCombatAgent(String name, Location location) {
        AgentConfig config = AgentConfig.builder(name, IAgent.AgentType.COMBAT)
            .spawnLocation(location)
            .maxHealth(100.0)
            .movementSpeed(1.0)
            .attackRange(3.0)
            .detectionRange(16.0)
            .build();

        return createAgent(config, location);
    }

    /**
     * Create a patrol agent
     */
    public IAgent createPatrolAgent(String name, Location location, List<Location> waypoints) {
        AgentConfig config = AgentConfig.builder(name, IAgent.AgentType.PATROL)
            .spawnLocation(location)
            .maxHealth(80.0)
            .movementSpeed(0.8)
            .detectionRange(12.0)
            .build();

        IAgent agent = createAgent(config, location);
        if (agent instanceof PatrolAgent && waypoints != null) {
            ((PatrolAgent) agent).setWaypoints(waypoints);
        }

        return agent;
    }

    /**
     * Get an agent by ID
     */
    public IAgent getAgent(String id) {
        return agents.get(id);
    }

    /**
     * Get all agents
     */
    public Collection<IAgent> getAllAgents() {
        return new ArrayList<>(agents.values());
    }

    /**
     * Get agents by type
     */
    public List<IAgent> getAgentsByType(IAgent.AgentType type) {
        return new ArrayList<>(agentsByType.getOrDefault(type, Collections.emptyList()));
    }

    /**
     * Get agents in a specific world
     */
    public List<IAgent> getAgentsInWorld(World world) {
        return new ArrayList<>(agentsByWorld.getOrDefault(world, Collections.emptyList()));
    }

    /**
     * Get agents near a location
     */
    public List<IAgent> getAgentsNearLocation(Location location, double radius) {
        return agents.values().stream()
            .filter(agent -> agent.isActive())
            .filter(agent -> {
                Location agentLocation = agent.getLocation();
                return agentLocation != null && agentLocation.distance(location) <= radius;
            })
            .collect(Collectors.toList());
    }

    /**
     * Despawn an agent
     */
    public boolean despawnAgent(String id) {
        IAgent agent = agents.get(id);
        if (agent != null) {
            agent.despawn();
            unregisterAgent(agent);
            return true;
        }
        return false;
    }

    /**
     * Despawn all agents
     */
    public void despawnAllAgents() {
        List<IAgent> agentsToDespawn = new ArrayList<>(agents.values());
        for (IAgent agent : agentsToDespawn) {
            agent.despawn();
        }
        agents.clear();
        agentsByType.clear();
        agentsByWorld.clear();
    }

    /**
     * Despawn agents by type
     */
    public void despawnAgentsByType(IAgent.AgentType type) {
        List<IAgent> agentsOfType = agentsByType.get(type);
        if (agentsOfType != null) {
            for (IAgent agent : new ArrayList<>(agentsOfType)) {
                agent.despawn();
                unregisterAgent(agent);
            }
        }
    }

    /**
     * Despawn agents in a specific world
     */
    public void despawnAgentsInWorld(World world) {
        List<IAgent> worldAgents = agentsByWorld.get(world);
        if (worldAgents != null) {
            for (IAgent agent : new ArrayList<>(worldAgents)) {
                agent.despawn();
                unregisterAgent(agent);
            }
        }
    }

    /**
     * Get agent statistics
     */
    public AgentStats getStats() {
        int totalAgents = agents.size();
        int activeAgents = (int) agents.values().stream().filter(IAgent::isActive).count();
        int deadAgents = (int) agents.values().stream().filter(agent -> agent.getState() == IAgent.AgentState.DEAD).count();

        Map<IAgent.AgentType, Integer> agentsByTypeCount = new HashMap<>();
        for (IAgent.AgentType type : IAgent.AgentType.values()) {
            agentsByTypeCount.put(type, agentsByType.getOrDefault(type, Collections.emptyList()).size());
        }

        return new AgentStats(totalAgents, activeAgents, deadAgents, agentsByTypeCount);
    }

    /**
     * Register an agent in the internal data structures
     */
    private void registerAgent(IAgent agent) {
        agents.put(agent.getId(), agent);

        // Register by type
        agentsByType.computeIfAbsent(agent.getType(), k -> new ArrayList<>()).add(agent);

        // Register by world (if spawned)
        if (agent.getWorld() != null) {
            agentsByWorld.computeIfAbsent(agent.getWorld(), k -> new ArrayList<>()).add(agent);
        }
    }

    /**
     * Unregister an agent from the internal data structures
     */
    private void unregisterAgent(IAgent agent) {
        agents.remove(agent.getId());

        // Remove from type list
        List<IAgent> typeList = agentsByType.get(agent.getType());
        if (typeList != null) {
            typeList.remove(agent);
            if (typeList.isEmpty()) {
                agentsByType.remove(agent.getType());
            }
        }

        // Remove from world list
        World world = agent.getWorld();
        if (world != null) {
            List<IAgent> worldList = agentsByWorld.get(world);
            if (worldList != null) {
                worldList.remove(agent);
                if (worldList.isEmpty()) {
                    agentsByWorld.remove(world);
                }
            }
        }
    }

    /**
     * Start the main update loop
     */
    private void startUpdateLoop() {
        // This would typically run on a separate thread or be called by the server's tick event
        // For now, we'll implement it as a simple loop that can be called externally
    }

    /**
     * Update all agents (should be called every tick)
     */
    public void updateAllAgents() {
        if (!isRunning) {
            return;
        }

        // Update all active agents
        for (IAgent agent : agents.values()) {
            if (agent.isActive()) {
                try {
                    agent.tick();
                } catch (Exception e) {
                    // Log error and potentially despawn problematic agent
                    despawnAgent(agent.getId());
                }
            }
        }

        // Clean up dead agents
        cleanupDeadAgents();
    }

    /**
     * Clean up dead agents that can be removed
     */
    private void cleanupDeadAgents() {
        List<IAgent> deadAgents = agents.values().stream()
            .filter(agent -> agent.getState() == IAgent.AgentState.DEAD)
            .collect(Collectors.toList());

        for (IAgent deadAgent : deadAgents) {
            // Check if agent can respawn
            AgentConfig config = deadAgent.getConfig();
            if (config.canRespawn()) {
                // Reset and respawn after delay
                deadAgent.reset();
                // Note: In a real implementation, you'd want to schedule the respawn
                // rather than doing it immediately
                deadAgent.spawn(config.getSpawnLocation());
            } else {
                // Remove permanently
                unregisterAgent(deadAgent);
            }
        }
    }

    /**
     * Statistics class for agent information
     */
    public static class AgentStats {
        private final int totalAgents;
        private final int activeAgents;
        private final int deadAgents;
        private final Map<IAgent.AgentType, Integer> agentsByTypeCount;

        public AgentStats(int totalAgents, int activeAgents, int deadAgents,
                         Map<IAgent.AgentType, Integer> agentsByTypeCount) {
            this.totalAgents = totalAgents;
            this.activeAgents = activeAgents;
            this.deadAgents = deadAgents;
            this.agentsByTypeCount = agentsByTypeCount;
        }

        public int getTotalAgents() {
            return totalAgents;
        }

        public int getActiveAgents() {
            return activeAgents;
        }

        public int getDeadAgents() {
            return deadAgents;
        }

        public Map<IAgent.AgentType, Integer> getAgentsByTypeCount() {
            return new HashMap<>(agentsByTypeCount);
        }
    }
}
