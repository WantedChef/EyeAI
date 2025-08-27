package chef.sheesh.eyeAI.ai.fakeplayer.events;

import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Base event class for all fake player related events
 */
public abstract class FakePlayerEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    protected final FakePlayer fakePlayer;

    public FakePlayerEvent(FakePlayer fakePlayer) {
        this.fakePlayer = fakePlayer;
    }

    public FakePlayerEvent(FakePlayer fakePlayer, boolean async) {
        super(async);
        this.fakePlayer = fakePlayer;
    }

    /**
     * Get the fake player involved in this event
     */
    public FakePlayer getFakePlayer() {
        return fakePlayer;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
