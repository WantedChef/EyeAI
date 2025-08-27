package chef.sheesh.eyeAI.core.ml.rewards;

import chef.sheesh.eyeAI.core.ml.models.Action;

/**
 * A system for calculating rewards for AI agent actions.
 * This class provides a centralized way to define the reward structure for the learning process.
 */
public class RewardSystem {

    // Combat Rewards
    private static final double DAMAGE_DEALT_REWARD = 10.0;
    private static final double DAMAGE_RECEIVED_PENALTY = -15.0;
    private static final double KILL_REWARD = 100.0;
    private static final double DEATH_PENALTY = -50.0;

    // Movement Rewards
    private static final double EFFICIENT_MOVEMENT_REWARD = 1.0; // Per second
    private static final double STUCK_PENALTY = -5.0;
    private static final double EXPLORATION_REWARD = 20.0;

    // Social Rewards
    private static final double HELP_PLAYER_REWARD = 25.0;
    private static final double HINDER_PLAYER_PENALTY = -25.0;
    private static final double TEAMWORK_REWARD = 15.0;

    /**
     * Calculates the reward for a combat-related event.
     *
     * @param damageDealt The amount of damage dealt by the agent.
     * @param damageReceived The amount of damage received by the agent.
     * @param madeKill True if the agent killed an entity.
     * @param died True if the agent died.
     * @return The calculated combat reward.
     */
    public static double calculateCombatReward(double damageDealt, double damageReceived, boolean madeKill, boolean died) {
        double reward = 0.0;
        reward += damageDealt * DAMAGE_DEALT_REWARD;
        reward += damageReceived * DAMAGE_RECEIVED_PENALTY;
        if (madeKill) {
            reward += KILL_REWARD;
        }
        if (died) {
            reward += DEATH_PENALTY;
        }
        return reward;
    }

    /**
     * Calculates the reward for movement.
     *
     * @param isStuck True if the agent is stuck.
     * @param newAreaDiscovered True if the agent discovered a new area.
     * @param secondsOfMovement Seconds of efficient movement.
     * @return The calculated movement reward.
     */
    public static double calculateMovementReward(boolean isStuck, boolean newAreaDiscovered, double secondsOfMovement) {
        double reward = 0.0;
        if (isStuck) {
            reward += STUCK_PENALTY;
        }
        if (newAreaDiscovered) {
            reward += EXPLORATION_REWARD;
        }
        reward += secondsOfMovement * EFFICIENT_MOVEMENT_REWARD;
        return reward;
    }

    /**
     * Calculates the reward for social interactions.
     *
     * @param helpedPlayer True if the agent helped a player.
     * @param hinderedPlayer True if the agent hindered a player.
     * @param successfulTeamAction True if a team action was successful.
     * @return The calculated social reward.
     */
    public static double calculateSocialReward(boolean helpedPlayer, boolean hinderedPlayer, boolean successfulTeamAction) {
        double reward = 0.0;
        if (helpedPlayer) {
            reward += HELP_PLAYER_REWARD;
        }
        if (hinderedPlayer) {
            reward += HINDER_PLAYER_PENALTY;
        }
        if (successfulTeamAction) {
            reward += TEAMWORK_REWARD;
        }
        return reward;
    }
}
