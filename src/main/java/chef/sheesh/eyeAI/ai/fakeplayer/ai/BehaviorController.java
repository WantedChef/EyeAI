package chef.sheesh.eyeAI.ai.fakeplayer.ai;

import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerState;

/**
 * Behavior controller for fake players - manages high-level behavior modes
 */
public class BehaviorController {

    private final FakePlayer fakePlayer;
    private BehaviorMode currentMode = BehaviorMode.IDLE;
    private boolean combatMode = false;
    private boolean patrolMode = false;
    private boolean exploreMode = false;
    private boolean defendMode = false;
    private boolean escortMode = false;

    public BehaviorController(FakePlayer fakePlayer) {
        this.fakePlayer = fakePlayer;
    }

    /**
     * Update behavior logic
     */
    public void tick() {
        // Update behavior based on current mode
        switch (currentMode) {
            case COMBAT -> handleCombatBehavior();
            case PATROL -> handlePatrolBehavior();
            case EXPLORE -> handleExploreBehavior();
            case DEFEND -> handleDefendBehavior();
            case ESCORT -> handleEscortBehavior();
            case IDLE -> handleIdleBehavior();
        }
    }

    /**
     * Handle combat behavior
     */
    private void handleCombatBehavior() {
        if (!fakePlayer.getCombatController().isInCombat()) {
            // No combat target, switch to patrol or idle
            if (patrolMode) {
                setCurrentMode(BehaviorMode.PATROL);
            } else {
                setCurrentMode(BehaviorMode.IDLE);
            }
        }
    }

    /**
     * Handle patrol behavior
     */
    private void handlePatrolBehavior() {
        // Check for threats first
        if (fakePlayer.getTargetSelector().hasTarget()) {
            setCurrentMode(BehaviorMode.COMBAT);
            return;
        }

        // Continue patrol movement
        if (!fakePlayer.getMovementController().isMoving()) {
            // TODO: Implement patrol waypoint system
            // For now, just stay idle when not moving
        }
    }

    /**
     * Handle explore behavior
     */
    private void handleExploreBehavior() {
        // Check for threats first
        if (fakePlayer.getTargetSelector().hasTarget()) {
            setCurrentMode(BehaviorMode.COMBAT);
            return;
        }

        // TODO: Implement exploration logic
        // Random movement, area scanning, etc.
    }

    /**
     * Handle defend behavior
     */
    private void handleDefendBehavior() {
        // Always prioritize threats in defend mode
        if (fakePlayer.getTargetSelector().hasTarget()) {
            setCurrentMode(BehaviorMode.COMBAT);
            return;
        }

        // TODO: Implement defensive positioning
        // Stay near defended area, watch for threats
    }

    /**
     * Handle escort behavior
     */
    private void handleEscortBehavior() {
        // Check for threats to escort target
        if (fakePlayer.getTargetSelector().hasTarget()) {
            setCurrentMode(BehaviorMode.COMBAT);
            return;
        }

        // TODO: Implement escort following logic
        // Follow escort target, maintain formation
    }

    /**
     * Handle idle behavior
     */
    private void handleIdleBehavior() {
        // Check for threats
        if (fakePlayer.getTargetSelector().hasTarget()) {
            setCurrentMode(BehaviorMode.COMBAT);
            return;
        }

        // Stay idle, maybe look around occasionally
        if (fakePlayer.getState() != FakePlayerState.IDLE) {
            fakePlayer.setState(FakePlayerState.IDLE);
        }
    }

    /**
     * Set current behavior mode
     */
    private void setCurrentMode(BehaviorMode mode) {
        this.currentMode = mode;

        // Update fake player state based on mode
        switch (mode) {
            case COMBAT -> fakePlayer.setState(FakePlayerState.ATTACKING);
            case PATROL, EXPLORE, ESCORT -> fakePlayer.setState(FakePlayerState.MOVING);
            case DEFEND -> fakePlayer.setState(FakePlayerState.IDLE);
            case IDLE -> fakePlayer.setState(FakePlayerState.IDLE);
        }
    }

    // Mode setters for GroupCoordinator integration

    public void setCombatMode(boolean enabled) {
        this.combatMode = enabled;
        if (enabled) {
            setCurrentMode(BehaviorMode.COMBAT);
        }
    }

    public void setPatrolMode(boolean enabled) {
        this.patrolMode = enabled;
        if (enabled && !combatMode) {
            setCurrentMode(BehaviorMode.PATROL);
        }
    }

    public void setExploreMode(boolean enabled) {
        this.exploreMode = enabled;
        if (enabled && !combatMode) {
            setCurrentMode(BehaviorMode.EXPLORE);
        }
    }

    public void setDefendMode(boolean enabled) {
        this.defendMode = enabled;
        if (enabled && !combatMode) {
            setCurrentMode(BehaviorMode.DEFEND);
        }
    }

    public void setEscortMode(boolean enabled) {
        this.escortMode = enabled;
        if (enabled && !combatMode) {
            setCurrentMode(BehaviorMode.ESCORT);
        }
    }

    /**
     * Get current behavior mode
     */
    public BehaviorMode getCurrentMode() {
        return currentMode;
    }

    /**
     * Reset all behavior modes
     */
    public void resetModes() {
        this.combatMode = false;
        this.patrolMode = false;
        this.exploreMode = false;
        this.defendMode = false;
        this.escortMode = false;
        setCurrentMode(BehaviorMode.IDLE);
    }

    /**
     * Behavior modes enum
     */
    public enum BehaviorMode {
        IDLE,       // No specific behavior
        COMBAT,     // Engaging in combat
        PATROL,     // Patrolling area
        EXPLORE,    // Exploring new areas
        DEFEND,     // Defending position
        ESCORT      // Escorting target
    }
}
