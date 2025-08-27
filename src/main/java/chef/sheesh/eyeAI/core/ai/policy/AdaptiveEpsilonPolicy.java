package chef.sheesh.eyeAI.core.ai.policy;

import java.util.ArrayList;
import java.util.List;

/**
 * Adaptive epsilon-greedy policy that dynamically adjusts exploration rate
 * based on learning progress, reward trends, and exploration success.
 */
public class AdaptiveEpsilonPolicy extends EpsilonPolicy {

    // Adaptive parameters
    private double initialEpsilon;
    private double adaptationRate;
    private int rewardWindowSize;
    private double rewardImprovementThreshold;

    // Learning tracking
    private final List<Double> rewardHistory;
    private long stepCount;
    private double lastAverageReward;
    private int explorationSuccessCount;
    private int explorationAttemptCount;
    private double explorationSuccessRate;

    // Adaptation strategies
    private boolean rewardBasedAdaptation;
    private boolean performanceBasedAdaptation;
    private boolean timeBasedAdaptation;

    /**
     * Create adaptive epsilon policy with default parameters
     */
    public AdaptiveEpsilonPolicy(double initialEpsilon, double minEpsilon, double epsilonDecay) {
        super(initialEpsilon, minEpsilon, epsilonDecay);
        this.initialEpsilon = initialEpsilon;
        this.adaptationRate = 0.01;
        this.rewardWindowSize = 100;
        this.rewardImprovementThreshold = 0.01;

        this.rewardHistory = new ArrayList<>();
        this.stepCount = 0;
        this.lastAverageReward = 0.0;
        this.explorationSuccessCount = 0;
        this.explorationAttemptCount = 0;
        this.explorationSuccessRate = 0.0;

        // Enable all adaptation strategies by default
        this.rewardBasedAdaptation = true;
        this.performanceBasedAdaptation = true;
        this.timeBasedAdaptation = true;
    }

    /**
     * Create adaptive epsilon policy with custom parameters
     */
    public AdaptiveEpsilonPolicy(double initialEpsilon, double minEpsilon, double epsilonDecay,
                               double adaptationRate, int rewardWindowSize, double rewardImprovementThreshold) {
        super(initialEpsilon, minEpsilon, epsilonDecay);
        this.initialEpsilon = initialEpsilon;
        this.adaptationRate = adaptationRate;
        this.rewardWindowSize = rewardWindowSize;
        this.rewardImprovementThreshold = rewardImprovementThreshold;

        this.rewardHistory = new ArrayList<>();
        this.stepCount = 0;
        this.lastAverageReward = 0.0;
        this.explorationSuccessCount = 0;
        this.explorationAttemptCount = 0;
        this.explorationSuccessRate = 0.0;

        this.rewardBasedAdaptation = true;
        this.performanceBasedAdaptation = true;
        this.timeBasedAdaptation = true;
    }

    @Override
    public int selectAction(double[] qValues) {
        boolean isExploring = shouldExplore();
        int action = super.selectAction(qValues);

        if (isExploring) {
            explorationAttemptCount++;
        }

        return action;
    }

    @Override
    public int selectAction(double[] qValues, double epsilonOverride) {
        double currentEpsilon = epsilonOverride >= 0 ? epsilonOverride : epsilon;
        boolean isExploring = random.nextDouble() < currentEpsilon;
        int action;

        if (isExploring) {
            action = random.nextInt(qValues.length);
            explorationAttemptCount++;
        } else {
            action = getBestAction(qValues);
        }

        return action;
    }

    @Override
    public void decayEpsilon() {
        // First apply base decay
        super.decayEpsilon();

        // Then apply adaptive adjustments
        adaptEpsilon();
    }

    /**
     * Update policy with learning feedback to enable adaptation
     * @param reward Reward received for the action
     * @param wasExplorationAction Whether the action was selected during exploration
     * @param qValueBefore Q-value before the action
     * @param qValueAfter Q-value after the update
     */
    public void updateLearningFeedback(double reward, boolean wasExplorationAction,
                                     double qValueBefore, double qValueAfter) {
        stepCount++;

        // Track reward history
        rewardHistory.add(reward);
        if (rewardHistory.size() > rewardWindowSize) {
            rewardHistory.remove(0);
        }

        // Track exploration success
        if (wasExplorationAction) {
            if (qValueAfter > qValueBefore) {
                explorationSuccessCount++;
            }
            explorationSuccessRate = (double) explorationSuccessCount / explorationAttemptCount;
        }

        // Adapt epsilon based on learning progress
        adaptEpsilon();
    }

    /**
     * Update policy with reward information only
     * @param reward Reward received
     */
    public void updateReward(double reward) {
        updateLearningFeedback(reward, false, 0.0, 0.0);
    }

    /**
     * Adapt epsilon based on current learning state
     */
    private void adaptEpsilon() {
        double adaptation = 0.0;

        if (rewardBasedAdaptation) {
            adaptation += calculateRewardBasedAdaptation();
        }

        if (performanceBasedAdaptation) {
            adaptation += calculatePerformanceBasedAdaptation();
        }

        if (timeBasedAdaptation) {
            adaptation += calculateTimeBasedAdaptation();
        }

        // Apply adaptation
        if (adaptation != 0.0) {
            epsilon = Math.max(minEpsilon, Math.min(initialEpsilon, epsilon + adaptation));
        }
    }

    /**
     * Calculate adaptation based on reward trends
     */
    private double calculateRewardBasedAdaptation() {
        if (rewardHistory.size() < 10) {
            return 0.0; // Not enough data
        }

        double currentAverage = rewardHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double improvement = currentAverage - lastAverageReward;

        lastAverageReward = currentAverage;

        // If rewards are decreasing, increase exploration
        if (improvement < -rewardImprovementThreshold) {
            return adaptationRate;
        }
        // If rewards are improving, decrease exploration slightly
        else if (improvement > rewardImprovementThreshold) {
            return -adaptationRate * 0.5;
        }

        return 0.0;
    }

    /**
     * Calculate adaptation based on exploration success rate
     */
    private double calculatePerformanceBasedAdaptation() {
        if (explorationAttemptCount < 10) {
            return 0.0; // Not enough exploration attempts
        }

        // If exploration success rate is low, increase exploration
        if (explorationSuccessRate < 0.3) {
            return adaptationRate * 1.5;
        }
        // If exploration success rate is high, decrease exploration
        else if (explorationSuccessRate > 0.7) {
            return -adaptationRate;
        }

        return 0.0;
    }

    /**
     * Calculate time-based adaptation (simple decay with adaptive adjustments)
     */
    private double calculateTimeBasedAdaptation() {
        if (stepCount < 100) {
            return 0.0; // Let initial decay handle early stages
        }

        // Adaptive time-based decay
        double progress = Math.min(1.0, (double) stepCount / 10000.0);
        double adaptiveDecay = adaptationRate * (1.0 - progress);

        return -adaptiveDecay;
    }

    @Override
    public void resetEpsilon() {
        epsilon = initialEpsilon;
        stepCount = 0;
        rewardHistory.clear();
        lastAverageReward = 0.0;
        explorationSuccessCount = 0;
        explorationAttemptCount = 0;
        explorationSuccessRate = 0.0;
    }

    /**
     * Reset learning tracking while preserving epsilon value
     */
    public void resetLearningTracking() {
        stepCount = 0;
        rewardHistory.clear();
        lastAverageReward = 0.0;
        explorationSuccessCount = 0;
        explorationAttemptCount = 0;
        explorationSuccessRate = 0.0;
    }

    /**
     * Get current learning statistics
     */
    public AdaptivePolicyStats getStatistics() {
        double avgReward = rewardHistory.isEmpty() ? 0.0 :
            rewardHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        return new AdaptivePolicyStats(
            stepCount,
            avgReward,
            explorationSuccessRate,
            explorationAttemptCount,
            epsilon,
            rewardHistory.size()
        );
    }

    // Configuration methods

    public void setAdaptationRate(double adaptationRate) {
        this.adaptationRate = Math.max(0.001, Math.min(0.1, adaptationRate));
    }

    public double getAdaptationRate() {
        return adaptationRate;
    }

    public void setRewardWindowSize(int rewardWindowSize) {
        this.rewardWindowSize = Math.max(10, rewardWindowSize);
    }

    public int getRewardWindowSize() {
        return rewardWindowSize;
    }

    public void setRewardImprovementThreshold(double threshold) {
        this.rewardImprovementThreshold = Math.max(0.001, threshold);
    }

    public double getRewardImprovementThreshold() {
        return rewardImprovementThreshold;
    }

    public void setRewardBasedAdaptation(boolean enabled) {
        this.rewardBasedAdaptation = enabled;
    }

    public boolean isRewardBasedAdaptationEnabled() {
        return rewardBasedAdaptation;
    }

    public void setPerformanceBasedAdaptation(boolean enabled) {
        this.performanceBasedAdaptation = enabled;
    }

    public boolean isPerformanceBasedAdaptationEnabled() {
        return performanceBasedAdaptation;
    }

    public void setTimeBasedAdaptation(boolean enabled) {
        this.timeBasedAdaptation = enabled;
    }

    public boolean isTimeBasedAdaptationEnabled() {
        return timeBasedAdaptation;
    }

    /**
     * Create an AdaptiveEpsilonPolicy from configuration
     * @param config ConfigurationManager instance
     * @param configPrefix Configuration prefix (e.g., "training.epsilon")
     * @return Configured AdaptiveEpsilonPolicy
     */
    public static AdaptiveEpsilonPolicy fromConfig(chef.sheesh.eyeAI.infra.config.ConfigurationManager config, String configPrefix) {
        double initialEpsilon = config.getDouble(configPrefix + ".start", 0.4);
        double minEpsilon = config.getDouble(configPrefix + ".min", 0.02);
        double epsilonDecay = config.getDouble(configPrefix + ".decay", 0.995);
        double adaptationRate = config.getDouble(configPrefix + ".adaptationRate", 0.01);
        int rewardWindowSize = config.getInt(configPrefix + ".rewardWindowSize", 100);
        double rewardImprovementThreshold = config.getDouble(configPrefix + ".rewardImprovementThreshold", 0.01);

        return new AdaptiveEpsilonPolicy(initialEpsilon, minEpsilon, epsilonDecay,
                                       adaptationRate, rewardWindowSize, rewardImprovementThreshold);
    }

    /**
     * Create a default AdaptiveEpsilonPolicy
     * @return Default AdaptiveEpsilonPolicy with reasonable defaults
     */
    public static AdaptiveEpsilonPolicy createDefault() {
        return new AdaptiveEpsilonPolicy(0.4, 0.02, 0.995);
    }

    /**
     * Statistics for adaptive epsilon policy
     */
    public static class AdaptivePolicyStats {
        public final long stepCount;
        public final double averageReward;
        public final double explorationSuccessRate;
        public final int explorationAttempts;
        public final double currentEpsilon;
        public final int rewardHistorySize;

        public AdaptivePolicyStats(long stepCount, double averageReward, double explorationSuccessRate,
                                 int explorationAttempts, double currentEpsilon, int rewardHistorySize) {
            this.stepCount = stepCount;
            this.averageReward = averageReward;
            this.explorationSuccessRate = explorationSuccessRate;
            this.explorationAttempts = explorationAttempts;
            this.currentEpsilon = currentEpsilon;
            this.rewardHistorySize = rewardHistorySize;
        }

        @Override
        public String toString() {
            return String.format("AdaptivePolicyStats{steps=%d, avgReward=%.3f, explorationSuccess=%.2f, attempts=%d, epsilon=%.4f, historySize=%d}",
                               stepCount, averageReward, explorationSuccessRate, explorationAttempts, currentEpsilon, rewardHistorySize);
        }
    }
}
