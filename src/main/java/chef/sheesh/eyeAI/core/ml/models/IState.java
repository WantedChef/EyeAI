package chef.sheesh.eyeAI.core.ml.models;

/**
 * Interface for state representations in the EyeAI system.
 * All state classes should implement this interface to provide
 * a consistent way to convert states to neural network inputs.
 */
public interface IState {
    
    /**
     * Convert the state to a flattened double array for neural network input.
     * @return A double array representation of the state
     */
    double[] flatten();
    
    /**
     * Get the size of the flattened state vector.
     * @return The number of elements in the flattened array
     */
    int getStateSize();
    
    /**
     * Create a copy of this state.
     * @return A new instance with the same state values
     */
    IState copy();
    
    /**
     * Check if this state represents a terminal state.
     * @return true if this is a terminal state, false otherwise
     */
    boolean isTerminal();
}
