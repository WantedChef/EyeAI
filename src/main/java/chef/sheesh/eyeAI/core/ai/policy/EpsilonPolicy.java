package chef.sheesh.eyeAI.core.ai.policy;

import java.util.Random;

/**
 * Epsilon-greedy policy for reinforcement learning agents.
 * Balances exploration and exploitation by randomly selecting actions
 * with probability epsilon, or greedily selecting the best action otherwise.
 */
public class EpsilonPolicy {

    protected final Random random = new Random();
    protected double epsilon;
    protected double minEpsilon;
    protected double epsilonDecay;

    /**
     * Create a new epsilon-greedy policy with fixed epsilon
     * @param epsilon Probability of exploration (0.0 to 1.0)
     */
    public EpsilonPolicy(double epsilon) {
        this.epsilon = epsilon;
        this.minEpsilon = epsilon;
        this.epsilonDecay = 1.0; // No decay by default
    }

    /**
     * Create a new epsilon-greedy policy with decay parameters
     * @param initialEpsilon Initial exploration probability
     * @param minEpsilon Minimum exploration probability
     * @param epsilonDecay Decay factor per step (0.0 to 1.0)
     */
    public EpsilonPolicy(double initialEpsilon, double minEpsilon, double epsilonDecay) {
        this.epsilon = initialEpsilon;
        this.minEpsilon = minEpsilon;
        this.epsilonDecay = epsilonDecay;
    }

    /**
     * Select an action using epsilon-greedy strategy
     * @param qValues Array of Q-values for each action
     * @return Selected action index
     */
    public int selectAction(double[] qValues) {
        if (qValues == null || qValues.length == 0) {
            throw new IllegalArgumentException("Q-values array cannot be null or empty");
        }

        // Exploration
        if (random.nextDouble() < epsilon) {
            return random.nextInt(qValues.length);
        }

        // Exploitation - find best action
        return getBestAction(qValues);
    }

    /**
     * Select an action with epsilon override
     * @param qValues Array of Q-values for each action
     * @param epsilonOverride Override epsilon value (-1 to use current epsilon)
     * @return Selected action index
     */
    public int selectAction(double[] qValues, double epsilonOverride) {
        double currentEpsilon = epsilonOverride >= 0 ? epsilonOverride : epsilon;

        if (qValues == null || qValues.length == 0) {
            throw new IllegalArgumentException("Q-values array cannot be null or empty");
        }

        // Exploration
        if (random.nextDouble() < currentEpsilon) {
            return random.nextInt(qValues.length);
        }

        // Exploitation - find best action
        return getBestAction(qValues);
    }

    /**
     * Select an action with limited action count
     * @param qValues Array of Q-values for each action
     * @param maxActions Maximum number of actions to consider
     * @return Selected action index
     */
    public int selectAction(double[] qValues, int maxActions) {
        if (qValues == null || qValues.length == 0) {
            throw new IllegalArgumentException("Q-values array cannot be null or empty");
        }

        int actionCount = Math.min(maxActions, qValues.length);

        // Exploration
        if (random.nextDouble() < epsilon) {
            return random.nextInt(actionCount);
        }

        // Exploitation - find best action within limit
        return getBestAction(qValues, actionCount);
    }

    /**
     * Get the best action based on Q-values
     * @param qValues Array of Q-values
     * @return Index of best action
     */
    public int getBestAction(double[] qValues) {
        return getBestAction(qValues, qValues.length);
    }

    /**
     * Get the best action within action count limit
     * @param qValues Array of Q-values
     * @param actionCount Maximum actions to consider
     * @return Index of best action
     */
    public int getBestAction(double[] qValues, int actionCount) {
        int bestAction = 0;
        double bestValue = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < Math.min(qValues.length, actionCount); i++) {
            if (qValues[i] > bestValue) {
                bestValue = qValues[i];
                bestAction = i;
            }
        }

        return bestAction;
    }

    /**
     * Decay epsilon based on the configured decay rate
     */
    public void decayEpsilon() {
        epsilon = Math.max(minEpsilon, epsilon * epsilonDecay);
    }

    /**
     * Reset epsilon to initial value
     */
    public void resetEpsilon() {
        // This method can be overridden by subclasses
    }

    /**
     * Get current epsilon value
     */
    public double getEpsilon() {
        return epsilon;
    }

    /**
     * Set epsilon value
     */
    public void setEpsilon(double epsilon) {
        this.epsilon = Math.max(0.0, Math.min(1.0, epsilon));
    }

    /**
     * Get minimum epsilon value
     */
    public double getMinEpsilon() {
        return minEpsilon;
    }

    /**
     * Set minimum epsilon value
     */
    public void setMinEpsilon(double minEpsilon) {
        this.minEpsilon = Math.max(0.0, Math.min(1.0, minEpsilon));
    }

    /**
     * Get epsilon decay rate
     */
    public double getEpsilonDecay() {
        return epsilonDecay;
    }

    /**
     * Set epsilon decay rate
     */
    public void setEpsilonDecay(double epsilonDecay) {
        this.epsilonDecay = Math.max(0.0, Math.min(1.0, epsilonDecay));
    }

    /**
     * Check if policy is in exploration mode for current step
     */
    public boolean shouldExplore() {
        return random.nextDouble() < epsilon;
    }

    /**
     * Get exploration probability for current epsilon
     */
    public double getExplorationRate() {
        return epsilon;
    }

    /**
     * Get exploitation probability for current epsilon
     */
    public double getExploitationRate() {
        return 1.0 - epsilon;
    }

    /**
     * Create an EpsilonPolicy from configuration
     * @param config ConfigurationManager instance
     * @param configPrefix Configuration prefix (e.g., "training.epsilon")
     * @return Configured EpsilonPolicy
     */
    public static EpsilonPolicy fromConfig(chef.sheesh.eyeAI.infra.config.ConfigurationManager config, String configPrefix) {
        double epsilon = config.getDouble(configPrefix + ".start", 0.1);
        double minEpsilon = config.getDouble(configPrefix + ".min", 0.01);
        double epsilonDecay = config.getDouble(configPrefix + ".decay", 0.995);

        return new EpsilonPolicy(epsilon, minEpsilon, epsilonDecay);
    }

    /**
     * Create a default EpsilonPolicy
     * @return Default EpsilonPolicy with reasonable defaults
     */
    public static EpsilonPolicy createDefault() {
        return new EpsilonPolicy(0.3, 0.01, 0.995);
    }
}
