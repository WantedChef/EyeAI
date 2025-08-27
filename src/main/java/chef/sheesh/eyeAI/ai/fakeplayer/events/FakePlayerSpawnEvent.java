package chef.sheesh.eyeAI.ai.fakeplayer.events;

import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;

/**
 * Event fired when a fake player is spawned
 */
public class FakePlayerSpawnEvent extends FakePlayerEvent implements Cancellable {

    private boolean cancelled = false;
    private final Location spawnLocation;

    public FakePlayerSpawnEvent(FakePlayer fakePlayer, Location spawnLocation) {
        super(fakePlayer);
        this.spawnLocation = spawnLocation.clone();
    }

    /**
     * Get the spawn location of the fake player
     */
    public Location getSpawnLocation() {
        return spawnLocation.clone();
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
