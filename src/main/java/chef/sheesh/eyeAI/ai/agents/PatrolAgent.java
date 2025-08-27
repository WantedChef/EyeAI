package chef.sheesh.eyeAI.ai.agents;

import chef.sheesh.eyeAI.ai.behavior.BehaviorTreeFactory;
import chef.sheesh.eyeAI.ai.behavior.IBehaviorTree;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerManager;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerWrapper;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent specialized in patrol behaviors.
 * This agent will move between predefined waypoints.
 */
public class PatrolAgent extends AbstractAgent {

    private List<Location> waypoints;
    private int currentWaypointIndex;
    private long lastWaypointTime;
    private final long waypointWaitTime = 3000; // Wait 3 seconds at each waypoint
    private final BehaviorTreeFactory behaviorTreeFactory;

    public PatrolAgent(AgentConfig config) {
        super(config);
        this.behaviorTreeFactory = new BehaviorTreeFactory();
        this.waypoints = new ArrayList<>();
        this.currentWaypointIndex = 0;
        this.lastWaypointTime = 0;

        // Set default patrol behavior tree if none provided
        if (this.behaviorTree == null) {
            this.behaviorTree = behaviorTreeFactory.createPatrolTree();
        }
    }

    @Override
    protected IFakePlayer createFakePlayer(Location location) {
        // Create a fake player through the FakePlayerManager
        FakePlayerManager fakePlayerManager = getFakePlayerManager();
        if (fakePlayerManager != null) {
            return fakePlayerManager.createFakePlayer(location, getName());
        }

        // Fallback: create a minimal fake player implementation
        return new MinimalFakePlayer(location, getName());
    }

    @Override
    protected void onSpawn(Location location) {
        // Initialize patrol waypoints if none set
        if (waypoints.isEmpty()) {
            initializeDefaultWaypoints(location);
        }

        if (fakePlayer instanceof FakePlayer) {
            FakePlayer realFakePlayer = (FakePlayer) fakePlayer;
            realFakePlayer.setState(chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerState.PATROLLING);
        }
    }

    @Override
    protected void onDespawn() {
        // Clean up patrol-specific resources
        waypoints.clear();
        currentWaypointIndex = 0;
    }

    @Override
    protected void onTick(long deltaTime) {
        if (waypoints.isEmpty()) {
            return;
        }

        Location currentLocation = fakePlayer.getLocation();
        Location currentWaypoint = waypoints.get(currentWaypointIndex);

        // Check if we've reached the current waypoint
        if (currentLocation.distance(currentWaypoint) < 2.0) {
            // Arrived at waypoint, wait before moving to next
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastWaypointTime > waypointWaitTime) {
                moveToNextWaypoint();
                lastWaypointTime = currentTime;
            }
        } else {
            // Move towards current waypoint
            moveTowardsWaypoint(currentWaypoint);
        }
    }

    @Override
    protected void onTickError(Exception e) {
        // On error, move to next waypoint
        moveToNextWaypoint();
    }

    @Override
    protected void onReset() {
        currentWaypointIndex = 0;
        lastWaypointTime = 0;
    }

    @Override
    protected void onDamage(double amount) {
        // Patrol agents might flee when damaged
        if (fakePlayer instanceof FakePlayer) {
            FakePlayer realFakePlayer = (FakePlayer) fakePlayer;
            realFakePlayer.setState(chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerState.FLEEING);
        }
    }

    @Override
    protected void onHeal(double amount) {
        // Return to patrolling when healed
        if (fakePlayer instanceof FakePlayer) {
            FakePlayer realFakePlayer = (FakePlayer) fakePlayer;
            realFakePlayer.setState(chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerState.PATROLLING);
        }
    }

    @Override
    protected void onDeath() {
        // Patrol agent death logic
        waypoints.clear();
    }

    @Override
    protected void onBehaviorTreeResult(IBehaviorTree.ExecutionResult result) {
        // Handle behavior tree results specific to patrol
        switch (result) {
            case SUCCESS:
                // Patrol action succeeded, move to next waypoint
                moveToNextWaypoint();
                break;
            case FAILURE:
                // Patrol action failed, try again or skip waypoint
                moveToNextWaypoint();
                break;
            case RUNNING:
                // Patrol action is ongoing
                break;
        }
    }

    @Override
    public AgentType getType() {
        return AgentType.PATROL;
    }

    /**
     * Set the patrol waypoints
     */
    public void setWaypoints(List<Location> waypoints) {
        this.waypoints = new ArrayList<>(waypoints);
        this.currentWaypointIndex = 0;
    }

    /**
     * Add a waypoint to the patrol route
     */
    public void addWaypoint(Location waypoint) {
        this.waypoints.add(waypoint);
    }

    /**
     * Get the current patrol waypoints
     */
    public List<Location> getWaypoints() {
        return new ArrayList<>(waypoints);
    }

    /**
     * Get the current waypoint index
     */
    public int getCurrentWaypointIndex() {
        return currentWaypointIndex;
    }

    /**
     * Initialize default waypoints around spawn location
     */
    private void initializeDefaultWaypoints(Location spawnLocation) {
        double radius = 10.0;
        for (int i = 0; i < 4; i++) {
            double angle = (Math.PI * 2 * i) / 4;
            double x = spawnLocation.getX() + Math.cos(angle) * radius;
            double z = spawnLocation.getZ() + Math.sin(angle) * radius;
            Location waypoint = new Location(spawnLocation.getWorld(), x, spawnLocation.getY(), z);
            waypoints.add(waypoint);
        }
    }

    /**
     * Move towards the specified waypoint
     */
    private void moveTowardsWaypoint(Location waypoint) {
        if (fakePlayer == null) {
            return;
        }

        fakePlayer.moveTo(waypoint);
    }

    /**
     * Move to the next waypoint in the patrol route
     */
    private void moveToNextWaypoint() {
        if (waypoints.isEmpty()) {
            return;
        }

        currentWaypointIndex = (currentWaypointIndex + 1) % waypoints.size();
        Location nextWaypoint = waypoints.get(currentWaypointIndex);

        if (fakePlayer != null) {
            fakePlayer.moveTo(nextWaypoint);
        }
    }

    /**
     * Get the FakePlayerManager (this would need to be implemented in your system)
     */
    private FakePlayerManager getFakePlayerManager() {
        // This is a placeholder - you would need to implement a way to get the FakePlayerManager
        // Perhaps through dependency injection or a service locator pattern
        return null;
    }
}
