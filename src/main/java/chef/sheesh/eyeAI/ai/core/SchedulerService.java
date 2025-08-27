package chef.sheesh.eyeAI.ai.core;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

/**
 * Service for managing async and sync task scheduling.
 * Ensures thread safety by providing clear separation between main thread and async operations.
 */
public class SchedulerService {

    private final JavaPlugin plugin;
    private final ExecutorService asyncExecutor;
    private int tickTaskId = -1;

    public SchedulerService(JavaPlugin plugin) {
        this.plugin = plugin;
        // Create thread pool based on available processors
        int threadCount = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
        this.asyncExecutor = Executors.newFixedThreadPool(threadCount);
    }

    /**
     * Run a task on the main thread safely
     * @param runnable The task to execute on main thread
     */
    public void runOnMain(Runnable runnable) {
        if (Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    /**
     * Run a task asynchronously
     * @param runnable The task to execute async
     * @return CompletableFuture for chaining operations
     */
    public CompletableFuture<Void> runAsync(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, asyncExecutor);
    }

    /**
     * Start the scheduler to fire AI tick events
     */
    public void start() {
        if (tickTaskId != -1) {
            return; // Already running
        }

        tickTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            // Fire AI tick event for all listeners
            plugin.getServer().getPluginManager().callEvent(new AITickEvent());
        }, 1L, 1L).getTaskId(); // Start immediately, repeat every tick
    }

    /**
     * Stop the scheduler and cleanup resources
     */
    public void stop() {
        if (tickTaskId != -1) {
            Bukkit.getScheduler().cancelTask(tickTaskId);
            tickTaskId = -1;
        }

        asyncExecutor.shutdownNow();
    }

    /**
     * Check if the scheduler is currently running
     */
    public boolean isRunning() {
        return tickTaskId != -1;
    }
}
