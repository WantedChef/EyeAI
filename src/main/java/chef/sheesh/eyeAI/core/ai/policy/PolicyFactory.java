package chef.sheesh.eyeAI.core.ai.policy;

import chef.sheesh.eyeAI.infra.config.ConfigurationManager;

/**
 * Factory class for creating epsilon-greedy policies based on configuration
 */
public final class PolicyFactory {

    private PolicyFactory() {
        // Utility class
    }

    /**
     * Create a policy based on configuration settings
     * @param config ConfigurationManager instance
     * @param configPrefix Configuration prefix (e.g., "training.epsilon")
     * @return EpsilonPolicy or AdaptiveEpsilonPolicy based on config
     */
    public static EpsilonPolicy createPolicy(ConfigurationManager config, String configPrefix) {
        boolean useAdaptive = config.getBoolean(configPrefix + ".adaptive", false);

        if (useAdaptive) {
            return AdaptiveEpsilonPolicy.fromConfig(config, configPrefix);
        } else {
            return EpsilonPolicy.fromConfig(config, configPrefix);
        }
    }

    /**
     * Create a basic epsilon-greedy policy
     * @param epsilon Exploration probability (0.0 to 1.0)
     * @return EpsilonPolicy instance
     */
    public static EpsilonPolicy createBasicPolicy(double epsilon) {
        return new EpsilonPolicy(epsilon);
    }

    /**
     * Create a basic epsilon-greedy policy with decay
     * @param initialEpsilon Initial exploration probability
     * @param minEpsilon Minimum exploration probability
     * @param epsilonDecay Decay factor per step
     * @return EpsilonPolicy instance
     */
    public static EpsilonPolicy createBasicPolicy(double initialEpsilon, double minEpsilon, double epsilonDecay) {
        return new EpsilonPolicy(initialEpsilon, minEpsilon, epsilonDecay);
    }

    /**
     * Create an adaptive epsilon-greedy policy
     * @param initialEpsilon Initial exploration probability
     * @param minEpsilon Minimum exploration probability
     * @param epsilonDecay Decay factor per step
     * @return AdaptiveEpsilonPolicy instance
     */
    public static AdaptiveEpsilonPolicy createAdaptivePolicy(double initialEpsilon, double minEpsilon, double epsilonDecay) {
        return new AdaptiveEpsilonPolicy(initialEpsilon, minEpsilon, epsilonDecay);
    }

    /**
     * Create an adaptive epsilon-greedy policy with custom parameters
     * @param initialEpsilon Initial exploration probability
     * @param minEpsilon Minimum exploration probability
     * @param epsilonDecay Decay factor per step
     * @param adaptationRate Rate of adaptive adjustments
     * @param rewardWindowSize Size of reward history window
     * @param rewardImprovementThreshold Threshold for reward improvement detection
     * @return AdaptiveEpsilonPolicy instance
     */
    public static AdaptiveEpsilonPolicy createAdaptivePolicy(double initialEpsilon, double minEpsilon,
                                                           double epsilonDecay, double adaptationRate,
                                                           int rewardWindowSize, double rewardImprovementThreshold) {
        return new AdaptiveEpsilonPolicy(initialEpsilon, minEpsilon, epsilonDecay,
                                       adaptationRate, rewardWindowSize, rewardImprovementThreshold);
    }

    /**
     * Create a default policy based on common use cases
     * @param useAdaptive Whether to use adaptive policy
     * @return Default policy instance
     */
    public static EpsilonPolicy createDefaultPolicy(boolean useAdaptive) {
        if (useAdaptive) {
            return AdaptiveEpsilonPolicy.createDefault();
        } else {
            return EpsilonPolicy.createDefault();
        }
    }
}
