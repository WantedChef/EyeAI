package chef.sheesh.eyeAI.ai.core;

import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayerManager;
import chef.sheesh.eyeAI.ai.movement.IMovementEngine;
import chef.sheesh.eyeAI.ai.movement.NavGraph;

/**
 * Interface for AI Engine to break circular dependencies
 */
public interface IAIEngine {

    /**
     * Enable the AI system
     */
    void enable();

    /**
     * Disable the AI system
     */
    void disable();

    /**
     * Get the fake player manager
     */
    IFakePlayerManager getFakePlayerManager();

    /**
     * Get the movement engine
     */
    IMovementEngine getMovementEngine();

    /**
     * Get the navigation graph
     */
    NavGraph getNavGraph();

    /**
     * Check if the AI system is enabled
     */
    boolean isEnabled();

    /**
     * Get the plugin instance
     */
    org.bukkit.plugin.java.JavaPlugin getPlugin();

    /**
     * Get the scheduler service
     */
    SchedulerService getScheduler();
}
