package chef.sheesh.eyeAI.core.ml.buffer;

import java.util.Arrays;

/**
 * A SumTree data structure for Prioritized Experience Replay.
 * This tree allows for efficient sampling of experiences based on their priorities.
 * Each leaf node holds the priority of an experience, and each internal node
 * holds the sum of the priorities of its children.
 */
public class SumTree {

    private final double[] tree;
    private final int capacity;
    private int size = 0;
    private int writeIndex = 0;

    /**
     * Constructs a SumTree with a given capacity.
     * The tree size is 2 * capacity - 1.
     *
     * @param capacity The number of leaf nodes, which corresponds to the buffer capacity.
     */
    public SumTree(int capacity) {
        this.capacity = capacity;
        this.tree = new double[2 * capacity - 1];
    }

    /**
     * Adds a new priority to the tree. If the tree is full, it overwrites the oldest entry.
     *
     * @param priority The priority of the new experience.
     */
    public void add(double priority) {
        int treeIndex = writeIndex + capacity - 1;
        update(treeIndex, priority);

        writeIndex = (writeIndex + 1) % capacity;
        if (size < capacity) {
            size++;
        }
    }

    /**
     * Updates the priority of a node in the tree and propagates the change upwards.
     *
     * @param treeIndex The index in the tree array to update.
     * @param priority  The new priority value.
     */
    public void update(int treeIndex, double priority) {
        double change = priority - tree[treeIndex];
        tree[treeIndex] = priority;
        propagate(treeIndex, change);
    }

    /**
     * Propagates the change in priority up the tree to the root.
     *
     * @param treeIndex The starting index of the change.
     * @param change    The change in priority.
     */
    private void propagate(int treeIndex, double change) {
        int parentIndex = (treeIndex - 1) / 2;
        tree[parentIndex] += change;
        if (parentIndex != 0) {
            propagate(parentIndex, change);
        }
    }

    /**
     * Gets a leaf index and its priority for a given value.
     * This is used for sampling.
     *
     * @param value A value between 0 and the total sum of priorities.
     * @return An array containing [leafIndex, priority, treeIndex].
     */
    public double[] get(double value) {
        int parentIndex = 0;
        while (true) {
            int leftChildIndex = 2 * parentIndex + 1;
            int rightChildIndex = leftChildIndex + 1;

            if (leftChildIndex >= tree.length) { // parent is a leaf
                break;
            }

            if (value <= tree[leftChildIndex]) {
                parentIndex = leftChildIndex;
            } else {
                value -= tree[leftChildIndex];
                parentIndex = rightChildIndex;
            }
        }
        int dataIndex = parentIndex - (capacity - 1);
        return new double[]{dataIndex, tree[parentIndex], parentIndex};
    }

    /**
     * @return The total sum of all priorities.
     */
    public double getTotalPriority() {
        return tree[0];
    }

    /**
     * @return The current number of entries in the tree.
     */
    public int getSize() {
        return size;
    }

    /**
     * @return The index of the oldest entry.
     */
    public int getWriteIndex() {
        return writeIndex;
    }
    
    /**
     * @return The capacity of the tree.
     */
    public int getCapacity() {
        return capacity;
    }
}
