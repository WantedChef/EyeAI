package chef.sheesh.eyeAI.core.ml.rl;

import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.core.sim.SimExperience;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Advanced reward model for reinforcement learning agents.
 * Calculates rewards based on multiple factors including combat, movement, and survival.
 */
public final class RewardModel {

    private static final Logger logger = Logger.getLogger(RewardModel.class.getName());

    // Reward weights
    private double combatRewardWeight = 1.0;
    private double movementRewardWeight = 0.5;
    private double survivalRewardWeight = 1.5;
    private double explorationRewardWeight = 0.3;
    private double timePenaltyWeight = -0.01;

    // Reward values
    private double killReward = 10.0;
    private double damageDealtReward = 0.1;
    private double damageTakenPenalty = -0.2;
    private double successfulMovementReward = 0.05;
    private double explorationReward = 0.02;
    private double goalCompletionReward = 5.0;
    private double deathPenalty = -15.0;

    // Tracking state
    private final Map<String, Double> playerLastHealth = new HashMap<>();
    private final Map<String, Location> playerLastLocation = new HashMap<>();
    private final Map<String, Long> playerLastActionTime = new HashMap<>();
    private final Map<String, Integer> exploredChunks = new HashMap<>();

    // Player-specific reward tracking
    private final Map<String, PlayerRewardStats> playerRewardStats = new HashMap<>();
    private boolean detailedLoggingEnabled = true; // Configurable logging

    /**
     * Calculate reward for a fake player based on recent actions
     */
    public double calculateReward(FakePlayer fakePlayer, SimExperience experience) {
        String playerId = fakePlayer.getId().toString();
        String playerName = fakePlayer.getName();

        // Get or create player reward stats
        PlayerRewardStats stats = playerRewardStats.computeIfAbsent(playerId,
            k -> new PlayerRewardStats(playerId, playerName));

        double totalReward = 0.0;
        double combatReward = 0.0;
        double movementReward = 0.0;
        double survivalReward = 0.0;
        double explorationReward = 0.0;
        double timePenalty = 0.0;

        // Combat rewards
        combatReward = calculateCombatReward(fakePlayer, experience) * combatRewardWeight;
        totalReward += combatReward;

        // Movement rewards
        movementReward = calculateMovementReward(fakePlayer, experience) * movementRewardWeight;
        totalReward += movementReward;

        // Survival rewards
        survivalReward = calculateSurvivalReward(fakePlayer, experience) * survivalRewardWeight;
        totalReward += survivalReward;

        // Exploration rewards
        explorationReward = calculateExplorationReward(fakePlayer, experience) * explorationRewardWeight;
        totalReward += explorationReward;

        // Time penalty (encourage efficiency)
        timePenalty = calculateTimePenalty(fakePlayer) * timePenaltyWeight;
        totalReward += timePenalty;

        // Update player reward statistics
        stats.addReward(totalReward, combatReward, movementReward, survivalReward, explorationReward, timePenalty);

        // Log detailed reward information
        if (detailedLoggingEnabled && (stats.rewardCalculations % 100 == 0 || Math.abs(totalReward) > 5.0)) {
            logPlayerRewardDetails(fakePlayer, stats, totalReward, combatReward, movementReward,
                                 survivalReward, explorationReward, timePenalty, experience);
        }

        // Update tracking state
        updatePlayerState(fakePlayer);

        return totalReward;
    }

    /**
     * Log detailed reward information for a player
     */
    private void logPlayerRewardDetails(FakePlayer fakePlayer, PlayerRewardStats stats, double totalReward,
                                       double combatReward, double movementReward, double survivalReward,
                                       double explorationReward, double timePenalty, SimExperience experience) {
        String playerId = fakePlayer.getId().toString();
        String playerName = fakePlayer.getName();

        // Log basic reward information
        logger.info(String.format("[RewardModel] Player %s (%s): Total=%.3f, Count=%d, Avg=%.3f",
                                 playerName, playerId, totalReward, stats.rewardCalculations, stats.averageReward));

        // Log detailed breakdown for significant rewards
        if (Math.abs(totalReward) > 5.0) {
            logger.info(String.format("[RewardModel] Detailed breakdown for %s: Combat=%.3f, Movement=%.3f, " +
                                     "Survival=%.3f, Exploration=%.3f, TimePenalty=%.3f",
                                     playerName, combatReward, movementReward, survivalReward,
                                     explorationReward, timePenalty));

            // Log experience details if available
            if (experience.isQUpdate()) {
                logger.info(String.format("[RewardModel] Experience details for %s: State=%d, Action=%d, " +
                                         "NextState=%d, Terminal=%s",
                                         playerName, experience.getStateHash(), experience.getAction(),
                                         experience.getNextStateHash(), experience.isTerminal()));
            }
        }

        // Log milestone achievements
        if (stats.rewardCalculations % 500 == 0) {
            logger.info(String.format("[RewardModel] Milestone: Player %s reached %d reward calculations. " +
                                     "Lifetime stats: Total=%.2f, Best=%.3f, Worst=%.3f, Avg=%.3f",
                                     playerName, stats.rewardCalculations, stats.totalReward,
                                     stats.highestReward, stats.lowestReward, stats.averageReward));
        }

        // Log performance warnings
        if (stats.averageReward < -1.0 && stats.rewardCalculations > 100) {
            logger.warning(String.format("[RewardModel] Player %s showing poor performance: Avg reward=%.3f " +
                                        "over %d calculations", playerName, stats.averageReward, stats.rewardCalculations));
        }
    }

    /**
     * Get reward statistics for a specific player
     */
    public PlayerRewardStats getPlayerRewardStats(String playerId) {
        return playerRewardStats.get(playerId);
    }

    /**
     * Get reward statistics for a fake player
     */
    public PlayerRewardStats getPlayerRewardStats(FakePlayer fakePlayer) {
        return getPlayerRewardStats(fakePlayer.getId().toString());
    }

    /**
     * Get all player reward statistics
     */
    public Map<String, PlayerRewardStats> getAllPlayerRewardStats() {
        return new HashMap<>(playerRewardStats);
    }

    /**
     * Clear reward statistics for a specific player
     */
    public void clearPlayerRewardStats(String playerId) {
        PlayerRewardStats stats = playerRewardStats.remove(playerId);
        if (stats != null && detailedLoggingEnabled) {
            logger.info(String.format("[RewardModel] Cleared reward statistics for player %s (%s): %s",
                                     stats.playerName, playerId, stats));
        }
    }

    /**
     * Clear all player reward statistics
     */
    public void clearAllPlayerRewardStats() {
        int clearedCount = playerRewardStats.size();
        playerRewardStats.clear();
        if (detailedLoggingEnabled) {
            logger.info(String.format("[RewardModel] Cleared reward statistics for %d players", clearedCount));
        }
    }

    /**
     * Set detailed logging enabled/disabled
     */
    public void setDetailedLoggingEnabled(boolean enabled) {
        this.detailedLoggingEnabled = enabled;
        logger.info(String.format("[RewardModel] Detailed logging %s", enabled ? "enabled" : "disabled"));
    }

    /**
     * Get reward statistics summary
     */
    public RewardStatsSummary getRewardStatsSummary() {
        int totalPlayers = playerRewardStats.size();
        double totalReward = playerRewardStats.values().stream().mapToDouble(s -> s.totalReward).sum();
        double averageReward = totalPlayers > 0 ? totalReward / totalPlayers : 0.0;
        int totalCalculations = playerRewardStats.values().stream().mapToInt(s -> s.rewardCalculations).sum();

        double bestAverageReward = playerRewardStats.values().stream()
                .mapToDouble(s -> s.averageReward)
                .max().orElse(0.0);
        double worstAverageReward = playerRewardStats.values().stream()
                .mapToDouble(s -> s.averageReward)
                .min().orElse(0.0);

        return new RewardStatsSummary(totalPlayers, totalReward, averageReward, totalCalculations,
                                    bestAverageReward, worstAverageReward);
    }

    /**
     * Summary of reward statistics across all players
     */
    public static class RewardStatsSummary {
        public final int totalPlayers;
        public final double totalReward;
        public final double averageReward;
        public final int totalCalculations;
        public final double bestAverageReward;
        public final double worstAverageReward;

        public RewardStatsSummary(int totalPlayers, double totalReward, double averageReward,
                                int totalCalculations, double bestAverageReward, double worstAverageReward) {
            this.totalPlayers = totalPlayers;
            this.totalReward = totalReward;
            this.averageReward = averageReward;
            this.totalCalculations = totalCalculations;
            this.bestAverageReward = bestAverageReward;
            this.worstAverageReward = worstAverageReward;
        }

        @Override
        public String toString() {
            return String.format("RewardStatsSummary{players=%d, totalReward=%.2f, avgReward=%.3f, " +
                               "calculations=%d, bestAvg=%.3f, worstAvg=%.3f}",
                               totalPlayers, totalReward, averageReward, totalCalculations,
                               bestAverageReward, worstAverageReward);
        }
    }

    /**
     * Calculate reward for combat actions
     */
    private double calculateCombatReward(FakePlayer fakePlayer, SimExperience experience) {
        double reward = 0.0;

        // Check if this was a combat action
        if (experience.isQUpdate() && experience.getAction() < 5) { // Assuming actions 0-4 are combat
            double currentHealth = fakePlayer.getHealth();
            double lastHealth = playerLastHealth.getOrDefault(fakePlayer.getId().toString(), currentHealth);

            // Reward for dealing damage (health decrease indicates successful attack)
            if (currentHealth > lastHealth) {
                reward += damageDealtReward * (currentHealth - lastHealth);
            }

            // Penalty for taking damage
            if (currentHealth < lastHealth) {
                reward += damageTakenPenalty * (lastHealth - currentHealth);
            }

            // Large reward for kills (if health went to 0)
            if (currentHealth <= 0 && lastHealth > 0) {
                reward += killReward;
            }
        }

        return reward;
    }

    /**
     * Calculate reward for movement actions
     */
    private double calculateMovementReward(FakePlayer fakePlayer, SimExperience experience) {
        double reward = 0.0;

        Location currentLocation = fakePlayer.getLocation();
        Location lastLocation = playerLastLocation.get(fakePlayer.getId().toString());

        if (lastLocation != null) {
            double distanceMoved = currentLocation.distance(lastLocation);

            // Reward for successful movement
            if (distanceMoved > 0.1) { // Moved more than 10cm
                reward += successfulMovementReward;

                // Bonus for moving towards targets or goals
                if (hasNearbyTargets(fakePlayer)) {
                    reward += successfulMovementReward * 0.5; // Bonus for moving towards targets
                }
            }
        }

        return reward;
    }

    /**
     * Calculate reward for survival
     */
    private double calculateSurvivalReward(FakePlayer fakePlayer, SimExperience experience) {
        double reward = 0.0;

        // Reward for staying alive
        if (fakePlayer.isAlive()) {
            reward += 0.01; // Small reward for each timestep alive
        } else {
            reward += deathPenalty;
        }

        // Reward for maintaining health
        double currentHealth = fakePlayer.getHealth();
        double maxHealth = fakePlayer.getMaxHealth();
        double healthRatio = currentHealth / maxHealth;

        // Bonus for high health
        if (healthRatio > 0.8) {
            reward += 0.02;
        } else if (healthRatio < 0.3) {
            reward -= 0.05; // Penalty for low health
        }

        return reward;
    }

    /**
     * Calculate reward for exploration
     */
    private double calculateExplorationReward(FakePlayer fakePlayer, SimExperience experience) {
        double reward = 0.0;

        Location currentLocation = fakePlayer.getLocation();
        int chunkX = currentLocation.getBlockX() >> 4;
        int chunkZ = currentLocation.getBlockZ() >> 4;
        String chunkKey = chunkX + "," + chunkZ;

        String playerId = fakePlayer.getId().toString();
        int exploredCount = exploredChunks.getOrDefault(playerId, 0);

        // Reward for exploring new chunks
        if (!exploredChunks.containsKey(chunkKey + "_" + playerId)) {
            exploredChunks.put(chunkKey + "_" + playerId, exploredCount + 1);
            reward += explorationReward;
        }

        return reward;
    }

    /**
     * Calculate time penalty to encourage efficiency
     */
    private double calculateTimePenalty(FakePlayer fakePlayer) {
        String playerId = fakePlayer.getId().toString();
        long currentTime = System.currentTimeMillis();
        long lastActionTime = playerLastActionTime.getOrDefault(playerId, currentTime);

        // Small penalty for taking too long (encourage decisive action)
        long timeSinceLastAction = currentTime - lastActionTime;
        if (timeSinceLastAction > 5000) { // More than 5 seconds
            return -0.01;
        }

        return 0.0;
    }

    /**
     * Check if there are nearby targets
     */
    private boolean hasNearbyTargets(FakePlayer fakePlayer) {
        Location location = fakePlayer.getLocation();

        // Check for nearby players (potential targets)
        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distance(location) < 20.0 &&
                !player.getUniqueId().equals(fakePlayer.getId())) {
                return true;
            }
        }

        // Check for nearby entities (potential targets)
        for (Entity entity : location.getWorld().getNearbyEntities(location, 10, 10, 10)) {
            if (entity instanceof Player || entity instanceof org.bukkit.entity.Monster) {
                return true;
            }
        }

        return false;
    }

    /**
     * Update player state for next reward calculation
     */
    private void updatePlayerState(FakePlayer fakePlayer) {
        String playerId = fakePlayer.getId().toString();

        playerLastHealth.put(playerId, fakePlayer.getHealth());
        playerLastLocation.put(playerId, fakePlayer.getLocation().clone());
        playerLastActionTime.put(playerId, System.currentTimeMillis());
    }

    /**
     * Calculate goal-specific rewards
     */
    public double calculateGoalReward(FakePlayer fakePlayer, String goalType, boolean goalCompleted) {
        if (goalCompleted) {
            switch (goalType.toLowerCase()) {
                case "combat":
                    return goalCompletionReward * 2.0;
                case "exploration":
                    return goalCompletionReward * 1.5;
                case "survival":
                    return goalCompletionReward * 2.5;
                case "movement":
                    return goalCompletionReward * 1.0;
                default:
                    return goalCompletionReward;
            }
        }
        return 0.0;
    }

    /**
     * Calculate reward based on state transition
     */
    public double calculateStateTransitionReward(long oldState, long newState, int action) {
        // Simple state transition reward based on state hashes
        // This can be extended with more sophisticated state analysis

        double reward = 0.0;

        // Reward for state changes (indicates action had effect)
        if (oldState != newState) {
            reward += 0.001;
        }

        // Action-specific rewards
        switch (action) {
            case 0: // Attack action
                reward += 0.01;
                break;
            case 1: // Move action
                reward += 0.005;
                break;
            case 2: // Heal/use item action
                reward += 0.02;
                break;
            case 3: // Flee action
                reward -= 0.01; // Slight penalty for fleeing
                break;
        }

        return reward;
    }

    // Configuration methods

    public void setCombatRewardWeight(double weight) {
        this.combatRewardWeight = weight;
    }

    public void setMovementRewardWeight(double weight) {
        this.movementRewardWeight = weight;
    }

    public void setSurvivalRewardWeight(double weight) {
        this.survivalRewardWeight = weight;
    }

    public void setExplorationRewardWeight(double weight) {
        this.explorationRewardWeight = weight;
    }

    public void setKillReward(double reward) {
        this.killReward = reward;
    }

    public void setDamageDealtReward(double reward) {
        this.damageDealtReward = reward;
    }

    public void setDamageTakenPenalty(double penalty) {
        this.damageTakenPenalty = penalty;
    }

    public void setDeathPenalty(double penalty) {
        this.deathPenalty = penalty;
    }

    public void setGoalCompletionReward(double reward) {
        this.goalCompletionReward = reward;
    }

    /**
     * Reset all tracking state
     */
    public void reset() {
        playerLastHealth.clear();
        playerLastLocation.clear();
        playerLastActionTime.clear();
        exploredChunks.clear();
    }

    /**
     * Get reward statistics
     */
    public RewardStats getStatistics() {
        return new RewardStats(
            exploredChunks.size(),
            playerLastHealth.size(),
            combatRewardWeight,
            movementRewardWeight,
            survivalRewardWeight,
            explorationRewardWeight
        );
    }

    /**
     * Statistics for reward model
     */
    public static class RewardStats {
        public final int totalExploredChunks;
        public final int activePlayers;
        public final double combatWeight;
        public final double movementWeight;
        public final double survivalWeight;
        public final double explorationWeight;

        public RewardStats(int totalExploredChunks, int activePlayers, double combatWeight,
                          double movementWeight, double survivalWeight, double explorationWeight) {
            this.totalExploredChunks = totalExploredChunks;
            this.activePlayers = activePlayers;
            this.combatWeight = combatWeight;
            this.movementWeight = movementWeight;
            this.survivalWeight = survivalWeight;
            this.explorationWeight = explorationWeight;
        }

        @Override
        public String toString() {
            return String.format("RewardStats{explored=%d, players=%d, weights=[%.2f, %.2f, %.2f, %.2f]}",
                               totalExploredChunks, activePlayers, combatWeight, movementWeight,
                               survivalWeight, explorationWeight);
        }
    }

    /**
     * Tracks detailed reward statistics for individual players
     */
    public static class PlayerRewardStats {
        public final String playerId;
        public final String playerName;
        public double totalReward = 0.0;
        public double combatReward = 0.0;
        public double movementReward = 0.0;
        public double survivalReward = 0.0;
        public double explorationReward = 0.0;
        public double timePenalty = 0.0;
        public int rewardCalculations = 0;
        public long lastRewardTime = System.currentTimeMillis();
        public double averageReward = 0.0;
        public double highestReward = Double.NEGATIVE_INFINITY;
        public double lowestReward = Double.POSITIVE_INFINITY;

        public PlayerRewardStats(String playerId, String playerName) {
            this.playerId = playerId;
            this.playerName = playerName;
        }

        public void addReward(double totalReward, double combatReward, double movementReward,
                            double survivalReward, double explorationReward, double timePenalty) {
            this.totalReward += totalReward;
            this.combatReward += combatReward;
            this.movementReward += movementReward;
            this.survivalReward += survivalReward;
            this.explorationReward += explorationReward;
            this.timePenalty += timePenalty;
            this.rewardCalculations++;

            // Update statistics
            this.averageReward = this.totalReward / this.rewardCalculations;
            this.highestReward = Math.max(this.highestReward, totalReward);
            this.lowestReward = Math.min(this.lowestReward, totalReward);
            this.lastRewardTime = System.currentTimeMillis();
        }

        @Override
        public String toString() {
            return String.format("PlayerRewardStats{id=%s, name=%s, total=%.2f, avg=%.3f, count=%d, " +
                               "combat=%.2f, movement=%.2f, survival=%.2f, exploration=%.2f}",
                               playerId, playerName, totalReward, averageReward, rewardCalculations,
                               combatReward, movementReward, survivalReward, explorationReward);
        }
    }
}
