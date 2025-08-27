package chef.sheesh.eyeAI.ai.fakeplayer;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Interface for fake player manager to break circular dependencies
 */
public interface IFakePlayerManager {

    /**
     * Spawn a new fake player
     */
    void spawnFakePlayer(String name, Location spawn);

    /**
     * Despawn a fake player
     */
    boolean despawnFakePlayer(FakePlayer fakePlayer);

    /**
     * Despawn a fake player by ID
     */
    boolean despawnFakePlayer(java.util.UUID id);

    /**
     * Despawn all fake players
     */
    void despawnAll();

    /**
     * Get all active fake players
     */
    Collection<FakePlayer> getActiveFakePlayers();

    /**
     * Get all active fake players as a list of interfaces.
     * @return A list of all active fake players.
     */
    List<IFakePlayer> getAllFakePlayers();

    /**
     * Get a fake player by ID
     */
    Optional<FakePlayer> getFakePlayer(java.util.UUID id);

    /**
     * Get a fake player by name
     */
    Optional<FakePlayer> getFakePlayer(String name);

    /**
     * Check if there are any active fake players
     */
    boolean hasActiveFakePlayers();

    /**
     * Get the count of active fake players
     */
    int getActiveFakePlayerCount();

    /**
     * Get the count of active fake players (alias for compatibility)
     */
    default int getActiveCount() {
        return getActiveFakePlayerCount();
    }

    /**
     * Tick all fake players (for event handling)
     */
    void tickAll();

    /**
     * Create fake damage event
     */
    void callFakeDamage(FakePlayer attacker, Entity target);

    /**
     * Save fake players to persistence
     */
    void saveToPersistence();

    /**
     * Load fake players from persistence
     */
    void loadFromPersistence();

    /**
     * Get the logger
     */
    java.util.logging.Logger getLogger();
}
