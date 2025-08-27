package chef.sheesh.eyeAI.ai.behavior.nodes;

import chef.sheesh.eyeAI.ai.behavior.BehaviorTree;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerState;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Action node that makes a fake player patrol between multiple waypoints.
 * Supports configurable patrol patterns and timing.
 */
public class PatrolNode extends BehaviorTree {

    private final List<Location> waypoints;
    private final double speed;
    private final long waitTimeBetweenPoints;
    private final boolean loop;
    private final PatrolPattern pattern;

    private int currentWaypointIndex = 0;
    private long lastArrivalTime = 0;
    private boolean isWaiting = false;
    private MoveToNode currentMovement = null;
    private final Random random = new Random();

    public enum PatrolPattern {
        SEQUENTIAL,     // Follow waypoints in order
        RANDOM,         // Jump to random waypoints
        ROUND_TRIP,     // Go to end and return
        CIRCULAR        // Continuous loop
    }

    public PatrolNode(List<Location> waypoints) {
        this(waypoints, 1.0, 3000, true, PatrolPattern.SEQUENTIAL);
    }

    public PatrolNode(List<Location> waypoints, double speed, long waitTimeBetweenPoints,
                     boolean loop, PatrolPattern pattern) {
        if (waypoints == null || waypoints.isEmpty()) {
            throw new IllegalArgumentException("Waypoints list cannot be null or empty");
        }

        this.waypoints = new ArrayList<>(waypoints);
        this.speed = Math.max(0.1, Math.min(speed, 2.0));
        this.waitTimeBetweenPoints = Math.max(0, waitTimeBetweenPoints);
        this.loop = loop;
        this.pattern = pattern;
        this.name = "PatrolNode";
    }

    @Override
    public ExecutionResult execute(IFakePlayer fakePlayer) {
        if (!(fakePlayer instanceof FakePlayer)) {
            return failure();
        }

        FakePlayer realFakePlayer = (FakePlayer) fakePlayer;
        @SuppressWarnings("unused") // TODO: Use currentLocation for patrol logic
        Location currentLocation = realFakePlayer.getLocation();
        long currentTime = System.currentTimeMillis();

        // Check if we're waiting at a waypoint
        if (isWaiting) {
            if (currentTime - lastArrivalTime >= waitTimeBetweenPoints) {
                isWaiting = false;
                moveToNextWaypoint();
            } else {
                realFakePlayer.setState(FakePlayerState.IDLE);
                return running();
            }
        }

        // If we don't have an active movement, start moving to the next waypoint
        if (currentMovement == null) {
            moveToNextWaypoint();
        }

        // Execute the current movement
        if (currentMovement != null) {
            ExecutionResult movementResult = currentMovement.execute(fakePlayer);

            switch (movementResult) {
                case SUCCESS:
                    // Arrived at waypoint
                    arriveAtWaypoint(currentTime);
                    return running();

                case FAILURE:
                    // Failed to reach waypoint, try next one
                    moveToNextWaypoint();
                    return running();

                case RUNNING:
                    // Still moving
                    return running();
            }
        }

        // Check if we've completed the patrol pattern
        if (!loop && isPatrolComplete()) {
            reset();
            return success();
        }

        return running();
    }

    /**
     * Move to the next waypoint based on the patrol pattern
     */
    private void moveToNextWaypoint() {
        Location nextWaypoint = getNextWaypoint();
        if (nextWaypoint != null) {
            // Use 5-arg constructor: (blackboardKey, targetLocation, speed, tolerance, usePathfinding)
            currentMovement = new MoveToNode(null, nextWaypoint, speed, 1.0, false);
        } else {
            currentMovement = null;
        }
    }

    /**
     * Get the next waypoint based on the patrol pattern
     */
    private Location getNextWaypoint() {
        if (waypoints.isEmpty()) {
            return null;
        }

        switch (pattern) {
            case SEQUENTIAL:
            case ROUND_TRIP:
                currentWaypointIndex = (currentWaypointIndex + 1) % waypoints.size();
                break;

            case RANDOM:
                currentWaypointIndex = random.nextInt(waypoints.size());
                break;

            case CIRCULAR:
                currentWaypointIndex = (currentWaypointIndex + 1) % waypoints.size();
                break;
        }

        return waypoints.get(currentWaypointIndex);
    }

    /**
     * Handle arrival at a waypoint
     */
    private void arriveAtWaypoint(long arrivalTime) {
        lastArrivalTime = arrivalTime;
        isWaiting = true;
        currentMovement = null;
    }

    /**
     * Check if the patrol pattern is complete
     */
    private boolean isPatrolComplete() {
        switch (pattern) {
            case SEQUENTIAL:
            case CIRCULAR:
                return false; // These patterns never complete

            case RANDOM:
                return false; // Random patrols never complete

            case ROUND_TRIP:
                // For round trip, complete when we return to the start
                return currentWaypointIndex == 0;

            default:
                return false;
        }
    }

    @Override
    public void reset() {
        currentWaypointIndex = 0;
        lastArrivalTime = 0;
        isWaiting = false;
        currentMovement = null;
        markNotRunning();
    }

    @Override
    public String getDescription() {
        return String.format("Patrol (%d waypoints) [pattern: %s, speed: %.1f]",
            waypoints.size(),
            pattern.name(),
            speed);
    }

    @Override
    public String getCategory() {
        return "Movement";
    }

    /**
     * Add a waypoint to the patrol route
     */
    public void addWaypoint(Location waypoint) {
        waypoints.add(waypoint.clone());
    }

    /**
     * Remove a waypoint from the patrol route
     */
    public boolean removeWaypoint(int index) {
        if (index >= 0 && index < waypoints.size()) {
            waypoints.remove(index);
            if (currentWaypointIndex >= waypoints.size()) {
                currentWaypointIndex = 0;
            }
            return true;
        }
        return false;
    }

    /**
     * Clear all waypoints
     */
    public void clearWaypoints() {
        waypoints.clear();
        currentWaypointIndex = 0;
        currentMovement = null;
    }

    /**
     * Get the current waypoint index
     */
    public int getCurrentWaypointIndex() {
        return currentWaypointIndex;
    }

    /**
     * Get all waypoints
     */
    public List<Location> getWaypoints() {
        return new ArrayList<>(waypoints);
    }

    /**
     * Get the patrol speed
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Get the wait time between points
     */
    public long getWaitTimeBetweenPoints() {
        return waitTimeBetweenPoints;
    }

    /**
     * Check if the patrol loops
     */
    public boolean isLoop() {
        return loop;
    }

    /**
     * Get the patrol pattern
     */
    public PatrolPattern getPattern() {
        return pattern;
    }

    /**
     * Check if currently waiting at a waypoint
     */
    public boolean isWaiting() {
        return isWaiting;
    }

    /**
     * Get the time until the next movement (if waiting)
     */
    public long getTimeUntilNextMovement(long currentTime) {
        if (!isWaiting) {
            return 0;
        }

        long elapsed = currentTime - lastArrivalTime;
        return Math.max(0, waitTimeBetweenPoints - elapsed);
    }

    /**
     * Create a circular patrol around a center point
     */
    public static PatrolNode createCircularPatrol(Location center, double radius, int pointCount, double speed) {
        List<Location> waypoints = new ArrayList<>();

        for (int i = 0; i < pointCount; i++) {
            double angle = (2 * Math.PI * i) / pointCount;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            Location waypoint = new Location(center.getWorld(), x, center.getY(), z);
            waypoints.add(waypoint);
        }

        return new PatrolNode(waypoints, speed, 2000, true, PatrolPattern.CIRCULAR);
    }

    /**
     * Create a square patrol around a center point
     */
    public static PatrolNode createSquarePatrol(Location center, double sideLength, double speed) {
        List<Location> waypoints = new ArrayList<>();
        double halfSide = sideLength / 2;

        waypoints.add(center.clone().add(halfSide, 0, halfSide));
        waypoints.add(center.clone().add(halfSide, 0, -halfSide));
        waypoints.add(center.clone().add(-halfSide, 0, -halfSide));
        waypoints.add(center.clone().add(-halfSide, 0, halfSide));

        return new PatrolNode(waypoints, speed, 3000, true, PatrolPattern.CIRCULAR);
    }
}
