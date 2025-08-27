package chef.sheesh.eyeAI.ai.core;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event fired on every AI tick to allow other parts of the plugin
 * to react to AI updates and synchronize with fake player actions.
 */
public class AITickEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public AITickEvent() {
        super(true); // async allowed
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}

