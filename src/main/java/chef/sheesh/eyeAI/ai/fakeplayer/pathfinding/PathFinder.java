package chef.sheesh.eyeAI.ai.fakeplayer.pathfinding;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Advanced pathfinding system for fake players
 */
public class PathFinder {

    private final World world;
    private final int maxSearchDistance;
    private final int maxIterations;
    private final boolean avoidWater;
    private final boolean avoidLava;
    private final boolean avoidCliffs;

    public PathFinder(World world) {
        this(world, 32, 1000, true, true, true);
    }

    public PathFinder(World world, int maxSearchDistance, int maxIterations,
                     boolean avoidWater, boolean avoidLava, boolean avoidCliffs) {
        this.world = world;
        this.maxSearchDistance = maxSearchDistance;
        this.maxIterations = maxIterations;
        this.avoidWater = avoidWater;
        this.avoidLava = avoidLava;
        this.avoidCliffs = avoidCliffs;
    }

    /**
     * Find a path from start to target
     */
    public List<Location> findPath(Location start, Location target) {
        if (start.getWorld() != target.getWorld()) {
            return Collections.emptyList();
        }

        if (start.distance(target) > maxSearchDistance) {
            return createDirectPath(start, target);
        }

        return aStarPathfinding(start, target);
    }

    /**
     * A* pathfinding algorithm
     */
    private List<Location> aStarPathfinding(Location start, Location target) {
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(Node::getF));
        Set<Vector> closedSet = new HashSet<>();
        Map<Vector, Node> nodeMap = new HashMap<>();

        Vector startVec = start.toVector();
        Vector targetVec = target.toVector();

        Node startNode = new Node(startVec, null, 0, heuristic(startVec, targetVec));
        openSet.add(startNode);
        nodeMap.put(startVec, startNode);

        int iterations = 0;

        while (!openSet.isEmpty() && iterations < maxIterations) {
            Node current = openSet.poll();
            Vector currentVec = current.getPosition();

            if (currentVec.distance(targetVec) < 1.0) {
                return reconstructPath(current, start, target);
            }

            closedSet.add(currentVec);

            // Check all 8 directions (including diagonals)
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) {
                        continue;
                    }

                    Vector neighborVec = new Vector(currentVec.getX() + dx, currentVec.getY(), currentVec.getZ() + dz);

                    if (closedSet.contains(neighborVec)) {
                        continue;
                    }

                    Location neighborLoc = neighborVec.toLocation(world);
                    if (!isWalkable(neighborLoc)) {
                        continue;
                    }

                    double tentativeG = current.getG() + (dx != 0 && dz != 0 ? 1.414 : 1.0); // Diagonal cost

                    Node neighbor = nodeMap.get(neighborVec);
                    if (neighbor == null) {
                        neighbor = new Node(neighborVec, current, tentativeG, heuristic(neighborVec, targetVec));
                        nodeMap.put(neighborVec, neighbor);
                        openSet.add(neighbor);
                    } else if (tentativeG < neighbor.getG()) {
                        neighbor.setParent(current);
                        neighbor.setG(tentativeG);
                        neighbor.setF(neighbor.getG() + neighbor.getH());
                        // Re-add to priority queue with updated cost
                        openSet.remove(neighbor);
                        openSet.add(neighbor);
                    }
                }
            }

            iterations++;
        }

        // If A* fails, return direct path
        return createDirectPath(start, target);
    }

    /**
     * Create a simple direct path when A* fails
     */
    private List<Location> createDirectPath(Location start, Location target) {
        List<Location> path = new ArrayList<>();
        Vector direction = target.toVector().subtract(start.toVector()).normalize();
        double distance = start.distance(target);

        for (double d = 0; d < distance; d += 1.0) {
            Vector point = start.toVector().add(direction.clone().multiply(d));
            Location loc = point.toLocation(world);
            loc.setY(start.getY()); // Keep same Y level for simplicity
            path.add(loc);
        }

        path.add(target);
        return path;
    }

    /**
     * Heuristic function for A* (Manhattan distance)
     */
    private double heuristic(Vector from, Vector to) {
        return Math.abs(from.getX() - to.getX()) + Math.abs(from.getZ() - to.getZ());
    }

    /**
     * Check if a location is walkable
     */
    private boolean isWalkable(Location location) {
        Block block = location.getBlock();
        Block above = location.clone().add(0, 1, 0).getBlock();
        Block below = location.clone().add(0, -1, 0).getBlock();

        // Check if block is solid
        if (block.getType().isSolid() && block.getType() != Material.LADDER && block.getType() != Material.VINE) {
            return false;
        }

        // Check if block above is solid (can't fit)
        if (above.getType().isSolid()) {
            return false;
        }

        // Check if there's a block below (need support)
        if (!below.getType().isSolid() && below.getType() != Material.WATER) {
            return false;
        }

        // Avoid dangerous blocks
        if (avoidWater && (block.getType() == Material.WATER || above.getType() == Material.WATER)) {
            return false;
        }

        if (avoidLava && (block.getType() == Material.LAVA || above.getType() == Material.LAVA)) {
            return false;
        }

        // Avoid cliffs (sharp drops)
        if (avoidCliffs && isNearCliff(location)) {
            return false;
        }

        return true;
    }

    /**
     * Check if location is near a cliff
     */
    private boolean isNearCliff(Location location) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) {
                    continue;
                }

                Location check = location.clone().add(x, -2, z);
                if (!check.getBlock().getType().isSolid()) {
                    return true; // 2 blocks down is air, potential cliff
                }
            }
        }
        return false;
    }

    /**
     * Reconstruct path from A* result
     */
    private List<Location> reconstructPath(Node endNode, Location start, Location target) {
        List<Location> path = new ArrayList<>();
        Node current = endNode;

        while (current != null) {
            Vector pos = current.getPosition();
            Location loc = new Location(world, pos.getX(), start.getY(), pos.getZ());
            path.add(0, loc); // Add at beginning to reverse order
            current = current.getParent();
        }

        // Add the target if not already included
        if (!path.isEmpty() && !path.get(path.size() - 1).equals(target)) {
            path.add(target);
        }

        return path;
    }

    /**
     * Smooth the path to make it more natural
     */
    public List<Location> smoothPath(List<Location> path) {
        if (path.size() <= 2) {
            return path;
        }

        List<Location> smoothed = new ArrayList<>();
        smoothed.add(path.get(0)); // Always include start

        for (int i = 1; i < path.size() - 1; i++) {
            Location prev = smoothed.get(smoothed.size() - 1);
            Location current = path.get(i);
            Location next = path.get(i + 1);

            // Check if we can skip this waypoint
            if (hasLineOfSight(prev, next)) {
                // Skip current waypoint
                continue;
            } else {
                // Include current waypoint
                smoothed.add(current);
            }
        }

        // Always include target
        smoothed.add(path.get(path.size() - 1));

        return smoothed;
    }

    /**
     * Check line of sight between two locations
     */
    private boolean hasLineOfSight(Location from, Location to) {
        Vector direction = to.toVector().subtract(from.toVector()).normalize();
        double distance = from.distance(to);

        for (double d = 0; d < distance; d += 0.5) {
            Vector point = from.toVector().add(direction.clone().multiply(d));
            Location check = point.toLocation(world);

            if (check.getBlock().getType().isSolid()) {
                return false; // Blocked by solid block
            }
        }

        return true;
    }

    /**
     * Node class for A* algorithm
     */
    private static class Node {
        private final Vector position;
        private Node parent;
        private double g; // Cost from start
        private double h; // Heuristic cost to target
        private double f; // Total cost

        public Node(Vector position, Node parent, double g, double h) {
            this.position = position;
            this.parent = parent;
            this.g = g;
            this.h = h;
            this.f = g + h;
        }

        public Vector getPosition() { return position; }
        public Node getParent() { return parent; }
        public double getG() { return g; }
        public double getH() { return h; }
        public double getF() { return f; }

        public void setParent(Node parent) { this.parent = parent; }
        public void setG(double g) { this.g = g; }
        public void setF(double f) { this.f = f; }
    }
}
