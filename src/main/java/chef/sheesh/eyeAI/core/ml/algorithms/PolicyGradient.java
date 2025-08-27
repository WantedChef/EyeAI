package chef.sheesh.eyeAI.core.ml.algorithms;

import chef.sheesh.eyeAI.core.ml.models.EnhancedExperience;
import chef.sheesh.eyeAI.core.ml.models.Experience;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Policy gradient algorithm implementation for reinforcement learning.
 * This algorithm learns a policy directly rather than learning a value function.
 */
public class PolicyGradient implements ILearningAlgorithm {

    private static final Logger logger = Logger.getLogger(PolicyGradient.class.getName());

    private final List<EnhancedExperience> experienceBuffer;
    private final double learningRate;
    private final int batchSize;
    private double explorationRate;

    public PolicyGradient(double learningRate, int batchSize) {
        this.learningRate = learningRate;
        this.batchSize = batchSize;
        this.experienceBuffer = new ArrayList<>();
        this.explorationRate = 0.1; // Default exploration rate
    }

    @Override
    public int selectAction(double[] state) {
        // Implement policy-based action selection with exploration
        if (Math.random() < explorationRate) {
            // Explore: select random action
            return (int) (Math.random() * 8); // Assuming 8 possible actions (from Action enum)
        } else {
            // Exploit: use policy to select action
            // This would typically use a neural network to compute action probabilities
            // For now, return a policy-based action (placeholder implementation)
            return computePolicyAction(state);
        }
    }

    @Override
    public void train(Experience experience) {
        // Convert Experience to EnhancedExperience and add to buffer
        EnhancedExperience enhancedExperience = EnhancedExperience.fromExperience(experience);
        addExperience(enhancedExperience);
    }

    @Override
    public void trainEnhanced(EnhancedExperience experience) {
        // Delegate to the policy gradient experience buffer for batch training
        addExperience(experience);
    }

    @Override
    public double getExplorationRate() {
        return explorationRate;
    }

    @Override
    public void setExplorationRate(double explorationRate) {
        this.explorationRate = Math.max(0.0, Math.min(1.0, explorationRate)); // Clamp between 0 and 1
    }

    @Override
    public void saveModel(String filepath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filepath))) {
            // Save model parameters
            oos.writeDouble(learningRate);
            oos.writeInt(batchSize);
            oos.writeDouble(explorationRate);
            oos.writeObject(new ArrayList<>(experienceBuffer)); // Save current buffer
            logger.info("PolicyGradient model saved to: " + filepath);
        } catch (IOException e) {
            logger.severe("Failed to save PolicyGradient model: " + e.getMessage());
        }
    }

    @Override
    public void loadModel(String filepath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filepath))) {
            // Load model parameters - note: learningRate and batchSize are final, so we can only log them
            double savedLearningRate = ois.readDouble();
            int savedBatchSize = ois.readInt();
            this.explorationRate = ois.readDouble();

            @SuppressWarnings("unchecked")
            List<EnhancedExperience> savedBuffer = (List<EnhancedExperience>) ois.readObject();
            experienceBuffer.clear();
            experienceBuffer.addAll(savedBuffer);

            logger.info("PolicyGradient model loaded from: " + filepath);
            logger.info("Loaded parameters - Learning Rate: " + savedLearningRate +
                       ", Batch Size: " + savedBatchSize + ", Exploration Rate: " + explorationRate);
        } catch (IOException | ClassNotFoundException e) {
            logger.severe("Failed to load PolicyGradient model: " + e.getMessage());
        }
    }

    /**
     * Update method for backward compatibility (not part of interface).
     * @param state Current state array
     * @param action Action index
     * @param reward Reward value
     * @param nextState Next state array
     * @param done Terminal flag
     */
    public void update(double[] state, int action, double reward, double[] nextState, boolean done) {
        // Add experience to buffer for batch training
        EnhancedExperience experience = new EnhancedExperience(state, action, reward, nextState, done);
        addExperience(experience);
    }

    /**
     * Compute policy-based action selection.
     * @param state The current state
     * @return The selected action index
     */
    private int computePolicyAction(double[] state) {
        // Placeholder implementation - in a real policy gradient algorithm,
        // this would use a neural network to compute action probabilities
        // and sample from the policy distribution

        // For now, use a simple heuristic based on state
        double sum = 0.0;
        for (double value : state) {
            sum += value;
        }
        return (int) (Math.abs(sum) % 8); // Simple deterministic policy
    }

    /**
     * Add an experience to the training buffer.
     * @param experience The experience to add
     */
    public void addExperience(EnhancedExperience experience) {
        experienceBuffer.add(experience);

        // Train when we have enough experiences
        if (experienceBuffer.size() >= batchSize) {
            trainBatch();
            experienceBuffer.clear();
        }
    }

    /**
     * Train the policy on a batch of experiences.
     */
    private void trainBatch() {
        if (experienceBuffer.isEmpty()) {
            return;
        }

        // Implement policy gradient training
        // This would typically:
        // 1. Compute policy gradients from the trajectory
        // 2. Update the policy network parameters
        // 3. Apply gradient ascent to maximize expected reward

        // Placeholder implementation
        double totalReward = experienceBuffer.stream()
            .mapToDouble(EnhancedExperience::reward)
            .sum();

        // Log training progress
        System.out.println("PolicyGradient: Trained on batch of " + experienceBuffer.size() +
                          " experiences. Total reward: " + totalReward);
    }

    /**
     * Get the current learning rate.
     * @return The learning rate
     */
    public double getLearningRate() {
        return learningRate;
    }

    /**
     * Get the batch size.
     * @return The batch size
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * Get the number of experiences in the buffer.
     * @return The buffer size
     */
    public int getBufferSize() {
        return experienceBuffer.size();
    }
}
