package chef.sheesh.eyeAI.core.ml.rnn;

import chef.sheesh.eyeAI.core.sim.SimExperience;
import chef.sheesh.eyeAI.infra.util.Async;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Recurrent Neural Network for movement pattern prediction and learning.
 * Uses LSTM-like architecture to predict future movement sequences based on past behavior.
 */
public final class MovementRNN {

    // Network architecture
    private int inputSize = 6;  // [x, y, z, velocity_x, velocity_y, velocity_z]
    private int hiddenSize = 32;
    private int outputSize = 6; // Predicted next position/delta

    // Network parameters
    private double[][] inputToHiddenWeights;
    private double[][] hiddenToHiddenWeights;
    private double[][] hiddenToOutputWeights;
    private double[] hiddenBiases;
    private double[] outputBiases;

    // Hidden state (persistent across time steps)
    private double[] hiddenState;
    private double[] cellState; // For LSTM-like behavior

    // Training parameters
    private double learningRate = 0.01;
    private double gradientClipValue = 5.0;
    private int sequenceLength = 20;
    private boolean useLSTM = true;

    // Statistics
    private long trainingSteps = 0;
    private double averageLoss = 0.0;
    private final List<Double> lossHistory = new ArrayList<>();

    public MovementRNN() {
        initializeNetwork();
    }

    public MovementRNN(int inputSize, int hiddenSize, int outputSize) {
        this.inputSize = inputSize;
        this.hiddenSize = hiddenSize;
        this.outputSize = outputSize;
        initializeNetwork();
    }

    /**
     * Initialize network with random weights
     */
    public void initRandom() {
        initializeNetwork();
    }

    /**
     * Initialize network parameters
     */
    private void initializeNetwork() {
        Random random = new Random();

        // Xavier/Glorot initialization
        double inputScale = Math.sqrt(2.0 / (inputSize + hiddenSize));
        double hiddenScale = Math.sqrt(2.0 / (hiddenSize + hiddenSize));
        double outputScale = Math.sqrt(2.0 / (hiddenSize + outputSize));

        inputToHiddenWeights = new double[hiddenSize][inputSize];
        hiddenToHiddenWeights = new double[hiddenSize][hiddenSize];
        hiddenToOutputWeights = new double[outputSize][hiddenSize];

        hiddenBiases = new double[hiddenSize];
        outputBiases = new double[outputSize];

        // Initialize weights
        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < inputSize; j++) {
                inputToHiddenWeights[i][j] = random.nextGaussian() * inputScale;
            }
            for (int j = 0; j < hiddenSize; j++) {
                hiddenToHiddenWeights[i][j] = random.nextGaussian() * hiddenScale;
            }
            hiddenBiases[i] = random.nextGaussian() * inputScale;
        }

        for (int i = 0; i < outputSize; i++) {
            for (int j = 0; j < hiddenSize; j++) {
                hiddenToOutputWeights[i][j] = random.nextGaussian() * outputScale;
            }
            outputBiases[i] = random.nextGaussian() * outputScale;
        }

        // Initialize hidden and cell states
        hiddenState = new double[hiddenSize];
        cellState = new double[hiddenSize];

        // Reset statistics
        trainingSteps = 0;
        averageLoss = 0.0;
        lossHistory.clear();
    }

    /**
     * Predict next movement based on input sequence
     */
    public double[] predict(double[] input) {
        if (input.length != inputSize) {
            throw new IllegalArgumentException("Input size mismatch");
        }

        // Forward pass
        double[] newHiddenState = new double[hiddenSize];
        double[] newCellState = new double[hiddenSize];

        if (useLSTM) {
            // LSTM forward pass
            forwardLSTM(input, newHiddenState, newCellState);
        } else {
            // Simple RNN forward pass
            forwardRNN(input, newHiddenState);
        }

        // Update hidden state
        hiddenState = newHiddenState;
        cellState = newCellState;

        // Compute output
        double[] output = new double[outputSize];
        for (int i = 0; i < outputSize; i++) {
            output[i] = outputBiases[i];
            for (int j = 0; j < hiddenSize; j++) {
                output[i] += hiddenToOutputWeights[i][j] * hiddenState[j];
            }
            output[i] = Math.tanh(output[i]); // Activation function
        }

        return output;
    }

    /**
     * Train on a batch of movement sequences
     */
    public void trainOnBatch(List<double[]> sequences) {
        if (sequences.isEmpty()) {
            return;
        }

        // Offload heavy computation to background thread
        Async.IO.submit(() -> {
            try {
                double totalLoss = 0.0;
                int batchSize = sequences.size();

                for (double[] sequence : sequences) {
                    if (sequence.length % inputSize != 0) {
                        continue; // Skip malformed sequences
                    }

                    // Convert sequence to training examples
                    int numSteps = sequence.length / inputSize - 1;
                    for (int t = 0; t < Math.min(numSteps, sequenceLength); t++) {
                        // Input at time t
                        double[] input = new double[inputSize];
                        System.arraycopy(sequence, t * inputSize, input, 0, inputSize);

                        // Target output (next position)
                        double[] target = new double[outputSize];
                        System.arraycopy(sequence, (t + 1) * inputSize, target, 0, outputSize);

                        // Forward and backward pass
                        double loss = trainSingleStep(input, target);
                        totalLoss += loss;
                    }
                }

                double batchLoss = totalLoss / batchSize;
                updateLossStatistics(batchLoss);

            } catch (Exception e) {
                System.err.println("RNN training error: " + e.getMessage());
            }
        });
    }

    /**
     * Train on a single input-target pair
     */
    private double trainSingleStep(double[] input, double[] target) {
        // Forward pass
        double[] output = predict(input);

        // Compute loss (Mean Squared Error)
        double loss = 0.0;
        for (int i = 0; i < outputSize; i++) {
            double diff = output[i] - target[i];
            loss += diff * diff;
        }
        loss /= outputSize;

        // Backward pass (simplified gradient descent)
        backpropagate(output, target);

        return loss;
    }

    /**
     * Simple RNN forward pass
     */
    private void forwardRNN(double[] input, double[] newHiddenState) {
        for (int i = 0; i < hiddenSize; i++) {
            newHiddenState[i] = hiddenBiases[i];

            // Input to hidden
            for (int j = 0; j < inputSize; j++) {
                newHiddenState[i] += inputToHiddenWeights[i][j] * input[j];
            }

            // Hidden to hidden (recurrent connection)
            for (int j = 0; j < hiddenSize; j++) {
                newHiddenState[i] += hiddenToHiddenWeights[i][j] * hiddenState[j];
            }

            // Activation function
            newHiddenState[i] = Math.tanh(newHiddenState[i]);
        }
    }

    /**
     * LSTM forward pass
     */
    private void forwardLSTM(double[] input, double[] newHiddenState, double[] newCellState) {
        for (int i = 0; i < hiddenSize; i++) {
            // Forget gate
            double forgetGate = sigmoid(
                hiddenBiases[i] +
                dotProduct(inputToHiddenWeights[i], input) +
                dotProduct(hiddenToHiddenWeights[i], hiddenState)
            );

            // Input gate
            double inputGate = sigmoid(
                hiddenBiases[i] * 1.1 + // Slight bias for input gate
                dotProduct(inputToHiddenWeights[i], input) +
                dotProduct(hiddenToHiddenWeights[i], hiddenState)
            );

            // Candidate values
            double candidate = Math.tanh(
                hiddenBiases[i] * 1.2 + // Slight bias for candidate
                dotProduct(inputToHiddenWeights[i], input) +
                dotProduct(hiddenToHiddenWeights[i], hiddenState)
            );

            // Output gate
            double outputGate = sigmoid(
                hiddenBiases[i] * 1.3 + // Slight bias for output gate
                dotProduct(inputToHiddenWeights[i], input) +
                dotProduct(hiddenToHiddenWeights[i], hiddenState)
            );

            // Update cell state
            newCellState[i] = forgetGate * cellState[i] + inputGate * candidate;

            // Update hidden state
            newHiddenState[i] = outputGate * Math.tanh(newCellState[i]);
        }
    }

    /**
     * Simplified backpropagation
     */
    private void backpropagate(double[] output, double[] target) {
        // Compute output layer gradients
        double[] outputGradients = new double[outputSize];
        for (int i = 0; i < outputSize; i++) {
            double error = output[i] - target[i];
            outputGradients[i] = error * (1 - output[i] * output[i]); // tanh derivative
        }

        // Update output layer weights
        for (int i = 0; i < outputSize; i++) {
            outputBiases[i] -= learningRate * outputGradients[i];

            for (int j = 0; j < hiddenSize; j++) {
                double gradient = outputGradients[i] * hiddenState[j];
                gradient = Math.max(-gradientClipValue, Math.min(gradientClipValue, gradient));
                hiddenToOutputWeights[i][j] -= learningRate * gradient;
            }
        }

        // Simplified hidden layer update (would be more complex in full implementation)
        for (int i = 0; i < hiddenSize; i++) {
            double hiddenGradient = 0.0;
            for (int j = 0; j < outputSize; j++) {
                hiddenGradient += outputGradients[j] * hiddenToOutputWeights[j][i];
            }

            hiddenBiases[i] -= learningRate * hiddenGradient * 0.1; // Smaller learning rate for hidden layer
        }
    }

    /**
     * Reset hidden state (useful for starting new sequences)
     */
    public void resetState() {
        Arrays.fill(hiddenState, 0.0);
        Arrays.fill(cellState, 0.0);
    }

    /**
     * Get prediction confidence (simplified)
     */
    public double getConfidence(double[] prediction) {
        double confidence = 0.0;
        for (double value : prediction) {
            confidence += Math.abs(value); // Simple confidence based on output magnitude
        }
        return Math.min(confidence / prediction.length, 1.0);
    }

    /**
     * Generate movement sequence prediction
     */
    public List<double[]> predictSequence(double[] initialInput, int steps) {
        List<double[]> sequence = new ArrayList<>();
        double[] currentInput = initialInput.clone();

        for (int i = 0; i < steps; i++) {
            double[] prediction = predict(currentInput);
            sequence.add(prediction.clone());

            // Use prediction as next input (autoregressive)
            currentInput = prediction.clone();
        }

        return sequence;
    }

    /**
     * Convert movement experience to RNN input format
     */
    public double[] experienceToInput(SimExperience experience) {
        // Convert experience data to RNN input format
        // This is a simplified conversion - would be more sophisticated in practice
        double[] input = new double[inputSize];

        // Position data (simplified)
        input[0] = experience.getStateHash() % 1000 / 1000.0; // Normalized x
        input[1] = (experience.getStateHash() / 1000) % 1000 / 1000.0; // Normalized y
        input[2] = (experience.getStateHash() / 1000000) % 1000 / 1000.0; // Normalized z

        // Velocity/action data
        input[3] = experience.getAction() / 10.0; // Normalized action
        input[4] = experience.getReward() / 100.0; // Normalized reward
        input[5] = 0.5; // Placeholder for additional features

        return input;
    }

    // Configuration methods

    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }

    public void setSequenceLength(int sequenceLength) {
        this.sequenceLength = sequenceLength;
    }

    public void setUseLSTM(boolean useLSTM) {
        this.useLSTM = useLSTM;
    }

    public void setHiddenSize(int hiddenSize) {
        this.hiddenSize = hiddenSize;
        initializeNetwork(); // Reinitialize with new size
    }

    /**
     * Get training statistics
     */
    public RNNStats getStatistics() {
        return new RNNStats(
            trainingSteps,
            averageLoss,
            lossHistory.size() > 0 ? lossHistory.get(lossHistory.size() - 1) : 0.0,
            hiddenSize,
            useLSTM
        );
    }

    /**
     * Export network parameters
     */
    public RNNParameters exportParameters() {
        return new RNNParameters(
            copyMatrix(inputToHiddenWeights),
            copyMatrix(hiddenToHiddenWeights),
            copyMatrix(hiddenToOutputWeights),
            hiddenBiases.clone(),
            outputBiases.clone(),
            hiddenState.clone(),
            cellState.clone()
        );
    }

    /**
     * Import network parameters
     */
    public void importParameters(RNNParameters parameters) {
        inputToHiddenWeights = copyMatrix(parameters.inputToHiddenWeights);
        hiddenToHiddenWeights = copyMatrix(parameters.hiddenToHiddenWeights);
        hiddenToOutputWeights = copyMatrix(parameters.hiddenToOutputWeights);
        hiddenBiases = parameters.hiddenBiases.clone();
        outputBiases = parameters.outputBiases.clone();
        hiddenState = parameters.hiddenState.clone();
        cellState = parameters.cellState.clone();
    }

    // Private helper methods

    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    private double dotProduct(double[] a, double[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    private void updateLossStatistics(double loss) {
        trainingSteps++;
        averageLoss = (averageLoss * (trainingSteps - 1) + loss) / trainingSteps;

        lossHistory.add(loss);
        if (lossHistory.size() > 1000) {
            lossHistory.remove(0);
        }
    }

    private double[][] copyMatrix(double[][] matrix) {
        double[][] copy = new double[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            copy[i] = matrix[i].clone();
        }
        return copy;
    }

    /**
     * Statistics for RNN
     */
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

    /**
     * Network parameters for serialization
     */
    public static class RNNParameters {
        public final double[][] inputToHiddenWeights;
        public final double[][] hiddenToHiddenWeights;
        public final double[][] hiddenToOutputWeights;
        public final double[] hiddenBiases;
        public final double[] outputBiases;
        public final double[] hiddenState;
        public final double[] cellState;

        public RNNParameters(double[][] inputToHiddenWeights, double[][] hiddenToHiddenWeights,
                           double[][] hiddenToOutputWeights, double[] hiddenBiases,
                           double[] outputBiases, double[] hiddenState, double[] cellState) {
            this.inputToHiddenWeights = inputToHiddenWeights;
            this.hiddenToHiddenWeights = hiddenToHiddenWeights;
            this.hiddenToOutputWeights = hiddenToOutputWeights;
            this.hiddenBiases = hiddenBiases;
            this.outputBiases = outputBiases;
            this.hiddenState = hiddenState;
            this.cellState = cellState;
        }
    }
}
