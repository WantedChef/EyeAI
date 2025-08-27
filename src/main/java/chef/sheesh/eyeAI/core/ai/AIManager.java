package chef.sheesh.eyeAI.core.ai;

import chef.sheesh.eyeAI.core.ml.MLCore;
import chef.sheesh.eyeAI.core.sim.FakePlayerEngine;
import chef.sheesh.eyeAI.infra.config.ConfigurationManager;
import chef.sheesh.eyeAI.infra.events.EventBus;
import chef.sheesh.eyeAI.infra.packets.PacketBridge;
import org.bukkit.plugin.java.JavaPlugin;

public final class AIManager {
    private final EventBus bus;
    private final ConfigurationManager config;
    private final PacketBridge packets;
    private final JavaPlugin plugin;
    private FakePlayerEngine simEngine;
    private MLCore mlCore;
    private TrainingScheduler scheduler;

    public AIManager(EventBus bus, ConfigurationManager config, PacketBridge packets, JavaPlugin plugin) {
        this.bus = bus;
        this.config = config;
        this.packets = packets;
        this.plugin = plugin;
        // Enable ML components - they are complete and ready for training
        this.mlCore = new MLCore(bus, config, plugin);
        this.simEngine = new FakePlayerEngine(bus, mlCore, packets);
        this.scheduler = new TrainingScheduler(mlCore, simEngine, config);
    }

    public void enable() {
        // Initialize ML components and start training automatically
        if (mlCore != null) {
            mlCore.init();
        }

        // Only start training automatically if autoStart is enabled
        if (config.getBoolean("training.autoStart", true)) {
            startTraining();
        }

        getLogger().info("AI Manager enabled" + (config.getBoolean("training.autoStart", true) ? " with auto training" : " (training disabled by default)"));
    }

    /**
     * Start ML training with fake players
     */
    public void startTraining() {
        if (simEngine != null && !simEngine.isTrainingActive()) {
            int fakePlayerCount = config.getInt("training.fakePlayers", 50);
            simEngine.start(fakePlayerCount);
            getLogger().info("Started training with " + fakePlayerCount + " fake players");
        }

        if (scheduler != null && config.getBoolean("training.enabled", true)) {
            scheduler.start();
            getLogger().info("Training scheduler started");
        }
    }

    /**
     * Stop ML training
     */
    public void stopTraining() {
        if (scheduler != null) {
            scheduler.stop();
            getLogger().info("Training scheduler stopped");
        }

        if (simEngine != null && simEngine.isTrainingActive()) {
            simEngine.stop();
            getLogger().info("Fake player training stopped");
        }
    }

    /**
     * Check if training is currently active
     */
    public boolean isTrainingActive() {
        return simEngine != null && simEngine.isTrainingActive();
    }

    /**
     * Get current training statistics
     */
    public String getTrainingStatus() {
        if (simEngine == null) {
            return "Training engine not available";
        }

        if (!simEngine.isTrainingActive()) {
            return "Training not active (use /ai start to begin training)";
        }

        int fakePlayerCount = simEngine.getCount();
        return "Training active with " + fakePlayerCount + " fake players";
    }

    public void disable() {
        stopTraining(); // Ensure training is stopped on disable

        if (mlCore != null) {
            mlCore.shutdown();
        }
        getLogger().info("AI Manager disabled");
    }

    private java.util.logging.Logger getLogger() {
        return java.util.logging.Logger.getLogger("AI Manager");
    }

    public MLCore ml() { return mlCore; }
    public TrainingScheduler scheduler() { return scheduler; }
    public FakePlayerEngine sim() { return simEngine; }

    // Component availability getters
    public boolean hasMLCore() { return mlCore != null; }
    public boolean hasSimEngine() { return simEngine != null; }
    public boolean hasScheduler() { return scheduler != null; }
}

