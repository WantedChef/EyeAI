package chef.sheesh.eyeAI.core.ml.algorithms;

import chef.sheesh.eyeAI.core.ml.models.IMLAgent;
import chef.sheesh.eyeAI.core.ml.models.EnhancedExperience;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.logging.Logger;

/**
 * Manages multi-agent reinforcement learning using a centralized training approach.
 * A shared learning algorithm is used to train on the experiences of all agents.
 */
public class MultiAgentRL implements Listener {

    private static final Logger logger = Logger.getLogger(MultiAgentRL.class.getName());

    private final List<IMLAgent> agents;
    private final ILearningAlgorithm sharedLearningAlgorithm;

    public MultiAgentRL(List<IMLAgent> agents, ILearningAlgorithm sharedAlgorithm) {
        this.agents = agents;
        this.sharedLearningAlgorithm = sharedAlgorithm;
    }

    /**
     * Coordinates the actions and learning for all agents for a single time step.
     */
    public void coordinateTick() {
        for (IMLAgent agent : agents) {
            if (!agent.isActive() || agent.getCurrentState() == null) {
                continue;
            }

            try {
                // 1. Get the agent's current state
                double[] state = agent.getCurrentState().flatten();

                // 2. Select an action using the shared policy
                int actionIndex = sharedLearningAlgorithm.selectAction(state);

                // 3. Execute the action on the agent
                agent.executeAction(actionIndex);

                // 4. Get the experience after action execution
                EnhancedExperience experience = agent.getLastExperience();

                if (experience != null) {
                    // 5. Train the shared model on the agent's experience via the common interface
                    sharedLearningAlgorithm.trainEnhanced(experience);
                }
            } catch (Exception e) {
                logger.severe("Error during agent coordination for agent " +
                            agent.getId() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Event handler for inter-agent communication via Bukkit events.
     * @param event The agent message event containing communication data
     */
    @EventHandler
    public void onAgentMessage(AgentMessageEvent event) {
        // Logic to update shared state or coordinate based on messages
        try {
            String senderId = event.getSenderId();
            String receiverId = event.getReceiverId();
            String message = event.getMessage();

            logger.info("Agent communication: " + senderId + " -> " + receiverId + ": " + message);

            // Find the receiving agent and process the message
            for (IMLAgent agent : agents) {
                if (agent.getId().equals(receiverId)) {
                    // Process the message for the receiving agent
                    // This could involve updating the agent's state or triggering specific behaviors
                    processAgentMessage(agent, senderId, message, event.getData());
                    break;
                }
            }
        } catch (Exception e) {
            logger.severe("Error processing agent message: " + e.getMessage());
        }
    }

    /**
     * Process a message for a specific agent.
     * @param agent The receiving agent
     * @param senderId The ID of the sending agent
     * @param message The message content
     * @param data Additional message data
     */
    private void processAgentMessage(IMLAgent agent, String senderId, String message, Object data) {
        // Implement specific message processing logic here
        // This could involve coordinating actions, sharing learned information, etc.
        logger.fine("Processing message for agent " + agent.getId() + " from " + senderId);
    }

    /**
     * Get the list of agents managed by this multi-agent system.
     * @return The list of ML agents
     */
    public List<IMLAgent> getAgents() {
        return agents;
    }

    /**
     * Get the shared learning algorithm.
     * @return The shared learning algorithm
     */
    public ILearningAlgorithm getSharedLearningAlgorithm() {
        return sharedLearningAlgorithm;
    }
}
