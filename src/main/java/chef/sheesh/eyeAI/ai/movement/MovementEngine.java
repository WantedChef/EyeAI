package chef.sheesh.eyeAI.ai.movement;

import chef.sheesh.eyeAI.ai.core.SchedulerService;
import org.bukkit.Location;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Engine for handling movement calculations and pathfinding.
 * Uses async computation for heavy pathfinding operations.
 */
public class MovementEngine implements IMovementEngine {

    private final NavGraph navGraph;
    private final SchedulerService scheduler;

    public MovementEngine(NavGraph navGraph, SchedulerService scheduler) {
        this.navGraph = navGraph;
        this.scheduler = scheduler;
    }

    /**
     * Compute a path asynchronously between two locations
     * @param from Starting location
     * @param to Target location
     * @return CompletableFuture that resolves to the computed path
     */
    public CompletableFuture<Path> computePathAsync(Location from, Location to) {
        // Run pathfinding on async thread
        return CompletableFuture.supplyAsync(() -> navGraph.findPath(from, to).smooth());
    }

    /**
     * Compute a path and apply it to a callback on the main thread
     * @param from Starting location
     * @param to Target location
     * @param callback Function to call with the computed path on main thread
     */
    @Override
    public void computePathAsync(Location from, Location to, Consumer<Path> callback) {
        computePathAsync(from, to).thenAccept(path -> {
            scheduler.runOnMain(() -> callback.accept(path));
        });
    }

    /**
     * Synchronous path computation (use sparingly, blocks calling thread)
     */
    public Path computePathSync(Location from, Location to) {
        return navGraph.findPath(from, to).smooth();
    }

    /**
     * Check if a location is reachable from another location
     */
    public CompletableFuture<Boolean> isReachableAsync(Location from, Location to) {
        return computePathAsync(from, to)
                .thenApply(path -> !path.isEmpty());
    }

    /**
     * Get the estimated distance between two points (ignoring obstacles)
     */
    public double getEstimatedDistance(Location from, Location to) {
        return from.distance(to);
    }

    /**
     * Callback interface for path computation results
     */
    public interface PathCallback {
        void onPathComputed(Path path);
    }

    /**
     * Advanced path computation with waypoints
     */
    public CompletableFuture<Path> computePathWithWaypointsAsync(Location start, Location... waypoints) {
        if (waypoints.length == 0) {
            return CompletableFuture.completedFuture(new Path(java.util.Collections.emptyList()));
        }

        return computePathAsync(start, waypoints[0])
                .thenCompose(path -> {
                    if (path.isEmpty()) {
                        return CompletableFuture.completedFuture(new Path(java.util.Collections.emptyList()));
                    }

                    // Recursively compute paths for remaining waypoints
                    return computePathWithWaypointsAsync(waypoints[0], java.util.Arrays.copyOfRange(waypoints, 1, waypoints.length))
                            .thenApply(remainingPath -> {
                                if (remainingPath.isEmpty()) {
                                    return path;
                                }

                                // Combine paths
                                java.util.List<Location> combinedWaypoints = new java.util.ArrayList<>();
                                combinedWaypoints.addAll(path.getWaypoints());
                                combinedWaypoints.addAll(remainingPath.getWaypoints());
                                return new Path(combinedWaypoints).smooth();
                            });
                });
    }

    /**
     * Move an entity along a path
     * @param entity The entity to move
     * @param path The path to follow
     */
    @Override
    public void moveAlongPath(org.bukkit.entity.Entity entity, Path path) {
        if (path == null || path.isEmpty()) {
            return;
        }

        Location nextWaypoint = path.getNextWaypoint();
        if (nextWaypoint != null) {
            // Simple movement towards next waypoint
            Location entityLoc = entity.getLocation();
            double dx = nextWaypoint.getX() - entityLoc.getX();
            double dy = Math.max(-0.5, Math.min(0.5, nextWaypoint.getY() - entityLoc.getY())); // clamp vertical
            double dz = nextWaypoint.getZ() - entityLoc.getZ();

            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance > 0.1) {
                double speed = getMovementSpeed();
                entity.setVelocity(new org.bukkit.util.Vector(dx / distance * speed, dy * 0.1, dz / distance * speed));
            }

            // Advance waypoint when close enough
            if (path.shouldAdvance(entityLoc, 0.6)) {
                boolean hasMore = path.advance();
                if (!hasMore) {
                    // End of path reached
                    entity.setVelocity(new org.bukkit.util.Vector(0, entity.getVelocity().getY(), 0));
                }
            }
        }
    }

    /**
     * Check if a location is walkable
     * @param location The location to check
     * @return true if the location is walkable
     */
    @Override
    public boolean isWalkable(Location location) {
        return navGraph.isWalkable(location);
    }

    /**
     * Get the movement speed for path following
     * @return Movement speed multiplier
     */
    @Override
    public double getMovementSpeed() {
        return 0.2; // Default movement speed
    }
}
