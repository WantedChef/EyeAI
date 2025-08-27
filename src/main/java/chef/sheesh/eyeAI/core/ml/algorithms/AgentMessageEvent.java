package chef.sheesh.eyeAI.core.ml.algorithms;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Custom event for inter-agent communication in multi-agent reinforcement learning.
 */
public class AgentMessageEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final String senderId;
    private final String receiverId;
    private final String message;
    private final Object data;

    public AgentMessageEvent(String senderId, String receiverId, String message, Object data) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.data = data;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
