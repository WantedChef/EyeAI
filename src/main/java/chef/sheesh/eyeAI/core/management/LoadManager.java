package chef.sheesh.eyeAI.core.management;

import org.bukkit.Bukkit;

/**
 * Manages the load on the server by adjusting the complexity of the AI.
 */
public class LoadManager {

    private static final double HIGH_TPS_THRESHOLD = 18.0;
    private static final double MEDIUM_TPS_THRESHOLD = 15.0;

    /**
     * Gets the current AI complexity level based on the server's TPS.
     *
     * @return The recommended AI complexity level.
     */
    public AIComplexityLevel getAIComplexityLevel() {
        double tps = getTps();

        if (tps >= HIGH_TPS_THRESHOLD) {
            return AIComplexityLevel.HIGH;
        } else if (tps >= MEDIUM_TPS_THRESHOLD) {
            return AIComplexityLevel.MEDIUM;
        } else {
            return AIComplexityLevel.LOW;
        }
    }

    /**
     * Gets the server's current Ticks Per Second (TPS).
     * NOTE: This is a placeholder. A proper implementation would require a more robust way
     * to get the TPS, as Bukkit does not provide a direct API for it.
     * A common method is to use a repeating task to measure the time between ticks.
     *
     * @return The current TPS.
     */
    private double getTps() {
        // This is a simplified placeholder. In a real plugin, you would use a dedicated
        // runnable to calculate the TPS accurately.
        try {
            // Reflection to get recent TPS from Spigot
            Object server = Bukkit.getServer();
            java.lang.reflect.Field tpsField = server.getClass().getField("recentTps");
            double[] recentTps = (double[]) tpsField.get(server);
            return recentTps[0]; // Return the 1-minute average
        } catch (Exception e) {
            // Fallback if reflection fails (e.g., on non-Spigot servers)
            return 20.0;
        }
    }
}
