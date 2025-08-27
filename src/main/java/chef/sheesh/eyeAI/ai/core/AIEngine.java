package chef.sheesh.eyeAI.ai.core;

import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerManager;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayerManager;
import chef.sheesh.eyeAI.ai.movement.IMovementEngine;
import chef.sheesh.eyeAI.ai.movement.NavGraph;
import chef.sheesh.eyeAI.infra.config.ConfigurationManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Central AI Engine that manages the entire AI system.
 * Handles initialization, lifecycle management, and coordination between all AI components.
 */
public class AIEngine implements IAIEngine {

    private final JavaPlugin plugin;
    private final FakePlayerManager fakePlayerManager;
    private final SchedulerService scheduler;
    private final ConfigurationManager config;
    private final IMovementEngine movementEngine;
    private final NavGraph navGraph;
    private boolean enabled = false;

    public AIEngine(JavaPlugin plugin, ConfigurationManager config, IMovementEngine movementEngine, NavGraph navGraph) {
        this.plugin = plugin;
        this.config = config;
        this.scheduler = new SchedulerService(plugin);
        this.navGraph = navGraph;
        this.movementEngine = movementEngine;
        this.fakePlayerManager = new FakePlayerManager(plugin, scheduler);
    }

    /**
     * Enable the AI system - called during plugin enable
     */
    public void enable() {
        if (enabled) {
            return;
        }

        scheduler.start(); // starts periodic AI ticks
        fakePlayerManager.loadFromPersistence();
        enabled = true;

        // Register AI tick event listener
        plugin.getServer().getPluginManager().registerEvents(
            new AITickListener(fakePlayerManager), plugin
        );
    }

    /**
     * Disable the AI system - called during plugin disable
     */
    public void disable() {
        if (!enabled) {
            return;
        }

        fakePlayerManager.despawnAll();
        scheduler.stop();
        enabled = false;
    }

    /**
     * Get the fake player manager
     */
    @Override
    public IFakePlayerManager getFakePlayerManager() {
        return fakePlayerManager;
    }

    /**
     * Get the movement engine
     */
    @Override
    public IMovementEngine getMovementEngine() {
        return movementEngine;
    }

    /**
     * Get the navigation graph
     */
    @Override
    public NavGraph getNavGraph() {
        return navGraph;
    }

    /**
     * Check if the AI system is enabled
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get the plugin instance
     */
    @Override
    public org.bukkit.plugin.java.JavaPlugin getPlugin() {
        return plugin;
    }

    /**
     * Get the scheduler service
     */
    @Override
    public SchedulerService getScheduler() {
        return scheduler;
    }

    // Additional getters for internal use
    public FakePlayerManager getFakePlayerManagerInternal() {
        return fakePlayerManager;
    }

    public ConfigurationManager getConfig() {
        return config;
    }
}
