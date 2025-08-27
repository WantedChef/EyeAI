package chef.sheesh.eyeAI.ai.movement;

import org.bukkit.Location;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a path consisting of waypoints between two locations.
 * Provides methods for path following and smoothing.
 */
public class Path implements Iterable<Location> {

    private final List<Location> waypoints;
    private int currentIndex = 0;

    public Path(List<Location> waypoints) {
        this.waypoints = waypoints != null ? List.copyOf(waypoints) : Collections.emptyList();
    }

    /**
     * Check if the path is empty (no waypoints)
     */
    public boolean isEmpty() {
        return waypoints.isEmpty();
    }

    /**
     * Get the total length of the path
     */
    public int length() {
        return waypoints.size();
    }

    /**
     * Get the next waypoint to move towards
     */
    public Location getNextWaypoint() {
        if (currentIndex < waypoints.size()) {
            return waypoints.get(currentIndex);
        }
        return null;
    }

    /**
     * Move to the next waypoint
     */
    public boolean advance() {
        if (currentIndex < waypoints.size() - 1) {
            currentIndex++;
            return true;
        }
        return false;
    }

    /**
     * Check if we've reached the end of the path
     */
    public boolean isFinished() {
        return currentIndex >= waypoints.size();
    }

    /**
     * Reset the path to the beginning
     */
    public void reset() {
        currentIndex = 0;
    }

    /**
     * Get the current progress as a percentage (0.0 to 1.0)
     */
    public double getProgress() {
        if (waypoints.isEmpty()) {
            return 1.0;
        }
        return (double) currentIndex / waypoints.size();
    }

    /**
     * Get the distance to the next waypoint
     */
    public double getDistanceToNext(Location currentLocation) {
        Location next = getNextWaypoint();
        if (next != null) {
            return currentLocation.distance(next);
        }
        return 0;
    }

    /**
     * Check if we're close enough to the current waypoint to advance
     */
    public boolean shouldAdvance(Location currentLocation, double threshold) {
        return getDistanceToNext(currentLocation) < threshold;
    }

    /**
     * Get the final destination of the path
     */
    public Location getDestination() {
        if (!waypoints.isEmpty()) {
            return waypoints.get(waypoints.size() - 1);
        }
        return null;
    }

    /**
     * Get the starting point of the path
     */
    public Location getStart() {
        if (!waypoints.isEmpty()) {
            return waypoints.get(0);
        }
        return null;
    }

    /**
     * Get all waypoints
     */
    public List<Location> getWaypoints() {
        return waypoints;
    }

    /**
     * Create a smoothed version of the path (remove unnecessary waypoints)
     */
    public Path smooth() {
        if (waypoints.size() <= 2) {
            return new Path(waypoints);
        }

        List<Location> smoothed = new java.util.ArrayList<>();
        smoothed.add(waypoints.get(0)); // Always include start

        for (int i = 1; i < waypoints.size() - 1; i++) {
            Location prev = smoothed.get(smoothed.size() - 1);
            Location current = waypoints.get(i);
            Location next = waypoints.get(i + 1);

            // Check if we can skip this waypoint
            if (!canSkipWaypoint(prev, current, next)) {
                smoothed.add(current);
            }
        }

        smoothed.add(waypoints.get(waypoints.size() - 1)); // Always include end

        return new Path(smoothed);
    }

    /**
     * Check if a waypoint can be skipped in path smoothing
     */
    private boolean canSkipWaypoint(Location prev, Location current, Location next) {
        // Calculate distances
        double prevToCurrent = prev.distance(current);
        double currentToNext = current.distance(next);
        double prevToNext = prev.distance(next);

        // If direct distance is similar to path via current, we can skip
        return Math.abs((prevToCurrent + currentToNext) - prevToNext) < 1.0;
    }

    @Override
    public Iterator<Location> iterator() {
        return waypoints.iterator();
    }

    @Override
    public String toString() {
        return "Path{" +
                "length=" + length() +
                ", currentIndex=" + currentIndex +
                ", finished=" + isFinished() +
                ", progress=" + String.format("%.2f", getProgress() * 100) + "%" +
                '}';
    }
}
