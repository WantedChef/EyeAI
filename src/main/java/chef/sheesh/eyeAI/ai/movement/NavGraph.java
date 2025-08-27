package chef.sheesh.eyeAI.ai.movement;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.*;

/**
 * Navigation graph for pathfinding.
 * Uses A* algorithm to find paths between locations in the game world.
 */
public class NavGraph {

    private final World world;
    private final int maxPathLength = 1000; // Maximum nodes to explore

    public NavGraph(World world) {
        this.world = world;
    }

    /**
     * Find a path between two locations using A* algorithm
     */
    public Path findPath(Location start, Location end) {
        if (start.getWorld() != end.getWorld()) {
            return new Path(Collections.emptyList()); // Different worlds
        }

        // Convert locations to nodes
        Node startNode = new Node(start.getBlockX(), start.getBlockY(), start.getBlockZ());
        Node endNode = new Node(end.getBlockX(), end.getBlockY(), end.getBlockZ());

        // A* algorithm
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(Node::getFCost));
        Set<Node> closedSet = new HashSet<>();
        Map<Node, Node> cameFrom = new HashMap<>();

        startNode.setGCost(0);
        startNode.setHCost(calculateHeuristic(startNode, endNode));
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.equals(endNode)) {
                return reconstructPath(cameFrom, current, start, end);
            }

            closedSet.add(current);

            for (Node neighbor : getNeighbors(current)) {
                if (closedSet.contains(neighbor)) {
                    continue;
                }

                double tentativeGCost = current.getGCost() + calculateDistance(current, neighbor);

                if (tentativeGCost < neighbor.getGCost() || !openSet.contains(neighbor)) {
                    cameFrom.put(neighbor, current);
                    neighbor.setGCost(tentativeGCost);
                    neighbor.setHCost(calculateHeuristic(neighbor, endNode));

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }

            // Prevent infinite loops
            if (closedSet.size() > maxPathLength) {
                break;
            }
        }

        // No path found, return empty path
        return new Path(Collections.emptyList());
    }

    /**
     * Get walkable neighbors of a node
     */
    private List<Node> getNeighbors(Node node) {
        List<Node> neighbors = new ArrayList<>();

        // Check all 8 directions (including diagonals)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) {
                    continue; // Skip center
                }

                int newX = node.getX() + dx;
                int newZ = node.getZ() + dz;

                // Check if we can walk there
                if (isWalkable(newX, node.getY(), newZ)) {
                    neighbors.add(new Node(newX, node.getY(), newZ));
                } else {
                    // Try jumping up
                    if (isWalkable(newX, node.getY() + 1, newZ)) {
                        neighbors.add(new Node(newX, node.getY() + 1, newZ));
                    }
                }
            }
        }

        // Check if we can move down
        if (isWalkable(node.getX(), node.getY() - 1, node.getZ())) {
            neighbors.add(new Node(node.getX(), node.getY() - 1, node.getZ()));
        }

        return neighbors;
    }

    /**
     * Check if a position is walkable
     */
    private boolean isWalkable(int x, int y, int z) {
        Block block = world.getBlockAt(x, y, z);
        Block above = world.getBlockAt(x, y + 1, z);
        Block below = world.getBlockAt(x, y - 1, z);

        // Check if the block itself is solid (can't walk through it)
        if (block.getType().isSolid()) {
            return false;
        }

        // Check if there's a solid block above (can't fit)
        if (above.getType().isSolid()) {
            return false;
        }

        // Check if there's nothing below (would fall)
        if (!below.getType().isSolid() && y > world.getMinHeight()) {
            return false;
        }

        // Avoid dangerous blocks
        return !isDangerous(block.getType());
    }

    /**
     * Check if a material is dangerous for walking
     */
    private boolean isDangerous(Material material) {
        return material == Material.LAVA ||
               material == Material.FIRE ||
               material.name().contains("MAGMA");
    }

    /**
     * Calculate heuristic distance (Manhattan distance)
     */
    private double calculateHeuristic(Node a, Node b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getZ() - b.getZ());
    }

    /**
     * Calculate actual distance between nodes
     */
    private double calculateDistance(Node a, Node b) {
        int dx = Math.abs(a.getX() - b.getX());
        int dz = Math.abs(a.getZ() - b.getZ());
        return Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * Reconstruct the path from the cameFrom map
     */
    private Path reconstructPath(Map<Node, Node> cameFrom, Node current, Location start, Location end) {
        List<Location> path = new ArrayList<>();
        path.add(end); // Add end location

        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.add(new Location(world, current.getX() + 0.5, current.getY(), current.getZ() + 0.5));
        }

        path.add(start); // Add start location
        Collections.reverse(path); // Reverse to get correct order

        return new Path(path);
    }

    /**
     * Internal node class for A* algorithm
     */
    private static class Node {
        private final int x, y, z;
        private double gCost = Double.MAX_VALUE;
        private double hCost = 0;

        public Node(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public int getX() { return x; }
        public int getY() { return y; }
        public int getZ() { return z; }

        public double getGCost() { return gCost; }
        public void setGCost(double gCost) { this.gCost = gCost; }

        @SuppressWarnings("unused") // Method available for external use or future implementation
        public double getHCost() { return hCost; }
        public void setHCost(double hCost) { this.hCost = hCost; }

        public double getFCost() { return gCost + hCost; }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Node)) {
                return false;
            }
            Node node = (Node) obj;
            return x == node.x && y == node.y && z == node.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }
    }
}
