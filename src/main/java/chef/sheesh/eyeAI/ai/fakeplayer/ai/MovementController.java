package chef.sheesh.eyeAI.ai.fakeplayer.ai;

import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerState;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * Movement controller for fake players
 */
public class MovementController {

    private final FakePlayer fakePlayer;
    private Location targetLocation;
    private double movementSpeed = 0.2; // blocks per tick
    private boolean isUrgent = false;
    private boolean waitingForGroup = false;
    private double speedMultiplier = 1.0;

    public MovementController(FakePlayer fakePlayer) {
        this.fakePlayer = fakePlayer;
    }

    /**
     * Update movement logic
     */
    public void tick() {
        if (targetLocation != null && !hasReachedDestination()) {
            performMovement();
        }
    }

    /**
     * Move towards a target location
     */
    public void moveTowards(Location target) {
        if (target == null || target.getWorld() != fakePlayer.getLocation().getWorld()) {
            return;
        }

        this.targetLocation = target.clone();
        fakePlayer.setState(FakePlayerState.MOVING);
    }

    /**
     * Perform the actual movement
     */
    private void performMovement() {
        Location currentLoc = fakePlayer.getLocation();

        // Calculate direction vector
        Vector direction = targetLocation.toVector().subtract(currentLoc.toVector());
        double distance = direction.length();

        if (distance < 0.5) {
            // Close enough, consider reached
            fakePlayer.moveTo(targetLocation);
            targetLocation = null;
            fakePlayer.setState(FakePlayerState.IDLE);
            return;
        }

        // Normalize and apply speed
        direction.normalize();
        double actualSpeed = movementSpeed * speedMultiplier;
        if (isUrgent) {
            actualSpeed *= 1.5; // 50% speed boost when urgent
        }

        // Calculate new position
        Vector newPos = currentLoc.toVector().add(direction.multiply(actualSpeed));
        Location newLocation = new Location(currentLoc.getWorld(), newPos.getX(), newPos.getY(), newPos.getZ());

        // Update player location
        fakePlayer.moveTo(newLocation);
    }

    /**
     * Check if reached destination
     */
    public boolean hasReachedDestination() {
        if (targetLocation == null) {
            return true;
        }

        return fakePlayer.getLocation().distance(targetLocation) < 1.0;
    }

    /**
     * Set movement speed
     */
    public void setMovementSpeed(double speed) {
        this.movementSpeed = Math.max(0.1, Math.min(1.0, speed));
    }

    /**
     * Set urgent movement (higher priority)
     */
    public void setUrgent(boolean urgent) {
        this.isUrgent = urgent;
    }

    /**
     * Reduce speed by factor
     */
    public void reduceSpeed(double factor) {
        this.speedMultiplier = Math.max(0.1, Math.min(1.0, factor));
    }

    /**
     * Set waiting for group state
     */
    public void setWaitingForGroup(boolean waiting) {
        this.waitingForGroup = waiting;
        if (waiting) {
            this.speedMultiplier = 0.3; // Move very slowly when waiting
        } else {
            this.speedMultiplier = 1.0; // Resume normal speed
        }
    }

    /**
     * Stop current movement
     */
    public void stop() {
        this.targetLocation = null;
        this.isUrgent = false;
        this.speedMultiplier = 1.0;
        fakePlayer.setState(FakePlayerState.IDLE);
    }

    /**
     * Get current target location
     */
    public Location getTarget() {
        return targetLocation != null ? targetLocation.clone() : null;
    }

    /**
     * Check if currently moving
     */
    public boolean isMoving() {
        return targetLocation != null && !hasReachedDestination();
    }

    /**
     * Get movement speed
     */
    public double getMovementSpeed() {
        return movementSpeed * speedMultiplier;
    }
}
