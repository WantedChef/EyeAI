package chef.sheesh.eyeAI.core.ml.models;

/**
 * Enhanced experience record that supports both GameState objects and double arrays
 * for neural network compatibility. This allows seamless integration between
 * the existing Q-learning system and the new deep learning algorithms.
 */
public record EnhancedExperience(
    double[] state,      // Flattened state for neural networks
    int action,          // Action index (from Action enum)
    double reward,       // Reward received
    double[] nextState,  // Flattened next state
    boolean done         // Whether this is a terminal state
) {
    
    /**
     * Create an EnhancedExperience from the existing Experience format.
     * @param experience The original experience with GameState objects
     * @return Enhanced experience with flattened states
     */
    public static EnhancedExperience fromExperience(Experience experience) {
        return new EnhancedExperience(
            experience.state().flatten(),
            experience.action().ordinal(),
            experience.reward(),
            experience.nextState().flatten(),
            experience.nextState().isTerminal()
        );
    }
    
    /**
     * Create an EnhancedExperience directly from arrays.
     * @param state Current state array
     * @param action Action index
     * @param reward Reward value
     * @param nextState Next state array
     * @param done Terminal flag
     * @return New enhanced experience
     */
    public static EnhancedExperience create(double[] state, int action, double reward, double[] nextState, boolean done) {
        return new EnhancedExperience(state.clone(), action, reward, nextState.clone(), done);
    }
    
    /**
     * Get the state size (number of features).
     * @return Size of the state vector
     */
    public int getStateSize() {
        return state.length;
    }
    
    /**
     * Get the action as an Action enum value.
     * @return The Action enum corresponding to the action index
     */
    public Action getActionEnum() {
        return Action.values()[action];
    }
}
