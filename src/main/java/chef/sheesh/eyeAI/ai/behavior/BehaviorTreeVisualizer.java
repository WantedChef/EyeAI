package chef.sheesh.eyeAI.ai.behavior;

import chef.sheesh.eyeAI.ai.behavior.nodes.CompositeNode;
import chef.sheesh.eyeAI.ai.behavior.nodes.DecoratorNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for visualizing behavior trees as text diagrams.
 * Useful for debugging and understanding tree structure.
 */
public class BehaviorTreeVisualizer {

    /**
     * Generate a text-based visualization of a behavior tree
     */
    public static String visualize(IBehaviorTree tree) {
        return visualize(tree, "", true);
    }

    /**
     * Generate a text-based visualization of a behavior tree with options
     */
    public static String visualize(IBehaviorTree tree, String prefix, boolean isLast) {
        StringBuilder sb = new StringBuilder();

        // Add the current node
        sb.append(prefix);
        if (!prefix.isEmpty()) {
            sb.append(isLast ? "└── " : "├── ");
        }
        sb.append(formatNode(tree));
        sb.append("\n");

        // Add children recursively
        List<IBehaviorTree> children = getChildren(tree);
        for (int i = 0; i < children.size(); i++) {
            boolean lastChild = i == children.size() - 1;
            String childPrefix = prefix + (prefix.isEmpty() ? "" : (isLast ? "    " : "│   "));
            sb.append(visualize(children.get(i), childPrefix, lastChild));
        }

        return sb.toString();
    }

    /**
     * Generate a compact single-line representation
     */
    public static String visualizeCompact(IBehaviorTree tree) {
        return visualizeCompact(tree, new ArrayList<>());
    }

    private static String visualizeCompact(IBehaviorTree tree, List<String> path) {
        path.add(tree.getName());

        List<IBehaviorTree> children = getChildren(tree);
        if (children.isEmpty()) {
            // Leaf node
            return String.join(" → ", path);
        } else if (children.size() == 1) {
            // Single child (decorator)
            return visualizeCompact(children.get(0), path);
        } else {
            // Multiple children (composite)
            List<String> childPaths = new ArrayList<>();
            for (IBehaviorTree child : children) {
                List<String> childPath = new ArrayList<>(path);
                childPaths.add(visualizeCompact(child, childPath));
            }
            return String.join(" | ", childPaths);
        }
    }

    /**
     * Format a node for display
     */
    private static String formatNode(IBehaviorTree tree) {
        String status = tree.isRunning() ? "[RUNNING]" : "[IDLE]";
        return String.format("%s %s %s",
               tree.getCategory(),
               tree.getName(),
               status);
    }

    /**
     * Get children of a node (handles different node types)
     */
    private static List<IBehaviorTree> getChildren(IBehaviorTree tree) {
        if (tree instanceof CompositeNode composite) {
            return composite.getChildren();
        } else if (tree instanceof DecoratorNode decorator) {
            List<IBehaviorTree> children = new ArrayList<>();
            if (decorator.getChild() != null) {
                children.add(decorator.getChild());
            }
            return children;
        }
        return List.of(); // Leaf node
    }

    /**
     * Generate a detailed report with execution statistics
     */
    public static String generateReport(IBehaviorTree tree) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Behavior Tree Report ===\n");
        sb.append("Tree: ").append(tree.getName()).append("\n");
        sb.append("Category: ").append(tree.getCategory()).append("\n");
        sb.append("Description: ").append(tree.getDescription()).append("\n");
        sb.append("Running: ").append(tree.isRunning()).append("\n");

        // Add structure info
        sb.append("\nStructure:\n");
        sb.append(visualize(tree));

        // Add compact view
        sb.append("\nCompact: ");
        sb.append(visualizeCompact(tree));
        sb.append("\n");

        return sb.toString();
    }

    /**
     * Find nodes by category
     */
    public static List<IBehaviorTree> findNodesByCategory(IBehaviorTree tree, String category) {
        List<IBehaviorTree> result = new ArrayList<>();

        if (tree.getCategory().equals(category)) {
            result.add(tree);
        }

        // Search children
        for (IBehaviorTree child : getChildren(tree)) {
            result.addAll(findNodesByCategory(child, category));
        }

        return result;
    }

    /**
     * Count nodes by type
     */
    public static java.util.Map<String, Integer> countNodesByCategory(IBehaviorTree tree) {
        java.util.Map<String, Integer> counts = new java.util.HashMap<>();

        counts.put(tree.getCategory(),
                   counts.getOrDefault(tree.getCategory(), 0) + 1);

        for (IBehaviorTree child : getChildren(tree)) {
            java.util.Map<String, Integer> childCounts = countNodesByCategory(child);
            for (var entry : childCounts.entrySet()) {
                counts.put(entry.getKey(),
                          counts.getOrDefault(entry.getKey(), 0) + entry.getValue());
            }
        }

        return counts;
    }
}
