package chef.sheesh.eyeAI.ai.fakeplayer.events;

import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerState;
import org.bukkit.event.Cancellable;

/**
 * Event fired when a fake player changes state
 */
public class FakePlayerStateChangeEvent extends FakePlayerEvent implements Cancellable {

    private boolean cancelled = false;
    private final FakePlayerState oldState;
    private final FakePlayerState newState;
    private final StateChangeReason reason;

    public FakePlayerStateChangeEvent(FakePlayer fakePlayer, FakePlayerState oldState,
                                    FakePlayerState newState, StateChangeReason reason) {
        super(fakePlayer);
        this.oldState = oldState;
        this.newState = newState;
        this.reason = reason;
    }

    /**
     * Get the old state
     */
    public FakePlayerState getOldState() {
        return oldState;
    }

    /**
     * Get the new state
     */
    public FakePlayerState getNewState() {
        return newState;
    }

    /**
     * Get the reason for the state change
     */
    public StateChangeReason getReason() {
        return reason;
    }

    /**
     * Check if this is a state transition (different states)
     */
    public boolean isStateTransition() {
        return oldState != newState;
    }

    /**
     * Check if transitioning to a combat state
     */
    public boolean isEnteringCombat() {
        return !isCombatState(oldState) && isCombatState(newState);
    }

    /**
     * Check if transitioning from a combat state
     */
    public boolean isLeavingCombat() {
        return isCombatState(oldState) && !isCombatState(newState);
    }

    /**
     * Check if transitioning to death state
     */
    public boolean isDying() {
        return newState == FakePlayerState.DEAD;
    }

    /**
     * Check if respawning (from dead to another state)
     */
    public boolean isRespawning() {
        return oldState == FakePlayerState.DEAD && newState != FakePlayerState.DEAD;
    }

    /**
     * Check if state is a combat state
     */
    private boolean isCombatState(FakePlayerState state) {
        return state == FakePlayerState.ATTACKING || state == FakePlayerState.FLEEING;
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
     * Reasons for state changes
     */
    public enum StateChangeReason {
        AI_DECISION,        // AI behavior tree decision
        HEALTH_CHANGE,      // Health level changed
        TARGET_ACQUIRED,    // Found a target to attack
        TARGET_LOST,        // Lost current target
        THREAT_DETECTED,    // Detected nearby threat
        THREAT_CLEARED,     // Nearby threats cleared
        MANUAL_COMMAND,     // Changed by command/admin
        ENVIRONMENTAL,      // Environmental factors (time of day, weather)
        GROUP_COORDINATION, // Group coordination change
        PERFORMANCE_OPT,    // Performance optimization
        ERROR_CONDITION,    // Error or exceptional condition
        CUSTOM              // Custom reason
    }
}
