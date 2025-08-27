package chef.sheesh.eyeAI.ai.agents;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main integration point for the Agent system.
 * This class provides easy access to agent management functionality.
 */
public class AgentSystem {

    private static AgentManager agentManager;
    private static JavaPlugin plugin;

    /**
     * Initialize the agent system with a plugin instance
     */
    public static void initialize(JavaPlugin plugin) {
        AgentSystem.plugin = plugin;
        agentManager = new AgentManager();
    }

    /**
     * Start the agent system
     */
    public static void start() {
        if (agentManager != null) {
            agentManager.start();

            // Schedule the update task
            plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
                if (agentManager != null) {
                    agentManager.updateAllAgents();
                }
            }, 1L, 1L); // Update every tick
        }
    }

    /**
     * Stop the agent system
     */
    public static void stop() {
        if (agentManager != null) {
            agentManager.stop();
        }
    }

    /**
     * Get the agent manager instance
     */
    public static AgentManager getAgentManager() {
        return agentManager;
    }

    /**
     * Create and spawn a combat agent
     */
    public static IAgent spawnCombatAgent(String name, Location location) {
        if (agentManager == null) {
            return null;
        }
        return agentManager.createCombatAgent(name, location);
    }

    /**
     * Create and spawn a patrol agent
     */
    public static IAgent spawnPatrolAgent(String name, Location location) {
        if (agentManager == null) {
            return null;
        }
        return agentManager.createPatrolAgent(name, location, null);
    }

    /**
     * Create and spawn a custom agent
     */
    public static IAgent spawnAgent(AgentConfig config, Location location) {
        if (agentManager == null) {
            return null;
        }
        return agentManager.createAgent(config, location);
    }

    /**
     * Get an agent by ID
     */
    public static IAgent getAgent(String id) {
        if (agentManager == null) {
            return null;
        }
        return agentManager.getAgent(id);
    }

    /**
     * Despawn an agent
     */
    public static boolean despawnAgent(String id) {
        if (agentManager == null) {
            return false;
        }
        return agentManager.despawnAgent(id);
    }

    /**
     * Despawn all agents
     */
    public static void despawnAllAgents() {
        if (agentManager != null) {
            agentManager.despawnAllAgents();
        }
    }

    /**
     * Get agent statistics
     */
    public static AgentManager.AgentStats getStats() {
        if (agentManager == null) {
            return null;
        }
        return agentManager.getStats();
    }
}
