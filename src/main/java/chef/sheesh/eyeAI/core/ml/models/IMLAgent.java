package chef.sheesh.eyeAI.core.ml.models;

/**
 * Interface for machine learning capable agents.
 * Agents implementing this interface can participate in reinforcement learning.
 */
public interface IMLAgent {

    /**
     * Get the current state of the agent for ML processing.
     * @return The current state, or null if not available
     */
    IState getCurrentState();

    /**
     * Get the last experience recorded by this agent.
     * @return The last experience, or null if none available
     */
    EnhancedExperience getLastExperience();

    /**
     * Execute an action based on the action index.
     * @param actionIndex The index of the action to execute
     */
    void executeAction(int actionIndex);

    /**
     * Get the unique identifier of this agent.
     * @return The agent ID
     */
    String getId();

    /**
     * Check if the agent is active and ready for learning.
     * @return true if active, false otherwise
     */
    boolean isActive();
}
