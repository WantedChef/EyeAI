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
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;

public class DeepQNetwork implements ILearningAlgorithm {
    private static final Logger log = LoggerFactory.getLogger(DeepQNetwork.class);
    private MultiLayerNetwork qNetwork;       // Main network for Q-values
    private MultiLayerNetwork targetNetwork; // Target for stability (updated every N steps)
    private final Deque<EnhancedExperience> replayBuffer;  // Replay buffer for off-policy learning
    private final double discountFactor;           // Gamma for future rewards
    private final int batchSize;                   // Batch size for training
    private final int bufferCapacity;              // Max buffer size to manage memory
    private final Random random;                   // For epsilon-greedy
    private double epsilon;                        // Exploration rate (decays over time)
    private final int updateTargetEvery;                 // Frequency target update
    private int stepCounter = 0;                   // Counter for target updates

    public DeepQNetwork(int stateSize, int actionSize, int hiddenSize, double learningRate, double discountFactor,
                        int batchSize, int bufferCapacity, double epsilonStart, int updateTargetEvery) {
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
        log.info("DQN initialized with stateSize={}, actionSize={}, hiddenSize={}", stateSize, actionSize, hiddenSize);
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
        // This implementation uses EnhancedExperience, adapt if needed
        train(EnhancedExperience.fromExperience(experience));
    }
    
    @Override
    public void trainEnhanced(EnhancedExperience experience) {
        // Delegate to the existing EnhancedExperience training pathway
        train(experience);
    }
    
    public void train(EnhancedExperience experience) {
        try {
            if (replayBuffer.size() >= bufferCapacity) {
                replayBuffer.pollFirst();
            }
            replayBuffer.add(experience);

            if (replayBuffer.size() < batchSize) {
                return;
            }

            List<EnhancedExperience> batch = sampleBatch(batchSize);

            double[][] states = new double[batchSize][];
            double[][] nextStates = new double[batchSize][];
            for (int i = 0; i < batchSize; i++) {
                states[i] = batch.get(i).state();
                nextStates[i] = batch.get(i).nextState();
            }
            
            INDArray statesMatrix = Nd4j.create(states);
            INDArray nextStatesMatrix = Nd4j.create(nextStates);

            INDArray currentQ_s = qNetwork.output(statesMatrix);
            INDArray nextQ_s_prime = targetNetwork.output(nextStatesMatrix);

            INDArray targets = currentQ_s.dup();

            for (int i = 0; i < batchSize; i++) {
                EnhancedExperience exp = batch.get(i);
                double targetQ;
                if (exp.done()) {
                    targetQ = exp.reward();
                } else {
                    targetQ = exp.reward() + discountFactor * nextQ_s_prime.getRow(i).maxNumber().doubleValue();
                }
                targets.putScalar(i, exp.action(), targetQ);
            }

            qNetwork.fit(statesMatrix, targets);

            epsilon = Math.max(0.01, epsilon * 0.995); // Decay
            stepCounter++;
            if (stepCounter % updateTargetEvery == 0) {
                targetNetwork.setParams(qNetwork.params().dup());
                log.info("Target network updated at step {}", stepCounter);
            }
        } catch (Exception e) {
            log.error("Training failed", e);
        }
    }

    private List<EnhancedExperience> sampleBatch(int size) {
        List<EnhancedExperience> bufferAsList = new ArrayList<>(replayBuffer);
        Collections.shuffle(bufferAsList);
        return bufferAsList.subList(0, size);
    }

    private int argmax(double[] array) {
        int maxIdx = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[maxIdx]) {
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
            log.info("Model saved to {}", filepath);
        } catch (IOException e) {
            log.error("Failed to save model", e);
        }
    }

    @Override
    public void loadModel(String filepath) {
        try {
            qNetwork = MultiLayerNetwork.load(new File(filepath), true);
            targetNetwork.setParams(qNetwork.params());
            log.info("Model loaded from {}", filepath);
        } catch (IOException e) {
            log.error("Failed to load model", e);
        }
    }
}
