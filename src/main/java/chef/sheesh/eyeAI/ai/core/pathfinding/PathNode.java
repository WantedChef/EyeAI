package chef.sheesh.eyeAI.ai.core.pathfinding;

import org.bukkit.Location;

/**
 * Represents a node in a pathfinding search space.
 * Used by the A* algorithm.
 */
public class PathNode implements Comparable<PathNode> {

    public final Location location;
    public PathNode parent;
    public double gCost; // Cost from start to this node
    public double hCost; // Heuristic cost from this node to end

    public PathNode(Location location) {
        this.location = location;
    }

    public double getFCost() {
        return gCost + hCost;
    }

    @Override
    public int compareTo(PathNode other) {
        return Double.compare(this.getFCost(), other.getFCost());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PathNode pathNode = (PathNode) obj;
        return location.equals(pathNode.location);
    }

    @Override
    public int hashCode() {
        return location.hashCode();
    }
}
