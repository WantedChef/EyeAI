package chef.sheesh.eyeAI.infra.diagnostic;

import java.util.logging.Logger;
import org.bukkit.Bukkit;

public final class Diagnostic {
    private static Logger logger;
    private static volatile double lastTps = 20.0;

    public static void bootstrapLogger(Logger pluginLogger) {
        logger = pluginLogger;
    }

    public static double getTps() {
        try {
            // Paper exposes Server#getTPS(): double[] {1m, 5m, 15m}
            double[] tps = Bukkit.getServer().getTPS();
            if (tps != null && tps.length > 0) {
                lastTps = tps[0];
            }
        } catch (Throwable ignored) {
            // Keep lastTps
        }
        return lastTps;
    }

    public static void flush() {
        // Flush diagnostic data
    }
}
