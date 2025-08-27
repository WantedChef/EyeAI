package chef.sheesh.eyeAI.core.ml.algorithms;

import java.util.Arrays;

/**
 * SumTree data structure for efficient priority sampling in Prioritized Experience Replay.
 * Allows O(log n) time complexity for both adding experiences and sampling by priority.
 */
public class SumTree {
    private final double[] tree;
    private final int capacity;
    private final int dataPointer;

    /**
     * Constructor for SumTree.
     * @param capacity Maximum number of elements the tree can hold
     */
    public SumTree(int capacity) {
        this.capacity = capacity;
        this.tree = new double[2 * capacity - 1]; // Tree structure: root + internal nodes + leaves
        this.dataPointer = 0;
    }

    /**
     * Add a priority value to the tree.
     * @param priority The priority value to add
     * @param index The index in the data array where this priority belongs
     */
    public void add(double priority, int index) {
        int treeIndex = index + capacity - 1; // Convert data index to tree index
        update(treeIndex, priority);
    }

    /**
     * Update the priority of an element in the tree.
     * @param treeIndex Index in the tree array
     * @param priority New priority value
     */
    public void update(int treeIndex, double priority) {
        double change = priority - tree[treeIndex];
        tree[treeIndex] = priority;

        // Propagate the change up the tree
        while (treeIndex != 0) {
            treeIndex = (treeIndex - 1) / 2; // Move to parent
            tree[treeIndex] += change;
        }
    }

    /**
     * Get the priority value at a specific tree index.
     * @param treeIndex Index in the tree array
     * @return Priority value
     */
    public double get(int treeIndex) {
        return tree[treeIndex];
    }

    /**
     * Get the total sum of all priorities in the tree.
     * @return Total sum of priorities
     */
    public double total() {
        return tree[0]; // Root contains the total sum
    }

    /**
     * Sample an index based on priority (proportional to priority values).
     * @param value A random value between 0 and total()
     * @return The sampled tree index
     */
    public int getLeaf(double value) {
        int parent = 0;

        while (true) {
            int leftChild = 2 * parent + 1;
            int rightChild = leftChild + 1;

            // If we've reached a leaf node
            if (leftChild >= tree.length) {
                return parent;
            }

            // Decide whether to go left or right
            if (value <= tree[leftChild]) {
                parent = leftChild;
            } else {
                value -= tree[leftChild];
                parent = rightChild;
            }
        }
    }

    /**
     * Get the data index corresponding to a tree index.
     * @param treeIndex Index in the tree
     * @return Corresponding data index
     */
    public int getDataIndex(int treeIndex) {
        return treeIndex - capacity + 1;
    }

    /**
     * Get the priority of a leaf node by its data index.
     * @param dataIndex Index in the data array
     * @return Priority value
     */
    public double getPriority(int dataIndex) {
        int treeIndex = dataIndex + capacity - 1;
        return tree[treeIndex];
    }

    /**
     * Check if the tree is full.
     * @return true if tree is at capacity
     */
    public boolean isFull() {
        return dataPointer >= capacity;
    }

    /**
     * Get the current number of elements in the tree.
     * @return Number of elements
     */
    public int size() {
        return Math.min(dataPointer, capacity);
    }

    /**
     * Clear all elements from the tree.
     */
    public void clear() {
        Arrays.fill(tree, 0.0);
    }

    /**
     * Get the capacity of the tree.
     * @return Maximum capacity
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Print the tree structure (for debugging).
     */
    public void printTree() {
        System.out.println("SumTree structure:");
        int level = 0;
        int levelSize = 1;
        int index = 0;

        while (index < tree.length) {
            System.out.print("Level " + level + ": ");
            for (int i = 0; i < levelSize && index < tree.length; i++) {
                System.out.printf("%.3f ", tree[index++]);
            }
            System.out.println();
            level++;
            levelSize *= 2;
        }
    }
}
