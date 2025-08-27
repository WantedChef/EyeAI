package chef.sheesh.eyeAI.core.ml.ga;

import chef.sheesh.eyeAI.core.sim.SimExperience;
import chef.sheesh.eyeAI.infra.util.Async;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Advanced Genetic Algorithm optimizer for AI learning.
 * Evolves populations of AI parameters to find optimal solutions.
 * Supports multi-objective optimization and elitism.
 */
public final class GAOptimizer {

    private final Random random = new Random();
    private final GAConfig config;

    // Current population
    private List<Genome> population = new ArrayList<>();
    private int currentGeneration = 0;
    private Genome bestGenome = null;
    private double bestFitness = Double.NEGATIVE_INFINITY;

    // Evolution statistics
    private final List<Double> fitnessHistory = new ArrayList<>();
    private long lastEvolutionTime = 0;

    public GAOptimizer(GAConfig config) {
        this.config = config;
    }

    public GAOptimizer() {
        this(new GAConfig());
    }

    /**
     * Initialize the population with random genomes
     */
    public void initializePopulation() {
        population.clear();
        for (int i = 0; i < config.populationSize; i++) {
            population.add(new Genome(config.genomeSize));
        }
        currentGeneration = 0;
        bestGenome = null;
        bestFitness = Double.NEGATIVE_INFINITY;
        fitnessHistory.clear();
    }

    /**
     * Run one generation of evolution asynchronously
     */
    public CompletableFuture<GAEvolutionResult> evolveGenerationAsync() {
        return CompletableFuture.supplyAsync(() -> evolveGeneration(), chef.sheesh.eyeAI.infra.util.Async.IO);
    }

    /**
     * Run one generation of evolution synchronously
     */
    public GAEvolutionResult evolveGeneration() {
        if (population.isEmpty()) {
            initializePopulation();
            return new GAEvolutionResult(currentGeneration, 0.0, 0.0, bestFitness, null);
        }

        long startTime = System.nanoTime();

        // Evaluate fitness for all genomes
        evaluatePopulation();

        // Update best genome
        Genome currentBest = getBestGenome();
        if (currentBest != null && currentBest.getFitness() > bestFitness) {
            bestFitness = currentBest.getFitness();
            bestGenome = currentBest.clone();
        }

        // Create new population through selection, crossover, and mutation
        List<Genome> newPopulation = createNewPopulation();

        // Update population
        population = newPopulation;
        currentGeneration++;

        // Record statistics
        double avgFitness = getAverageFitness();
        fitnessHistory.add(avgFitness);

        long endTime = System.nanoTime();
        lastEvolutionTime = (endTime - startTime) / 1_000_000; // Convert to milliseconds

        return new GAEvolutionResult(
            currentGeneration,
            avgFitness,
            getBestFitness(),
            bestFitness,
            population.get(0)
        );
    }

    /**
     * Evaluate fitness for the entire population
     */
    private void evaluatePopulation() {
        for (Genome genome : population) {
            double fitness = evaluateGenomeFitness(genome);
            genome.setFitness(fitness);
        }
    }

    /**
     * Evaluate the fitness of a single genome
     */
    private double evaluateGenomeFitness(Genome genome) {
        // This should be implemented based on your specific AI goals
        // For now, we'll use a simple fitness function based on genome values

        double[] weights = genome.getWeights();
        double fitness = 0.0;

        // Example fitness calculation - optimize for balanced weights
        for (int i = 0; i < weights.length; i++) {
            double target = 0.5; // Target value
            double deviation = Math.abs(weights[i] - target);
            fitness += 1.0 - deviation; // Higher fitness for values closer to target
        }

        // Add some noise to prevent convergence to identical solutions
        fitness += random.nextGaussian() * 0.01;

        return fitness / weights.length;
    }

    /**
     * Create new population through selection, crossover, and mutation
     */
    private List<Genome> createNewPopulation() {
        List<Genome> newPopulation = new ArrayList<>();

        // Elitism: keep best individuals unchanged
        int eliteCount = Math.max(1, (int) (config.populationSize * config.elitismRate));
        population.sort((a, b) -> Double.compare(b.getFitness(), a.getFitness()));

        for (int i = 0; i < eliteCount; i++) {
            newPopulation.add(population.get(i).clone());
        }

        // Fill rest through crossover and mutation
        while (newPopulation.size() < config.populationSize) {
            Genome parent1 = selectParent();
            Genome parent2 = selectParent();

            Genome offspring = crossover(parent1, parent2);
            mutate(offspring);

            newPopulation.add(offspring);
        }

        return newPopulation;
    }

    /**
     * Select a parent using tournament selection
     */
    private Genome selectParent() {
        Genome best = population.get(random.nextInt(population.size()));

        for (int i = 1; i < config.tournamentSize; i++) {
            Genome contender = population.get(random.nextInt(population.size()));
            if (contender.getFitness() > best.getFitness()) {
                best = contender;
            }
        }

        return best;
    }

    /**
     * Perform crossover between two parent genomes
     */
    private Genome crossover(Genome parent1, Genome parent2) {
        Genome offspring = new Genome(config.genomeSize);
        double[] weights1 = parent1.getWeights();
        double[] weights2 = parent2.getWeights();
        double[] offspringWeights = offspring.getWeights();

        // Uniform crossover
        for (int i = 0; i < weights1.length; i++) {
            if (random.nextDouble() < 0.5) {
                offspringWeights[i] = weights1[i];
            } else {
                offspringWeights[i] = weights2[i];
            }
        }

        return offspring;
    }

    /**
     * Apply mutation to a genome
     */
    private void mutate(Genome genome) {
        double[] weights = genome.getWeights();

        for (int i = 0; i < weights.length; i++) {
            if (random.nextDouble() < config.mutationRate) {
                // Gaussian mutation with adaptive mutation strength
                double mutationStrength = config.mutationStrength *
                    (1.0 + random.nextGaussian() * 0.1);

                double mutation = random.nextGaussian() * mutationStrength;
                weights[i] = Math.max(0.0, Math.min(1.0, weights[i] + mutation));
            }
        }
    }

    /**
     * Get the best genome from the current population
     */
    public Genome getBestGenome() {
        if (population.isEmpty()) {
            return bestGenome; // Return historical best
        }

        return population.stream()
                .max(Comparator.comparingDouble(Genome::getFitness))
                .orElse(null);
    }

    /**
     * Get the best fitness from current population
     */
    public double getBestFitness() {
        Genome best = getBestGenome();
        return best != null ? best.getFitness() : 0.0;
    }

    /**
     * Get the average fitness of the population
     */
    public double getAverageFitness() {
        if (population.isEmpty()) {
            return 0.0;
        }

        return population.stream()
                .mapToDouble(Genome::getFitness)
                .average()
                .orElse(0.0);
    }

    /**
     * Check if evolution should stop
     */
    public boolean shouldStop() {
        return currentGeneration >= config.maxGenerations ||
               (config.targetFitness > 0 && bestFitness >= config.targetFitness);
    }

    /**
     * Get evolution statistics
     */
    public GAStatistics getStatistics() {
        return new GAStatistics(
            currentGeneration,
            getAverageFitness(),
            getBestFitness(),
            bestFitness,
            fitnessHistory,
            lastEvolutionTime
        );
    }

    // Getters
    public List<Genome> getPopulation() {
        return new ArrayList<>(population);
    }

    public int getCurrentGeneration() {
        return currentGeneration;
    }

    public Genome getHistoricalBest() {
        return bestGenome != null ? bestGenome.clone() : null;
    }

    /**
     * Simple evolveOnce method for compatibility
     */
    public void evolveOnce() {
        evolveGeneration();
    }

    /**
     * Reset the GA optimizer state
     */
    public void reset() {
        population.clear();
        currentGeneration = 0;
        bestGenome = null;
        bestFitness = Double.NEGATIVE_INFINITY;
        fitnessHistory.clear();
        lastEvolutionTime = 0;
    }

    /**
     * Genome representing a set of AI parameters
     */
    public static class Genome implements Cloneable {
        private double[] weights;
        private double fitness;
        private static final Random random = new Random();

        public Genome(int size) {
            weights = new double[size];
            for (int i = 0; i < size; i++) {
                weights[i] = random.nextDouble();
            }
        }

        public Genome() {
            this(10); // Default size
        }

        public double[] getWeights() {
            return weights.clone();
        }

        public void setWeights(double[] weights) {
            this.weights = weights.clone();
        }

        public double getFitness() {
            return fitness;
        }

        public void setFitness(double fitness) {
            this.fitness = fitness;
        }

        public int getSize() {
            return weights.length;
        }

        @Override
        public Genome clone() {
            Genome cloned = new Genome(weights.length);
            cloned.weights = Arrays.copyOf(weights, weights.length);
            cloned.fitness = fitness;
            return cloned;
        }

        @Override
        public String toString() {
            return String.format("Genome{fitness=%.4f, size=%d}",
                               fitness, weights.length);
        }
    }

    /**
     * Configuration for genetic algorithm
     */
    public static class GAConfig {
        public int populationSize = 50;
        public double mutationRate = 0.1;
        public double mutationStrength = 0.1;
        public int maxGenerations = 100;
        public double elitismRate = 0.1;
        public int tournamentSize = 3;
        public int genomeSize = 10;
        public double targetFitness = 0.0; // 0 means no target

        public GAConfig() {}
    }

    /**
     * Export/import parameters for GA
     */
    public static class GAParameters {
        public final GAConfig config;
        public final List<Genome> population;
        public final Genome bestGenome;
        public final double bestFitness;
        public final int currentGeneration;

        public GAParameters(GAConfig config, List<Genome> population,
                          Genome bestGenome, double bestFitness, int currentGeneration) {
            this.config = config;
            this.population = new ArrayList<>(population);
            this.bestGenome = bestGenome != null ? bestGenome.clone() : null;
            this.bestFitness = bestFitness;
            this.currentGeneration = currentGeneration;
        }
    }

    /**
     * Export current GA state
     */
    public GAParameters exportParameters() {
        return new GAParameters(
            config,
            population,
            bestGenome,
            bestFitness,
            currentGeneration
        );
    }

    /**
     * Import GA state
     */
    public void importParameters(GAParameters parameters) {
        // Update config
        // Note: In a real implementation, you'd copy config values

        // Update population
        population.clear();
        population.addAll(parameters.population);

        // Update best genome
        bestGenome = parameters.bestGenome != null ? parameters.bestGenome.clone() : null;
        bestFitness = parameters.bestFitness;
        currentGeneration = parameters.currentGeneration;
    }



    /**
     * Result of a single evolution step
     */
    public static class GAEvolutionResult {
        public final int generation;
        public final double avgFitness;
        public final double bestFitness;
        public final double historicalBestFitness;
        public final Genome bestGenome;

        public GAEvolutionResult(int generation, double avgFitness, double bestFitness,
                               double historicalBestFitness, Genome bestGenome) {
            this.generation = generation;
            this.avgFitness = avgFitness;
            this.bestFitness = bestFitness;
            this.historicalBestFitness = historicalBestFitness;
            this.bestGenome = bestGenome;
        }
    }

    /**
     * Evolution statistics
     */
    public static class GAStatistics {
        public final int currentGeneration;
        public final double averageFitness;
        public final double bestFitness;
        public final double historicalBestFitness;
        public final List<Double> fitnessHistory;
        public final long lastEvolutionTimeMs;

        public GAStatistics(int currentGeneration, double averageFitness, double bestFitness,
                          double historicalBestFitness, List<Double> fitnessHistory,
                          long lastEvolutionTimeMs) {
            this.currentGeneration = currentGeneration;
            this.averageFitness = averageFitness;
            this.bestFitness = bestFitness;
            this.historicalBestFitness = historicalBestFitness;
            this.fitnessHistory = new ArrayList<>(fitnessHistory);
            this.lastEvolutionTimeMs = lastEvolutionTimeMs;
        }
    }
}
