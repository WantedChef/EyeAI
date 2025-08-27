package chef.sheesh.eyeAI.ai.fakeplayer.persistence;

import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface for fake player persistence operations
 */
public interface IFakePlayerPersistence {

    /**
     * Save a fake player to persistent storage
     */
    void save(FakePlayer fakePlayer);

    /**
     * Load a fake player by ID
     */
    Optional<FakePlayer> load(UUID id);

    /**
     * Load all fake players
     */
    List<FakePlayer> loadAll();

    /**
     * Delete a fake player from persistent storage
     */
    void delete(UUID id);

    /**
     * Check if a fake player exists in storage
     */
    boolean exists(UUID id);

    /**
     * Get the count of stored fake players
     */
    int count();

    /**
     * Save all fake players in a batch
     */
    void saveAll(List<FakePlayer> fakePlayers);

    /**
     * Delete all fake players
     */
    void deleteAll();

    /**
     * Find fake players by name pattern
     */
    List<FakePlayer> findByName(String namePattern);

    /**
     * Find fake players in a specific world
     */
    List<FakePlayer> findByWorld(UUID worldId);

    /**
     * Check if persistence is enabled
     */
    boolean isEnabled();

    /**
     * Set persistence enabled/disabled
     */
    void setEnabled(boolean enabled);

    /**
     * Get statistics for a fake player
     */
    Optional<FakePlayerStatistics> getStatistics(UUID id);

    /**
     * Update statistics for a fake player
     */
    void updateStatistics(UUID id, FakePlayerStatistics stats);
}
