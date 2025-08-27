package chef.sheesh.eyeAI.core.ml.algorithms;

import chef.sheesh.eyeAI.core.ml.models.Experience;
import chef.sheesh.eyeAI.core.ml.models.EnhancedExperience;

/**
 * Interface for all machine learning algorithms in the EyeAI system.
 * Provides a common contract for training and decision making.
 */
public interface ILearningAlgorithm {
    
    /**
     * Select an action based on the current state.
     * @param state The current state representation as a double array
     * @return The selected action index
     */
    int selectAction(double[] state);
    
    /**
     * Train the algorithm with a single experience.
     * @param experience The experience to learn from
     */
    void train(Experience experience);

    /**
     * Train the algorithm with an enhanced experience that uses flattened arrays
     * for state and next state. This is preferred for deep learning algorithms.
     * @param experience The enhanced experience to learn from
     */
    void trainEnhanced(EnhancedExperience experience);
    
    /**
     * Get the current exploration rate (epsilon for epsilon-greedy policies).
     * @return The current exploration rate
     */
    double getExplorationRate();
    
    /**
     * Set the exploration rate.
     * @param explorationRate The new exploration rate
     */
    void setExplorationRate(double explorationRate);
    
    /**
     * Save the model to a file.
     * @param filepath The path to save the model
     */
    void saveModel(String filepath);
    
    /**
     * Load the model from a file.
     * @param filepath The path to load the model from
     */
    void loadModel(String filepath);
}
