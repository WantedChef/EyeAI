package chef.sheesh.eyeAI.core.ml;

import chef.sheesh.eyeAI.core.ml.algorithms.QAgent;
import chef.sheesh.eyeAI.core.ml.buffer.ExperienceBuffer;
import chef.sheesh.eyeAI.core.ml.models.Action;
import chef.sheesh.eyeAI.core.ml.models.Experience;
import chef.sheesh.eyeAI.core.ml.models.GameState;
import chef.sheesh.eyeAI.core.sim.SimExperience;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.infra.config.ConfigurationManager;
import chef.sheesh.eyeAI.infra.events.EventBus;
import chef.sheesh.eyeAI.core.ml.ga.GAOptimizer;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.UUID;

/**
 * Central manager for all Machine Learning operations.
 * This class orchestrates the agent, experience storage, and training processes.
 */
public class MLManager {

    private static final int BATCH_SIZE = 32;

    private final QAgent qAgent;
    private final ExperienceBuffer experienceBuffer;
    private final EventBus eventBus;
    private final ConfigurationManager config;
    private final GAOptimizer gaOptimizer = new GAOptimizer();

    private boolean learningEnabled = true;
    private int batchSize = BATCH_SIZE;

    private final MLStatistics statistics = new MLStatistics();
    private double rewardRunningTotal = 0.0;

    public MLManager(int experienceBufferCapacity, double learningRate, double discountFactor, double explorationRate) {
        this.experienceBuffer = new ExperienceBuffer(experienceBufferCapacity);
        this.qAgent = new QAgent(learningRate, discountFactor, explorationRate);
        this.eventBus = null;
        this.config = null;
    }

    // Compatibility constructor used by MLCore/MLService
    public MLManager(EventBus eventBus, ConfigurationManager config) {
        this.eventBus = eventBus;
        this.config = config;
        // Defaults if config not yet specifying values
        this.experienceBuffer = new ExperienceBuffer(10_000);
        this.qAgent = new QAgent(0.1, 0.99, 0.1);
    }

    /**
     * Gets the best action for a given agent in a specific state.
     *
     * @param state The current game state.
     * @param agentId The ID of the agent (currently unused as we have a single model).
     * @return The best action to take.
     */
    public Action getBestAction(GameState state, UUID agentId) {
        // All agents share the same underlying Q-learning model
        return qAgent.decideAction(state);
    }

    /**
     * Processes a new experience.
     * The experience is stored in the buffer for later batch training.
     *
     * @param state The state.
     * @param action The action taken.
     * @param reward The reward received.
     * @param nextState The next state observed.
     */
    public void processExperience(GameState state, Action action, double reward, GameState nextState) {
        Experience exp = new Experience(state, action, reward, nextState);
        experienceBuffer.addExperience(exp);
    }

    /**
     * Trains the model on a batch of experiences from the buffer using Prioritized Experience Replay.
     * This method should be called periodically.
     */
    public void trainBatch() {
        if (experienceBuffer.getSize() < BATCH_SIZE) {
            return; // Not enough experiences to form a batch
        }

        // Sample a batch of experiences with priorities
        ExperienceBuffer.SampledBatch batch = experienceBuffer.sampleBatch(BATCH_SIZE);

        // Train the agent on the batch and get the TD errors
        double[] tdErrors = qAgent.trainOnBatch(batch);

        // Update the priorities in the experience buffer
        experienceBuffer.updatePriorities(batch.treeIndices(), tdErrors);

        statistics.totalTrainingBatches++;
    }

    // This is a placeholder. In a real implementation, this would involve
    // getting the current state from the game world for a specific agent.
    private GameState getCurrentGameState(UUID agentId) {
        // Dummy implementation
        return null;
    }

    // ===================== Compatibility API for MLCore/MLService =====================

    public void initializeMLComponents() {
        // Initialize components if needed; currently no-op
    }

    public MLModels exportModels() {
        MLModels models = new MLModels();
        models.exportTime = System.currentTimeMillis();
        return models;
    }

    public void importModels(MLModels models) {
        // No-op placeholder for model import
    }

    public void addExperience(SimExperience exp) {
        statistics.totalExperiencesProcessed++;
        rewardRunningTotal += exp.getReward();
        statistics.averageReward = statistics.totalExperiencesProcessed > 0
            ? (rewardRunningTotal / statistics.totalExperiencesProcessed)
            : 0.0;

        // Update placeholder Q-stats table with simple heuristic (expand if needed)
        statistics.qStats.qTable.computeIfAbsent(exp.getStateHash(), k -> new double[Math.max(1, 10)]);
        statistics.qStats.updateCount++;
    }

    public void addPlayerExperience(FakePlayer fakePlayer, SimExperience experience) {
        addExperience(experience);
    }

    public Location predictNextLocation(Player player) {
        // Placeholder: return current location; a predictive model can be added later
        return player.getLocation().clone();
    }

    public int selectAction(long stateHash, int maxActions) {
        // Epsilon-greedy placeholder using stored qTable if present
        double[] qValues = statistics.qStats.qTable.get(stateHash);
        if (qValues == null || qValues.length == 0) {
            return 0;
        }
        int bestIdx = 0;
        double bestVal = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < Math.min(qValues.length, maxActions); i++) {
            if (qValues[i] > bestVal) {
                bestVal = qValues[i];
                bestIdx = i;
            }
        }
        return bestIdx;
    }

    public int getBestAction(long stateHash, int maxActions) {
        return selectAction(stateHash, maxActions);
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = Math.max(1, batchSize);
    }

    public CompletableFuture<MLTrainingResult> trainOnBatchAsync() {
        return CompletableFuture.supplyAsync(() -> {
            if (learningEnabled) {
                trainBatch();
            }
            return new MLTrainingResult(batchSize, experienceBuffer.getSize(), true);
        });
    }

    public CompletableFuture<GAOptimizer.GAEvolutionResult> evolveGAAsync() {
        return gaOptimizer.evolveGenerationAsync();
    }

    public MLStatistics getStatistics() {
        // Keep compatibility field updated
        statistics.experienceBufferSize = experienceBuffer.getSize();
        return statistics;
    }

    public void setLearningEnabled(boolean enabled) {
        this.learningEnabled = enabled;
    }

    public void reset() {
        statistics.totalExperiencesProcessed = 0;
        statistics.totalTrainingBatches = 0;
        statistics.averageReward = 0.0;
        statistics.qStats.qTable.clear();
        statistics.qStats.updateCount = 0;
        rewardRunningTotal = 0.0;
    }

    // ===================== Data classes =====================

    public static class MLModels {
        public long exportTime;
    }

    public static class MLStatistics {
        public long totalExperiencesProcessed;
        public long totalTrainingBatches;
        public double averageReward;
        // Compatibility field used by AICommands
        public long experienceBufferSize;
        public final QStats qStats = new QStats();
    }

    public static class QStats {
        public final Map<Long, double[]> qTable = new HashMap<>();
        public long updateCount;
    }

    public static class MLTrainingResult {
        public final int batchSize;
        public final long bufferSize;
        public final boolean success;

        public MLTrainingResult(int batchSize, long bufferSize, boolean success) {
            this.batchSize = batchSize;
            this.bufferSize = bufferSize;
            this.success = success;
        }
    }
}
