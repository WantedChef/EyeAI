package chef.sheesh.eyeAI.ai.fakeplayer;

/**
 * Enumeration of possible states for a fake player
 */
public enum FakePlayerState {
    IDLE,
    MOVING,
    COMBAT,
    ATTACKING,
    FLEEING,
    PATROLLING,
    INTERACTING,
    DEAD,
    REMOVED;

    /**
     * Get the string representation of the state
     * @return the state name as lowercase string
     */
    public String getName() {
        return this.name().toLowerCase();
    }

    /**
     * Get enum from string
     * @param state the state string
     * @return the corresponding enum
     */
    public static FakePlayerState fromString(String state) {
        try {
            return FakePlayerState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            return IDLE;
        }
    }
}
