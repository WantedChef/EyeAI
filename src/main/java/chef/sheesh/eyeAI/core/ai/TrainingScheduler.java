package chef.sheesh.eyeAI.core.ai;

import chef.sheesh.eyeAI.core.ml.MLCore;
import chef.sheesh.eyeAI.core.sim.FakePlayerEngine;
import chef.sheesh.eyeAI.infra.config.ConfigurationManager;
import chef.sheesh.eyeAI.infra.diagnostic.Diagnostic;
import org.bukkit.Bukkit;

public final class TrainingScheduler {
    private final MLCore ml;
    private final FakePlayerEngine sim;
    private final ConfigurationManager cfg;
    private int taskId = -1;
    private boolean running = false;

    public TrainingScheduler(MLCore ml, FakePlayerEngine sim, ConfigurationManager cfg) {
        this.ml = ml; this.sim = sim; this.cfg = cfg;
    }

    public void start() {
        if (running) {
            return;
        }
        long period = 1L; // every tick
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                Bukkit.getPluginManager().getPlugin("ChefAI"),
                () -> {
                    if (Diagnostic.getTps() < cfg.getDouble("training.safety.minTPS", 18.0)) {
                        return;
                    }
                    // Run training cycle
                    sim.tick();
                    ml.trainIncremental(cfg.getInt("training.batchSize", 128));
                },
                1L, period);
        running = true;
    }

    public void stop() {
        if (!running) {
            return;
        }
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        taskId = -1;
        running = false;
    }

    public boolean isRunning() { return running; }
}
