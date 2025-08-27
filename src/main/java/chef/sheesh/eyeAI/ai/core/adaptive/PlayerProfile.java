package chef.sheesh.eyeAI.ai.core.adaptive;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Stores statistics and learned patterns about a specific player.
 */
public class PlayerProfile {

    private final UUID playerId;
    private final AtomicInteger encounters = new AtomicInteger(0);
    private final AtomicInteger attacksInitiated = new AtomicInteger(0);
    private final AtomicInteger timesFled = new AtomicInteger(0);

    public PlayerProfile(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void recordEncounter() {
        encounters.incrementAndGet();
    }

    public void recordAttack() {
        attacksInitiated.incrementAndGet();
    }

    public void recordFlee() {
        timesFled.incrementAndGet();
    }

    public int getEncounters() {
        return encounters.get();
    }

    /**
     * Calculates an aggression score for the player.
     * @return A value from 0.0 to 1.0, where 1.0 is very aggressive.
     */
    public double getAggressionScore() {
        int totalEncounters = encounters.get();
        if (totalEncounters == 0) {
            return 0.5; // Default aggression
        }
        return (double) attacksInitiated.get() / totalEncounters;
    }
}
