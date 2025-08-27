package chef.sheesh.eyeAI.ai.behavior.nodes;

import chef.sheesh.eyeAI.ai.behavior.BehaviorTree;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

/**
 * Action node that moves a fake player towards a target location.
 * The target can be a fixed location, an entity, or a location from the blackboard.
 */
public class MoveToNode extends BehaviorTree {

    private final String blackboardKey;
    private final Location fixedTargetLocation;
    private final double speed;
    private final double tolerance;
    private final boolean usePathfinding;

    private Location currentTarget;
    private long lastMovementTime = 0;

    /**
     * Moves to the agent's current target entity.
     */
    public MoveToNode() {
        this(null, null, 1.0, 1.0, false);
    }

    /**
     * Moves to a location from the blackboard.
     * @param blackboardKey The key for the location in the blackboard.
     */
    public MoveToNode(String blackboardKey) {
        this(blackboardKey, null, 1.0, 1.0, false);
    }

    /**
     * Moves to a fixed location.
     * @param targetLocation The fixed location to move to.
     */
    public MoveToNode(Location targetLocation) {
        this(null, targetLocation, 1.0, 1.0, false);
    }

    public MoveToNode(String blackboardKey, Location targetLocation, double speed, double tolerance, boolean usePathfinding) {
        this.blackboardKey = blackboardKey;
        this.fixedTargetLocation = targetLocation != null ? targetLocation.clone() : null;
        this.speed = Math.max(0.1, Math.min(speed, 2.0)); // Clamp speed
        this.tolerance = Math.max(0.1, tolerance);
        this.usePathfinding = usePathfinding;
        this.name = "MoveToNode";
    }

    @Override
    public ExecutionResult execute(IFakePlayer fakePlayer) {
        Location targetLocation = getTargetLocation(fakePlayer);
        if (targetLocation == null) {
            return failure(); // No target location found
        }

        if (!(fakePlayer instanceof FakePlayer realFakePlayer)) {
            return failure();
        }

        Location currentLocation = realFakePlayer.getLocation();

        if (currentLocation.distance(targetLocation) <= tolerance) {
            realFakePlayer.setState(chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerState.IDLE);
            reset();
            return success();
        }

        long currentTime = System.currentTimeMillis();
        // 100ms between movements
        long movementCooldown = 100;
        if (currentTime - lastMovementTime < movementCooldown) {
            return running();
        }

        if (currentTarget == null || currentLocation.distance(currentTarget) <= tolerance) {
            currentTarget = calculateNextTarget(currentLocation, targetLocation);
        }

        if (currentTarget == null) {
            reset();
            return failure();
        }

        Vector direction = currentTarget.toVector().subtract(currentLocation.toVector());
        double distance = direction.length();

        if (distance < 0.1) {
            currentTarget = calculateNextTarget(currentLocation, targetLocation);
            return running();
        }

        direction.normalize().multiply(Math.min(speed, distance));
        Location newLocation = currentLocation.clone().add(direction);

        if (!isLocationSafe(newLocation, realFakePlayer)) {
            Location alternativeTarget = findAlternativeTarget(currentLocation, targetLocation);
            if (alternativeTarget != null) {
                currentTarget = alternativeTarget;
                return running();
            }
            reset();
            return failure();
        }

        realFakePlayer.moveTo(newLocation);
        realFakePlayer.setState(chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerState.MOVING);
        lastMovementTime = currentTime;

        return running();
    }
    
    private Location getTargetLocation(IFakePlayer fakePlayer) {
        if (this.blackboardKey != null) {
            Object target = fakePlayer.getBlackboard().get(this.blackboardKey);
            if (target instanceof Location) {
                return (Location) target;
            }
            if (target instanceof Entity) {
                return ((Entity) target).getLocation();
            }
            if (target instanceof IFakePlayer) {
                return ((IFakePlayer) target).getLocation();
            }
        }
        if (this.fixedTargetLocation != null) {
            return this.fixedTargetLocation;
        }
        Entity targetEntity = fakePlayer.getTarget();
        if (targetEntity != null) {
            return targetEntity.getLocation();
        }
        return null;
    }

    private Location calculateNextTarget(Location current, Location target) {
        if (!usePathfinding) {
            return target.clone();
        }
        Vector direction = target.toVector().subtract(current.toVector());
        double distance = direction.length();
        if (distance <= 2.0) {
            return target.clone();
        }
        direction.normalize().multiply(2.0);
        return current.clone().add(direction);
    }

    private boolean isLocationSafe(Location location, FakePlayer fakePlayer) {
        if (location.getWorld() == null) {
            return false;
        }
        Location checkLocation = location.clone();
        checkLocation.setY(checkLocation.getY() - 0.1);
        if (checkLocation.getBlock().getType().isSolid()) {
            return false;
        }
        checkLocation.setY(checkLocation.getY() + 1.8);
        return !checkLocation.getBlock().getType().isSolid();
    }

    private Location findAlternativeTarget(Location current, Location target) {
        double[] offsets = {-1.0, 1.0, -2.0, 2.0};
        for (double xOffset : offsets) {
            for (double zOffset : offsets) {
                Location alternative = current.clone().add(xOffset, 0, zOffset);
                if (alternative.distance(target) < current.distance(target)) {
                    if (isLocationSafe(alternative, null)) {
                        return alternative;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void reset() {
        currentTarget = null;
        lastMovementTime = 0;
        markNotRunning();
    }

    @Override
    public String getDescription() {
        if (blackboardKey != null) {
            return String.format("Move to blackboard key '%s'", blackboardKey);
        }
        if (fixedTargetLocation != null) {
            return String.format("Move to (%s, %s, %s)", fixedTargetLocation.getBlockX(), fixedTargetLocation.getBlockY(), fixedTargetLocation.getBlockZ());
        }
        return "Move to current target";
    }

    @Override
    public String getCategory() {
        return "Movement";
    }
}
