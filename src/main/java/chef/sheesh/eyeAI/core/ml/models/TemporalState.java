package chef.sheesh.eyeAI.core.ml.models;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TemporalState extends spatial state representation with time-series capabilities.
 * Maintains a history of states to enable temporal feature extraction like
 * velocity, acceleration, and movement patterns.
 */
public class TemporalState implements IState {
    private static final Logger log = LoggerFactory.getLogger(TemporalState.class);

    private final Deque<IState> stateHistory;
    private final int historyLength;
    private final int baseStateSize;
    private IState currentState;

    /**
     * Constructor for TemporalState.
     * @param historyLength Number of previous states to maintain
     * @param baseStateSize Size of the flattened state vector for each individual state
     */
    public TemporalState(int historyLength, int baseStateSize) {
        this.historyLength = historyLength;
        this.baseStateSize = baseStateSize;
        this.stateHistory = new ArrayDeque<>(historyLength);
    }

    /**
     * Add a new state to the temporal sequence.
     * @param state The new state to add
     */
    public void addState(IState state) {
        if (stateHistory.size() >= historyLength) {
            stateHistory.pollFirst(); // Remove oldest state
        }
        stateHistory.addLast(state.copy());
        currentState = state;
    }

    /**
     * Get the current state.
     * @return The most recent state
     */
    public IState getCurrentState() {
        return currentState;
    }

    /**
     * Get the state history.
     * @return Deque of historical states (newest at end)
     */
    public Deque<IState> getStateHistory() {
        return new ArrayDeque<>(stateHistory);
    }

    @Override
    public double[] flatten() {
        if (stateHistory.isEmpty()) {
            log.warn("No states in history, returning zeros");
            return new double[getStateSize()];
        }

        double[] flattened = new double[getStateSize()];
        int offset = 0;

        // Add current state features
        double[] currentFlattened = currentState.flatten();
        System.arraycopy(currentFlattened, 0, flattened, offset, currentFlattened.length);
        offset += currentFlattened.length;

        // Add historical states
        for (IState historicalState : stateHistory) {
            double[] histFlattened = historicalState.flatten();
            System.arraycopy(histFlattened, 0, flattened, offset, histFlattened.length);
            offset += histFlattened.length;
        }

        // Add temporal features (velocity, acceleration if enough history)
        addTemporalFeatures(flattened, offset);

        return flattened;
    }

    @Override
    public int getStateSize() {
        // Current state + history states + temporal features
        return baseStateSize * (historyLength + 1) + getTemporalFeatureSize();
    }

    @Override
    public IState copy() {
        TemporalState copy = new TemporalState(historyLength, baseStateSize);
        copy.stateHistory.addAll(stateHistory);
        copy.currentState = currentState != null ? currentState.copy() : null;
        return copy;
    }

    @Override
    public boolean isTerminal() {
        return currentState != null && currentState.isTerminal();
    }

    /**
     * Calculate temporal features like velocity and acceleration.
     * @param flattened The flattened array to add features to
     * @param offset The offset to start adding features
     */
    private void addTemporalFeatures(double[] flattened, int offset) {
        if (stateHistory.size() < 2) {
            // Not enough history for temporal features
            return;
        }

        IState[] states = stateHistory.toArray(new IState[0]);
        int numStates = states.length;

        // Calculate velocity (difference between consecutive states)
        if (numStates >= 2) {
            double[] current = states[numStates - 1].flatten();
            double[] previous = states[numStates - 2].flatten();

            for (int i = 0; i < baseStateSize; i++) {
                flattened[offset + i] = current[i] - previous[i];
            }
            offset += baseStateSize;
        }

        // Calculate acceleration (change in velocity)
        if (numStates >= 3) {
            double[] current = states[numStates - 1].flatten();
            double[] previous = states[numStates - 2].flatten();
            double[] oldest = states[numStates - 3].flatten();

            double[] currentVelocity = new double[baseStateSize];
            double[] previousVelocity = new double[baseStateSize];

            for (int i = 0; i < baseStateSize; i++) {
                currentVelocity[i] = current[i] - previous[i];
                previousVelocity[i] = previous[i] - oldest[i];
            }

            for (int i = 0; i < baseStateSize; i++) {
                flattened[offset + i] = currentVelocity[i] - previousVelocity[i];
            }
        }
    }

    /**
     * Get the size of temporal features.
     * @return Number of temporal feature elements
     */
    private int getTemporalFeatureSize() {
        // Velocity (if >= 2 states) + Acceleration (if >= 3 states)
        int features = 0;
        if (historyLength >= 2) { features += baseStateSize; } // velocity
        if (historyLength >= 3) { features += baseStateSize; } // acceleration
        return features;
    }

    /**
     * Get the number of states currently in history.
     * @return Current history size
     */
    public int getHistorySize() {
        return stateHistory.size();
    }

    /**
     * Clear the state history.
     */
    public void clearHistory() {
        stateHistory.clear();
        currentState = null;
    }

    /**
     * Check if we have enough history for temporal features.
     * @return true if temporal features can be calculated
     */
    public boolean hasTemporalFeatures() {
        return stateHistory.size() >= 2;
    }
}
