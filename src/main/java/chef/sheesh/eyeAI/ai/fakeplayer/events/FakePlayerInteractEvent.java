package chef.sheesh.eyeAI.ai.fakeplayer.events;

import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/**
 * Event fired when a player interacts with a fake player
 */
public class FakePlayerInteractEvent extends FakePlayerEvent implements Cancellable {

    private boolean cancelled = false;
    private final Player player;
    private final InteractionType interactionType;
    private String responseMessage;

    public FakePlayerInteractEvent(FakePlayer fakePlayer, Player player, InteractionType interactionType) {
        super(fakePlayer);
        this.player = player;
        this.interactionType = interactionType;
        this.responseMessage = getDefaultResponseMessage();
    }

    /**
     * Get the player who interacted
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the type of interaction
     */
    public InteractionType getInteractionType() {
        return interactionType;
    }

    /**
     * Get the response message
     */
    public String getResponseMessage() {
        return responseMessage;
    }

    /**
     * Set the response message
     */
    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    /**
     * Get the default response message
     */
    private String getDefaultResponseMessage() {
        return switch (interactionType) {
            case RIGHT_CLICK -> "§e[NPC] §f" + fakePlayer.getName() + "§7: Hello! I'm an AI-controlled entity.";
            case LEFT_CLICK -> "§e[NPC] §f" + fakePlayer.getName() + "§7: Ouch! Please don't hurt me.";
            case COMMAND -> "§e[NPC] §f" + fakePlayer.getName() + "§7: How can I help you?";
            case TRADE -> "§e[NPC] §f" + fakePlayer.getName() + "§7: Sorry, I don't have anything to trade.";
            case QUEST -> "§e[NPC] §f" + fakePlayer.getName() + "§7: I don't have any quests for you right now.";
        };
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Types of player interactions with fake players
     */
    public enum InteractionType {
        RIGHT_CLICK,    // Right-click interaction
        LEFT_CLICK,     // Left-click (attack) interaction
        COMMAND,        // Command interaction
        TRADE,          // Trade request
        QUEST           // Quest interaction
    }
}
