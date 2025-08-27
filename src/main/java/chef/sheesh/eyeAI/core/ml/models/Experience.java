package chef.sheesh.eyeAI.core.ml.models;

/**
 * Represents a single experience tuple (State, Action, Reward, NextState, Done).
 * This is used to store experiences for reinforcement learning.
 *
 * @param state The state in which the action was taken.
 * @param action The action that was taken.
 * @param reward The reward received for taking the action.
 * @param nextState The resulting state after the action was taken.
 * @param done Whether the episode terminated at this step.
 */
public record Experience(IState state, Action action, double reward, IState nextState, boolean done) {
}
