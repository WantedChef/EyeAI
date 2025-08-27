package chef.sheesh.eyeAI.ai.fakeplayer.events;

import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerState;

/**
 * Event fired when a fake player is despawned
 */
public class FakePlayerDespawnEvent extends FakePlayerEvent {

    private final DespawnReason reason;
    private final FakePlayerState lastState;

    public FakePlayerDespawnEvent(FakePlayer fakePlayer, DespawnReason reason, FakePlayerState lastState) {
        super(fakePlayer);
        this.reason = reason;
        this.lastState = lastState;
    }

    /**
     * Get the reason for despawning
     */
    public DespawnReason getReason() {
        return reason;
    }

    /**
     * Get the last state of the fake player
     */
    public FakePlayerState getLastState() {
        return lastState;
    }

    /**
     * Reasons why a fake player might despawn
     */
    public enum DespawnReason {
        MANUAL,           // Manually despawned by command/admin
        PLUGIN_DISABLE,   // Plugin is being disabled
        SERVER_SHUTDOWN,  // Server is shutting down
        PERFORMANCE,      // Despawned due to performance optimization
        HEALTH_CRITICAL,  // Despawned due to critical health (death)
        ERROR,           // Despawned due to an error
        CONFIG_CHANGE,   // Despawned due to configuration change
        WORLD_CHANGE,    // Despawned due to world change/unload
        DISTANCE_LIMIT   // Despawned due to distance limit
    }
}
