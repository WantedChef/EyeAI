package chef.sheesh.eyeAI.core.ml.rl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Q-Learning agent implementation with epsilon-greedy exploration.
 * Supports learning rate decay, discount factor, and experience replay.
 */
public final class QTableAgent {

    private final Map<Long, double[]> qTable = new ConcurrentHashMap<>();
    private final Random random = new Random();

    // Learning parameters
    private double learningRate = 0.1;
    private double discountFactor = 0.9;
    private double epsilon = 0.1;
    private int maxActions = 10;

    // Learning rate decay
    private double initialLearningRate = 0.1;
    private double minLearningRate = 0.01;
    private double learningRateDecay = 0.999;

    // Exploration decay
    private double initialEpsilon = 0.3;
    private double minEpsilon = 0.01;
    private double epsilonDecay = 0.995;

    // Statistics
    private long updateCount = 0;
    private double totalReward = 0.0;
    private final List<Double> rewardHistory = new ArrayList<>();

    public QTableAgent() {
        this(0.1, 0.9, 0.1, 10);
    }

    public QTableAgent(double learningRate, double discountFactor, double epsilon, int maxActions) {
        this.learningRate = learningRate;
        this.discountFactor = discountFactor;
        this.epsilon = epsilon;
        this.maxActions = maxActions;
        this.initialLearningRate = learningRate;
        this.initialEpsilon = epsilon;
    }

    /**
     * Select action using epsilon-greedy policy
     */
    public int selectAction(long stateHash, double epsilonOverride, int actionCount) {
        double currentEpsilon = epsilonOverride >= 0 ? epsilonOverride : epsilon;
        actionCount = Math.min(actionCount, maxActions);

        // Ensure Q-values exist for this state
        double[] qValues = qTable.computeIfAbsent(stateHash, k -> new double[maxActions]);

        // Epsilon-greedy action selection
        if (random.nextDouble() < currentEpsilon) {
            // Explore: random action
            return random.nextInt(actionCount);
        } else {
            // Exploit: best action
            return getBestAction(qValues, actionCount);
        }
    }

    /**
     * Select action using current epsilon
     */
    public int selectAction(long stateHash, int actionCount) {
        return selectAction(stateHash, -1, actionCount);
    }

    /**
     * Q-Learning update
     */
    public double update(long currentState, int action, double reward, long nextState) {
        updateCount++;
        totalReward += reward;

        // Get or create Q-values for current state
        double[] currentQ = qTable.computeIfAbsent(currentState, k -> new double[maxActions]);
        double[] nextQ = qTable.computeIfAbsent(nextState, k -> new double[maxActions]);

        // Q-learning formula: Q(s,a) = Q(s,a) + α[r + γmaxQ(s',a') - Q(s,a)]
        double currentQValue = currentQ[action];
        double maxNextQValue = getMaxQValue(nextQ, maxActions);

        double tdTarget = reward + discountFactor * maxNextQValue;
        double tdError = tdTarget - currentQValue;

        currentQ[action] += learningRate * tdError;

        // Decay learning rate and epsilon
        decayParameters();

        // Keep reward history (last 1000 rewards)
        rewardHistory.add(reward);
        if (rewardHistory.size() > 1000) {
            rewardHistory.remove(0);
        }

        // Return TD error for loss calculation
        return tdError;
    }

    /**
     * Get the best action for a state
     */
    public int getBestAction(long stateHash, int actionCount) {
        double[] qValues = qTable.get(stateHash);
        if (qValues == null) {
            return random.nextInt(actionCount);
        }
        return getBestAction(qValues, actionCount);
    }

    /**
     * Get Q-value for a state-action pair
     */
    public double getQValue(long stateHash, int action) {
        double[] qValues = qTable.get(stateHash);
        if (qValues == null || action >= qValues.length) {
            return 0.0;
        }
        return qValues[action];
    }

    /**
     * Get all Q-values for a state
     */
    public double[] getQValues(long stateHash) {
        return qTable.getOrDefault(stateHash, new double[maxActions]).clone();
    }

    /**
     * Get the maximum Q-value for a state
     */
    public double getMaxQValue(long stateHash) {
        double[] qValues = qTable.get(stateHash);
        if (qValues == null) {
            return 0.0;
        }
        return getMaxQValue(qValues, maxActions);
    }

    /**
     * Set learning parameters
     */
    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
        this.initialLearningRate = learningRate;
    }

    public void setDiscountFactor(double discountFactor) {
        this.discountFactor = discountFactor;
    }

    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
        this.initialEpsilon = epsilon;
    }

    public void setMaxActions(int maxActions) {
        this.maxActions = maxActions;
    }

    /**
     * Configure learning rate decay
     */
    public void setLearningRateDecay(double minLearningRate, double decay) {
        this.minLearningRate = minLearningRate;
        this.learningRateDecay = decay;
    }

    /**
     * Configure epsilon decay
     */
    public void setEpsilonDecay(double minEpsilon, double decay) {
        this.minEpsilon = minEpsilon;
        this.epsilonDecay = decay;
    }

    /**
     * Reset the agent (clear Q-table)
     */
    public void reset() {
        qTable.clear();
        updateCount = 0;
        totalReward = 0.0;
        rewardHistory.clear();
        learningRate = initialLearningRate;
        epsilon = initialEpsilon;
    }

    /**
     * Get learning statistics
     */
    public QLearningStats getStatistics() {
        double avgReward = rewardHistory.isEmpty() ? 0.0 :
            rewardHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        return new QLearningStats(
            updateCount,
            qTable.size(),
            totalReward,
            avgReward,
            learningRate,
            epsilon,
            discountFactor,
            qTable
        );
    }

    /**
     * Export Q-table for persistence
     */
    public Map<Long, double[]> exportQTable() {
        Map<Long, double[]> copy = new HashMap<>();
        for (Map.Entry<Long, double[]> entry : qTable.entrySet()) {
            copy.put(entry.getKey(), entry.getValue().clone());
        }
        return copy;
    }

    /**
     * Import Q-table from persistence
     */
    public void importQTable(Map<Long, double[]> qTable) {
        this.qTable.clear();
        for (Map.Entry<Long, double[]> entry : qTable.entrySet()) {
            this.qTable.put(entry.getKey(), entry.getValue().clone());
        }
    }

    // Private helper methods

    private int getBestAction(double[] qValues, int actionCount) {
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

    private double getMaxQValue(double[] qValues, int actionCount) {
        double maxValue = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < Math.min(qValues.length, actionCount); i++) {
            if (qValues[i] > maxValue) {
                maxValue = qValues[i];
            }
        }

        return maxValue == Double.NEGATIVE_INFINITY ? 0.0 : maxValue;
    }

    private void decayParameters() {
        // Decay learning rate
        learningRate = Math.max(minLearningRate, learningRate * learningRateDecay);

        // Decay epsilon
        epsilon = Math.max(minEpsilon, epsilon * epsilonDecay);
    }

    /**
     * Statistics for Q-learning agent
     */
    public static class QLearningStats {
        public final long updateCount;
        public final int stateCount;
        public final double totalReward;
        public final double averageReward;
        public final double learningRate;
        public final double epsilon;
        public final double discountFactor;
        public final Map<Long, double[]> qTable;

        public QLearningStats(long updateCount, int stateCount, double totalReward,
                            double averageReward, double learningRate, double epsilon,
                            double discountFactor, Map<Long, double[]> qTable) {
            this.updateCount = updateCount;
            this.stateCount = stateCount;
            this.totalReward = totalReward;
            this.averageReward = averageReward;
            this.learningRate = learningRate;
            this.epsilon = epsilon;
            this.discountFactor = discountFactor;
            this.qTable = new HashMap<>(qTable);
        }

        @Override
        public String toString() {
            return String.format("QLearningStats{updates=%d, states=%d, totalReward=%.2f, avgReward=%.3f, lr=%.4f, eps=%.4f, gamma=%.2f}",
                               updateCount, stateCount, totalReward, averageReward, learningRate, epsilon, discountFactor);
        }
    }
}
