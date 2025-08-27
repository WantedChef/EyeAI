package chef.sheesh.eyeAI.ai.core.pathfinding;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.*;

/**
 * An optimized pathfinder using the A* algorithm.
 */
public class OptimizedPathfinder {

    private final World world;
    private final int maxIterations;

    public OptimizedPathfinder(World world, int maxIterations) {
        this.world = world;
        this.maxIterations = maxIterations;
    }

    public List<Location> findPath(Location start, Location end) {
        PriorityQueue<PathNode> openSet = new PriorityQueue<>();
        Set<Location> closedSet = new HashSet<>();

        PathNode startNode = new PathNode(start);
        startNode.gCost = 0;
        startNode.hCost = heuristic(start, end);
        openSet.add(startNode);

        int iterations = 0;
        while (!openSet.isEmpty() && iterations < maxIterations) {
            iterations++;
            PathNode currentNode = openSet.poll();

            if (currentNode.location.distance(end) < 1.5) {
                return reconstructPath(currentNode);
            }

            closedSet.add(currentNode.location);

            for (PathNode neighbor : getNeighbors(currentNode)) {
                if (closedSet.contains(neighbor.location)) {
                    continue;
                }

                double tentativeGCost = currentNode.gCost + currentNode.location.distance(neighbor.location);

                if (tentativeGCost < neighbor.gCost || !openSet.contains(neighbor)) {
                    neighbor.gCost = tentativeGCost;
                    neighbor.hCost = heuristic(neighbor.location, end);
                    neighbor.parent = currentNode;

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }

        return null; // No path found
    }

    private List<PathNode> getNeighbors(PathNode node) {
        List<PathNode> neighbors = new ArrayList<>();
        Location loc = node.location;

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }

                    Location neighborLoc = loc.clone().add(x, y, z);
                    if (isWalkable(neighborLoc)) {
                        neighbors.add(new PathNode(neighborLoc));
                    }
                }
            }
        }

        return neighbors;
    }

    private boolean isWalkable(Location loc) {
        // A very simple check. A real implementation would be much more complex.
        Material blockType = loc.getBlock().getType();
        Material blockBelowType = loc.clone().subtract(0, 1, 0).getBlock().getType();

        return !blockType.isSolid() && blockBelowType.isSolid();
    }

    private double heuristic(Location a, Location b) {
        // Euclidean distance
        return a.distance(b);
    }

    private List<Location> reconstructPath(PathNode endNode) {
        List<Location> path = new ArrayList<>();
        PathNode currentNode = endNode;
        while (currentNode != null) {
            path.add(currentNode.location);
            currentNode = currentNode.parent;
        }
        Collections.reverse(path);
        return path;
    }
}
