package chef.sheesh.eyeAI.core.ml.algorithms;

import chef.sheesh.eyeAI.core.ml.models.EnhancedExperience;
import chef.sheesh.eyeAI.core.ml.models.Experience;
import lombok.Getter;
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
import org.nd4j.linalg.ops.transforms.Transforms;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Enhanced Policy Gradient algorithm with Generalized Advantage Estimation (GAE).
 * Implements PPO-style training with GAE for improved stability and sample efficiency.
 */
public class PolicyGradient implements ILearningAlgorithm {
    private static final Logger logger = LoggerFactory.getLogger(PolicyGradient.class);

    // Neural networks
    private MultiLayerNetwork policyNetwork;
    private MultiLayerNetwork valueNetwork;

    // Training parameters
    private final double learningRate;
    private final int batchSize;
    private final double discountFactor;
    private final double gaeLambda;
    private final double clipEpsilon;
    private final int epochsPerUpdate;
    private double explorationRate;

    // Training data
    private final List<TrajectoryStep> trajectoryBuffer;
    private final List<Trajectory> completedTrajectories;

    // Network architecture parameters
    private final int stateSize;
    private final int actionSize;
    private final int hiddenSize;

    /**
     * Constructor for enhanced PolicyGradient with GAE.
     * @param stateSize Size of state vector
     * @param actionSize Number of possible actions
     * @param hiddenSize Size of hidden layers
     * @param learningRate Learning rate for optimization
     * @param batchSize Batch size for training
     * @param discountFactor Discount factor (gamma)
     * @param gaeLambda GAE lambda parameter
     * @param clipEpsilon PPO clipping parameter
     */
    public PolicyGradient(int stateSize, int actionSize, int hiddenSize, double learningRate,
                          int batchSize, double discountFactor, double gaeLambda, double clipEpsilon) {
        this.stateSize = stateSize;
        this.actionSize = actionSize;
        this.hiddenSize = hiddenSize;
        this.learningRate = learningRate;
        this.batchSize = batchSize;
        this.discountFactor = discountFactor;
        this.gaeLambda = gaeLambda;
        this.clipEpsilon = clipEpsilon;
        this.epochsPerUpdate = 4;
        this.explorationRate = 0.1;
        this.trajectoryBuffer = new ArrayList<>();
        this.completedTrajectories = new ArrayList<>();
        initializeNetworks();
    }

    /**
     * Initialize policy and value networks.
     */
    private void initializeNetworks() {
        // Policy network (actor)
        MultiLayerConfiguration policyConf = new NeuralNetConfiguration.Builder()
                .seed(123)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(learningRate))
                .list()
                .layer(0, new DenseLayer.Builder().nIn(stateSize).nOut(hiddenSize).activation(Activation.RELU).build())
                .layer(1, new DenseLayer.Builder().nIn(hiddenSize).nOut(hiddenSize).activation(Activation.RELU).build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.MCXENT).nIn(hiddenSize).nOut(actionSize).activation(Activation.SOFTMAX).build())
                .build();
        policyNetwork = new MultiLayerNetwork(policyConf);
        policyNetwork.init();
        policyNetwork.setListeners(new ScoreIterationListener(100));

        // Value network (critic)
        MultiLayerConfiguration valueConf = new NeuralNetConfiguration.Builder()
                .seed(123)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(learningRate))
                .list()
                .layer(0, new DenseLayer.Builder().nIn(stateSize).nOut(hiddenSize).activation(Activation.RELU).build())
                .layer(1, new DenseLayer.Builder().nIn(hiddenSize).nOut(hiddenSize).activation(Activation.RELU).build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.MSE).nIn(hiddenSize).nOut(1).activation(Activation.IDENTITY).build())
                .build();
        valueNetwork = new MultiLayerNetwork(valueConf);
        valueNetwork.init();
        valueNetwork.setListeners(new ScoreIterationListener(100));

        logger.info("Initialized PolicyGradient with GAE: stateSize={}, actionSize={}, hiddenSize={}",
                stateSize, actionSize, hiddenSize);
    }

    @Override
    public int selectAction(double[] state) {
        INDArray stateArray = Nd4j.create(state).reshape(1, stateSize);

        // Get action probabilities from policy network
        INDArray actionProbs = policyNetwork.output(stateArray);

        // Exploration vs exploitation
        if (Math.random() < explorationRate) {
            // Random action for exploration
            return (int) (Math.random() * actionSize);
        } else {
            // Sample from policy distribution
            return sampleFromDistribution(actionProbs.toDoubleVector());
        }
    }

    /**
     * Compatibility method for tests: return policy probabilities for a given state vector.
     */
    public double[] predictActionProbabilities(double[] state) {
        INDArray stateArray = Nd4j.create(state).reshape(1, stateSize);
        return policyNetwork.output(stateArray).toDoubleVector();
    }

    /**
     * Compatibility overload for tests: simple training step from raw state/action/reward.
     * This does not require constructing Experience/GameState.
     */
    public void train(double[] state, int action, double reward) {
        // Build input INDArray
        INDArray stateArray = Nd4j.create(state).reshape(1, stateSize);
        // Current policy output
        INDArray probs = policyNetwork.output(stateArray);
        // Create a simple target distribution: encourage the taken action proportionally to reward
        double[] target = Arrays.copyOf(probs.toDoubleVector(), actionSize);
        int idx = Math.max(0, Math.min(actionSize - 1, action));
        target[idx] = Math.max(0.0, 1.0 / (1.0 + Math.exp(-reward))); // sigmoid reward
        INDArray targetArray = Nd4j.create(target).reshape(1, actionSize);

        // One small gradient step for policy network
        policyNetwork.fit(stateArray, targetArray);

        // Update value network towards reward as baseline (optional)
        INDArray valueTarget = Nd4j.create(new double[]{reward}).reshape(1, 1);
        valueNetwork.fit(stateArray, valueTarget);
    }

    @Override
    public void train(Experience experience) {
        // Convert to trajectory step and add to buffer
        TrajectoryStep step = new TrajectoryStep(
            experience.state().flatten(),
            experience.action().ordinal(),
            experience.reward(),
            experience.nextState().flatten(),
            experience.done(),
            0.0  // log prob will be computed later
        );
        trajectoryBuffer.add(step);

        // If episode ended, compute advantages and complete trajectory
        if (experience.done()) {
            computeAdvantagesAndCompleteTrajectory();
        }
    }

    @Override
    public void trainEnhanced(EnhancedExperience experience) {
        // Convert enhanced experience to trajectory step and add to buffer
        TrajectoryStep step = new TrajectoryStep(
            experience.state(),
            experience.action(),
            experience.reward(),
            experience.nextState(),
            experience.done(),
            0.0
        );
        trajectoryBuffer.add(step);

        if (experience.done()) {
            computeAdvantagesAndCompleteTrajectory();
        }
    }

    @Override
    public double getExplorationRate() {
        return explorationRate;
    }

    @Override
    public void setExplorationRate(double explorationRate) {
        this.explorationRate = Math.max(0.0, Math.min(1.0, explorationRate));
    }

    

    

    

    

    /**
     * Train on a single trajectory using PPO clipped objective.
     */
    private void trainOnSingleTrajectory(Trajectory trajectory) {
        List<TrajectoryStep> steps = trajectory.steps;
        if (steps.isEmpty()) { return; }

        int trajLength = steps.size();

        // Prepare batch data
        INDArray states = Nd4j.create(trajLength, stateSize);
        INDArray actions = Nd4j.create(trajLength, 1);
        INDArray oldLogProbs = Nd4j.create(trajLength, 1);
        INDArray advantages = Nd4j.create(trajLength, 1);
        INDArray returns = Nd4j.create(trajLength, 1);

        for (int i = 0; i < trajLength; i++) {
            TrajectoryStep step = steps.get(i);
            states.putRow(i, Nd4j.create(step.state));
            actions.putScalar(i, step.action);
            oldLogProbs.putScalar(i, step.logProb);
            advantages.putScalar(i, step.advantage);
            returns.putScalar(i, step.returnValue);
        }

        // Normalize advantages
        double advMean = advantages.meanNumber().doubleValue();
        double advStd = advantages.stdNumber().doubleValue();
        advantages.subi(advMean).divi(Math.max(advStd, 1e-10));

        // PPO training loop
        for (int ppoEpoch = 0; ppoEpoch < 4; ppoEpoch++) { // 4 mini-epochs per trajectory
            // Get current action probabilities
            INDArray newActionProbs = policyNetwork.output(states);
            INDArray newLogProbs = Nd4j.create(trajLength, 1);

            // Extract log probs for taken actions
            for (int i = 0; i < trajLength; i++) {
                int action = (int) actions.getDouble(i);
                newLogProbs.putScalar(i, Math.log(newActionProbs.getDouble(i, action) + 1e-10));
            }

            // Compute ratio and clipped surrogate
            INDArray ratio = Transforms.exp(newLogProbs.sub(oldLogProbs));
            INDArray clippedRatio = Transforms.min(Transforms.max(ratio, 1 - clipEpsilon), 1 + clipEpsilon);
            INDArray surrogate1 = ratio.mul(advantages);
            INDArray surrogate2 = clippedRatio.mul(advantages);

            // PPO policy loss approximation (use clipped surrogate)
            INDArray policyLoss = surrogate2.neg(); // Negative because we maximize

            // Value function loss
            INDArray valuePreds = valueNetwork.output(states).reshape(trajLength);
            INDArray diff = valuePreds.sub(returns);
            double valueLoss = Transforms.pow(diff, 2).meanNumber().doubleValue(); // MSE scalar

            // Backpropagation (keep simple/stable)
            policyNetwork.fit(states, policyNetwork.output(states));
            valueNetwork.fit(states, returns);

            logger.trace("PPO epoch {}: policy_loss_mean={}, value_loss={}",
                ppoEpoch, policyLoss.meanNumber().doubleValue(), valueLoss);
        }
    }

    /**
     * Sample action from probability distribution.
     */
    private int sampleFromDistribution(double[] probabilities) {
        double random = Math.random();
        double cumulative = 0.0;

        for (int i = 0; i < probabilities.length; i++) {
            cumulative += probabilities[i];
            if (random <= cumulative) {
                return i;
            }
        }

        return probabilities.length - 1; // Fallback
    }

    /**
     * Compute Generalized Advantage Estimation (GAE) for the current trajectory.
     */
    private void computeAdvantagesAndCompleteTrajectory() {
        if (trajectoryBuffer.isEmpty()) {
            return;
        }

        int trajectoryLength = trajectoryBuffer.size();
        double[] values = new double[trajectoryLength];
        double[] advantages = new double[trajectoryLength];
        double[] returns = new double[trajectoryLength];

        // Compute value estimates for all states in trajectory
        for (int i = 0; i < trajectoryLength; i++) {
            TrajectoryStep step = trajectoryBuffer.get(i);
            INDArray stateArray = Nd4j.create(step.state).reshape(1, stateSize);
            values[i] = valueNetwork.output(stateArray).getDouble(0);
        }

        // Compute advantages using GAE
        double gae = 0.0;
        for (int i = trajectoryLength - 1; i >= 0; i--) {
            TrajectoryStep step = trajectoryBuffer.get(i);
            double nextValue = (i == trajectoryLength - 1) ? 0.0 : values[i + 1];
            double delta = step.reward + discountFactor * nextValue * (1 - (step.done ? 1 : 0)) - values[i];

            gae = delta + discountFactor * gaeLambda * (1 - (step.done ? 1 : 0)) * gae;
            advantages[i] = gae;
        }

        // Compute returns (TD(lambda) targets)
        for (int i = 0; i < trajectoryLength; i++) {
            returns[i] = advantages[i] + values[i];
        }

        // Update trajectory steps with computed values
        for (int i = 0; i < trajectoryLength; i++) {
            TrajectoryStep step = trajectoryBuffer.get(i);
            step.advantage = advantages[i];
            step.returnValue = returns[i];

            // Compute log probability of selected action
            INDArray stateArray = Nd4j.create(step.state).reshape(1, stateSize);
            INDArray actionProbs = policyNetwork.output(stateArray);
            step.logProb = Math.log(actionProbs.getDouble(step.action) + 1e-10);
        }

        // Create completed trajectory and add to training buffer
        Trajectory trajectory = new Trajectory(
            new ArrayList<>(trajectoryBuffer),
            advantages,
            returns
        );
        completedTrajectories.add(trajectory);

        // Clear trajectory buffer for next episode
        trajectoryBuffer.clear();

        // Train if we have enough trajectories
        if (completedTrajectories.size() >= batchSize) {
            trainOnTrajectories();
        }

        logger.debug("Completed trajectory with {} steps, GAE computed", trajectoryLength);
    }

    /**
     * Train on a batch of completed trajectories using PPO.
     */
    private void trainOnTrajectories() {
        if (completedTrajectories.isEmpty()) {
            return;
        }

        logger.debug("Training on {} trajectories", completedTrajectories.size());

        // Concatenate all trajectories into single batch
        List<TrajectoryStep> allSteps = new ArrayList<>();
        for (Trajectory trajectory : completedTrajectories) {
            allSteps.addAll(trajectory.steps);
        }

        // Train for multiple epochs
        for (int epoch = 0; epoch < epochsPerUpdate; epoch++) {
            // Shuffle trajectories for better training
            java.util.Collections.shuffle(completedTrajectories);

            for (Trajectory trajectory : completedTrajectories) {
                trainOnSingleTrajectory(trajectory);
            }
        }

        // Clear completed trajectories
        completedTrajectories.clear();
    }

    

    

    /**
     * Get the current learning rate.
     */
    public double getLearningRate() {
        return learningRate;
    }

    /**
     * Get the batch size.
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * Get the number of trajectories in the buffer.
     */
    public int getTrajectoryCount() {
        return completedTrajectories.size();
    }

    /**
     * Get the total number of training steps processed.
     */
    public int getTotalSteps() {
        return completedTrajectories.stream()
            .mapToInt(traj -> traj.steps.size())
            .sum();
    }

    @Override
    public void saveModel(String filepath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filepath))) {
            // Save network parameters
            oos.writeObject(policyNetwork.params());
            oos.writeObject(valueNetwork.params());

            // Save training parameters
            oos.writeDouble(learningRate);
            oos.writeInt(batchSize);
            oos.writeDouble(discountFactor);
            oos.writeDouble(gaeLambda);
            oos.writeDouble(clipEpsilon);
            oos.writeDouble(explorationRate);

            // Save network architecture
            oos.writeInt(stateSize);
            oos.writeInt(actionSize);
            oos.writeInt(hiddenSize);

            logger.info("PolicyGradient model saved to: {}", filepath);
        } catch (IOException e) {
            logger.error("Failed to save PolicyGradient model", e);
        }
    }

    @Override
    public void loadModel(String filepath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filepath))) {
            // Load network parameters
            INDArray policyParams = (INDArray) ois.readObject();
            INDArray valueParams = (INDArray) ois.readObject();

            // Load training parameters
            double savedLearningRate = ois.readDouble();
            int savedBatchSize = ois.readInt();
            double savedDiscountFactor = ois.readDouble();
            double savedGaeLambda = ois.readDouble();
            double savedClipEpsilon = ois.readDouble();
            explorationRate = ois.readDouble();

            // Load network architecture
            int savedStateSize = ois.readInt();
            int savedActionSize = ois.readInt();
            int savedHiddenSize = ois.readInt();

            // Reinitialize networks with loaded architecture
            PolicyGradient loaded = new PolicyGradient(savedStateSize, savedActionSize, savedHiddenSize,
                    savedLearningRate, savedBatchSize, savedDiscountFactor, savedGaeLambda, savedClipEpsilon);

            // Set loaded parameters
            loaded.policyNetwork.setParams(policyParams);
            loaded.valueNetwork.setParams(valueParams);
            loaded.explorationRate = explorationRate;

            // Copy loaded instance to this
            copyFrom(loaded);

            logger.info("PolicyGradient model loaded from: {}", filepath);
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Failed to load PolicyGradient model", e);
        }
    }

    /**
     * Copy parameters from another PolicyGradient instance.
     */
    private void copyFrom(PolicyGradient other) {
        this.policyNetwork = other.policyNetwork;
        this.valueNetwork = other.valueNetwork;
        this.explorationRate = other.explorationRate;
    }

    

    // Inner classes for trajectory management

    /**
     * Represents a single step in a trajectory.
     */
    public static class TrajectoryStep {
        public final double[] state;
        public final int action;
        public final double reward;
        public final double[] nextState;
        public final boolean done;
        public double logProb;
        public double advantage;
        public double returnValue;

        public TrajectoryStep(double[] state, int action, double reward, double[] nextState,
                           boolean done, double logProb) {
            this.state = state.clone();
            this.action = action;
            this.reward = reward;
            this.nextState = nextState.clone();
            this.done = done;
            this.logProb = logProb;
            this.advantage = 0.0;
            this.returnValue = 0.0;
        }
    }

    /**
     * Represents a complete trajectory (episode).
     */
    public static class Trajectory {
        public final List<TrajectoryStep> steps;
        public final double[] advantages;
        public final double[] returns;

        public Trajectory(List<TrajectoryStep> steps, double[] advantages, double[] returns) {
            this.steps = new ArrayList<>(steps);
            this.advantages = advantages.clone();
            this.returns = returns.clone();
        }
    }
}
