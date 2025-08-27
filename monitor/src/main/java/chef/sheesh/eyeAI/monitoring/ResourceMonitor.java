package chef.sheesh.eyeAI.monitoring;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Resource Monitor - Monitors system resources (CPU, Memory, Disk, Network)
 */
@Slf4j
public class ResourceMonitor {

    private final OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();
    private volatile SystemResources currentResources;

    /**
     * Collect current system resources
     */
    public SystemResources collectResources() {
        SystemResources resources = new SystemResources();
        resources.setTimestamp(System.currentTimeMillis());

        try {
            // Memory information
            Runtime runtime = Runtime.getRuntime();
            resources.setMemoryUsageMB((runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024));
            resources.setMaxMemoryMB(runtime.maxMemory() / (1024 * 1024));

            // CPU usage
            double cpuUsage = calculateCpuUsage();
            resources.setCpuUsagePercent(cpuUsage);

            // System load average
            double systemLoad = osMXBean.getSystemLoadAverage();
            if (systemLoad >= 0) {
                resources.setSystemLoadAverage(systemLoad);
            }

            // Thread count
            resources.setThreadCount(Thread.activeCount());

            // Disk usage
            DiskUsage diskUsage = getDiskUsage();
            resources.setDiskUsedBytes(diskUsage.used());
            resources.setDiskTotalBytes(diskUsage.total());

            // Uptime
            resources.setUptimeSeconds(ManagementFactory.getRuntimeMXBean().getUptime() / 1000);

        } catch (Exception e) {
            log.error("Error collecting system resources", e);
        }

        currentResources = resources;
        return resources;
    }

    /**
     * Calculate CPU usage percentage
     */
    private double calculateCpuUsage() {
        try {
            // Try to get more accurate CPU usage if available
            if (osMXBean instanceof com.sun.management.OperatingSystemMXBean sunOsMXBean) {
                // Get process CPU time and convert to percentage
                long processCpuTime = sunOsMXBean.getProcessCpuTime();
                long uptime = ManagementFactory.getRuntimeMXBean().getUptime();

                if (uptime > 0) {
                    int availableProcessors = Runtime.getRuntime().availableProcessors();
                    return (processCpuTime / 1_000_000.0) / uptime / availableProcessors * 100.0;
                }
            }

            // Fallback to system load average
            double loadAverage = osMXBean.getSystemLoadAverage();
            if (loadAverage >= 0) {
                return Math.min(100.0, loadAverage * 100.0 / Runtime.getRuntime().availableProcessors());
            }

            return 0.0;
        } catch (Exception e) {
            log.error("Error calculating CPU usage", e);
            return 0.0;
        }
    }

    /**
     * Get disk usage information
     */
    private DiskUsage getDiskUsage() {
        try {
            // Get the current working directory's file system
            File currentDir = new File(".");
            FileStore fileStore = Files.getFileStore(Paths.get(currentDir.getAbsolutePath()));

            long totalSpace = fileStore.getTotalSpace();
            long usableSpace = fileStore.getUsableSpace();
            long usedSpace = totalSpace - usableSpace;

            return new DiskUsage(usedSpace, totalSpace);
        } catch (Exception e) {
            log.error("Error getting disk usage", e);
            return new DiskUsage(0, 0);
        }
    }

    /**
     * Get detailed memory information
     */
    public MemoryInfo getDetailedMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        return MemoryInfo.builder()
                .totalMemory(runtime.totalMemory())
                .freeMemory(runtime.freeMemory())
                .usedMemory(runtime.totalMemory() - runtime.freeMemory())
                .maxMemory(runtime.maxMemory())
                .build();
    }

    /**
     * Get network interface information (placeholder)
     */
    public NetworkInfo getNetworkInfo() {
        // Placeholder - would use NetworkInterface to get actual network stats
        return NetworkInfo.builder()
                .bytesReceived(0L)
                .bytesSent(0L)
                .packetsReceived(0L)
                .packetsSent(0L)
                .build();
    }

    /**
     * Get current system resources
     */
    public SystemResources getCurrentResources() {
        return currentResources;
    }

    /**
     * Check if system resources are within acceptable limits
     */
    public ResourceHealth checkResourceHealth() {
        SystemResources resources = currentResources;
        if (resources == null) {
            return ResourceHealth.UNHEALTHY;
        }

        // Memory usage check (>90% is unhealthy)
        double memoryUsagePercent = (double) resources.getMemoryUsageMB() / resources.getMaxMemoryMB() * 100;
        if (memoryUsagePercent > 90) {
            return ResourceHealth.UNHEALTHY;
        }

        // CPU usage check (>95% is unhealthy)
        if (resources.getCpuUsagePercent() > 95) {
            return ResourceHealth.UNHEALTHY;
        }

        // System load check (> available processors * 2 is unhealthy)
        if (resources.getSystemLoadAverage() > Runtime.getRuntime().availableProcessors() * 2) {
            return ResourceHealth.UNHEALTHY;
        }

        // Disk usage check (>95% is unhealthy)
        if (resources.getDiskTotalBytes() > 0) {
            double diskUsagePercent = (double) resources.getDiskUsedBytes() / resources.getDiskTotalBytes() * 100;
            if (diskUsagePercent > 95) {
                return ResourceHealth.UNHEALTHY;
            }
        }

        return ResourceHealth.HEALTHY;
    }

    /**
     * Get resource usage trends (placeholder)
     */
    public ResourceTrends getResourceTrends() {
        // Placeholder - would track historical data
        return ResourceTrends.builder()
                .memoryTrend("stable")
                .cpuTrend("stable")
                .diskTrend("stable")
                .build();
    }

    // Data classes
    @Data
    @Builder
    public static class MemoryInfo {
        private long totalMemory;
        private long freeMemory;
        private long usedMemory;
        private long maxMemory;
    }

    @Data
    @Builder
    public static class NetworkInfo {
        private long bytesReceived;
        private long bytesSent;
        private long packetsReceived;
        private long packetsSent;
    }

    @Data
    @Builder
    public static class DiskUsage {
        private long used;
        private long total;
    }

    public enum ResourceHealth {
        HEALTHY, WARNING, UNHEALTHY
    }

    @Data
    @Builder
    public static class ResourceTrends {
        private String memoryTrend; // "increasing", "decreasing", "stable"
        private String cpuTrend;
        private String diskTrend;
    }
}
