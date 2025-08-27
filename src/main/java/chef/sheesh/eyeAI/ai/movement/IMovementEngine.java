package chef.sheesh.eyeAI.ai.movement;

import org.bukkit.Location;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for movement engine to break circular dependencies
 */
public interface IMovementEngine {

    /**
     * Calculate a path between two locations asynchronously
     * @param from Starting location
     * @param to Target location
     * @return CompletableFuture that resolves to the computed path
     */
    CompletableFuture<Path> computePathAsync(Location from, Location to);

    /**
     * Calculate a path and execute callback on main thread
     * @param from Starting location
     * @param to Target location
     * @param callback Callback to execute with the computed path
     */
    void computePathAsync(Location from, Location to, java.util.function.Consumer<Path> callback);

    /**
     * Move an entity along a path
     * @param entity The entity to move
     * @param path The path to follow
     */
    void moveAlongPath(org.bukkit.entity.Entity entity, Path path);

    /**
     * Check if a location is walkable
     * @param location The location to check
     * @return true if the location is walkable
     */
    boolean isWalkable(Location location);

    /**
     * Get the movement speed for path following
     * @return Movement speed multiplier
     */
    double getMovementSpeed();
}

