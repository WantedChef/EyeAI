package chef.sheesh.eyeAI.ai.agents;

import chef.sheesh.eyeAI.ai.behavior.IBehaviorTree;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Interface for AI agents that control fake players and execute behavior trees.
 * Agents represent autonomous entities that can perform complex behaviors in the game world.
 */
public interface IAgent {

    /**
     * Get the unique identifier of this agent
     */
    String getId();

    /**
     * Get the name of this agent
     */
    String getName();

    /**
     * Get the current location of this agent
     */
    Location getLocation();

    /**
     * Get the world this agent is in
     */
    World getWorld();

    /**
     * Get the fake player controlled by this agent
     */
    IFakePlayer getFakePlayer();

    /**
     * Get the behavior tree currently being executed
     */
    IBehaviorTree getBehaviorTree();

    /**
     * Set the behavior tree for this agent
     */
    void setBehaviorTree(IBehaviorTree behaviorTree);

    /**
     * Get the current state of this agent
     */
    AgentState getState();

    /**
     * Set the current state of this agent
     */
    void setState(AgentState state);

    /**
     * Check if this agent is active and functioning
     */
    boolean isActive();

    /**
     * Spawn this agent at the specified location
     */
    boolean spawn(Location location);

    /**
     * Despawn this agent and clean up resources
     */
    void despawn();

    /**
     * Update this agent's AI logic (called every tick)
     */
    void tick();

    /**
     * Reset this agent to its initial state
     */
    void reset();

    /**
     * Get the type of this agent
     */
    AgentType getType();

    /**
     * Check if this agent is alive
     */
    boolean isAlive();

    /**
     * Get the health of this agent (0.0 to 1.0)
     */
    double getHealth();

    /**
     * Set the health of this agent
     */
    void setHealth(double health);

    /**
     * Deal damage to this agent
     */
    void damage(double amount);

    /**
     * Heal this agent
     */
    void heal(double amount);

    /**
     * Kill this agent
     */
    void kill();

    /**
     * Get the configuration for this agent
     */
    AgentConfig getConfig();

    /**
     * Set the configuration for this agent
     */
    void setConfig(AgentConfig config);

    /**
     * Enumeration of possible agent states
     */
    enum AgentState {
        SPAWNING,
        ACTIVE,
        INACTIVE,
        DESPAWNING,
        DEAD
    }

    /**
     * Enumeration of agent types
     */
    enum AgentType {
        COMBAT,
        PATROL,
        GUARD,
        SCOUT,
        TRADER,
        CUSTOM
    }
}
