package chef.sheesh.eyeAI.ai.fakeplayer;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event fired when a player interacts with a fake player NPC
 */
public class FakePlayerInteractEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private final Player player;
    private final FakePlayer fakePlayer;

    public FakePlayerInteractEvent(Player player, FakePlayer fakePlayer) {
        this.player = player;
        this.fakePlayer = fakePlayer;
    }

    /**
     * Get the player who interacted with the fake player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the fake player that was interacted with
     */
    public FakePlayer getFakePlayer() {
        return fakePlayer;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
