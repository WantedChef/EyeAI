package chef.sheesh.eyeAI.core.ml.algorithms;

import chef.sheesh.eyeAI.core.ml.models.Action;
import chef.sheesh.eyeAI.core.ml.models.EnhancedExperience;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Enhanced Deep Q-Network with Prioritized Experience Replay (PER).
 * Implements prioritized sampling to improve sample efficiency by focusing
 * on experiences with high TD errors.
 */
public class DeepQNetwork implements ILearningAlgorithm {
    private static final Logger log = LoggerFactory.getLogger(DeepQNetwork.class);

    // Neural networks
    private MultiLayerNetwork qNetwork;
    private MultiLayerNetwork targetNetwork;

    // Training parameters
    private final double discountFactor;
    private final int batchSize;
    private final int bufferCapacity;
    private final Random random;
    private double epsilon;
    private final int updateTargetEvery;
    private int stepCounter = 0;

    // PER parameters
    private final double alpha;        // Priority exponent (how much to prioritize high-error experiences)
    private final double beta;         // Importance sampling exponent (starts low, increases to 1)
    private final double betaIncrement; // How much to increase beta per step
    private final double epsilonPER;   // Small constant to ensure no experience has zero probability

    // PER data structures
    private final SumTree sumTree;     // For efficient priority sampling
    private final List<EnhancedExperience> experiences;
    private final double[] priorities;
    private int experienceCount = 0;

    /**
     * Constructor for enhanced DeepQNetwork with Prioritized Experience Replay.
     * @param stateSize Size of state vector
     * @param actionSize Number of possible actions
     * @param hiddenSize Size of hidden layers
     * @param learningRate Learning rate for optimization
     * @param discountFactor Discount factor (gamma)
     * @param batchSize Batch size for training
     * @param bufferCapacity Maximum capacity of experience buffer
     * @param epsilonStart Starting exploration rate
     * @param updateTargetEvery Frequency to update target network
     * @param alpha Priority exponent for PER
     * @param betaStart Starting importance sampling exponent
     * @param betaIncrement How much to increase beta per step
     */
    public DeepQNetwork(int stateSize, int actionSize, int hiddenSize, double learningRate,
                       double discountFactor, int batchSize, int bufferCapacity,
                       double epsilonStart, int updateTargetEvery,
                       double alpha, double betaStart, double betaIncrement) {
        this.discountFactor = discountFactor;
        this.batchSize = batchSize;
        this.bufferCapacity = bufferCapacity;
        this.epsilon = epsilonStart;
        this.updateTargetEvery = updateTargetEvery;
        this.random = new Random();
        this.alpha = alpha;
        this.beta = betaStart;
        this.betaIncrement = betaIncrement;
        this.epsilonPER = 0.01;

        // Initialize PER data structures
        this.sumTree = new SumTree(bufferCapacity);
        this.experiences = new ArrayList<>(bufferCapacity);
        this.priorities = new double[bufferCapacity];

        // Initialize neural networks
        initializeNetworks(stateSize, actionSize, hiddenSize, learningRate);
        log.info("DQN with PER initialized: alpha={}, betaStart={}, bufferCapacity={}",
                alpha, betaStart, bufferCapacity);
    }

    /**
     * Initialize the Q-networks.
     */
    private void initializeNetworks(int stateSize, int actionSize, int hiddenSize, double learningRate) {
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(learningRate))
                .dropOut(0.2)
                .list()
                .layer(0, new DenseLayer.Builder().nIn(stateSize).nOut(hiddenSize).activation(Activation.RELU).build())
                .layer(1, new DenseLayer.Builder().nIn(hiddenSize).nOut(hiddenSize).activation(Activation.RELU).build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.MSE).nIn(hiddenSize).nOut(actionSize).activation(Activation.IDENTITY).build())
                .build();

        qNetwork = new MultiLayerNetwork(conf);
        qNetwork.init();
        qNetwork.setListeners(new ScoreIterationListener(100));

        targetNetwork = new MultiLayerNetwork(conf);
        targetNetwork.init();
        targetNetwork.setParams(qNetwork.params());
    }

    public double[] predictQValues(double[] state) {
        INDArray input = Nd4j.create(state).reshape(1, state.length);
        INDArray output = qNetwork.output(input, false);
        return output.toDoubleVector();
    }

    @Override
    public int selectAction(double[] state) {
        if (random.nextDouble() < epsilon) {
            log.debug("Exploration: Random action selected");
            return random.nextInt(Action.values().length);
        }
        double[] qValues = predictQValues(state);
        return argmax(qValues);
    }

    @Override
    public void train(chef.sheesh.eyeAI.core.ml.models.Experience experience) {
        // Convert to EnhancedExperience and train
        trainEnhanced(EnhancedExperience.fromExperience(experience));
    }

    @Override
    public void trainEnhanced(EnhancedExperience experience) {
        try {
            // Add experience to PER buffer
            addExperience(experience);

            // Only train if we have enough experiences
            if (experienceCount < batchSize) {
                return;
            }

            // Sample batch using priorities
            PERBatch perBatch = sampleBatch(batchSize);

            // Prepare training data
            double[][] states = new double[batchSize][];
            double[][] nextStates = new double[batchSize][];
            double[] rewards = new double[batchSize];
            int[] actions = new int[batchSize];
            boolean[] dones = new boolean[batchSize];
            double[] importanceWeights = new double[batchSize];

            for (int i = 0; i < batchSize; i++) {
                EnhancedExperience exp = perBatch.experiences.get(i);
                states[i] = exp.state();
                nextStates[i] = exp.nextState();
                rewards[i] = exp.reward();
                actions[i] = exp.action();
                dones[i] = exp.done();
                importanceWeights[i] = perBatch.importanceWeights[i];
            }

            // Convert to INDArray matrices
            INDArray statesMatrix = Nd4j.create(states);
            INDArray nextStatesMatrix = Nd4j.create(nextStates);

            // Compute current Q-values
            INDArray currentQValues = qNetwork.output(statesMatrix);

            // Compute target Q-values using target network
            INDArray nextQValues = targetNetwork.output(nextStatesMatrix);
            INDArray maxNextQValues = nextQValues.max(1); // Max over actions

            // Compute TD targets
            INDArray targets = currentQValues.dup();
            for (int i = 0; i < batchSize; i++) {
                double targetQ;
                if (dones[i]) {
                    targetQ = rewards[i];
                } else {
                    targetQ = rewards[i] + discountFactor * maxNextQValues.getDouble(i);
                }

                // Apply importance sampling weight to the TD error
                double tdError = targetQ - currentQValues.getDouble(i, actions[i]);
                targets.putScalar(i, actions[i], currentQValues.getDouble(i, actions[i]) + importanceWeights[i] * tdError);
            }

            // Train the network
            qNetwork.fit(statesMatrix, targets);

            // Update priorities based on new TD errors
            updatePriorities(perBatch.indices, states, nextStates, rewards, actions, dones);

            // Update target network periodically
            stepCounter++;
            if (stepCounter % updateTargetEvery == 0) {
                targetNetwork.setParams(qNetwork.params().dup());
                log.info("Target network updated at step {}", stepCounter);
            }

            // Decay exploration rate
            epsilon = Math.max(0.01, epsilon * 0.995);

        } catch (Exception e) {
            log.error("Training failed", e);
        }
    }

    /**
     * Add an experience to the PER buffer.
     * @param experience The experience to add
     */
    private void addExperience(EnhancedExperience experience) {
        if (experienceCount >= bufferCapacity) {
            // Remove oldest experience
            experiences.remove(0);
            experienceCount--;
        }

        // Add to experiences list
        experiences.add(experience);

        // Initialize with maximum priority for new experiences
        double initialPriority = getMaxPriority();
        if (initialPriority == 0.0) {
            initialPriority = 1.0; // For first experience
        }

        priorities[experienceCount] = initialPriority;
        sumTree.add(initialPriority, experienceCount);
        experienceCount++;
    }

    /**
     * Sample a batch using prioritized sampling.
     * @param batchSize Size of batch to sample
     * @return PERBatch containing sampled experiences and metadata
     */
    private PERBatch sampleBatch(int batchSize) {
        List<EnhancedExperience> sampledExperiences = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        double[] importanceWeights = new double[batchSize];

        double totalPriority = sumTree.total();
        double currentBeta = Math.min(1.0, beta + stepCounter * betaIncrement);

        for (int i = 0; i < batchSize; i++) {
            // Sample a priority value
            double priorityValue = random.nextDouble() * totalPriority;

            // Get the corresponding tree index
            int treeIndex = sumTree.getLeaf(priorityValue);
            int dataIndex = sumTree.getDataIndex(treeIndex);

            // Get the experience
            sampledExperiences.add(experiences.get(dataIndex));
            indices.add(dataIndex);

            // Calculate importance sampling weight
            double priority = sumTree.getPriority(dataIndex);
            double probability = priority / totalPriority;
            importanceWeights[i] = Math.pow(1.0 / (experienceCount * probability), currentBeta);
        }

        // Normalize importance weights
        double maxWeight = java.util.Arrays.stream(importanceWeights).max().orElse(1.0);
        for (int i = 0; i < importanceWeights.length; i++) {
            importanceWeights[i] /= maxWeight;
        }

        return new PERBatch(sampledExperiences, indices, importanceWeights);
    }

    /**
     * Update priorities based on TD errors.
     * @param indices Indices of experiences to update
     * @param states State arrays
     * @param nextStates Next state arrays
     * @param rewards Reward values
     * @param actions Action indices
     * @param dones Terminal flags
     */
    private void updatePriorities(List<Integer> indices, double[][] states, double[][] nextStates,
                                 double[] rewards, int[] actions, boolean[] dones) {
        for (int i = 0; i < indices.size(); i++) {
            int dataIndex = indices.get(i);

            // Compute current TD error
            INDArray stateArray = Nd4j.create(states[i]).reshape(1, states[i].length);
            INDArray nextStateArray = Nd4j.create(nextStates[i]).reshape(1, nextStates[i].length);

            INDArray currentQ = qNetwork.output(stateArray);
            INDArray nextQ = targetNetwork.output(nextStateArray);

            double currentQValue = currentQ.getDouble(0, actions[i]);
            double targetQValue;

            if (dones[i]) {
                targetQValue = rewards[i];
            } else {
                targetQValue = rewards[i] + discountFactor * nextQ.maxNumber().doubleValue();
            }

            double tdError = Math.abs(targetQValue - currentQValue);

            // Update priority: p_i = (TD_error + epsilon)^alpha
            double newPriority = Math.pow(tdError + epsilonPER, alpha);
            priorities[dataIndex] = newPriority;
            sumTree.update(dataIndex + bufferCapacity - 1, newPriority);
        }
    }

    /**
     * Get the maximum priority in the buffer.
     * @return Maximum priority value
     */
    private double getMaxPriority() {
        if (experienceCount == 0) {
            return 0.0;
        }

        double maxPriority = 0.0;
        for (int i = 0; i < experienceCount; i++) {
            if (priorities[i] > maxPriority) {
                maxPriority = priorities[i];
            }
        }
        return maxPriority;
    }

    /**
     * Find the action with maximum Q-value.
     * @param qValues Array of Q-values
     * @return Index of action with highest Q-value
     */
    private int argmax(double[] qValues) {
        int maxIdx = 0;
        double maxVal = qValues[0];
        for (int i = 1; i < qValues.length; i++) {
            if (qValues[i] > maxVal) {
                maxVal = qValues[i];
                maxIdx = i;
            }
        }
        return maxIdx;
    }

    @Override
    public double getExplorationRate() {
        return epsilon;
    }

    @Override
    public void setExplorationRate(double explorationRate) {
        this.epsilon = explorationRate;
    }

    @Override
    public void saveModel(String filepath) {
        try {
            qNetwork.save(new File(filepath), true);
            log.info("DQN model saved to {}", filepath);
        } catch (IOException e) {
            log.error("Failed to save DQN model", e);
        }
    }

    @Override
    public void loadModel(String filepath) {
        try {
            qNetwork = MultiLayerNetwork.load(new File(filepath), true);
            targetNetwork.setParams(qNetwork.params());
            log.info("DQN model loaded from {}", filepath);
        } catch (IOException e) {
            log.error("Failed to load DQN model", e);
        }
    }

    /**
     * Get statistics about the PER buffer.
     * @return String with buffer statistics
     */
    public String getBufferStats() {
        return String.format("PER Buffer: %d/%d experiences, total priority: %.3f",
                experienceCount, bufferCapacity, sumTree.total());
    }

    /**
     * Inner class to hold a batch sampled from PER.
     */
    private static class PERBatch {
        public final List<EnhancedExperience> experiences;
        public final List<Integer> indices;
        public final double[] importanceWeights;

        public PERBatch(List<EnhancedExperience> experiences, List<Integer> indices, double[] importanceWeights) {
            this.experiences = experiences;
            this.indices = indices;
            this.importanceWeights = importanceWeights;
        }
    }
}
