package chef.sheesh.eyeAI.ai.fakeplayer;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.Optional;
import java.util.UUID;

/**
 * Interface for controlling packet-based NPC representations of fake players
 */
public interface IFakePlayerPacketController {
    
    /**
     * Create a visual NPC representation for a fake player
     */
    void createVisualNpc(FakePlayer fakePlayer);
    
    /**
     * Remove the visual NPC representation
     */
    void removeVisualNpc(FakePlayer fakePlayer);
    
    /**
     * Update the NPC's location
     */
    void updateLocation(FakePlayer fakePlayer, Location newLocation);
    
    /**
     * Check if the NPC system is enabled
     */
    boolean isEnabled();
    
    /**
     * Set enabled/disabled state
     */
    void setEnabled(boolean enabled);
    
    /**
     * Get or create a proxy entity for damage events
     */
    Entity getOrCreateProxyEntity(FakePlayer fakePlayer);
    
    /**
     * Queue an update for the fake player
     */
    void queueUpdate(FakePlayer fakePlayer);
    
    /**
     * Flush all pending packet updates
     */
    void flush();
    
    /**
     * Update NPC visibility for all players
     */
    void updateVisibility();

    /**
     * Gets the FakePlayer associated with a given entity ID.
     * @param entityId The UUID of the visual NPC entity.
     * @return An Optional containing the FakePlayer if found, otherwise empty.
     */
    Optional<FakePlayer> getFakePlayerByEntityId(UUID entityId);
}
