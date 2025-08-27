package chef.sheesh.eyeAI.ai.fakeplayer.ai;

import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerState;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Target selection controller for fake players
 */
public class TargetSelector {

    private final FakePlayer fakePlayer;
    private Entity currentTarget;
    private long lastTargetUpdateTime = 0;
    private static final long TARGET_UPDATE_INTERVAL = 2000; // 2 seconds
    private static final double MAX_TARGET_DISTANCE = 16.0;

    public TargetSelector(FakePlayer fakePlayer) {
        this.fakePlayer = fakePlayer;
    }

    /**
     * Update target selection logic
     */
    public void tick() {
        long currentTime = System.currentTimeMillis();

        // Update target periodically or if current target is invalid
        if (currentTime - lastTargetUpdateTime > TARGET_UPDATE_INTERVAL || !isValidTarget(currentTarget)) {
            updateTarget();
            lastTargetUpdateTime = currentTime;
        }
    }

    /**
     * Set a specific target
     */
    public void setTarget(Entity target) {
        if (target != null && isValidTarget(target)) {
            this.currentTarget = target;
            this.lastTargetUpdateTime = System.currentTimeMillis();
        } else {
            this.currentTarget = null;
        }
    }

    /**
     * Update target automatically
     */
    private void updateTarget() {
        Entity bestTarget = findBestTarget();
        setTarget(bestTarget);
    }

    /**
     * Find the best target nearby
     */
    private Entity findBestTarget() {
        Location playerLoc = fakePlayer.getLocation();
        List<Entity> potentialTargets = new ArrayList<>();

        // Find all nearby hostile entities - must run on main thread
        // We'll schedule this to run on main thread and wait for result
        try {
            CompletableFuture<List<Entity>> future = new CompletableFuture<>();
            
            fakePlayer.getManager().getScheduler().runOnMain(() -> {
                try {
                    List<Entity> nearbyEntities = new ArrayList<>();
                    playerLoc.getWorld().getNearbyEntities(playerLoc, MAX_TARGET_DISTANCE, MAX_TARGET_DISTANCE, MAX_TARGET_DISTANCE).forEach(entity -> {
                        if (isHostileEntity(entity) && isValidTarget(entity)) {
                            nearbyEntities.add(entity);
                        }
                    });
                    future.complete(nearbyEntities);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            });
            
            potentialTargets = future.get(100, TimeUnit.MILLISECONDS); // Wait max 100ms
        } catch (Exception e) {
            fakePlayer.getManager().getLogger().warning("Failed to find nearby entities for " + fakePlayer.getName() + ": " + e.getMessage());
            return null;
        }

        if (potentialTargets.isEmpty()) {
            return null;
        }

        // Sort by priority (distance, health, threat level)
        potentialTargets.sort((e1, e2) -> {
            if (!(e1 instanceof LivingEntity) || !(e2 instanceof LivingEntity)) {
                return 0;
            }
            
            LivingEntity le1 = (LivingEntity) e1;
            LivingEntity le2 = (LivingEntity) e2;
            
            // Distance priority (closer is better)
            double dist1 = e1.getLocation().distance(playerLoc);
            double dist2 = e2.getLocation().distance(playerLoc);
            int distanceComparison = Double.compare(dist1, dist2);
            
            if (distanceComparison != 0) {
                return distanceComparison;
            }
            
            // Health priority (lower health is better)
            double health1 = le1.getHealth();
            double health2 = le2.getHealth();
            int healthComparison = Double.compare(health1, health2);
            
            if (healthComparison != 0) {
                return healthComparison;
            }
            
            // Default to 0 if all else equal
            return 0;
        });

        return potentialTargets.get(0); // Return closest target
    }

    /**
     * Check if entity is hostile
     */
    private boolean isHostileEntity(Entity entity) {
        if (!(entity instanceof LivingEntity)) {
            return false;
        }

        String entityType = entity.getType().name();
        return entityType.contains("ZOMBIE") || entityType.contains("SKELETON") ||
               entityType.contains("CREEPER") || entityType.contains("SPIDER") ||
               entityType.contains("ENDERMAN") || entityType.contains("BLAZE") ||
               entityType.contains("WITCH") || entityType.contains("PHANTOM");
    }

    /**
     * Check if target is valid
     */
    private boolean isValidTarget(Entity target) {
        if (target == null || !target.isValid() || target.isDead()) {
            return false;
        }

        if (!(target instanceof LivingEntity livingTarget)) {
            return false;
        }

        // Check if target is too far away
        double distance = fakePlayer.getLocation().distance(target.getLocation());
        if (distance > MAX_TARGET_DISTANCE) {
            return false;
        }

        // Check if target has health
        return livingTarget.getHealth() > 0;
    }

    /**
     * Get current target
     */
    public Entity getCurrentTarget() {
        return currentTarget;
    }

    /**
     * Check if has valid target
     */
    public boolean hasTarget() {
        return currentTarget != null && isValidTarget(currentTarget);
    }

    /**
     * Clear current target
     */
    public void clearTarget() {
        this.currentTarget = null;
    }

    /**
     * Get distance to current target
     */
    public double getDistanceToTarget() {
        if (!hasTarget()) {
            return Double.MAX_VALUE;
        }

        return fakePlayer.getLocation().distance(currentTarget.getLocation());
    }

    /**
     * Target priority comparator
     */
    private static class TargetPriorityComparator implements Comparator<Entity> {
        private final Location playerLocation;

        public TargetPriorityComparator(Location playerLocation) {
            this.playerLocation = playerLocation;
        }

        @Override
        public int compare(Entity e1, Entity e2) {
            // Prioritize by distance (closer is better)
            double dist1 = playerLocation.distance(e1.getLocation());
            double dist2 = playerLocation.distance(e2.getLocation());

            // Add threat level consideration
            int threat1 = getThreatLevel(e1);
            int threat2 = getThreatLevel(e2);

            // Higher threat = higher priority (lower value)
            if (threat1 != threat2) {
                return Integer.compare(threat2, threat1);
            }

            // If same threat level, prioritize by distance
            return Double.compare(dist1, dist2);
        }

        private int getThreatLevel(Entity entity) {
            String type = entity.getType().name();
            if (type.contains("CREEPER")) {
                return 5; // Highest threat
            }
            if (type.contains("SKELETON")) {
                return 4;
            }
            if (type.contains("ZOMBIE")) {
                return 3;
            }
            if (type.contains("SPIDER")) {
                return 2;
            }
            return 1; // Default threat level
        }
    }
}
