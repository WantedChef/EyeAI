package chef.sheesh.eyeAI.core.ml.algorithms;

import chef.sheesh.eyeAI.core.ml.models.Action;
import chef.sheesh.eyeAI.core.ml.models.EnhancedExperience;
import chef.sheesh.eyeAI.core.ml.models.Experience;
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
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

/**
 * Deep Q-Network implementation with target network and replay buffer.
 * Designed to be extensible for distributed setups, but this variant runs
 * locally without external cluster dependencies. Uses a small MLP with
 * dropout and periodically updates a target network for stability.
 */
public class DistributedDeepQNetwork implements ILearningAlgorithm {
    private static final Logger log = LoggerFactory.getLogger(DistributedDeepQNetwork.class);
    private MultiLayerNetwork qNetwork;
    private MultiLayerNetwork targetNetwork;
    private final Deque<EnhancedExperience> replayBuffer;
    private final double discountFactor;
    private final int batchSize;
    private final int bufferCapacity;
    private final Random random;
    private double epsilon;
    private final int updateTargetEvery;
    private int stepCounter = 0;

    public DistributedDeepQNetwork(int stateSize, int actionSize, int hiddenSize, double learningRate,
                                   double discountFactor, int batchSize, int bufferCapacity,
                                   double epsilonStart, int updateTargetEvery) {
        this.discountFactor = discountFactor;
        this.batchSize = batchSize;
        this.bufferCapacity = bufferCapacity;
        this.epsilon = epsilonStart;
        this.updateTargetEvery = updateTargetEvery;
        this.random = new Random();
        this.replayBuffer = new ArrayDeque<>(bufferCapacity);

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(learningRate))
                .dropOut(0.2)
                .list()
                .layer(0, new DenseLayer.Builder().nIn(stateSize).nOut(hiddenSize)
                        .activation(Activation.RELU).build())
                .layer(1, new DenseLayer.Builder().nIn(hiddenSize).nOut(hiddenSize)
                        .activation(Activation.RELU).build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .nIn(hiddenSize).nOut(actionSize).activation(Activation.IDENTITY).build())
                .build();

        qNetwork = new MultiLayerNetwork(conf);
        qNetwork.init();
        qNetwork.setListeners(new ScoreIterationListener(100));

        targetNetwork = new MultiLayerNetwork(conf);
        targetNetwork.init();
        targetNetwork.setParams(qNetwork.params());
        log.info("Distributed DQN initialized with stateSize={}, actionSize={}, hiddenSize={}, dropout=0.2",
                stateSize, actionSize, hiddenSize);
    }

    public double[] predictQValues(double[] state) {
        INDArray input = Nd4j.create(state);
        INDArray output = qNetwork.output(input);
        return output.toDoubleVector();
    }

    @Override
    public int selectAction(double[] state) {
        if (random.nextDouble() < epsilon) {
            return random.nextInt(Action.values().length);
        } else {
            double[] qValues = predictQValues(state);
            int bestAction = 0;
            double bestValue = qValues[0];
            for (int i = 1; i < qValues.length; i++) {
                if (qValues[i] > bestValue) {
                    bestValue = qValues[i];
                    bestAction = i;
                }
            }
            return bestAction;
        }
    }

    @Override
    public void train(Experience experience) {
        // Convert Experience to EnhancedExperience and delegate
        trainEnhanced(EnhancedExperience.fromExperience(experience));
    }

    @Override
    public void trainEnhanced(EnhancedExperience experience) {
        replayBuffer.add(experience);
        if (replayBuffer.size() > bufferCapacity) {
            replayBuffer.pollFirst();
        }

        if (replayBuffer.size() >= batchSize) {
            // Sample a batch from the replay buffer
            EnhancedExperience[] batch = sampleBatch();

            // Determine dimensions from data rather than layer conf
            int inputSize = batch[0].state().length;
            int actions = Action.values().length;
            INDArray states = Nd4j.create(batchSize, inputSize);
            INDArray targets = Nd4j.create(batchSize, actions);

            for (int i = 0; i < batchSize; i++) {
                EnhancedExperience exp = batch[i];
                INDArray state = Nd4j.create(exp.state());
                INDArray nextState = Nd4j.create(exp.nextState());

                states.putRow(i, state);

                double[] target = qNetwork.output(state).toDoubleVector();
                double[] nextQValues = targetNetwork.output(nextState).toDoubleVector();

                double targetValue;
                if (exp.done()) {
                    targetValue = exp.reward();
                } else {
                    targetValue = exp.reward() + discountFactor * max(nextQValues);
                }

                target[exp.action()] = targetValue;
                targets.putRow(i, Nd4j.create(target));
            }

            // Train the network
            qNetwork.fit(states, targets);

            // Update target network periodically
            stepCounter++;
            if (stepCounter % updateTargetEvery == 0) {
                targetNetwork.setParams(qNetwork.params());
                log.info("Target network updated");
            }
        }
    }

    private EnhancedExperience[] sampleBatch() {
        EnhancedExperience[] batch = new EnhancedExperience[batchSize];
        int i = 0;
        for (EnhancedExperience experience : replayBuffer) {
            if (i >= batchSize) {
                break;
            }
            batch[i++] = experience;
        }
        return batch;
    }

    private double max(double[] array) {
        double max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    @Override
    public void saveModel(String filepath) {
        try {
            File locationToSave = new File(filepath);
            qNetwork.save(locationToSave, true);
            log.info("DQN model saved to {}", filepath);
        } catch (IOException e) {
            log.error("Failed to save DQN model to {}", filepath, e);
        }
    }

    @Override
    public void loadModel(String filepath) {
        try {
            File locationToLoad = new File(filepath);
            if (locationToLoad.exists()) {
                qNetwork = MultiLayerNetwork.load(locationToLoad, true);
                targetNetwork = MultiLayerNetwork.load(locationToLoad, true);
                log.info("DQN model loaded from {}", filepath);
            } else {
                log.warn("Model file not found at {}, initializing new model", filepath);
            }
        } catch (IOException e) {
            log.error("Failed to load DQN model from {}", filepath, e);
        }
    }

    @Override
    public double getExplorationRate() {
        return epsilon;
    }

    @Override
    public void setExplorationRate(double explorationRate) {
        this.epsilon = explorationRate;
    }
}
