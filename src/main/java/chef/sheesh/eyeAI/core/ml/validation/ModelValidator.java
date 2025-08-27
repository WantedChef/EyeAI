package chef.sheesh.eyeAI.core.ml.validation;

import chef.sheesh.eyeAI.core.ml.features.FeatureEngineer;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.core.sim.SimExperience;
import chef.sheesh.eyeAI.infra.util.Async;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Model validation and performance testing for ML components.
 * Provides comprehensive evaluation metrics and validation strategies.
 */
public final class ModelValidator {

    // Validation configuration
    private double trainTestSplit = 0.8; // 80% train, 20% test
    private int crossValidationFolds = 5;
    private int minTestSamples = 100;
    private boolean useStratifiedSampling = true;

    // Performance metrics
    private final Map<String, ValidationMetrics> metricsHistory = new HashMap<>();
    private final List<ValidationResult> validationHistory = new ArrayList<>();

    /**
     * Validate Q-learning agent performance
     */
    public CompletableFuture<ValidationResult> validateQAgent(
            Map<Long, double[]> qTable,
            List<SimExperience> testExperiences) {

        return CompletableFuture.supplyAsync(() -> {
            if (testExperiences.size() < minTestSamples) {
                return new ValidationResult("INSUFFICIENT_DATA", 0.0, 0.0, 0.0,
                                          Collections.emptyMap(), System.currentTimeMillis());
            }

            double totalReward = 0.0;
            double correctPredictions = 0.0;
            double totalPredictions = 0.0;
            int convergenceSteps = 0;

            Map<String, Double> additionalMetrics = new HashMap<>();

            // Simulate agent performance on test data
            for (SimExperience exp : testExperiences) {
                long state = exp.getStateHash();
                int trueAction = exp.getAction();
                double reward = exp.getReward();

                totalReward += reward;

                // Get Q-values for this state
                double[] qValues = qTable.getOrDefault(state, new double[10]);

                if (qValues.length > 0) {
                    // Find best action according to model
                    int predictedAction = 0;
                    double maxQ = Double.NEGATIVE_INFINITY;

                    for (int i = 0; i < qValues.length; i++) {
                        if (qValues[i] > maxQ) {
                            maxQ = qValues[i];
                            predictedAction = i;
                        }
                    }

                    totalPredictions++;
                    if (predictedAction == trueAction) {
                        correctPredictions++;
                    }

                    // Track convergence (how quickly Q-values stabilize)
                    if (Math.abs(qValues[trueAction] - reward) < 0.1) {
                        convergenceSteps++;
                    }
                }
            }

            double accuracy = totalPredictions > 0 ? correctPredictions / totalPredictions : 0.0;
            double avgReward = testExperiences.size() > 0 ? totalReward / testExperiences.size() : 0.0;
            double convergenceRate = testExperiences.size() > 0 ? (double) convergenceSteps / testExperiences.size() : 0.0;

            additionalMetrics.put("total_predictions", totalPredictions);
            additionalMetrics.put("correct_predictions", correctPredictions);
            additionalMetrics.put("total_reward", totalReward);
            additionalMetrics.put("convergence_steps", (double) convergenceSteps);
            additionalMetrics.put("q_table_size", (double) qTable.size());

            String status = accuracy > 0.6 ? "GOOD" : accuracy > 0.4 ? "FAIR" : "POOR";

            ValidationResult result = new ValidationResult(
                status, accuracy, avgReward, convergenceRate,
                additionalMetrics, System.currentTimeMillis()
            );

            validationHistory.add(result);
            metricsHistory.put("Q_AGENT", new ValidationMetrics(
                accuracy, avgReward, convergenceRate, testExperiences.size()
            ));

            return result;
        });
    }

    /**
     * Validate RNN movement prediction performance
     */
    public CompletableFuture<ValidationResult> validateRNNMovement(
            List<double[]> testSequences,
            java.util.function.Function<double[], double[]> predictor) {

        return CompletableFuture.supplyAsync(() -> {
            if (testSequences.size() < minTestSamples) {
                return new ValidationResult("INSUFFICIENT_DATA", 0.0, 0.0, 0.0,
                                          Collections.emptyMap(), System.currentTimeMillis());
            }

            double totalMSE = 0.0;
            double totalMAE = 0.0;
            int totalPredictions = 0;
            double maxError = 0.0;

            Map<String, Double> additionalMetrics = new HashMap<>();

            // Test prediction accuracy
            for (double[] sequence : testSequences) {
                if (sequence.length < 12) {
                    continue; // Need at least input + output
                }

                // Use first part as input
                double[] input = new double[6];
                System.arraycopy(sequence, 0, input, 0, 6);

                // Next part as expected output
                double[] expected = new double[6];
                System.arraycopy(sequence, 6, expected, 0, 6);

                // Get prediction
                double[] prediction = predictor.apply(input);

                // Calculate errors
                double mse = 0.0;
                double mae = 0.0;

                for (int i = 0; i < 6; i++) {
                    double error = prediction[i] - expected[i];
                    mse += error * error;
                    mae += Math.abs(error);
                    maxError = Math.max(maxError, Math.abs(error));
                }

                mse /= 6.0;
                mae /= 6.0;

                totalMSE += mse;
                totalMAE += mae;
                totalPredictions++;
            }

            double avgMSE = totalPredictions > 0 ? totalMSE / totalPredictions : 0.0;
            double avgMAE = totalPredictions > 0 ? totalMAE / totalPredictions : 0.0;
            double rmse = Math.sqrt(avgMSE);

            // Convert to accuracy score (lower error = higher accuracy)
            double accuracy = Math.max(0.0, 1.0 - Math.min(1.0, avgMAE / 10.0));

            additionalMetrics.put("mse", avgMSE);
            additionalMetrics.put("mae", avgMAE);
            additionalMetrics.put("rmse", rmse);
            additionalMetrics.put("max_error", maxError);
            additionalMetrics.put("total_predictions", (double) totalPredictions);

            String status = accuracy > 0.7 ? "GOOD" : accuracy > 0.5 ? "FAIR" : "POOR";

            ValidationResult result = new ValidationResult(
                status, accuracy, -avgMAE, rmse, // Negative MAE as "reward"
                additionalMetrics, System.currentTimeMillis()
            );

            validationHistory.add(result);
            metricsHistory.put("RNN_MOVEMENT", new ValidationMetrics(
                accuracy, -avgMAE, rmse, testSequences.size()
            ));

            return result;
        });
    }

    /**
     * Validate GA performance
     */
    public CompletableFuture<ValidationResult> validateGA(
            List<double[]> testGenomes,
            java.util.function.Function<double[], Double> fitnessFunction) {

        return CompletableFuture.supplyAsync(() -> {
            if (testGenomes.size() < 10) {
                return new ValidationResult("INSUFFICIENT_DATA", 0.0, 0.0, 0.0,
                                          Collections.emptyMap(), System.currentTimeMillis());
            }

            double bestFitness = Double.NEGATIVE_INFINITY;
            double avgFitness = 0.0;
            double fitnessVariance = 0.0;
            int diverseGenomes = 0;

            List<Double> fitnesses = new ArrayList<>();

            // Evaluate all genomes
            for (double[] genome : testGenomes) {
                double fitness = fitnessFunction.apply(genome);
                fitnesses.add(fitness);
                avgFitness += fitness;

                if (fitness > bestFitness) {
                    bestFitness = fitness;
                }

                // Count diverse solutions (fitness within 80% of best)
                if (fitness > bestFitness * 0.8) {
                    diverseGenomes++;
                }
            }

            avgFitness /= testGenomes.size();

            // Calculate variance
            for (double fitness : fitnesses) {
                fitnessVariance += Math.pow(fitness - avgFitness, 2);
            }
            fitnessVariance /= testGenomes.size();

            double diversityRatio = (double) diverseGenomes / testGenomes.size();
            double improvementRate = bestFitness / avgFitness; // How much better is best than average

            // GA accuracy = combination of best fitness and diversity
            double accuracy = (bestFitness / 10.0 + diversityRatio) / 2.0; // Normalized
            accuracy = Math.max(0.0, Math.min(1.0, accuracy));

            Map<String, Double> additionalMetrics = new HashMap<>();
            additionalMetrics.put("best_fitness", bestFitness);
            additionalMetrics.put("avg_fitness", avgFitness);
            additionalMetrics.put("fitness_variance", fitnessVariance);
            additionalMetrics.put("diversity_ratio", diversityRatio);
            additionalMetrics.put("improvement_rate", improvementRate);
            additionalMetrics.put("population_size", (double) testGenomes.size());

            String status = accuracy > 0.7 ? "GOOD" : accuracy > 0.5 ? "FAIR" : "POOR";

            ValidationResult result = new ValidationResult(
                status, accuracy, bestFitness, diversityRatio,
                additionalMetrics, System.currentTimeMillis()
            );

            validationHistory.add(result);
            metricsHistory.put("GA", new ValidationMetrics(
                accuracy, bestFitness, diversityRatio, testGenomes.size()
            ));

            return result;
        });
    }

    /**
     * Cross-validation for robust evaluation
     */
    public CompletableFuture<CrossValidationResult> crossValidate(
            List<SimExperience> allExperiences,
            java.util.function.Function<List<SimExperience>, ValidationResult> validator) {

        return CompletableFuture.supplyAsync(() -> {
            if (allExperiences.size() < crossValidationFolds * minTestSamples) {
                return new CrossValidationResult(
                    "INSUFFICIENT_DATA",
                    new double[0],
                    0.0, 0.0, 0.0, 0.0,
                    System.currentTimeMillis()
                );
            }

            // Shuffle data
            List<SimExperience> shuffled = new ArrayList<>(allExperiences);
            Collections.shuffle(shuffled, new Random());

            int foldSize = shuffled.size() / crossValidationFolds;
            double[] foldAccuracies = new double[crossValidationFolds];
            double[] foldRewards = new double[crossValidationFolds];

            // Perform cross-validation
            for (int fold = 0; fold < crossValidationFolds; fold++) {
                int testStart = fold * foldSize;
                int testEnd = (fold + 1) * foldSize;

                // Split data
                List<SimExperience> testData = shuffled.subList(testStart, testEnd);
                List<SimExperience> trainData = new ArrayList<>();
                trainData.addAll(shuffled.subList(0, testStart));
                trainData.addAll(shuffled.subList(testEnd, shuffled.size()));

                // Note: In a real implementation, you would train on trainData here
                // For now, we'll just validate on testData as a placeholder

                ValidationResult foldResult = validator.apply(testData);
                foldAccuracies[fold] = foldResult.accuracy;
                foldRewards[fold] = foldResult.averageReward;
            }

            // Calculate statistics
            double meanAccuracy = Arrays.stream(foldAccuracies).average().orElse(0.0);
            double stdAccuracy = calculateStdDev(foldAccuracies, meanAccuracy);
            double meanReward = Arrays.stream(foldRewards).average().orElse(0.0);
            double stdReward = calculateStdDev(foldRewards, meanReward);

            String status = stdAccuracy < 0.1 ? "STABLE" : stdAccuracy < 0.2 ? "MODERATE" : "UNSTABLE";

            return new CrossValidationResult(
                status,
                foldAccuracies,
                meanAccuracy,
                stdAccuracy,
                meanReward,
                stdReward,
                System.currentTimeMillis()
            );
        });
    }

    /**
     * Get comprehensive validation report
     */
    public ValidationReport getValidationReport() {
        return new ValidationReport(
            metricsHistory,
            validationHistory,
            System.currentTimeMillis()
        );
    }

    /**
     * Clear validation history
     */
    public void clearHistory() {
        metricsHistory.clear();
        validationHistory.clear();
    }

    // Configuration methods

    public void setTrainTestSplit(double split) {
        this.trainTestSplit = Math.max(0.1, Math.min(0.9, split));
    }

    public void setCrossValidationFolds(int folds) {
        this.crossValidationFolds = Math.max(2, Math.min(10, folds));
    }

    public void setMinTestSamples(int samples) {
        this.minTestSamples = Math.max(10, samples);
    }

    public void setUseStratifiedSampling(boolean use) {
        this.useStratifiedSampling = use;
    }

    // Private helper methods

    private double calculateStdDev(double[] values, double mean) {
        double variance = 0.0;
        for (double value : values) {
            variance += Math.pow(value - mean, 2);
        }
        variance /= values.length;
        return Math.sqrt(variance);
    }

    // Data classes

    public static class ValidationResult {
        public final String status;
        public final double accuracy;
        public final double averageReward;
        public final double additionalMetric;
        public final Map<String, Double> metrics;
        public final long timestamp;

        public ValidationResult(String status, double accuracy, double averageReward,
                              double additionalMetric, Map<String, Double> metrics, long timestamp) {
            this.status = status;
            this.accuracy = accuracy;
            this.averageReward = averageReward;
            this.additionalMetric = additionalMetric;
            this.metrics = new HashMap<>(metrics);
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return String.format("ValidationResult{status=%s, accuracy=%.3f, avgReward=%.3f, timestamp=%d}",
                               status, accuracy, averageReward, timestamp);
        }
    }

    public static class CrossValidationResult {
        public final String stability;
        public final double[] foldAccuracies;
        public final double meanAccuracy;
        public final double stdAccuracy;
        public final double meanReward;
        public final double stdReward;
        public final long timestamp;

        public CrossValidationResult(String stability, double[] foldAccuracies,
                                   double meanAccuracy, double stdAccuracy,
                                   double meanReward, double stdReward, long timestamp) {
            this.stability = stability;
            this.foldAccuracies = foldAccuracies.clone();
            this.meanAccuracy = meanAccuracy;
            this.stdAccuracy = stdAccuracy;
            this.meanReward = meanReward;
            this.stdReward = stdReward;
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return String.format("CrossValidationResult{stability=%s, meanAcc=%.3f±%.3f, meanReward=%.3f±%.3f}",
                               stability, meanAccuracy, stdAccuracy, meanReward, stdReward);
        }
    }

    public static class ValidationMetrics {
        public final double accuracy;
        public final double performance;
        public final double stability;
        public final int sampleSize;

        public ValidationMetrics(double accuracy, double performance, double stability, int sampleSize) {
            this.accuracy = accuracy;
            this.performance = performance;
            this.stability = stability;
            this.sampleSize = sampleSize;
        }
    }

    public static class ValidationReport {
        public final Map<String, ValidationMetrics> currentMetrics;
        public final List<ValidationResult> validationHistory;
        public final long generatedAt;

        public ValidationReport(Map<String, ValidationMetrics> currentMetrics,
                              List<ValidationResult> validationHistory, long generatedAt) {
            this.currentMetrics = new HashMap<>(currentMetrics);
            this.validationHistory = new ArrayList<>(validationHistory);
            this.generatedAt = generatedAt;
        }

        public String getOverallStatus() {
            if (currentMetrics.isEmpty()) {
                return "NO_DATA";
            }

            double avgAccuracy = currentMetrics.values().stream()
                .mapToDouble(m -> m.accuracy)
                .average().orElse(0.0);

            if (avgAccuracy > 0.8) {
                return "EXCELLENT";
            }
            if (avgAccuracy > 0.7) {
                return "GOOD";
            }
            if (avgAccuracy > 0.6) {
                return "FAIR";
            }
            return "NEEDS_IMPROVEMENT";
        }
    }
}
