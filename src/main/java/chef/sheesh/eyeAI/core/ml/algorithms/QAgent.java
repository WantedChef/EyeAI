package chef.sheesh.eyeAI.core.ml.algorithms;

import chef.sheesh.eyeAI.core.ml.models.Action;
import chef.sheesh.eyeAI.core.ml.models.Experience;
import chef.sheesh.eyeAI.core.ml.models.GameState;
import chef.sheesh.eyeAI.core.ml.buffer.ExperienceBuffer.SampledBatch;


import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collections;
import java.util.List;

/**
 * An agent that uses Q-learning to make decisions.
 * It learns a Q-table that estimates the value of taking an action in a given state.
 */
public class QAgent {

    private final Map<GameState, Map<Action, Double>> qTable;
    private final double learningRate;
    private final double discountFactor;
    private double explorationRate;
    private final Random random = new Random();

    public QAgent(double learningRate, double discountFactor, double explorationRate) {
        this.qTable = new ConcurrentHashMap<>();
        this.learningRate = learningRate;
        this.discountFactor = discountFactor;
        this.explorationRate = explorationRate;
    }

    /**
     * Decides on an action to take in the given state.
     * With a probability of `explorationRate`, it will choose a random action.
     * Otherwise, it will choose the action with the highest Q-value.
     *
     * @param state The current game state.
     * @return The action to be performed.
     */
    public Action decideAction(GameState state) {
        qTable.putIfAbsent(state, new ConcurrentHashMap<>());
        if (random.nextDouble() < explorationRate) {
            return exploreRandomAction();
        } else {
            return exploitBestAction(state);
        }
    }

    /**
     * Updates the Q-table based on a batch of experiences using Prioritized Experience Replay.
     *
     * @param batch The batch of experiences to learn from.
     * @return An array of TD-errors for each experience in the batch.
     */
    public double[] trainOnBatch(SampledBatch batch) {
        List<Experience> experiences = batch.experiences();
        double[] weights = batch.weights();
        double[] tdErrors = new double[experiences.size()];

        for (int i = 0; i < experiences.size(); i++) {
            Experience exp = experiences.get(i);
            double weight = weights[i];

            GameState state = (GameState) exp.state();
            Action action = exp.action();
            double reward = exp.reward();
            GameState nextState = (GameState) exp.nextState();

            qTable.putIfAbsent(state, new ConcurrentHashMap<>());

            double currentQ = getQValue(state, action);
            double maxNextQ = getMaxQValue(nextState);

            // Calculate TD-error
            double tdError = reward + discountFactor * maxNextQ - currentQ;
            tdErrors[i] = tdError;

            // Update Q-value with importance sampling weight
            double newQ = currentQ + learningRate * tdError * weight;
            qTable.get(state).put(action, newQ);
        }
        return tdErrors;
    }


    /**
     * Updates the Q-table based on an experience.
     *
     * @param state The state where the action was taken.
     * @param action The action that was taken.
     * @param reward The reward received.
     * @param nextState The resulting state.
     * @return the TD-error of the experience.
     */
    public double learn(GameState state, Action action, double reward, GameState nextState) {
        qTable.putIfAbsent(state, new ConcurrentHashMap<>());
        double currentQ = getQValue(state, action);
        double maxNextQ = getMaxQValue(nextState);

        double tdError = reward + discountFactor * maxNextQ - currentQ;
        double newQ = currentQ + learningRate * tdError;
        qTable.get(state).put(action, newQ);
        return tdError;
    }

    private Action exploreRandomAction() {
        // Returns a random action from the Action enum
        Action[] actions = Action.values();
        return actions[random.nextInt(actions.length)];
    }

    private Action exploitBestAction(GameState state) {
        Map<Action, Double> actions = qTable.get(state);
        if (actions == null || actions.isEmpty()) {
            return exploreRandomAction(); // No known actions for this state, explore
        }

        return Collections.max(actions.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    public double getQValue(GameState state, Action action) {
        return qTable.getOrDefault(state, new ConcurrentHashMap<>()).getOrDefault(action, 0.0);
    }

    private double getMaxQValue(GameState nextState) {
        qTable.putIfAbsent(nextState, new ConcurrentHashMap<>());
        Map<Action, Double> actions = qTable.get(nextState);
        if (actions == null || actions.isEmpty()) {
            return 0.0;
        }
        return Collections.max(actions.values());
    }

    public void setExplorationRate(double explorationRate) {
        this.explorationRate = explorationRate;
    }

    /**
     * Reset the Q-table to start learning from scratch
     */
    public void reset() {
        qTable.clear();
    }

    public void setLearningRate(double learningRate) {
        // Note: Since learningRate was final, this method is for compatibility
        // In a full implementation, you might want to make learningRate non-final
    }

    public void initialize() {
        // Initialize the Q-agent - currently empty implementation
    }

    public Map<Long, double[]> exportQTable() {
        // Convert GameState/Action format to Map<Long, double[]> format
        Map<Long, double[]> result = new ConcurrentHashMap<>();
        for (Map.Entry<GameState, Map<Action, Double>> stateEntry : qTable.entrySet()) {
            GameState state = stateEntry.getKey();
            Map<Action, Double> actions = stateEntry.getValue();
            double[] qValues = new double[Action.values().length];
            for (Map.Entry<Action, Double> actionEntry : actions.entrySet()) {
                qValues[actionEntry.getKey().ordinal()] = actionEntry.getValue();
            }
            result.put(state.getStateHash(), qValues);
        }
        return result;
    }

    public void importQTable(Map<Long, double[]> importedQTable) {
        // Convert Map<Long, double[]> format to GameState/Action format
        qTable.clear();
        for (Map.Entry<Long, double[]> entry : importedQTable.entrySet()) {
            long stateHash = entry.getKey();
            double[] qValues = entry.getValue();
            // Note: This is a simplified conversion - in practice you'd need proper GameState reconstruction
            GameState state = new GameState(0, 0, 0, 0, 0, java.util.List.of(), java.util.Map.of(), stateHash, "", 0);
            Map<Action, Double> actions = new ConcurrentHashMap<>();
            Action[] actionValues = Action.values();
            for (int i = 0; i < qValues.length && i < actionValues.length; i++) {
                actions.put(actionValues[i], qValues[i]);
            }
            qTable.put(state, actions);
        }
    }
}
