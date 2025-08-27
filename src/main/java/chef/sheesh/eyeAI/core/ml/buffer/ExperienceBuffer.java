package chef.sheesh.eyeAI.core.ml.buffer;

import chef.sheesh.eyeAI.core.ml.models.Experience;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * An experience buffer using Prioritized Experience Replay (PER).
 * This implementation uses a SumTree to store experience priorities, allowing for
 * efficient, prioritized sampling. It also calculates importance sampling weights.
 */
public class ExperienceBuffer {

    // Hyperparameters for PER
    private static final double ALPHA = 0.6; // Prioritization exponent
    private static final double BETA_START = 0.4; // Initial importance-sampling exponent
    private static final double BETA_INCREMENT = 0.001; // Beta increment per sampling
    private static final double EPSILON = 0.01; // Small constant to ensure non-zero priority

    private final int capacity;
    private final SumTree priorityTree;
    private final Experience[] experiences;
    private final Random random = new Random();

    private double beta = BETA_START;
    private double maxPriority = 1.0;

    /**
     * A record to hold a batch of sampled experiences along with their
     * tree indices and importance sampling weights.
     */
    public record SampledBatch(List<Experience> experiences, int[] treeIndices, double[] weights) {}

    /**
     * Constructs an ExperienceBuffer with a given capacity.
     *
     * @param capacity The maximum number of experiences to store.
     */
    public ExperienceBuffer(int capacity) {
        this.capacity = capacity;
        this.priorityTree = new SumTree(capacity);
        this.experiences = new Experience[capacity];
    }

    /**
     * Adds a new experience to the buffer with maximum priority.
     * If the buffer is at capacity, the oldest experience is overwritten.
     *
     * @param exp The experience to add.
     */
    public void addExperience(Experience exp) {
        int dataIndex = priorityTree.getWriteIndex();
        experiences[dataIndex] = exp;
        priorityTree.add(maxPriority);
    }

    /**
     * Samples a batch of experiences from the buffer using Prioritized Experience Replay.
     *
     * @param batchSize The number of experiences to sample.
     * @return A {@link SampledBatch} containing experiences, their indices, and IS weights.
     */
    public SampledBatch sampleBatch(int batchSize) {
        List<Experience> batch = new ArrayList<>(batchSize);
        int[] treeIndices = new int[batchSize];
        double[] weights = new double[batchSize];

        double totalPriority = priorityTree.getTotalPriority();
        double prioritySegment = totalPriority / batchSize;

        // Anneal beta
        beta = Math.min(1.0, beta + BETA_INCREMENT);

        double maxWeight = 0.0;

        for (int i = 0; i < batchSize; i++) {
            double value = random.nextDouble() * prioritySegment + i * prioritySegment;
            double[] sample = priorityTree.get(value);

            int dataIndex = (int) sample[0];
            double priority = sample[1];
            int treeIndex = (int) sample[2];

            batch.add(experiences[dataIndex]);
            treeIndices[i] = treeIndex;

            // Calculate importance sampling weight
            double samplingProbability = priority / totalPriority;
            double weight = Math.pow(priorityTree.getSize() * samplingProbability, -beta);
            weights[i] = weight;
            if (weight > maxWeight) {
                maxWeight = weight;
            }
        }

        // Normalize weights
        if (maxWeight > 0) {
            for (int i = 0; i < batchSize; i++) {
                weights[i] /= maxWeight;
            }
        }

        return new SampledBatch(batch, treeIndices, weights);
    }

    /**
     * Updates the priorities of the sampled experiences.
     *
     * @param treeIndices The tree indices of the experiences to update.
     * @param tdErrors    The temporal difference errors for each experience.
     */
    public void updatePriorities(int[] treeIndices, double[] tdErrors) {
        for (int i = 0; i < treeIndices.length; i++) {
            double priority = Math.pow(Math.abs(tdErrors[i]) + EPSILON, ALPHA);
            priorityTree.update(treeIndices[i], priority);

            if (priority > maxPriority) {
                maxPriority = priority;
            }
        }
    }

    /**
     * Gets the current number of experiences in the buffer.
     *
     * @return The size of the buffer.
     */
    public int getSize() {
        return priorityTree.getSize();
    }
}
