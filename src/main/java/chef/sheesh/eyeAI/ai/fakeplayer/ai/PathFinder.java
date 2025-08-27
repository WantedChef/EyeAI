package chef.sheesh.eyeAI.ai.fakeplayer.ai;

import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.ai.movement.Path;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Pathfinding controller for fake players
 */
public class PathFinder {

    private final FakePlayer fakePlayer;
    private Path currentPath;
    private Location targetLocation;
    private boolean isPathfinding = false;
    private long lastPathfindTime = 0;
    private static final long PATHFIND_COOLDOWN = 1000; // 1 second cooldown

    public PathFinder(FakePlayer fakePlayer) {
        this.fakePlayer = fakePlayer;
    }

    /**
     * Update pathfinding logic
     */
    public void tick() {
        if (currentPath != null && !currentPath.isFinished()) {
            // Continue following current path
            followPath();
        }
    }

    /**
     * Find a path to the target location
     */
    public boolean findPath(Location target) {
        if (target == null || target.getWorld() != fakePlayer.getLocation().getWorld()) {
            return false;
        }

        // Cooldown to prevent spam
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPathfindTime < PATHFIND_COOLDOWN) {
            return false;
        }

        this.targetLocation = target.clone();
        this.lastPathfindTime = currentTime;
        this.isPathfinding = true;

        // Simple pathfinding - direct line for now
        // TODO: Implement A* pathfinding algorithm
        List<Location> waypoints = new ArrayList<>();
        waypoints.add(fakePlayer.getLocation().clone());
        waypoints.add(target.clone());

        this.currentPath = new Path(waypoints);
        return true;
    }

    /**
     * Follow the current path
     */
    private void followPath() {
        if (currentPath == null || currentPath.isFinished()) {
            return;
        }

        Location nextWaypoint = currentPath.getNextWaypoint();
        if (nextWaypoint != null) {
            double distance = fakePlayer.getLocation().distance(nextWaypoint);
            if (distance < 1.0) {
                // Reached waypoint, move to next
                currentPath.advance();
            } else {
                // Move towards waypoint
                fakePlayer.getMovementController().moveTowards(nextWaypoint);
            }
        }
    }

    /**
     * Clear current path
     */
    public void clearPath() {
        this.currentPath = null;
        this.targetLocation = null;
        this.isPathfinding = false;
    }

    /**
     * Check if currently pathfinding
     */
    public boolean isPathfinding() {
        return isPathfinding && currentPath != null && !currentPath.isFinished();
    }

    /**
     * Get current target location
     */
    public Location getTarget() {
        return targetLocation != null ? targetLocation.clone() : null;
    }

    /**
     * Get current path
     */
    public Path getCurrentPath() {
        return currentPath;
    }
}
