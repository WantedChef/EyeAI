package chef.sheesh.eyeAI.core.ml.rnn;

import chef.sheesh.eyeAI.core.sim.SimExperience;
import chef.sheesh.eyeAI.infra.util.Async;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * Optimized Recurrent Neural Network for movement pattern prediction using LSTM architecture.
 * Focuses on efficiency, modularity, and numerical stability.
 */
public final class MovementRNN {
    // Network configuration
    private static final int DEFAULT_INPUT_SIZE = 6;  // [x, y, z, velocity_x, velocity_y, velocity_z]
    private static final int DEFAULT_HIDDEN_SIZE = 32;
    private static final int DEFAULT_OUTPUT_SIZE = 6;
    private static final double DEFAULT_LEARNING_RATE = 0.001; // More stable default
    private static final double GRADIENT_CLIP_VALUE = 5.0;
    private static final int MAX_LOSS_HISTORY = 1000;

    // Network parameters
    private final int inputSize;
    private int hiddenSize;
    private final int outputSize;
    private double learningRate;
    private final boolean useLSTM;
    private int sequenceLength;

    // Weight matrices and biases
    private double[][] inputToHiddenWeights;
    private double[][] hiddenToHiddenWeights;
    private double[][] hiddenToOutputWeights;
    private double[] hiddenBiases;
    private double[] outputBiases;

    // LSTM gates (optimized storage)
    private double[][] forgetGateWeights;
    private double[][] inputGateWeights;
    private double[][] candidateWeights;
    private double[][] outputGateWeights;
    private double[] forgetGateBiases;
    private double[] inputGateBiases;
    private double[] candidateBiases;
    private double[] outputGateBiases;

    // States
    private double[] hiddenState;
    private double[] cellState;

    // Training statistics
    private long trainingSteps;
    private double averageLoss;
    private final List<Double> lossHistory;

    public MovementRNN() {
        this(DEFAULT_INPUT_SIZE, DEFAULT_HIDDEN_SIZE, DEFAULT_OUTPUT_SIZE);
    }

    public MovementRNN(int inputSize, int hiddenSize, int outputSize) {
        this.inputSize = inputSize;
        this.hiddenSize = hiddenSize;
        this.outputSize = outputSize;
        this.learningRate = DEFAULT_LEARNING_RATE;
        this.useLSTM = true;
        this.sequenceLength = 20;
        this.lossHistory = new ArrayList<>(MAX_LOSS_HISTORY);
        initializeNetwork();
    }

    /**
     * Initializes network with Xavier/Glorot weight initialization for better convergence.
     */
    private void initializeNetwork() {
        Random random = new Random();
        double inputScale = Math.sqrt(6.0 / (inputSize + hiddenSize));
        double hiddenScale = Math.sqrt(6.0 / (hiddenSize + hiddenSize));
        double outputScale = Math.sqrt(6.0 / (hiddenSize + outputSize));

        // Initialize weight matrices
        inputToHiddenWeights = initMatrix(random, hiddenSize, inputSize, inputScale);
        hiddenToHiddenWeights = initMatrix(random, hiddenSize, hiddenSize, hiddenScale);
        hiddenToOutputWeights = initMatrix(random, outputSize, hiddenSize, outputScale);
        hiddenBiases = initVector(random, hiddenSize, inputScale);
        outputBiases = initVector(random, outputSize, outputScale);

        // LSTM-specific weights
        if (useLSTM) {
            forgetGateWeights = initMatrix(random, hiddenSize, inputSize + hiddenSize, inputScale);
            inputGateWeights = initMatrix(random, hiddenSize, inputSize + hiddenSize, inputScale);
            candidateWeights = initMatrix(random, hiddenSize, inputSize + hiddenSize, inputScale);
            outputGateWeights = initMatrix(random, hiddenSize, inputSize + hiddenSize, inputScale);
            forgetGateBiases = initVector(random, hiddenSize, inputScale);
            inputGateBiases = initVector(random, hiddenSize, inputScale);
            candidateBiases = initVector(random, hiddenSize, inputScale);
            outputGateBiases = initVector(random, hiddenSize, inputScale);
        }

        hiddenState = new double[hiddenSize];
        cellState = new double[hiddenSize];
        resetStatistics();
    }

    private double[][] initMatrix(Random random, int rows, int cols, double scale) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = random.nextGaussian() * scale;
            }
        }
        return matrix;
    }

    private double[] initVector(Random random, int size, double scale) {
        double[] vector = new double[size];
        for (int i = 0; i < size; i++) {
            vector[i] = random.nextGaussian() * scale;
        }
        return vector;
    }

    /**
     * Predicts next movement based on input sequence.
     */
    public double[] predict(double[] input) {
        validateInput(input);
        double[] newHiddenState = new double[hiddenSize];
        double[] newCellState = useLSTM ? new double[hiddenSize] : cellState;

        if (useLSTM) {
            forwardLSTM(input, newHiddenState, newCellState);
        } else {
            forwardRNN(input, newHiddenState);
        }

        hiddenState = newHiddenState;
        cellState = newCellState;

        double[] output = computeOutput(newHiddenState);
        return output;
    }

    private void validateInput(double[] input) {
        if (input.length != inputSize) {
            throw new IllegalArgumentException("Input size mismatch: expected " + inputSize + ", got " + input.length);
        }
    }

    private double[] computeOutput(double[] hiddenState) {
        double[] output = new double[outputSize];
        for (int i = 0; i < outputSize; i++) {
            output[i] = outputBiases[i];
            for (int j = 0; j < hiddenSize; j++) {
                output[i] += hiddenToOutputWeights[i][j] * hiddenState[j];
            }
            output[i] = Math.tanh(output[i]);
        }
        return output;
    }

    /**
     * Trains on a batch of movement sequences asynchronously.
     */
    public CompletableFuture<Void> trainOnBatch(List<double[]> sequences) {
        return CompletableFuture.runAsync(() -> {
            if (sequences.isEmpty()) { return; }

            double totalLoss = 0.0;
            int validSequences = 0;

            for (double[] sequence : sequences) {
                if (sequence.length % inputSize != 0) { continue; }

                resetState(); // Reset hidden/cell state per sequence

                int numSteps = Math.min(sequence.length / inputSize - 1, sequenceLength);
                for (int t = 0; t < numSteps; t++) {
                    double[] input = Arrays.copyOfRange(sequence, t * inputSize, (t + 1) * inputSize);
                    double[] target = Arrays.copyOfRange(sequence, (t + 1) * inputSize, (t + 2) * inputSize);
                    totalLoss += trainSingleStep(input, target);
                    validSequences++;
                }
            }

            if (validSequences > 0) {
                updateLossStatistics(totalLoss / validSequences);
            }
        }, Async.IO);
    }

    private double trainSingleStep(double[] input, double[] target) {
        double[] output = predict(input);
        double loss = computeLoss(output, target);
        backpropagate(output, target);
        return loss;
    }

    private double computeLoss(double[] output, double[] target) {
        double loss = 0.0;
        for (int i = 0; i < outputSize; i++) {
            double diff = output[i] - target[i];
            loss += diff * diff;
        }
        return loss / outputSize;
    }

    private void forwardRNN(double[] input, double[] newHiddenState) {
        for (int i = 0; i < hiddenSize; i++) {
            newHiddenState[i] = hiddenBiases[i] +
                    dotProduct(inputToHiddenWeights[i], input) +
                    dotProduct(hiddenToHiddenWeights[i], hiddenState);
            newHiddenState[i] = Math.tanh(newHiddenState[i]);
        }
    }

    private void forwardLSTM(double[] input, double[] newHiddenState, double[] newCellState) {
        double[] combinedInput = new double[inputSize + hiddenSize];
        System.arraycopy(input, 0, combinedInput, 0, inputSize);
        System.arraycopy(hiddenState, 0, combinedInput, inputSize, hiddenSize);

        for (int i = 0; i < hiddenSize; i++) {
            double forgetGate = sigmoid(dotProduct(forgetGateWeights[i], combinedInput) + forgetGateBiases[i]);
            double inputGate = sigmoid(dotProduct(inputGateWeights[i], combinedInput) + inputGateBiases[i]);
            double candidate = Math.tanh(dotProduct(candidateWeights[i], combinedInput) + candidateBiases[i]);
            double outputGate = sigmoid(dotProduct(outputGateWeights[i], combinedInput) + outputGateBiases[i]);

            newCellState[i] = forgetGate * cellState[i] + inputGate * candidate;
            newHiddenState[i] = outputGate * Math.tanh(newCellState[i]);
        }
    }

    private void backpropagate(double[] output, double[] target) {
        double[] outputGradients = new double[outputSize];
        for (int i = 0; i < outputSize; i++) {
            outputGradients[i] = (output[i] - target[i]) * (1 - output[i] * output[i]);
        }

        for (int i = 0; i < outputSize; i++) {
            outputBiases[i] -= learningRate * outputGradients[i];
            for (int j = 0; j < hiddenSize; j++) {
                double gradient = clipGradient(outputGradients[i] * hiddenState[j]);
                hiddenToOutputWeights[i][j] -= learningRate * gradient;
            }
        }

        double[] hiddenGradients = new double[hiddenSize];
        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < outputSize; j++) {
                hiddenGradients[i] += outputGradients[j] * hiddenToOutputWeights[j][i];
            }
            hiddenBiases[i] -= learningRate * clipGradient(hiddenGradients[i]) * 0.1;
        }
    }

    private double clipGradient(double gradient) {
        return Math.max(-GRADIENT_CLIP_VALUE, Math.min(GRADIENT_CLIP_VALUE, gradient));
    }

    public void resetState() {
        Arrays.fill(hiddenState, 0.0);
        Arrays.fill(cellState, 0.0);
    }

    public double getConfidence(double[] prediction) {
        double confidence = 0.0;
        for (double value : prediction) {
            confidence += Math.abs(value);
        }
        return Math.min(confidence / prediction.length, 1.0);
    }

    public List<double[]> predictSequence(double[] initialInput, int steps) {
        List<double[]> sequence = new ArrayList<>(steps);
        double[] currentInput = initialInput.clone();

        for (int i = 0; i < steps; i++) {
            double[] prediction = predict(currentInput);
            sequence.add(prediction.clone());

            if (outputSize == inputSize) {
                currentInput = prediction.clone();
            } else {
                double[] nextInput = new double[inputSize];
                int len = Math.min(outputSize, inputSize);
                System.arraycopy(prediction, 0, nextInput, 0, len);
                currentInput = nextInput;
            }
        }
        return sequence;
    }

    public double[] experienceToInput(SimExperience experience) {
        double[] input = new double[inputSize];
        input[0] = normalize(experience.getStateHash() % 1000);
        input[1] = normalize((experience.getStateHash() / 1000) % 1000);
        input[2] = normalize((experience.getStateHash() / 1000000) % 1000);
        input[3] = normalize(experience.getAction(), 10.0);
        input[4] = normalize(experience.getReward(), 100.0);
        input[5] = 0.5; // Placeholder for future features
        return input;
    }

    private double normalize(double value) {
        return value / 1000.0;
    }

    private double normalize(double value, double scale) {
        return value / scale;
    }

    // Configuration setters
    public void setLearningRate(double learningRate) {
        this.learningRate = Math.max(1e-6, Math.min(0.1, learningRate));
    }

    public void setSequenceLength(int sequenceLength) {
        this.sequenceLength = Math.max(1, sequenceLength);
    }

    public void setHiddenSize(int hiddenSize) {
        this.hiddenSize = Math.max(1, hiddenSize);
        initializeNetwork();
    }

    public RNNStats getStatistics() {
        return new RNNStats(
                trainingSteps,
                averageLoss,
                lossHistory.isEmpty() ? 0.0 : lossHistory.get(lossHistory.size() - 1),
                hiddenSize,
                useLSTM
        );
    }

    public RNNParameters exportParameters() {
        return new RNNParameters(
                copyMatrix(inputToHiddenWeights),
                copyMatrix(hiddenToHiddenWeights),
                copyMatrix(hiddenToOutputWeights),
                hiddenBiases.clone(),
                outputBiases.clone(),
                hiddenState.clone(),
                cellState.clone(),
                useLSTM ? copyMatrix(forgetGateWeights) : null,
                useLSTM ? copyMatrix(inputGateWeights) : null,
                useLSTM ? copyMatrix(candidateWeights) : null,
                useLSTM ? copyMatrix(outputGateWeights) : null,
                useLSTM ? forgetGateBiases.clone() : null,
                useLSTM ? inputGateBiases.clone() : null,
                useLSTM ? candidateBiases.clone() : null,
                useLSTM ? outputGateBiases.clone() : null
        );
    }

    public void importParameters(RNNParameters params) {
        inputToHiddenWeights = copyMatrix(params.inputToHiddenWeights);
        hiddenToHiddenWeights = copyMatrix(params.hiddenToHiddenWeights);
        hiddenToOutputWeights = copyMatrix(params.hiddenToOutputWeights);
        hiddenBiases = params.hiddenBiases.clone();
        outputBiases = params.outputBiases.clone();
        hiddenState = params.hiddenState.clone();
        cellState = params.cellState.clone();

        if (useLSTM) {
            forgetGateWeights = copyMatrix(params.forgetGateWeights);
            inputGateWeights = copyMatrix(params.inputGateWeights);
            candidateWeights = copyMatrix(params.candidateWeights);
            outputGateWeights = copyMatrix(params.outputGateWeights);
            forgetGateBiases = params.forgetGateBiases.clone();
            inputGateBiases = params.inputGateBiases.clone();
            candidateBiases = params.candidateBiases.clone();
            outputGateBiases = params.outputGateBiases.clone();
        }
    }

    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-Math.max(-500, Math.min(500, x)))); // Prevent overflow
    }

    private double dotProduct(double[] a, double[] b) {
        double sum = 0.0;
        for (int i = 0; i < Math.min(a.length, b.length); i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    private void updateLossStatistics(double loss) {
        trainingSteps++;
        averageLoss = (averageLoss * (trainingSteps - 1) + loss) / trainingSteps;
        lossHistory.add(loss);
        if (lossHistory.size() > MAX_LOSS_HISTORY) {
            lossHistory.remove(0);
        }
    }

    private double[][] copyMatrix(double[][] matrix) {
        if (matrix == null) { return null; }
        double[][] copy = new double[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            copy[i] = matrix[i].clone();
        }
        return copy;
    }

    private void resetStatistics() {
        trainingSteps = 0;
        averageLoss = 0.0;
        lossHistory.clear();
    }

    public static class RNNStats {
        public final long trainingSteps;
        public final double averageLoss;
        public final double lastLoss;
        public final int hiddenSize;
        public final boolean useLSTM;

        public RNNStats(long trainingSteps, double averageLoss, double lastLoss,
                        int hiddenSize, boolean useLSTM) {
            this.trainingSteps = trainingSteps;
            this.averageLoss = averageLoss;
            this.lastLoss = lastLoss;
            this.hiddenSize = hiddenSize;
            this.useLSTM = useLSTM;
        }

        @Override
        public String toString() {
            return String.format("RNNStats{steps=%d, avgLoss=%.4f, lastLoss=%.4f, hidden=%d, lstm=%s}",
                    trainingSteps, averageLoss, lastLoss, hiddenSize, useLSTM);
        }
    }

    public static class RNNParameters {
        public final double[][] inputToHiddenWeights;
        public final double[][] hiddenToHiddenWeights;
        public final double[][] hiddenToOutputWeights;
        public final double[] hiddenBiases;
        public final double[] outputBiases;
        public final double[] hiddenState;
        public final double[] cellState;
        public final double[][] forgetGateWeights;
        public final double[][] inputGateWeights;
        public final double[][] candidateWeights;
        public final double[][] outputGateWeights;
        public final double[] forgetGateBiases;
        public final double[] inputGateBiases;
        public final double[] candidateBiases;
        public final double[] outputGateBiases;

        public RNNParameters(double[][] inputToHiddenWeights, double[][] hiddenToHiddenWeights,
                             double[][] hiddenToOutputWeights, double[] hiddenBiases,
                             double[] outputBiases, double[] hiddenState, double[] cellState,
                             double[][] forgetGateWeights, double[][] inputGateWeights,
                             double[][] candidateWeights, double[][] outputGateWeights,
                             double[] forgetGateBiases, double[] inputGateBiases,
                             double[] candidateBiases, double[] outputGateBiases) {
            this.inputToHiddenWeights = inputToHiddenWeights;
            this.hiddenToHiddenWeights = hiddenToHiddenWeights;
            this.hiddenToOutputWeights = hiddenToOutputWeights;
            this.hiddenBiases = hiddenBiases;
            this.outputBiases = outputBiases;
            this.hiddenState = hiddenState;
            this.cellState = cellState;
            this.forgetGateWeights = forgetGateWeights;
            this.inputGateWeights = inputGateWeights;
            this.candidateWeights = candidateWeights;
            this.outputGateWeights = outputGateWeights;
            this.forgetGateBiases = forgetGateBiases;
            this.inputGateBiases = inputGateBiases;
            this.candidateBiases = candidateBiases;
            this.outputGateBiases = outputGateBiases;
        }
    }
}