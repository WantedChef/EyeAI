package chef.sheesh.eyeAI.ai.fakeplayer.ai;

import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerState;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    // Escort system
    private Entity escortTarget = null;
    private Location escortOffset = null;
    private static final double ESCORT_DISTANCE = 3.0;
    private static final double ESCORT_MAX_DISTANCE = 15.0;

    // Patrol system
    private final List<Location> patrolWaypoints = new ArrayList<>();
    private long lastWaypointCheck = 0L;
    private static final long WAYPOINT_CHECK_INTERVAL = 1000L; // ms
    private int currentWaypointIndex = 0;
    private static final double WAYPOINT_REACH_DISTANCE = 1.5; // blocks

    // Exploration system
    private Location explorationTarget = null;
    private long lastExplorationTargetUpdate = 0L;
    private static final long EXPLORATION_UPDATE_INTERVAL = 5000L; // ms
    private static final double EXPLORATION_RADIUS = 24.0; // blocks

    // Defend system
    private Location defendPosition = null;
    private static final double DEFEND_RADIUS = 16.0; // max distance from defendPosition
    private static final double DEFEND_PATROL_RADIUS = 6.0; // local patrol radius around defendPosition

    public BehaviorController(FakePlayer fakePlayer) {
        this.fakePlayer = fakePlayer;
        initializePatrolWaypoints();
    }

    /**
     * Initialize patrol waypoints around the spawn area
     */
    private void initializePatrolWaypoints() {
        Location baseLocation = fakePlayer.getLocation().clone();
        double patrolRadius = 20.0; // 20 block patrol radius

        // Create a diamond-shaped patrol pattern
        patrolWaypoints.add(baseLocation.clone().add(patrolRadius, 0, 0));      // East
        patrolWaypoints.add(baseLocation.clone().add(patrolRadius, 0, patrolRadius)); // Southeast
        patrolWaypoints.add(baseLocation.clone().add(0, 0, patrolRadius));      // South
        patrolWaypoints.add(baseLocation.clone().add(-patrolRadius, 0, patrolRadius)); // Southwest
        patrolWaypoints.add(baseLocation.clone().add(-patrolRadius, 0, 0));     // West
        patrolWaypoints.add(baseLocation.clone().add(-patrolRadius, 0, -patrolRadius)); // Northwest
        patrolWaypoints.add(baseLocation.clone().add(0, 0, -patrolRadius));     // North
        patrolWaypoints.add(baseLocation.clone().add(patrolRadius, 0, -patrolRadius)); // Northeast
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

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastWaypointCheck > WAYPOINT_CHECK_INTERVAL) {
            lastWaypointCheck = currentTime;

            // Check if we've reached the current waypoint
            if (!patrolWaypoints.isEmpty() && currentWaypointIndex < patrolWaypoints.size()) {
                Location currentWaypoint = patrolWaypoints.get(currentWaypointIndex);
                double distance = fakePlayer.getLocation().distance(currentWaypoint);

                if (distance < WAYPOINT_REACH_DISTANCE) {
                    // Reached waypoint, move to next
                    currentWaypointIndex = (currentWaypointIndex + 1) % patrolWaypoints.size();
                    currentWaypoint = patrolWaypoints.get(currentWaypointIndex);
                }

                // Move towards current waypoint
                if (fakePlayer.getPathfinder() != null) {
                    fakePlayer.getPathfinder().findPath(currentWaypoint);
                }
                if (fakePlayer.getMovementController() != null) {
                    fakePlayer.getMovementController().moveTowards(currentWaypoint);
                }
            }
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

        long currentTime = System.currentTimeMillis();
        Location currentLocation = fakePlayer.getLocation();

        // Update exploration target periodically or if we've reached current target
        if (explorationTarget == null ||
            currentLocation.distance(explorationTarget) < 3.0 ||
            currentTime - lastExplorationTargetUpdate > EXPLORATION_UPDATE_INTERVAL) {

            generateNewExplorationTarget();
            lastExplorationTargetUpdate = currentTime;
        }

        // Move towards exploration target
        if (explorationTarget != null) {
            if (fakePlayer.getPathfinder() != null) {
                fakePlayer.getPathfinder().findPath(explorationTarget);
            }
            if (fakePlayer.getMovementController() != null) {
                fakePlayer.getMovementController().moveTowards(explorationTarget);
            }
        }
    }

    /**
     * Generate a new random exploration target
     */
    private void generateNewExplorationTarget() {
        Location baseLocation = fakePlayer.getLocation().clone();
        Random random = new Random();

        // Generate random offset within exploration radius
        double angle = random.nextDouble() * 2 * Math.PI;
        double distance = random.nextDouble() * EXPLORATION_RADIUS;

        double offsetX = Math.cos(angle) * distance;
        double offsetZ = Math.sin(angle) * distance;

        explorationTarget = baseLocation.clone().add(offsetX, 0, offsetZ);

        // Ensure the target is on solid ground (simple Y adjustment)
        explorationTarget.setY(baseLocation.getY());
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

        // Initialize defend position if not set
        if (defendPosition == null) {
            defendPosition = fakePlayer.getLocation().clone();
        }

        Location currentLocation = fakePlayer.getLocation();

        // Check if we're too far from defend position
        if (currentLocation.distance(defendPosition) > DEFEND_RADIUS) {
            // Return to defend position
            if (fakePlayer.getPathfinder() != null) {
                fakePlayer.getPathfinder().findPath(defendPosition);
            }
            if (fakePlayer.getMovementController() != null) {
                fakePlayer.getMovementController().moveTowards(defendPosition);
            }
        } else {
            // Patrol around defend position
            patrolAroundDefendPosition();
        }
    }

    /**
     * Patrol around the defend position
     */
    private void patrolAroundDefendPosition() {
        long currentTime = System.currentTimeMillis();

        // Update patrol target periodically
        if (explorationTarget == null ||
            fakePlayer.getLocation().distance(explorationTarget) < 2.0 ||
            currentTime - lastExplorationTargetUpdate > 3000) { // 3 seconds

            generateDefendPatrolTarget();
            lastExplorationTargetUpdate = currentTime;
        }

        // Move towards patrol target
        if (explorationTarget != null) {
            if (fakePlayer.getPathfinder() != null) {
                fakePlayer.getPathfinder().findPath(explorationTarget);
            }
            if (fakePlayer.getMovementController() != null) {
                fakePlayer.getMovementController().moveTowards(explorationTarget);
            }
        }
    }

    /**
     * Generate a new patrol target around the defend position
     */
    private void generateDefendPatrolTarget() {
        if (defendPosition == null) {
            defendPosition = fakePlayer.getLocation().clone();
        }

        Random random = new Random();

        // Generate random offset within defend patrol radius
        double angle = random.nextDouble() * 2 * Math.PI;
        double distance = random.nextDouble() * DEFEND_PATROL_RADIUS;

        double offsetX = Math.cos(angle) * distance;
        double offsetZ = Math.sin(angle) * distance;

        explorationTarget = defendPosition.clone().add(offsetX, 0, offsetZ);
        explorationTarget.setY(defendPosition.getY());
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

        // Check if escort target is still valid
        if (escortTarget == null || !escortTarget.isValid() || escortTarget.isDead()) {
            // Escort target lost, return to idle
            setCurrentMode(BehaviorMode.IDLE);
            escortTarget = null;
            escortOffset = null;
            return;
        }

        Location targetLocation = escortTarget.getLocation();
        Location currentLocation = fakePlayer.getLocation();

        // Check if we're too far from escort target
        if (currentLocation.distance(targetLocation) > ESCORT_MAX_DISTANCE) {
            // Try to catch up
            if (fakePlayer.getPathfinder() != null) {
                fakePlayer.getPathfinder().findPath(targetLocation);
            }
            if (fakePlayer.getMovementController() != null) {
                fakePlayer.getMovementController().moveTowards(targetLocation);
            }
        } else {
            // Maintain escort position
            maintainEscortPosition(targetLocation);
        }
    }

    /**
     * Maintain position relative to escort target
     */
    private void maintainEscortPosition(Location targetLocation) {
        // Initialize escort offset if not set
        if (escortOffset == null) {
            escortOffset = fakePlayer.getLocation().clone().subtract(targetLocation).toVector().normalize().multiply(ESCORT_DISTANCE).toLocation(targetLocation.getWorld());
        }

        // Calculate desired escort position
        Location desiredPosition = targetLocation.clone().add(escortOffset);

        // Check if we need to move to maintain position
        if (fakePlayer.getLocation().distance(desiredPosition) > 1.5) {
            if (fakePlayer.getPathfinder() != null) {
                fakePlayer.getPathfinder().findPath(desiredPosition);
            }
            if (fakePlayer.getMovementController() != null) {
                fakePlayer.getMovementController().moveTowards(desiredPosition);
            }
        }
    }

    /**
     * Set escort target
     */
    public void setEscortTarget(Entity target) {
        if (target != null && target.isValid() && !target.isDead()) {
            this.escortTarget = target;
            this.escortOffset = null; // Will be initialized on next tick
            setCurrentMode(BehaviorMode.ESCORT);
        }
    }

    /**
     * Clear escort target
     */
    public void clearEscortTarget() {
        this.escortTarget = null;
        this.escortOffset = null;
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
