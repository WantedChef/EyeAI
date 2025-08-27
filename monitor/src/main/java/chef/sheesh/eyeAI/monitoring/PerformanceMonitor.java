package chef.sheesh.eyeAI.monitoring;

import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Performance Monitor - Collects system and AI performance metrics
 */
@Slf4j
public class PerformanceMonitor {

    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private final OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();
    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    // Performance tracking
    private final AtomicLong lastCpuTime = new AtomicLong(0);
    private final AtomicLong lastMeasurementTime = new AtomicLong(System.nanoTime());
    private volatile PerformanceMetrics currentMetrics;

    /**
     * Collect current performance metrics
     */
    public PerformanceMetrics collectMetrics() {
        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setTimestamp(System.currentTimeMillis());

        try {
            // Memory metrics
            long usedMemory = memoryMXBean.getHeapMemoryUsage().getUsed();
            long maxMemory = memoryMXBean.getHeapMemoryUsage().getMax();
            metrics.setMemoryUsageMB(usedMemory / (1024 * 1024));

            // CPU metrics
            double cpuUsage = calculateCpuUsage();
            metrics.setCpuUsagePercent(cpuUsage);

            // System load
            double systemLoad = osMXBean.getSystemLoadAverage();
            if (systemLoad >= 0) {
                metrics.setSystemLoadAverage(systemLoad);
            }

            // Thread count
            int threadCount = threadMXBean.getThreadCount();

            // Training performance (placeholders - would be integrated with actual training)
            metrics.setTrainingBatchesPerSecond(calculateTrainingBatchesPerSecond());
            metrics.setAverageTrainingTime(calculateAverageTrainingTime());
            metrics.setExperiencesPerSecond(calculateExperiencesPerSecond());

            // Disk I/O (placeholders)
            metrics.setDiskReadBytesPerSecond(calculateDiskReadBytesPerSecond());
            metrics.setDiskWriteBytesPerSecond(calculateDiskWriteBytesPerSecond());

        } catch (Exception e) {
            log.error("Error collecting performance metrics", e);
        }

        currentMetrics = metrics;
        return metrics;
    }

    /**
     * Calculate CPU usage percentage
     */
    private double calculateCpuUsage() {
        try {
            long currentTime = System.nanoTime();
            long currentCpuTime = getProcessCpuTime();

            if (lastCpuTime.get() == 0) {
                lastCpuTime.set(currentCpuTime);
                lastMeasurementTime.set(currentTime);
                return 0.0;
            }

            long cpuTimeDiff = currentCpuTime - lastCpuTime.get();
            long timeDiff = currentTime - lastMeasurementTime.get();

            if (timeDiff == 0) {
                return 0.0;
            }

            int availableProcessors = Runtime.getRuntime().availableProcessors();
            double cpuUsage = (cpuTimeDiff / 1_000_000.0) / timeDiff / availableProcessors * 100.0;

            lastCpuTime.set(currentCpuTime);
            lastMeasurementTime.set(currentTime);

            return Math.max(0.0, Math.min(100.0, cpuUsage));
        } catch (Exception e) {
            log.error("Error calculating CPU usage", e);
            return 0.0;
        }
    }

    /**
     * Get process CPU time (platform dependent)
     */
    private long getProcessCpuTime() {
        try {
            // Try to use com.sun.management.OperatingSystemMXBean for more accurate readings
            if (osMXBean instanceof com.sun.management.OperatingSystemMXBean sunOsMXBean) {
                return sunOsMXBean.getProcessCpuTime();
            }
            // Fallback to system time (less accurate)
            return System.nanoTime();
        } catch (Exception e) {
            return System.nanoTime();
        }
    }

    /**
     * Calculate training batches per second (placeholder)
     */
    private double calculateTrainingBatchesPerSecond() {
        // Placeholder - would track actual training batch processing
        return 15.7; // Dummy value
    }

    /**
     * Calculate average training time (placeholder)
     */
    private double calculateAverageTrainingTime() {
        // Placeholder - would track actual training times
        return 45.2; // Dummy value in milliseconds
    }

    /**
     * Calculate experiences per second (placeholder)
     */
    private double calculateExperiencesPerSecond() {
        // Placeholder - would track actual experience processing
        return 1250.5; // Dummy value
    }

    /**
     * Calculate disk read bytes per second (placeholder)
     */
    private double calculateDiskReadBytesPerSecond() {
        // Placeholder - would use FileStore or similar for actual disk I/O
        return 1024.0 * 50; // 50 KB/s dummy value
    }

    /**
     * Calculate disk write bytes per second (placeholder)
     */
    private double calculateDiskWriteBytesPerSecond() {
        // Placeholder - would use FileStore or similar for actual disk I/O
        return 1024.0 * 75; // 75 KB/s dummy value
    }

    /**
     * Get JVM memory information
     */
    public JVMMemoryInfo getJVMMemoryInfo() {
        return JVMMemoryInfo.builder()
                .heapUsed(memoryMXBean.getHeapMemoryUsage().getUsed())
                .heapCommitted(memoryMXBean.getHeapMemoryUsage().getCommitted())
                .heapMax(memoryMXBean.getHeapMemoryUsage().getMax())
                .nonHeapUsed(memoryMXBean.getNonHeapMemoryUsage().getUsed())
                .nonHeapCommitted(memoryMXBean.getNonHeapMemoryUsage().getCommitted())
                .nonHeapMax(memoryMXBean.getNonHeapMemoryUsage().getMax())
                .build();
    }

    /**
     * Get system information
     */
    public SystemInfo getSystemInfo() {
        return SystemInfo.builder()
                .availableProcessors(Runtime.getRuntime().availableProcessors())
                .totalMemory(Runtime.getRuntime().totalMemory())
                .freeMemory(Runtime.getRuntime().freeMemory())
                .maxMemory(Runtime.getRuntime().maxMemory())
                .systemLoadAverage(osMXBean.getSystemLoadAverage())
                .threadCount(threadMXBean.getThreadCount())
                .peakThreadCount(threadMXBean.getPeakThreadCount())
                .build();
    }

    /**
     * Get current performance metrics
     */
    public PerformanceMetrics getCurrentMetrics() {
        return currentMetrics;
    }

    /**
     * Reset performance counters
     */
    public void resetCounters() {
        lastCpuTime.set(0);
        lastMeasurementTime.set(System.nanoTime());
        log.info("Performance counters reset");
    }

    // Data classes
    @Data
    @Builder
    public static class JVMMemoryInfo {
        private long heapUsed;
        private long heapCommitted;
        private long heapMax;
        private long nonHeapUsed;
        private long nonHeapCommitted;
        private long nonHeapMax;
    }

    @Data
    @Builder
    public static class SystemInfo {
        private int availableProcessors;
        private long totalMemory;
        private long freeMemory;
        private long maxMemory;
        private double systemLoadAverage;
        private int threadCount;
        private int peakThreadCount;
    }
}
