package chef.sheesh.eyeAI.core.sim;

import chef.sheesh.eyeAI.ai.core.SchedulerService;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerManager;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;
import chef.sheesh.eyeAI.core.ml.MLCore;
import chef.sheesh.eyeAI.infra.events.EventBus;
import chef.sheesh.eyeAI.infra.packets.PacketBridge;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class FakePlayerEngine {
    private final EventBus bus;
    private final MLCore ml;
    private final PacketBridge packets;
    private final ConcurrentMap<UUID, FakePlayer> fakePlayers = new ConcurrentHashMap<>();
    private FakePlayerManager fakePlayerManager;
    private MLTrainingMonitor trainingMonitor;

    public FakePlayerEngine(EventBus bus, MLCore ml, PacketBridge packets) {
        this.bus = bus;
        this.ml = ml;
        this.packets = packets;
    }

    public void start(int count) {
        // Initialize fake player manager if not already done
        if (fakePlayerManager == null) {
            // Get plugin instance and create scheduler service
            JavaPlugin plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin("ChefAI");
            SchedulerService scheduler = new SchedulerService(plugin);
            fakePlayerManager = new FakePlayerManager(plugin, scheduler);
        }

        // Spawn fake players for training
        Location spawnLocation = new Location(Bukkit.getWorlds().get(0), 0, 100, 0);

        for (int i = 0; i < count; i++) {
            String name = "AI_Trainer_" + i;
            IFakePlayer fakePlayerInterface = fakePlayerManager.createFakePlayer(spawnLocation, name);

            if (fakePlayerInterface instanceof FakePlayer fakePlayer) {
                fakePlayers.put(fakePlayer.getId(), fakePlayer);
            }
        }

        // Initialize training monitor
        if (trainingMonitor == null) {
            trainingMonitor = new MLTrainingMonitor(fakePlayerManager, ml, bus);
        }
        trainingMonitor.startTrainingSession(count);

        bus.post(new TrainingStartedEvent(count));
    }

    public void stop() {
        // Stop training monitor
        if (trainingMonitor != null) {
            trainingMonitor.endTrainingSession();
        }

        // Despawn all fake players
        for (FakePlayer fp : fakePlayers.values()) {
            fakePlayerManager.despawnFakePlayer(fp);
        }
        fakePlayers.clear();
        bus.post(new TrainingStoppedEvent());
    }

    public void tick() {
        long tickStartTime = System.nanoTime();

        for (FakePlayer fp : fakePlayers.values()) {
            updateFakePlayer(fp);
        }

        // Update training monitor
        if (trainingMonitor != null) {
            long tickEndTime = System.nanoTime();
            long tickDurationMs = (tickEndTime - tickStartTime) / 1_000_000;
            trainingMonitor.recordTick(tickDurationMs, fakePlayers.size());
        }
    }

    public int getCount() { return fakePlayers.size(); }

    /**
     * Check if training is currently active
     */
    public boolean isTrainingActive() {
        return !fakePlayers.isEmpty();
    }

    private void updateFakePlayer(FakePlayer fp) {
        if (!fp.isAlive()) {
            return;
        }

        Location from = fp.getLocation();
        // For fake player training, we don't have a real player to predict from
        // Generate a dynamic target location for training purposes
        Location target = generateTrainingTarget(fp, from);

        // Calculate state and action for Q-learning
        long stateHash = calculateStateHash(fp, from);
        int action = ml.selectAction(stateHash, 10); // 10 possible actions

        // Execute action
        Location nextLocation = executeAction(fp, action, from);

        // Calculate reward based on action outcome
        double reward = calculateReward(fp, from, nextLocation, target);

        // Create experience
        long nextStateHash = calculateStateHash(fp, nextLocation);
        SimExperience experience = new SimExperience(stateHash, action, reward, nextStateHash, false);

        // Add to ML system
        ml.addPlayerExperience(fp, experience);

        // Record training data for monitoring
        if (trainingMonitor != null) {
            trainingMonitor.recordExperience(fp, experience, action, reward);
        }

        // Update fake player location
        fp.moveTo(nextLocation);
    }

    private long calculateStateHash(FakePlayer fp, Location location) {
        // Simple state representation: position + health
        return Objects.hash(
            location.getBlockX(),
            location.getBlockY(),
            location.getBlockZ(),
            (int) fp.getHealth()
        );
    }

    private Location executeAction(FakePlayer fp, int action, Location from) {
        // Define actions: 0-3: move directions, 4-6: other actions, etc.
        double moveDistance = 1.0;

        return switch (action) {
            case 0 -> from.clone().add(moveDistance, 0, 0);  // East
            case 1 -> from.clone().add(-moveDistance, 0, 0); // West
            case 2 -> from.clone().add(0, 0, moveDistance);  // South
            case 3 -> from.clone().add(0, 0, -moveDistance); // North
            case 4 -> from.clone().add(0, 1, 0);             // Up
            case 5 -> from.clone().add(0, -1, 0);            // Down
            default -> from.clone(); // Stay
        };
    }

    private double calculateReward(FakePlayer fp, Location from, Location to, Location target) {
        double reward = 0.0;

        // Distance to target reward
        double distanceBefore = from.distance(target);
        double distanceAfter = to.distance(target);

        if (distanceAfter < distanceBefore) {
            reward += 0.1; // Getting closer to target
        } else if (distanceAfter > distanceBefore) {
            reward -= 0.05; // Getting further from target
        }

        // Movement success reward
        if (!to.getBlock().getType().isSolid()) {
            reward += 0.02; // Successful movement
        } else {
            reward -= 0.1; // Hit a wall
        }

        // Survival reward
        if (fp.isAlive()) {
            reward += 0.01;
        }

        return reward;
    }

    /**
     * Generate a training target location for fake player learning
     * This creates dynamic goals to encourage exploration and learning
     */
    private Location generateTrainingTarget(FakePlayer fp, Location current) {
        // Simple exploration pattern: move toward areas with different characteristics
        double distance = 15.0 + Math.random() * 25.0; // 15-40 block radius
        double angle = Math.random() * 2 * Math.PI; // Random direction

        // Add some vertical variation to encourage 3D movement
        double heightOffset = (Math.random() - 0.5) * 10.0; // -5 to +5 blocks

        double targetX = current.getX() + Math.cos(angle) * distance;
        double targetY = Math.max(1, Math.min(250, current.getY() + heightOffset));
        double targetZ = current.getZ() + Math.sin(angle) * distance;

        return new Location(current.getWorld(), targetX, targetY, targetZ);
    }

    // Event classes
    public static class TrainingStartedEvent {
        public final int fakePlayerCount;
        public TrainingStartedEvent(int fakePlayerCount) {
            this.fakePlayerCount = fakePlayerCount;
        }
    }

    public static class TrainingStoppedEvent {
        public TrainingStoppedEvent() {}
    }
}

