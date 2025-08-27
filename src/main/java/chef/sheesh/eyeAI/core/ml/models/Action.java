package chef.sheesh.eyeAI.core.ml.models;

/**
 * Defines the set of possible actions an AI agent can perform.
 * Each action can have associated parameters (e.g., target location, entity).
 */
public enum Action {
    /**
     * Move to a specific location.
     * Parameters: x, y, z coordinates.
     */
    MOVE_TO,

    /**
     * Attack a specific entity.
     * Parameters: entity ID.
     */
    ATTACK_ENTITY,

    /**
     * Use an item from the inventory.
     * Parameters: item ID, target (optional).
     */
    USE_ITEM,

    /**
     * Interact with a block.
     * Parameters: block location, interaction type (e.g., break, place).
     */
    INTERACT_BLOCK,

    /**
     * Send a chat message.
     * Parameters: message content.
     */
    CHAT_MESSAGE,

    /**
     * Follow a specific player.
     * Parameters: player ID.
     */
    FOLLOW_PLAYER,

    /**
     * Flee from a source of danger.
     * Parameters: danger location.
     */
    FLEE_FROM,

    /**
     * Do nothing.
     */
    IDLE;
}
