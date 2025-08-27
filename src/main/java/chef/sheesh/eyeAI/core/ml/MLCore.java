package chef.sheesh.eyeAI.core.ml;

import chef.sheesh.eyeAI.core.sim.SimExperience;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.infra.config.ConfigurationManager;
import chef.sheesh.eyeAI.infra.events.EventBus;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Enhanced ML Core that leverages the new MLManager for unified ML operations.
 * Provides backward compatibility while using modern ML infrastructure.
 */
public final class MLCore {

    private final EventBus bus;
    private final ConfigurationManager cfg;
    private final MLManager mlManager;
    private final MLModelPersistenceManager persistenceManager;

    // Legacy compatibility fields
    private boolean initialized = false;

    public MLCore(EventBus bus, ConfigurationManager cfg, JavaPlugin plugin) {
        this.bus = bus;
        this.cfg = cfg;
        this.mlManager = new MLManager(bus, cfg);
        this.persistenceManager = new MLModelPersistenceManager(plugin.getDataFolder(), bus);
    }

    /**
     * Initialize the ML system
     */
    public void init() {
        mlManager.initializeMLComponents();

        // Try to load existing models
        loadExistingModels();

        initialized = true;
    }

    /**
     * Shutdown the ML system
     */
    public void shutdown() {
        // Export models before shutdown
        MLManager.MLModels models = mlManager.exportModels();
        persistModels(models);
        initialized = false;
    }

    /**
     * Add experience to the ML system
     */
    public void addExperience(SimExperience exp) {
        mlManager.addExperience(exp);
    }

    /**
     * Add experience from fake player with reward calculation
     */
    public void addPlayerExperience(FakePlayer fakePlayer, SimExperience experience) {
        mlManager.addPlayerExperience(fakePlayer, experience);
    }

    /**
     * Predict next location using ML models
     */
    public Location predictNextLocation(Player player) {
        return mlManager.predictNextLocation(player);
    }

    /**
     * Select action using Q-learning
     */
    public int selectAction(long stateHash, int maxActions) {
        return mlManager.selectAction(stateHash, maxActions);
    }

    /**
     * Get best action for state
     */
    public int getBestAction(long stateHash, int maxActions) {
        return mlManager.getBestAction(stateHash, maxActions);
    }

    /**
     * Train incrementally on a batch
     */
    public void trainIncremental(int batchSize) {
        mlManager.setBatchSize(batchSize);
        mlManager.trainOnBatchAsync().whenComplete((result, throwable) -> {
            if (throwable != null) {
                bus.post(new RuntimeException("MLCore training error", throwable));
            }
        });
    }

    /**
     * Run GA evolution
     */
    public CompletableFuture<?> evolveGA() {
        return mlManager.evolveGAAsync();
    }

    /**
     * Get comprehensive ML statistics
     */
    public MLManager.MLStatistics getStatistics() {
        return mlManager.getStatistics();
    }

    /**
     * Enable/disable learning
     */
    public void setLearningEnabled(boolean enabled) {
        mlManager.setLearningEnabled(enabled);
    }

    /**
     * Reset all ML models
     */
    public void reset() {
        mlManager.reset();
    }

    /**
     * Export current models
     */
    public MLManager.MLModels exportModels() {
        return mlManager.exportModels();
    }

    /**
     * Import models from data
     */
    public void importModels(MLManager.MLModels models) {
        mlManager.importModels(models);
    }

    // Legacy compatibility methods

    public void enqueue(SimExperience exp) {
        addExperience(exp);
    }

    public boolean isInitialized() {
        return initialized;
    }

    // Private methods

    private void persistModels(MLManager.MLModels models) {
        try {
            persistenceManager.saveModelsWithBackup(models);
            bus.post(new ModelsPersistedEvent(models.exportTime, true));
        } catch (Exception e) {
            bus.post(new ModelsPersistedEvent(System.currentTimeMillis(), false));
            bus.post(new RuntimeException("Model persistence failed", e));
        }
    }

    /**
     * Load existing models from persistent storage
     */
    private void loadExistingModels() {
        try {
            Optional<MLManager.MLModels> existingModels = persistenceManager.loadLatestModels();
            if (existingModels.isPresent()) {
                mlManager.importModels(existingModels.get());
                bus.post(new ModelsLoadedEvent(existingModels.get().exportTime, true));
            } else {
                bus.post(new ModelsLoadedEvent(System.currentTimeMillis(), false));
            }
        } catch (Exception e) {
            bus.post(new ModelsLoadedEvent(System.currentTimeMillis(), false));
            bus.post(new RuntimeException("Model loading failed", e));
        }
    }

    // Event classes

    public static class ModelsPersistedEvent {
        public final long timestamp;
        public final boolean success;

        public ModelsPersistedEvent(long timestamp, boolean success) {
            this.timestamp = timestamp;
            this.success = success;
        }
    }

    public static class ModelsLoadedEvent {
        public final long timestamp;
        public final boolean success;

        public ModelsLoadedEvent(long timestamp, boolean success) {
            this.timestamp = timestamp;
            this.success = success;
        }
    }
}
