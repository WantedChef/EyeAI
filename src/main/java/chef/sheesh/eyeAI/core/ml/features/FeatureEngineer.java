package chef.sheesh.eyeAI.core.ml.features;

import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.ai.core.DecisionContext;
import chef.sheesh.eyeAI.core.sim.SimExperience;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Feature engineering for ML models.
 * Extracts relevant features from game state for better ML performance.
 */
public final class FeatureEngineer {

    // Feature dimensions
    public static final int MOVEMENT_FEATURES = 12;
    public static final int COMBAT_FEATURES = 15;
    public static final int ENVIRONMENT_FEATURES = 10;
    public static final int SOCIAL_FEATURES = 8;

    /**
     * Extract movement features from a fake player
     */
    public static double[] extractMovementFeatures(FakePlayer fakePlayer) {
        DecisionContext ctx = fakePlayer.createDecisionContext();
        return extractMovementFeatures(ctx, fakePlayer);
    }

    /**
     * Extract movement features from a DecisionContext
     */
    public static double[] extractMovementFeatures(DecisionContext ctx, FakePlayer fakePlayer) {
        double[] features = new double[MOVEMENT_FEATURES];
        Location location = ctx.getCurrentLocation();

        // Position features (normalized)
        features[0] = location.getX() / 10000.0; // World-relative position
        features[1] = location.getY() / 256.0;   // Height (0-1)
        features[2] = location.getZ() / 10000.0; // World-relative position

        // Velocity features (derived from recent movement)
        double[] velocity = calculateVelocity(fakePlayer);
        features[3] = velocity[0] / 10.0; // X velocity (normalized)
        features[4] = velocity[1] / 10.0; // Y velocity
        features[5] = velocity[2] / 10.0; // Z velocity

        // Orientation features
        features[6] = location.getYaw() / 360.0;   // Horizontal rotation (0-1)
        features[7] = location.getPitch() / 180.0; // Vertical rotation (-1 to 1, normalized to 0-1)

        // Movement capability features
        features[8] = fakePlayer.isAlive() ? 1.0 : 0.0;                           // Can move
        features[9] = fakePlayer.getHealth() / fakePlayer.getMaxHealth();         // Health ratio
        features[10] = ctx.isOnGround() ? 1.0 : 0.0;                              // Grounded state
        features[11] = ctx.getObstacleDensity() / 10.0;                           // Obstacle density (normalized)

        return features;
    }

    /**
     * Extract combat features
     */
    public static double[] extractCombatFeatures(FakePlayer fakePlayer) {
        DecisionContext ctx = fakePlayer.createDecisionContext();
        return extractCombatFeatures(ctx, fakePlayer);
    }

    public static double[] extractCombatFeatures(DecisionContext ctx, FakePlayer fakePlayer) {
        double[] features = new double[COMBAT_FEATURES];

        // Health and damage features
        features[0] = fakePlayer.getHealth() / fakePlayer.getMaxHealth(); // Health ratio
        features[1] = calculateHealthTrend(fakePlayer);                   // Health change rate
        features[2] = calculateDamageTaken(fakePlayer);                   // Recent damage
        features[3] = calculateDamageDealt(fakePlayer);                   // Recent damage dealt

        // Target features using precomputed nearby entities
        Location playerLoc = ctx.getCurrentLocation();
        List<Entity> nearby = ctx.getNearbyEntities();
        long within20 = nearby.stream()
                .filter(e -> playerLoc.distance(e.getLocation()) <= 20.0)
                .count();
        features[4] = within20 / 10.0; // Nearby entity count (normalized)

        // Find nearest hostile
        Entity nearestHostile = null;
        double minDist = Double.MAX_VALUE;
        for (Entity e : nearby) {
            double d = playerLoc.distance(e.getLocation());
            if (d <= 50.0 && isHostile(e)) {
                if (d < minDist) {
                    minDist = d;
                    nearestHostile = e;
                }
            }
        }

        if (nearestHostile != null) {
            features[5] = minDist / 50.0;                 // Distance to nearest hostile
            features[6] = calculateThreatLevel(nearestHostile); // Threat assessment
            features[7] = isInLineOfSight(fakePlayer, nearestHostile) ? 1.0 : 0.0; // Line of sight (placeholder)
        } else {
            features[5] = 1.0; // No hostile nearby
            features[6] = 0.0; // No threat
            features[7] = 0.0; // No line of sight
        }

        // Weapon/item features
        features[8] = hasWeapon(fakePlayer) ? 1.0 : 0.0;        // Has weapon
        features[9] = calculateWeaponEffectiveness(fakePlayer); // Weapon power
        features[10] = hasArmor(fakePlayer) ? 1.0 : 0.0;        // Has armor
        features[11] = calculateArmorRating(fakePlayer);        // Armor effectiveness

        // Combat state features
        features[12] = isInCombat(fakePlayer) ? 1.0 : 0.0;      // Currently in combat
        features[13] = calculateCombatDuration(fakePlayer);     // How long in combat
        features[14] = calculateRetreatUrgency(fakePlayer);     // Need to retreat

        return features;
    }

    /**
     * Extract environmental features
     */
    public static double[] extractEnvironmentFeatures(FakePlayer fakePlayer) {
        DecisionContext ctx = fakePlayer.createDecisionContext();
        return extractEnvironmentFeatures(ctx);
    }

    public static double[] extractEnvironmentFeatures(DecisionContext ctx) {
        double[] features = new double[ENVIRONMENT_FEATURES];
        long time = ctx.getWorldTime();

        // Time features
        features[0] = (time % 24000L) / 24000.0; // Day/night cycle (0-1)
        features[1] = isNightTime(time) ? 1.0 : 0.0; // Is night
        features[2] = ctx.getLightLevel(); // Light level (0-1)

        // Weather features
        features[3] = ctx.isThundering() ? 1.0 : 0.0; // Thunderstorm
        features[4] = ctx.hasStorm() ? 1.0 : 0.0;     // Rain/storm

        // Terrain features (already computed on main thread)
        features[5] = ctx.getTerrainDifficulty();     // Terrain navigability
        features[6] = ctx.isNearWater() ? 1.0 : 0.0;  // Near water
        features[7] = ctx.isNearLava() ? 1.0 : 0.0;   // Near lava (danger)
        features[8] = ctx.getElevation() / 256.0;     // Height advantage (normalized)
        features[9] = ctx.getCoverDensity();          // Available cover (precomputed)

        return features;
    }

    /**
     * Extract social features (interaction with other players/entities)
     */
    public static double[] extractSocialFeatures(FakePlayer fakePlayer) {
        DecisionContext ctx = fakePlayer.createDecisionContext();
        return extractSocialFeatures(ctx, fakePlayer);
    }

    public static double[] extractSocialFeatures(DecisionContext ctx, FakePlayer fakePlayer) {
        double[] features = new double[SOCIAL_FEATURES];

        // Player interaction features via precomputed players
        Location playerLoc = ctx.getCurrentLocation();
        List<Player> players = ctx.getNearbyPlayers();
        long nearCount = players.stream()
                .filter(p -> playerLoc.distance(p.getLocation()) <= 50.0)
                .count();
        features[0] = nearCount / 10.0; // Nearby player count

        // Find nearest player
        Player nearestPlayer = null;
        double minDist = Double.MAX_VALUE;
        for (Player p : players) {
            double d = playerLoc.distance(p.getLocation());
            if (d < minDist) {
                minDist = d;
                nearestPlayer = p;
            }
        }

        if (nearestPlayer != null) {
            features[1] = calculateRelationship(fakePlayer, nearestPlayer); // Relationship score
            features[2] = calculateInteractionHistory(fakePlayer, nearestPlayer); // Past interactions
        } else {
            features[1] = 0.5; // Neutral
            features[2] = 0.0; // No history
        }

        // Group features
        features[3] = isInGroup(fakePlayer) ? 1.0 : 0.0;        // Part of group
        features[4] = calculateGroupSize(fakePlayer);           // Group size
        features[5] = calculateGroupCohesion(fakePlayer);       // Group unity

        // Social cues
        features[6] = calculateNearbyHelpSignals(fakePlayer);   // Help requests
        features[7] = calculateNearbyThreatSignals(fakePlayer); // Warning signals

        return features;
    }

    /**
     * Create comprehensive feature vector combining all feature types
     */
    public static double[] createComprehensiveFeatures(FakePlayer fakePlayer) {
        DecisionContext ctx = fakePlayer.createDecisionContext();
        double[] movement = extractMovementFeatures(ctx, fakePlayer);
        double[] combat = extractCombatFeatures(ctx, fakePlayer);
        double[] environment = extractEnvironmentFeatures(ctx);
        double[] social = extractSocialFeatures(ctx, fakePlayer);

        // Combine all features
        double[] comprehensive = new double[
                MOVEMENT_FEATURES + COMBAT_FEATURES + ENVIRONMENT_FEATURES + SOCIAL_FEATURES
            ];

        System.arraycopy(movement, 0, comprehensive, 0, MOVEMENT_FEATURES);
        System.arraycopy(combat, 0, comprehensive, MOVEMENT_FEATURES, COMBAT_FEATURES);
        System.arraycopy(environment, 0, comprehensive, MOVEMENT_FEATURES + COMBAT_FEATURES, ENVIRONMENT_FEATURES);
        System.arraycopy(social, 0, comprehensive, MOVEMENT_FEATURES + COMBAT_FEATURES + ENVIRONMENT_FEATURES, SOCIAL_FEATURES);

        return comprehensive;
    }

    /**
     * Convert SimExperience to feature vector
     */
    public static double[] experienceToFeatures(SimExperience experience) {
        // Extract features from experience data
        double[] features = new double[MOVEMENT_FEATURES];

        // Basic state features
        long stateHash = experience.getStateHash();
        features[0] = (stateHash % 1000) / 1000.0;        // Position X (derived)
        features[1] = ((stateHash / 1000) % 1000) / 1000.0; // Position Y
        features[2] = ((stateHash / 1000000) % 1000) / 1000.0; // Position Z

        // Action features
        features[3] = experience.getAction() / 10.0;     // Action type
        features[4] = experience.getReward() / 100.0;    // Reward signal
        features[5] = 0.5;                               // Placeholder

        // Additional derived features
        features[6] = experience.isQUpdate() ? 1.0 : 0.0; // Is Q-learning update
        features[7] = experience.hasSequence() ? 1.0 : 0.0; // Has sequence data
        features[8] = 1.0;                                // Valid experience
        features[9] = 0.0;                                // Placeholder
        features[10] = 0.0;                               // Placeholder
        features[11] = 0.0;                               // Placeholder

        return features;
    }

    /**
     * Normalize feature vector
     */
    public static double[] normalizeFeatures(double[] features) {
        double[] normalized = new double[features.length];

        // Z-score normalization (simplified)
        double mean = Arrays.stream(features).average().orElse(0.0);
        double std = Math.sqrt(Arrays.stream(features)
            .map(x -> Math.pow(x - mean, 2))
            .average().orElse(1.0));

        if (std == 0.0) {
            std = 1.0; // Avoid division by zero
        }

        for (int i = 0; i < features.length; i++) {
            normalized[i] = (features[i] - mean) / std;
        }

        return normalized;
    }

    /**
     * Select important features using simple feature selection
     */
    public static double[] selectFeatures(double[] features, int maxFeatures) {
        if (features.length <= maxFeatures) {
            return features.clone();
        }

        // Simple feature selection based on variance
        double[] variances = new double[features.length];
        double mean = Arrays.stream(features).average().orElse(0.0);

        for (int i = 0; i < features.length; i++) {
            variances[i] = Math.pow(features[i] - mean, 2);
        }

        // Select features with highest variance
        Integer[] indices = new Integer[features.length];
        for (int i = 0; i < features.length; i++) {
            indices[i] = i;
        }

        Arrays.sort(indices, Comparator.comparingDouble((Integer i) -> variances[i]).reversed());

        double[] selected = new double[maxFeatures];
        for (int i = 0; i < maxFeatures; i++) {
            selected[i] = features[indices[i]];
        }

        return selected;
    }

    // Private helper methods (implementations would be specific to your game mechanics)

    private static double[] calculateVelocity(FakePlayer fakePlayer) {
        // Implementation depends on your movement tracking
        return new double[]{0.0, 0.0, 0.0}; // Placeholder
    }

    private static double calculateHealthTrend(FakePlayer fakePlayer) {
        // Implementation would track health over time
        return 0.0; // Placeholder
    }

    private static double calculateDamageTaken(FakePlayer fakePlayer) {
        // Implementation would track recent damage
        return 0.0; // Placeholder
    }

    private static double calculateDamageDealt(FakePlayer fakePlayer) {
        // Implementation would track recent damage dealt
        return 0.0; // Placeholder
    }

    private static boolean isHostile(Entity entity) {
        // Implementation depends on your game's definition of hostile entities
        return entity instanceof org.bukkit.entity.Monster ||
               (entity instanceof Player && isPlayerHostile((Player) entity));
    }

    private static boolean isPlayerHostile(Player player) {
        // Implementation depends on your PvP system
        return false; // Placeholder
    }

    private static double calculateThreatLevel(Entity entity) {
        // Implementation depends on entity type and attributes
        return 0.5; // Placeholder
    }

    private static boolean isInLineOfSight(FakePlayer fakePlayer, Entity entity) {
        // Implementation would use ray tracing
        return true; // Placeholder
    }

    private static boolean hasWeapon(FakePlayer fakePlayer) {
        // Implementation depends on your inventory system
        return false; // Placeholder
    }

    private static double calculateWeaponEffectiveness(FakePlayer fakePlayer) {
        // Implementation depends on equipped weapon
        return 0.5; // Placeholder
    }

    private static boolean hasArmor(FakePlayer fakePlayer) {
        // Implementation depends on armor system
        return false; // Placeholder
    }

    private static double calculateArmorRating(FakePlayer fakePlayer) {
        // Implementation depends on armor system
        return 0.5; // Placeholder
    }

    private static boolean isInCombat(FakePlayer fakePlayer) {
        // Implementation depends on combat tracking
        return false; // Placeholder
    }

    private static double calculateCombatDuration(FakePlayer fakePlayer) {
        // Implementation depends on combat tracking
        return 0.0; // Placeholder
    }

    private static double calculateRetreatUrgency(FakePlayer fakePlayer) {
        // Implementation depends on health and threat assessment
        return 0.0; // Placeholder
    }

    private static boolean isNightTime(long time) {
        return time > 13000 && time < 23000; // Minecraft time
    }

    private static double calculateRelationship(FakePlayer fakePlayer, Player player) {
        // Implementation depends on your relationship/faction system
        return 0.5; // Placeholder
    }

    private static double calculateInteractionHistory(FakePlayer fakePlayer, Player player) {
        // Implementation depends on interaction tracking
        return 0.0; // Placeholder
    }

    private static boolean isInGroup(FakePlayer fakePlayer) {
        // Implementation depends on group/party system
        return false; // Placeholder
    }

    private static double calculateGroupSize(FakePlayer fakePlayer) {
        // Implementation depends on group system
        return 1.0; // Placeholder (just self)
    }

    private static double calculateGroupCohesion(FakePlayer fakePlayer) {
        // Implementation depends on group dynamics
        return 1.0; // Placeholder
    }

    private static double calculateNearbyHelpSignals(FakePlayer fakePlayer) {
        // Implementation depends on signaling system
        return 0.0; // Placeholder
    }

    private static double calculateNearbyThreatSignals(FakePlayer fakePlayer) {
        // Implementation depends on signaling system
        return 0.0; // Placeholder
    }
}
