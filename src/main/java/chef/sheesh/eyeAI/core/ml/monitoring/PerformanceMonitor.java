package chef.sheesh.eyeAI.core.ml.monitoring;

/**
 * Minimal PerformanceMonitor stub to satisfy TrainingManager dependencies.
 * Provides no-op monitoring suitable for headless training runs and tests.
 */
public class PerformanceMonitor {

    public PerformanceMonitor() {
        // no-op
    }

    public void startMonitoring() {
        // no-op
    }

    public void stopMonitoring() {
        // no-op
    }

    public void recordStepTime(long nanos) {
        // no-op
    }

    // --- Basic aggregation for TrainingManager ---
    private long totalStepTimeNanos = 0L;
    private long stepCount = 0L;

    public synchronized void recordStepTiming(long nanos) {
        totalStepTimeNanos += Math.max(0, nanos);
        stepCount++;
    }

    /**
     * Average step time in nanoseconds. Returns 1e9 (1 second) as safe default if no data yet.
     */
    public synchronized double getAverageStepTime() {
        if (stepCount <= 0) {
            return 1_000_000_000.0;
        }
        return (double) totalStepTimeNanos / (double) stepCount;
    }

    /**
     * Shutdown hook for compatibility.
     */
    public void shutdown() {
        // no resources to release in stub
    }
}
