package chef.sheesh.eyeAI.ai.behavior.nodes;

import chef.sheesh.eyeAI.ai.behavior.BehaviorTree;
import chef.sheesh.eyeAI.ai.core.DecisionContext;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerState;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.Comparator;
import java.util.Optional;

/**
 * Action node that makes a fake player flee from threats.
 * Handles threat detection, safe location finding, and escape behavior.
 */
public class FleeNode extends BehaviorTree {

    private final double fleeDistance;
    private final double safeDistance;
    private final double speed;
    private final long maxFleeTime;
    private final boolean useRandomEscape;

    private Location fleeTarget = null;
    private long fleeStartTime = 0;
    private MoveToNode currentMovement = null;

    public FleeNode(double fleeDistance, double safeDistance, double speed,
                   long maxFleeTime, boolean useRandomEscape) {
        this.fleeDistance = Math.max(5.0, fleeDistance);
        this.safeDistance = Math.max(10.0, safeDistance);
        this.speed = Math.max(0.5, Math.min(speed, 3.0)); // Faster than normal movement
        this.maxFleeTime = Math.max(5000, maxFleeTime); // Minimum 5 seconds
        this.useRandomEscape = useRandomEscape;
        this.name = "FleeNode";
    }

    public FleeNode() {
        this(15.0, 20.0, 2.0, 15000, true);
    }

    @Override
    public ExecutionResult execute(IFakePlayer fakePlayer) {
        if (!(fakePlayer instanceof FakePlayer realFakePlayer)) {
            return failure();
        }

        long currentTime = System.currentTimeMillis();

        // Initialize flee behavior
        if (fleeStartTime == 0) {
            fleeStartTime = currentTime;
            realFakePlayer.setState(FakePlayerState.FLEEING);
        }

        // Check if we've been fleeing too long
        if (currentTime - fleeStartTime > maxFleeTime) {
            reset();
            return success(); // Consider flee successful after timeout
        }

        // Get decision context to identify threats
        DecisionContext context = realFakePlayer.createDecisionContext();
        Location currentLocation = realFakePlayer.getLocation();

        // Find the nearest threat
        Optional<Entity> nearestThreat = findNearestThreat(context, currentLocation);

        if (nearestThreat.isEmpty()) {
            // No threats nearby, flee successful
            reset();
            return success();
        }

        Entity threat = nearestThreat.get();
        Location threatLocation = threat.getLocation();

        // Check if we're far enough from the threat
        double distanceToThreat = currentLocation.distance(threatLocation);
        if (distanceToThreat >= safeDistance) {
            reset();
            return success();
        }

        // Calculate flee direction (away from threat)
        if (fleeTarget == null || shouldRecalculateFleeTarget(currentLocation, threatLocation)) {
            fleeTarget = calculateFleeTarget(currentLocation, threatLocation);
            currentMovement = new MoveToNode(null, fleeTarget, speed, 2.0, false);
        }

        // Execute flee movement
        if (currentMovement != null) {
            ExecutionResult movementResult = currentMovement.execute(fakePlayer);

            switch (movementResult) {
                case SUCCESS:
                    // Reached flee target, check if we need a new one
                    fleeTarget = null;
                    currentMovement = null;
                    return running();

                case FAILURE:
                    // Failed to reach flee target, try a new one
                    fleeTarget = calculateFleeTarget(currentLocation, threatLocation);
                    currentMovement = new MoveToNode(null, fleeTarget, speed, 2.0, false);
                    return running();

                case RUNNING:
                    // Still fleeing
                    return running();
            }
        }

        // If we get here, we couldn't calculate a flee target
        reset();
        return failure();
    }

    /**
     * Find the nearest hostile entity
     */
    private Optional<Entity> findNearestThreat(DecisionContext context, Location currentLocation) {
        return context.getNearbyEntities().stream()
            .filter(this::isHostile)
            .min(Comparator.comparingDouble(e -> currentLocation.distance(e.getLocation())));
    }

    /**
     * Check if an entity is considered hostile
     */
    private boolean isHostile(Entity entity) {
        String typeName = entity.getType().name();
        return typeName.contains("ZOMBIE") ||
               typeName.contains("SKELETON") ||
               typeName.contains("CREEPER") ||
               typeName.contains("SPIDER") ||
               typeName.contains("ENDERMAN") ||
               typeName.contains("BLAZE") ||
               typeName.contains("GHAST") ||
               typeName.contains("WITHER");
    }

    /**
     * Check if we should recalculate the flee target
     */
    private boolean shouldRecalculateFleeTarget(Location currentLocation, Location threatLocation) {
        if (fleeTarget == null) {
            return true;
        }

        // Recalculate if we're close to the flee target or if threat has moved significantly
        double distanceToFleeTarget = currentLocation.distance(fleeTarget);
        return distanceToFleeTarget < 3.0;
    }

    /**
     * Calculate a safe location to flee to
     */
    private Location calculateFleeTarget(Location currentLocation, Location threatLocation) {
        // Calculate direction away from threat
        Vector awayFromThreat = currentLocation.toVector().subtract(threatLocation.toVector());
        awayFromThreat.normalize();
        awayFromThreat.multiply(fleeDistance);

        Location potentialTarget = currentLocation.clone().add(awayFromThreat);

        // If random escape is enabled, add some randomness
        if (useRandomEscape) {
            double randomAngle = (Math.random() - 0.5) * Math.PI / 2; // ±45 degrees
            double cos = Math.cos(randomAngle);
            double sin = Math.sin(randomAngle);

            Vector randomOffset = new Vector(
                awayFromThreat.getX() * cos - awayFromThreat.getZ() * sin,
                0,
                awayFromThreat.getX() * sin + awayFromThreat.getZ() * cos
            );

            potentialTarget.add(randomOffset.multiply(0.5));
        }

        // Ensure the target is within world bounds and on solid ground
        potentialTarget = validateAndAdjustTarget(potentialTarget, currentLocation.getWorld());

        return potentialTarget;
    }

    /**
     * Validate and adjust the flee target to ensure it's safe
     */
    private Location validateAndAdjustTarget(Location target, org.bukkit.World world) {
        // Keep within world borders (simple check)
        double maxX = 30000000;
        double maxZ = 30000000;
        double minX = -30000000;
        double minZ = -30000000;

        double x = Math.max(minX, Math.min(maxX, target.getX()));
        double z = Math.max(minZ, Math.min(maxZ, target.getZ()));
        double y = target.getY();

        Location adjustedTarget = new Location(world, x, y, z);

        // Find a safe Y level (simple ground detection)
        for (int i = 0; i < 10; i++) {
            Location checkLocation = adjustedTarget.clone();
            checkLocation.setY(y + i);

            if (checkLocation.getBlock().getType().isSolid()) {
                adjustedTarget.setY(y + i + 1);
                break;
            }
        }

        return adjustedTarget;
    }

    @Override
    public void reset() {
        fleeTarget = null;
        fleeStartTime = 0;
        currentMovement = null;
        markNotRunning();
    }

    @Override
    public String getDescription() {
        return String.format("Flee [distance: %.1f, speed: %.1f, maxTime: %ds]",
            fleeDistance,
            speed,
            maxFleeTime / 1000);
    }

    @Override
    public String getCategory() {
        return "Survival";
    }

    /**
     * Get the flee distance
     */
    public double getFleeDistance() {
        return fleeDistance;
    }

    /**
     * Get the safe distance
     */
    public double getSafeDistance() {
        return safeDistance;
    }

    /**
     * Get the flee speed
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Get the maximum flee time
     */
    public long getMaxFleeTime() {
        return maxFleeTime;
    }

    /**
     * Check if random escape is enabled
     */
    public boolean isUseRandomEscape() {
        return useRandomEscape;
    }

    /**
     * Get the current flee target
     */
    public Location getFleeTarget() {
        return fleeTarget != null ? fleeTarget.clone() : null;
    }

    /**
     * Get the time elapsed since flee started
     */
    public long getFleeElapsedTime(long currentTime) {
        return fleeStartTime > 0 ? currentTime - fleeStartTime : 0;
    }

    /**
     * Check if currently fleeing
     */
    public boolean isFleeing() {
        return fleeStartTime > 0;
    }
}
