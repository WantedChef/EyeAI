package chef.sheesh.eyeAI.core.ml.rewards;

import chef.sheesh.eyeAI.core.ml.models.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A system for calculating rewards for AI agent actions.
 * This class provides a centralized way to define the reward structure for the learning process.
 */
public class RewardSystem {
    private static final Logger log = LoggerFactory.getLogger(RewardSystem.class);

    // Combat Rewards
    private static final double DAMAGE_DEALT_REWARD = 10.0;
    private static final double DAMAGE_RECEIVED_PENALTY = -15.0;
    private static final double KILL_REWARD = 100.0;
    private static final double DEATH_PENALTY = -50.0;
    private static final double CRITICAL_HIT_BONUS = 25.0;
    private static final double KILL_STREAK_BONUS = 5.0; // Per kill in streak
    private static final double COMBO_MULTIPLIER = 1.5; // Reward multiplier for combos

    // Movement Rewards
    private static final double EFFICIENT_MOVEMENT_REWARD = 1.0; // Per second
    private static final double STUCK_PENALTY = -5.0;
    private static final double EXPLORATION_REWARD = 20.0;
    private static final double PATH_OPTIMIZATION_BONUS = 15.0;
    private static final double AVOID_DANGER_REWARD = 30.0;

    // Social Rewards
    private static final double HELP_PLAYER_REWARD = 25.0;
    private static final double HINDER_PLAYER_PENALTY = -25.0;
    private static final double TEAMWORK_REWARD = 15.0;
    private static final double SOCIAL_INFLUENCE_BONUS = 10.0; // Based on relationship strength
    private static final double GROUP_LEADERSHIP_BONUS = 20.0;

    // Resource & Economy Rewards
    private static final double RESOURCE_COLLECTION_REWARD = 5.0;
    private static final double ECONOMY_TRANSACTION_REWARD = 3.0;
    private static final double INVENTORY_OPTIMIZATION_BONUS = 8.0;

    // Exploration & Learning Rewards
    private static final double NEW_BLOCK_TYPE_REWARD = 10.0;
    private static final double NEW_ENTITY_TYPE_REWARD = 15.0;
    private static final double SKILL_IMPROVEMENT_BONUS = 50.0; // When agent improves at a task

    /**
     * Calculates the reward for a combat-related event.
     *
     * @param damageDealt The amount of damage dealt by the agent.
     * @param damageReceived The amount of damage received by the agent.
     * @param madeKill True if the agent killed an entity.
     * @param died True if the agent died.
     * @param criticalHit True if the agent landed a critical hit.
     * @param killStreak Current kill streak of the agent.
     * @param comboExecuted True if the agent executed a combo.
     * @return The calculated combat reward.
     */
    public static double calculateCombatReward(double damageDealt, double damageReceived, boolean madeKill, boolean died,
                                              boolean criticalHit, int killStreak, boolean comboExecuted) {
        double reward = 0.0;
        reward += damageDealt * DAMAGE_DEALT_REWARD;
        reward += damageReceived * DAMAGE_RECEIVED_PENALTY;
        
        if (madeKill) {
            reward += KILL_REWARD;
            log.debug("Kill reward added: {}", KILL_REWARD);
        }
        
        if (died) {
            reward += DEATH_PENALTY;
            log.debug("Death penalty applied: {}", DEATH_PENALTY);
        }
        
        if (criticalHit) {
            reward += CRITICAL_HIT_BONUS;
            log.debug("Critical hit bonus added: {}", CRITICAL_HIT_BONUS);
        }
        
        if (killStreak > 1) {
            double streakBonus = killStreak * KILL_STREAK_BONUS;
            reward += streakBonus;
            log.debug("Kill streak bonus added: {}", streakBonus);
        }
        
        if (comboExecuted) {
            reward *= COMBO_MULTIPLIER;
            log.debug("Combo multiplier applied: {}x", COMBO_MULTIPLIER);
        }
        
        log.debug("Total combat reward calculated: {}", reward);
        return reward;
    }

    /**
     * Calculates the reward for movement.
     *
     * @param isStuck True if the agent is stuck.
     * @param newAreaDiscovered True if the agent discovered a new area.
     * @param secondsOfMovement Seconds of efficient movement.
     * @param pathOptimized True if the agent found a more optimal path.
     * @param dangerAvoided True if the agent avoided danger.
     * @return The calculated movement reward.
     */
    public static double calculateMovementReward(boolean isStuck, boolean newAreaDiscovered, double secondsOfMovement,
                                                boolean pathOptimized, boolean dangerAvoided) {
        double reward = 0.0;
        
        if (isStuck) {
            reward += STUCK_PENALTY;
            log.debug("Stuck penalty applied: {}", STUCK_PENALTY);
        }
        
        if (newAreaDiscovered) {
            reward += EXPLORATION_REWARD;
            log.debug("Exploration reward added: {}", EXPLORATION_REWARD);
        }
        
        reward += secondsOfMovement * EFFICIENT_MOVEMENT_REWARD;
        log.debug("Efficient movement reward added: {}", secondsOfMovement * EFFICIENT_MOVEMENT_REWARD);
        
        if (pathOptimized) {
            reward += PATH_OPTIMIZATION_BONUS;
            log.debug("Path optimization bonus added: {}", PATH_OPTIMIZATION_BONUS);
        }
        
        if (dangerAvoided) {
            reward += AVOID_DANGER_REWARD;
            log.debug("Danger avoidance reward added: {}", AVOID_DANGER_REWARD);
        }
        
        log.debug("Total movement reward calculated: {}", reward);
        return reward;
    }

    /**
     * Calculates the reward for social interactions.
     *
     * @param helpedPlayer True if the agent helped a player.
     * @param hinderedPlayer True if the agent hindered a player.
     * @param successfulTeamAction True if a team action was successful.
     * @param socialInfluence Social influence factor from relationship matrix.
     * @param isLeader True if the agent is leading a group.
     * @return The calculated social reward.
     */
    public static double calculateSocialReward(boolean helpedPlayer, boolean hinderedPlayer, boolean successfulTeamAction,
                                              double socialInfluence, boolean isLeader) {
        double reward = 0.0;
        
        if (helpedPlayer) {
            reward += HELP_PLAYER_REWARD;
            log.debug("Help player reward added: {}", HELP_PLAYER_REWARD);
        }
        
        if (hinderedPlayer) {
            reward += HINDER_PLAYER_PENALTY;
            log.debug("Hinder player penalty applied: {}", HINDER_PLAYER_PENALTY);
        }
        
        if (successfulTeamAction) {
            reward += TEAMWORK_REWARD;
            log.debug("Teamwork reward added: {}", TEAMWORK_REWARD);
        }
        
        // Social influence bonus/penalty based on relationship strength
        double influenceReward = socialInfluence * SOCIAL_INFLUENCE_BONUS;
        reward += influenceReward;
        log.debug("Social influence reward added: {}", influenceReward);
        
        if (isLeader) {
            reward += GROUP_LEADERSHIP_BONUS;
            log.debug("Group leadership bonus added: {}", GROUP_LEADERSHIP_BONUS);
        }
        
        log.debug("Total social reward calculated: {}", reward);
        return reward;
    }

    /**
     * Calculates reward for resource collection and economy activities.
     *
     * @param resourcesCollected Number of resources collected.
     * @param transactionsCompleted Number of economy transactions completed.
     * @param inventoryOptimized True if inventory was optimized.
     * @return The calculated resource/economy reward.
     */
    public static double calculateResourceReward(int resourcesCollected, int transactionsCompleted, boolean inventoryOptimized) {
        double reward = 0.0;
        
        reward += resourcesCollected * RESOURCE_COLLECTION_REWARD;
        log.debug("Resource collection reward added: {}", resourcesCollected * RESOURCE_COLLECTION_REWARD);
        
        reward += transactionsCompleted * ECONOMY_TRANSACTION_REWARD;
        log.debug("Economy transaction reward added: {}", transactionsCompleted * ECONOMY_TRANSACTION_REWARD);
        
        if (inventoryOptimized) {
            reward += INVENTORY_OPTIMIZATION_BONUS;
            log.debug("Inventory optimization bonus added: {}", INVENTORY_OPTIMIZATION_BONUS);
        }
        
        log.debug("Total resource reward calculated: {}", reward);
        return reward;
    }

    /**
     * Calculates reward for exploration and learning activities.
     *
     * @param newBlockTypes Number of new block types discovered.
     * @param newEntityTypes Number of new entity types encountered.
     * @param skillImproved True if agent's skill level improved.
     * @return The calculated exploration/learning reward.
     */
    public static double calculateExplorationReward(int newBlockTypes, int newEntityTypes, boolean skillImproved) {
        double reward = 0.0;
        
        reward += newBlockTypes * NEW_BLOCK_TYPE_REWARD;
        log.debug("New block type reward added: {}", newBlockTypes * NEW_BLOCK_TYPE_REWARD);
        
        reward += newEntityTypes * NEW_ENTITY_TYPE_REWARD;
        log.debug("New entity type reward added: {}", newEntityTypes * NEW_ENTITY_TYPE_REWARD);
        
        if (skillImproved) {
            reward += SKILL_IMPROVEMENT_BONUS;
            log.debug("Skill improvement bonus added: {}", SKILL_IMPROVEMENT_BONUS);
        }
        
        log.debug("Total exploration reward calculated: {}", reward);
        return reward;
    }
}
