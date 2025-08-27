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
    // Small LRU cache for walkability checks to reduce block lookups
    private final Map<Long, Boolean> walkableCache = new LinkedHashMap<Long, Boolean>(4096, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, Boolean> eldest) {
            return size() > 4096;
        }
    };

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

        // Quick straight-line check (cheap visibility/pathability test)
        if (isStraightLineWalkable(startNode, endNode)) {
            List<Location> direct = new ArrayList<>();
            direct.add(new Location(world, startNode.getX() + 0.5, startNode.getY(), startNode.getZ() + 0.5));
            direct.add(new Location(world, endNode.getX() + 0.5, endNode.getY(), endNode.getZ() + 0.5));
            return new Path(direct);
        }

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
            // Maximum nodes to explore
            int maxPathLength = 1000;
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

        // Check all 8 horizontal directions (including diagonals)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) {
                    continue; // Skip center
                }

                int nx = node.getX() + dx;
                int ny = node.getY();
                int nz = node.getZ() + dz;

                // Same-level move
                if (isWalkable(nx, ny, nz)) {
                    neighbors.add(new Node(nx, ny, nz));
                } else {
                    // Try step up by 1
                    if (isWalkable(nx, ny + 1, nz)) {
                        neighbors.add(new Node(nx, ny + 1, nz));
                    }
                }
            }
        }

        // Check stepping down by 1 if the block below is walkable platform
        if (isWalkable(node.getX(), node.getY() - 1, node.getZ())) {
            neighbors.add(new Node(node.getX(), node.getY() - 1, node.getZ()));
        }

        return neighbors;
    }

    /**
     * Check if a position is walkable
     */
    private boolean isWalkable(int x, int y, int z) {
        long key = (((long) x & 0x3FFFFFF) << 38) | (((long) z & 0x3FFFFFF) << 12) | (y & 0xFFFL);
        Boolean cached = walkableCache.get(key);
        if (cached != null) { return cached; }

        Block block = world.getBlockAt(x, y, z);
        Block above = world.getBlockAt(x, y + 1, z);
        Block below = world.getBlockAt(x, y - 1, z);

        boolean walkable = true;

        // Check if the block itself is solid (can't walk through it)
        if (block.getType().isSolid()) {
            walkable = false;
        }

        // Check if there's a solid block above (can't fit)
        if (walkable && above.getType().isSolid()) {
            walkable = false;
        }

        // Check if there's nothing below (would fall)
        if (walkable && !below.getType().isSolid() && y > world.getMinHeight()) {
            walkable = false;
        }

        // Avoid dangerous blocks
        if (walkable && isDangerous(block.getType())) {
            walkable = false;
        }

        walkableCache.put(key, walkable);
        return walkable;
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
     * Calculate heuristic distance using weighted 3D Manhattan distance
     */
    private double calculateHeuristic(Node a, Node b) {
        int dx = Math.abs(a.getX() - b.getX());
        int dz = Math.abs(a.getZ() - b.getZ());
        int dy = Math.abs(a.getY() - b.getY());
        // Vertical movement generally costs more
        return dx + dz + (dy * 1.5);
    }

    /**
     * Calculate actual distance between nodes
     */
    private double calculateDistance(Node a, Node b) {
        int dx = Math.abs(a.getX() - b.getX());
        int dy = Math.abs(a.getY() - b.getY());
        int dz = Math.abs(a.getZ() - b.getZ());
        // Diagonals and vertical steps are slightly more expensive
        double base = Math.sqrt(dx * dx + dz * dz);
        return base + (dy * 1.2);
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
     * Public walkability check for a Bukkit Location
     */
    public boolean isWalkable(Location location) {
        return isWalkable(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    /**
     * Cheap straight-line check by stepping towards target and verifying walkability
     */
    private boolean isStraightLineWalkable(Node start, Node end) {
        int x0 = start.getX();
        int y0 = start.getY();
        int z0 = start.getZ();
        int x1 = end.getX();
        int y1 = end.getY();
        int z1 = end.getZ();

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int dz = Math.abs(z1 - z0);

        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int sz = z0 < z1 ? 1 : -1;

        int err1 = dx - dz;
        int err2;

        int x = x0, y = y0, z = z0;
        int steps = 0;
        int maxSteps = Math.max(dx + dz + dy, 1);
        while (true) {
            if (!isWalkable(x, y, z)) { return false; }
            if (x == x1 && y == y1 && z == z1) { return true; }
            if (steps++ > maxSteps) { return false; }

            int e2 = 2 * err1;
            if (e2 > -dz) { err1 -= dz; x += sx; }
            if (e2 < dx)  { err1 += dx; z += sz; }
            // Adjust Y gradually towards target
            if (y != y1) { y += sy; }
        }
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
