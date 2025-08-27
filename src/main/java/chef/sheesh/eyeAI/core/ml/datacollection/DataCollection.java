package chef.sheesh.eyeAI.core.ml.datacollection;

import chef.sheesh.eyeAI.core.ml.models.Experience;
import chef.sheesh.eyeAI.core.ml.models.IState;
import chef.sheesh.eyeAI.core.ml.models.GameState;
import chef.sheesh.eyeAI.core.ml.models.Action;
import chef.sheesh.eyeAI.core.ml.features.FeatureEngineering;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * DataCollection provides systematic training data gathering and preprocessing
 * for the EyeAI ML system. Collects experiences, states, and other training data.
 */
public class DataCollection {
    private static final Logger log = LoggerFactory.getLogger(DataCollection.class);

    private final Queue<Experience> experienceBuffer;
    private final Queue<IState> stateBuffer;
    private final Map<String, DataCollector> collectors;
    private final DataPreprocessor preprocessor;
    private final int bufferCapacity;
    private final AtomicInteger collectedExperiences;
    private boolean isCollecting;

    /**
     * Constructor for DataCollection.
     * @param bufferCapacity Maximum capacity for data buffers
     */
    public DataCollection(int bufferCapacity) {
        this.bufferCapacity = bufferCapacity;
        this.experienceBuffer = new ConcurrentLinkedQueue<>();
        this.stateBuffer = new ConcurrentLinkedQueue<>();
        this.collectors = new HashMap<>();
        this.preprocessor = new DataPreprocessor();
        this.collectedExperiences = new AtomicInteger(0);
        this.isCollecting = false;

        // Register default collectors
        registerDefaultCollectors();
    }

    /**
     * Start data collection.
     */
    public void startCollection() {
        log.info("Starting data collection...");
        isCollecting = true;
        collectedExperiences.set(0);
    }

    /**
     * Stop data collection.
     */
    public void stopCollection() {
        log.info("Stopping data collection. Collected {} experiences", collectedExperiences.get());
        isCollecting = false;
    }

    /**
     * Check if data collection is active.
     * @return true if collecting data
     */
    public boolean isCollecting() {
        return isCollecting;
    }

    /**
     * Collect an experience from the environment.
     * @param state Current state
     * @param action Action taken
     * @param reward Reward received
     * @param nextState Next state after action
     * @param done Whether episode is done
     */
    public void collectExperience(IState state, Action action, double reward,
                                IState nextState, boolean done) {
        if (!isCollecting) {
            return;
        }

        // Experience now accepts IState directly - no casting needed
        Experience experience = new Experience(state, action, reward, nextState, done);

        // Add to buffer with size limit
        if (experienceBuffer.size() >= bufferCapacity) {
            experienceBuffer.poll(); // Remove oldest
        }
        experienceBuffer.add(experience);

        collectedExperiences.incrementAndGet();

        // Trigger collectors
        for (DataCollector collector : collectors.values()) {
            collector.onExperienceCollected(experience);
        }
    }

    /**
     * Collect a state observation.
     * @param state The state to collect
     */
    public void collectState(IState state) {
        if (!isCollecting) {
            return;
        }

        if (stateBuffer.size() >= bufferCapacity) {
            stateBuffer.poll();
        }
        stateBuffer.add(state);

        for (DataCollector collector : collectors.values()) {
            collector.onStateCollected(state);
        }
    }

    /**
     * Collect data from a Minecraft world context.
     * @param playerLocation Player's current location
     * @param nearbyEntities List of nearby entities
     * @param world Current world
     */
    public void collectWorldData(Location playerLocation, List<Entity> nearbyEntities, World world) {
        if (!isCollecting) {
            return;
        }

        WorldData worldData = new WorldData(playerLocation, nearbyEntities, world,
                                          System.currentTimeMillis());

        for (DataCollector collector : collectors.values()) {
            collector.onWorldDataCollected(worldData);
        }
    }

    /**
     * Get a batch of experiences for training.
     * @param batchSize Size of batch to return
     * @return List of experiences
     */
    public List<Experience> getExperienceBatch(int batchSize) {
        return experienceBuffer.stream()
                .limit(batchSize)
                .collect(Collectors.toList());
    }

    /**
     * Get a batch of states for training.
     * @param batchSize Size of batch to return
     * @return List of states
     */
    public List<IState> getStateBatch(int batchSize) {
        return stateBuffer.stream()
                .limit(batchSize)
                .collect(Collectors.toList());
    }

    /**
     * Get preprocessed training data.
     * @param dataType Type of data to retrieve ("experiences", "states", "features")
     * @param batchSize Size of batch
     * @return Preprocessed training data
     */
    public TrainingData getPreprocessedData(String dataType, int batchSize) {
        return preprocessor.preprocess(dataType, this, batchSize);
    }

    /**
     * Register a custom data collector.
     * @param name Name of the collector
     * @param collector The collector implementation
     */
    public void registerCollector(String name, DataCollector collector) {
        collectors.put(name, collector);
        log.info("Registered data collector: {}", name);
    }

    /**
     * Unregister a data collector.
     * @param name Name of the collector to remove
     */
    public void unregisterCollector(String name) {
        collectors.remove(name);
        log.info("Unregistered data collector: {}", name);
    }

    /**
     * Get collection statistics.
     * @return DataCollectionStats object with statistics
     */
    public DataCollectionStats getStats() {
        return new DataCollectionStats(
            collectedExperiences.get(),
            experienceBuffer.size(),
            stateBuffer.size(),
            collectors.size(),
            isCollecting
        );
    }

    /**
     * Clear all collected data.
     */
    public void clearBuffers() {
        experienceBuffer.clear();
        stateBuffer.clear();
        collectedExperiences.set(0);
        log.info("Cleared all data buffers");
    }

    /**
     * Export collected data to files.
     * @param basePath Base path for exported files
     */
    public void exportData(String basePath) {
        // TODO: Implement data export functionality
        log.info("Exporting data to: {}", basePath);
    }

    /**
     * Register default data collectors.
     */
    private void registerDefaultCollectors() {
        // Experience statistics collector
        registerCollector("experience_stats", new ExperienceStatsCollector());

        // State diversity collector
        registerCollector("state_diversity", new StateDiversityCollector());

        // Reward analysis collector
        registerCollector("reward_analysis", new RewardAnalysisCollector());
    }

    // Inner classes

    /**
     * Interface for custom data collectors.
     */
    public interface DataCollector {
        void onExperienceCollected(Experience experience);
        void onStateCollected(IState state);
        void onWorldDataCollected(WorldData worldData);
        void reset();
        Map<String, Object> getStats();
    }

    /**
     * Data preprocessor for cleaning and transforming training data.
     */
    public static class DataPreprocessor {
        public TrainingData preprocess(String dataType, DataCollection dataCollection, int batchSize) {
            switch (dataType.toLowerCase()) {
                case "experiences":
                    return preprocessExperiences(dataCollection.getExperienceBatch(batchSize));
                case "states":
                    return preprocessStates(dataCollection.getStateBatch(batchSize));
                case "features":
                    return preprocessFeatures(dataCollection.getStateBatch(batchSize));
                default:
                    throw new IllegalArgumentException("Unknown data type: " + dataType);
            }
        }

        private TrainingData preprocessExperiences(List<Experience> experiences) {
            if (experiences.isEmpty()) {
                return new TrainingData(new double[0][], new double[0], new double[0][]);
            }

            int numExperiences = experiences.size();
            int stateSize = experiences.get(0).state().getStateSize();

            double[][] states = new double[numExperiences][];
            double[] rewards = new double[numExperiences];
            double[][] nextStates = new double[numExperiences][];

            for (int i = 0; i < numExperiences; i++) {
                Experience exp = experiences.get(i);
                states[i] = exp.state().flatten();
                rewards[i] = exp.reward();
                nextStates[i] = exp.nextState().flatten();
            }

            return new TrainingData(states, rewards, nextStates);
        }

        private TrainingData preprocessStates(List<IState> states) {
            if (states.isEmpty()) {
                return new TrainingData(new double[0][], new double[0], new double[0][]);
            }

            double[][] stateVectors = new double[states.size()][];
            for (int i = 0; i < states.size(); i++) {
                stateVectors[i] = states.get(i).flatten();
            }

            return new TrainingData(stateVectors, new double[states.size()], new double[0][]);
        }

        private TrainingData preprocessFeatures(List<IState> states) {
            if (states.isEmpty()) {
                return new TrainingData(new double[0][], new double[0], new double[0][]);
            }

            // Apply feature engineering transformations
            double[][] features = new double[states.size()][];
            for (int i = 0; i < states.size(); i++) {
                double[] originalFeatures = states.get(i).flatten();
                features[i] = FeatureEngineering.normalizeZScore(originalFeatures);
            }

            return new TrainingData(features, new double[states.size()], new double[0][]);
        }
    }

    /**
     * Container for training data.
     */
    public static class TrainingData {
        public final double[][] states;
        public final double[] rewards;
        public final double[][] nextStates;

        public TrainingData(double[][] states, double[] rewards, double[][] nextStates) {
            this.states = states;
            this.rewards = rewards;
            this.nextStates = nextStates;
        }
    }

    /**
     * Statistics about data collection.
     */
    public static class DataCollectionStats {
        public final int totalExperiences;
        public final int bufferSize;
        public final int stateBufferSize;
        public final int activeCollectors;
        public final boolean isCollecting;

        public DataCollectionStats(int totalExperiences, int bufferSize, int stateBufferSize,
                                 int activeCollectors, boolean isCollecting) {
            this.totalExperiences = totalExperiences;
            this.bufferSize = bufferSize;
            this.stateBufferSize = stateBufferSize;
            this.activeCollectors = activeCollectors;
            this.isCollecting = isCollecting;
        }

        @Override
        public String toString() {
            return String.format("DataCollectionStats{experiences=%d, buffers=%d/%d, collectors=%d, collecting=%s}",
                    totalExperiences, bufferSize, stateBufferSize, activeCollectors, isCollecting);
        }
    }

    /**
     * World data container for Minecraft-specific data.
     */
    public static class WorldData {
        public final Location playerLocation;
        public final List<Entity> nearbyEntities;
        public final World world;
        public final long timestamp;

        public WorldData(Location playerLocation, List<Entity> nearbyEntities,
                        World world, long timestamp) {
            this.playerLocation = playerLocation;
            this.nearbyEntities = nearbyEntities;
            this.world = world;
            this.timestamp = timestamp;
        }
    }

    // Default collector implementations

    public static class ExperienceStatsCollector implements DataCollector {
        private int experienceCount = 0;
        private double totalReward = 0.0;
        private double maxReward = Double.NEGATIVE_INFINITY;
        private double minReward = Double.POSITIVE_INFINITY;

        @Override
        public void onExperienceCollected(Experience experience) {
            experienceCount++;
            totalReward += experience.reward();
            maxReward = Math.max(maxReward, experience.reward());
            minReward = Math.min(minReward, experience.reward());
        }

        @Override
        public void onStateCollected(IState state) {
            // Not interested in states
        }

        @Override
        public void onWorldDataCollected(WorldData worldData) {
            // Not interested in world data
        }

        @Override
        public void reset() {
            experienceCount = 0;
            totalReward = 0.0;
            maxReward = Double.NEGATIVE_INFINITY;
            minReward = Double.POSITIVE_INFINITY;
        }

        @Override
        public Map<String, Object> getStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("count", experienceCount);
            stats.put("averageReward", experienceCount > 0 ? totalReward / experienceCount : 0.0);
            stats.put("maxReward", maxReward);
            stats.put("minReward", minReward);
            return stats;
        }
    }

    public static class StateDiversityCollector implements DataCollector {
        private final Map<String, Integer> stateHashes = new HashMap<>();

        @Override
        public void onExperienceCollected(Experience experience) {
            // Not interested in experiences
        }

        @Override
        public void onStateCollected(IState state) {
            String stateHash = java.util.Arrays.toString(state.flatten());
            stateHashes.put(stateHash, stateHashes.getOrDefault(stateHash, 0) + 1);
        }

        @Override
        public void onWorldDataCollected(WorldData worldData) {
            // Not interested in world data
        }

        @Override
        public void reset() {
            stateHashes.clear();
        }

        @Override
        public Map<String, Object> getStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("uniqueStates", stateHashes.size());
            stats.put("totalStates", stateHashes.values().stream().mapToInt(Integer::intValue).sum());
            return stats;
        }
    }

    public static class RewardAnalysisCollector implements DataCollector {
        private final List<Double> rewards = new ArrayList<>();

        @Override
        public void onExperienceCollected(Experience experience) {
            rewards.add(experience.reward());
        }

        @Override
        public void onStateCollected(IState state) {
            // Not interested in states
        }

        @Override
        public void onWorldDataCollected(WorldData worldData) {
            // Not interested in world data
        }

        @Override
        public void reset() {
            rewards.clear();
        }

        @Override
        public Map<String, Object> getStats() {
            Map<String, Object> stats = new HashMap<>();
            if (rewards.isEmpty()) {
                stats.put("analysis", "No rewards collected");
                return stats;
            }

            double mean = rewards.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double variance = rewards.stream().mapToDouble(r -> Math.pow(r - mean, 2)).average().orElse(0.0);

            stats.put("mean", mean);
            stats.put("variance", variance);
            stats.put("stdDev", Math.sqrt(variance));
            stats.put("count", rewards.size());

            return stats;
        }
    }
}
