package chef.sheesh.eyeAI.infra.data;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

/**
 * Asynchronous write queue for database operations
 * Provides background processing with error handling and monitoring
 */
public final class AsyncWriteQueue implements AutoCloseable {

    private final JavaPlugin plugin;
    private final ConcurrentLinkedQueue<WriteTask> queue = new ConcurrentLinkedQueue<>();
    private final ExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicLong processedTasks = new AtomicLong(0);
    private final AtomicLong failedTasks = new AtomicLong(0);
    private final int maxRetries;

    public AsyncWriteQueue(JavaPlugin plugin, int threadPoolSize, int maxRetries) {
        this.plugin = plugin;
        this.maxRetries = maxRetries;
        this.executor = Executors.newFixedThreadPool(threadPoolSize, r -> {
            Thread t = new Thread(r, "AsyncWriteQueue-" + System.currentTimeMillis());
            t.setDaemon(true);
            return t;
        });

        // Start background processing
        startBackgroundProcessing();
    }

    /**
     * Enqueue a write task for asynchronous execution
     */
    public void enqueue(Runnable task) {
        enqueue(task, 0);
    }

    /**
     * Enqueue a write task with priority (lower number = higher priority)
     */
    public void enqueue(Runnable task, int priority) {
        if (!running.get()) {
            plugin.getLogger().warning("Write queue is shutting down, task rejected");
            return;
        }

        WriteTask writeTask = new WriteTask(task, priority, System.currentTimeMillis());
        queue.offer(writeTask);

        if (queue.size() % 100 == 0) { // Log every 100 tasks
            plugin.getLogger().fine("Write queue size: " + queue.size());
        }
    }

    /**
     * Enqueue a write task with custom error handling
     */
    public void enqueue(Runnable task, Runnable errorHandler) {
        enqueue(task, errorHandler, 0);
    }

    /**
     * Enqueue a write task with error handling and priority
     */
    public void enqueue(Runnable task, Runnable errorHandler, int priority) {
        if (!running.get()) {
            plugin.getLogger().warning("Write queue is shutting down, task rejected");
            return;
        }

        WriteTask writeTask = new WriteTask(task, errorHandler, priority, System.currentTimeMillis());
        queue.offer(writeTask);
    }

    /**
     * Start background processing thread
     */
    private void startBackgroundProcessing() {
        executor.submit(() -> {
            plugin.getLogger().info("Async write queue processor started");

            while (running.get() || !queue.isEmpty()) {
                try {
                    WriteTask task = queue.poll();
                    if (task != null) {
                        processTask(task);
                    } else {
                        // No tasks available, sleep briefly
                        Thread.sleep(10);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Error in write queue processor", e);
                }
            }

            plugin.getLogger().info("Async write queue processor stopped");
        });
    }

    /**
     * Process a single write task with retry logic
     */
    private void processTask(WriteTask task) {
        int attempts = 0;
        while (attempts <= maxRetries) {
            try {
                task.runnable.run();
                processedTasks.incrementAndGet();
                return;
            } catch (Exception e) {
                attempts++;
                failedTasks.incrementAndGet();

                if (attempts <= maxRetries) {
                    plugin.getLogger().warning("Write task failed (attempt " + attempts + "/" + (maxRetries + 1) + "): " + e.getMessage());
                    try {
                        Thread.sleep(100 * attempts); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                } else {
                    plugin.getLogger().log(Level.SEVERE, "Write task failed permanently after " + attempts + " attempts", e);
                    if (task.errorHandler != null) {
                        try {
                            task.errorHandler.run();
                        } catch (Exception eh) {
                            plugin.getLogger().log(Level.SEVERE, "Error handler also failed", eh);
                        }
                    }
                }
            }
        }
    }

    /**
     * Get current queue size
     */
    public int getQueueSize() {
        return queue.size();
    }

    /**
     * Get statistics
     */
    public QueueStatistics getStatistics() {
        return new QueueStatistics(
            queue.size(),
            processedTasks.get(),
            failedTasks.get(),
            running.get()
        );
    }

    /**
     * Gracefully shutdown the queue
     */
    @Override
    public void close() {
        plugin.getLogger().info("Shutting down async write queue...");

        running.set(false);

        // Wait for existing tasks to complete
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                plugin.getLogger().warning("Write queue did not terminate gracefully, forcing shutdown");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // Process remaining tasks synchronously if needed
        if (!queue.isEmpty()) {
            plugin.getLogger().info("Processing remaining " + queue.size() + " tasks synchronously");
            WriteTask task;
            while ((task = queue.poll()) != null) {
                try {
                    task.runnable.run();
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to process remaining task", e);
                }
            }
        }

        plugin.getLogger().info("Async write queue shutdown complete");
    }

    /**
     * Write task wrapper with metadata
     */
    private static class WriteTask {
        final Runnable runnable;
        final Runnable errorHandler;
        @SuppressWarnings("unused") // Stored for future priority-based execution
        final int priority;
        @SuppressWarnings("unused") // Stored for future age-based task management
        final long createdAt;

        WriteTask(Runnable runnable, int priority, long createdAt) {
            this.runnable = runnable;
            this.errorHandler = null;
            this.priority = priority;
            this.createdAt = createdAt;
        }

        WriteTask(Runnable runnable, Runnable errorHandler, int priority, long createdAt) {
            this.runnable = runnable;
            this.errorHandler = errorHandler;
            this.priority = priority;
            this.createdAt = createdAt;
        }
    }

    /**
     * Queue statistics record
     */
    public record QueueStatistics(
        int queueSize,
        long processedTasks,
        long failedTasks,
        boolean running
    ) {}
}
