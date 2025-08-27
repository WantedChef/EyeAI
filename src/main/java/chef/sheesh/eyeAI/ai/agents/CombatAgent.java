package chef.sheesh.eyeAI.ai.agents;

import chef.sheesh.eyeAI.ai.behavior.BehaviorTreeFactory;
import chef.sheesh.eyeAI.ai.behavior.IBehaviorTree;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerManager;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerWrapper;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.List;

/**
 * Agent specialized in combat behaviors.
 * This agent will actively seek out and engage enemies.
 */
public class CombatAgent extends AbstractAgent {

    private Entity currentTarget;
    private long lastTargetScanTime;
    private final long targetScanInterval = 2000; // Scan for targets every 2 seconds
    private final BehaviorTreeFactory behaviorTreeFactory;

    public CombatAgent(AgentConfig config) {
        super(config);
        this.behaviorTreeFactory = new BehaviorTreeFactory();

        // Set default combat behavior tree if none provided
        if (this.behaviorTree == null) {
            this.behaviorTree = behaviorTreeFactory.createAdvancedCombatTree();
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
        // Set up combat-specific initialization
        if (fakePlayer instanceof FakePlayer) {
            FakePlayer realFakePlayer = (FakePlayer) fakePlayer;
            realFakePlayer.setState(chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerState.IDLE);
        }
    }

    @Override
    protected void onDespawn() {
        // Clean up combat-specific resources
        currentTarget = null;
    }

    @Override
    protected void onTick(long deltaTime) {
        // Scan for targets periodically
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTargetScanTime > targetScanInterval) {
            scanForTargets();
            lastTargetScanTime = currentTime;
        }

        // Update combat state based on current situation
        updateCombatState();
    }

    @Override
    protected void onTickError(Exception e) {
        // Log error and reset target
        currentTarget = null;
    }

    @Override
    protected void onReset() {
        currentTarget = null;
        lastTargetScanTime = 0;
    }

    @Override
    protected void onDamage(double amount) {
        // Combat agents might become more aggressive when damaged
        if (fakePlayer instanceof FakePlayer) {
            FakePlayer realFakePlayer = (FakePlayer) fakePlayer;
            realFakePlayer.setState(chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerState.ATTACKING);
        }
    }

    @Override
    protected void onHeal(double amount) {
        // Could implement healing behavior
    }

    @Override
    protected void onDeath() {
        // Combat agent death logic
        currentTarget = null;
    }

    @Override
    protected void onBehaviorTreeResult(IBehaviorTree.ExecutionResult result) {
        // Handle behavior tree results specific to combat
        switch (result) {
            case SUCCESS:
                // Combat action succeeded
                break;
            case FAILURE:
                // Combat action failed, might need to find new target
                currentTarget = null;
                break;
            case RUNNING:
                // Combat action is ongoing
                break;
        }
    }

    @Override
    public AgentType getType() {
        return AgentType.COMBAT;
    }

    /**
     * Scan for nearby targets
     */
    private void scanForTargets() {
        if (fakePlayer == null || !fakePlayer.isAlive()) {
            return;
        }

        Location location = fakePlayer.getLocation();
        if (location == null || location.getWorld() == null) {
            return;
        }

        double detectionRange = config.getDetectionRange();

        // Find nearby living entities
        List<Entity> nearbyEntities = (List<Entity>) location.getWorld().getNearbyEntities(
            location, detectionRange, detectionRange, detectionRange,
            entity -> entity instanceof LivingEntity && entity != fakePlayer
        );

        // Find the closest hostile target
        Entity closestTarget = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof LivingEntity)) {
                continue;
            }

            LivingEntity livingEntity = (LivingEntity) entity;

            // Skip if it's dead or if it's another fake player/agent
            if (!livingEntity.isValid() || livingEntity.isDead()) {
                continue;
            }

            // Check if it's a player or hostile mob
            if (isHostileEntity(livingEntity)) {
                double distance = location.distance(entity.getLocation());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestTarget = entity;
                }
            }
        }

        // Update current target
        if (closestTarget != null && closestTarget != currentTarget) {
            currentTarget = closestTarget;
            onTargetAcquired(currentTarget);
        } else if (closestTarget == null && currentTarget != null) {
            onTargetLost(currentTarget);
            currentTarget = null;
        }
    }

    /**
     * Update combat state based on current situation
     */
    private void updateCombatState() {
        if (fakePlayer instanceof FakePlayer) {
            FakePlayer realFakePlayer = (FakePlayer) fakePlayer;

            if (currentTarget != null && currentTarget.isValid() && !currentTarget.isDead()) {
                // Have a target, be aggressive
                realFakePlayer.setState(chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerState.ATTACKING);
            } else {
                // No target, return to idle or patrol
                realFakePlayer.setState(chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerState.IDLE);
            }
        }
    }

    /**
     * Check if an entity is considered hostile
     */
    private boolean isHostileEntity(LivingEntity entity) {
        // Check if it's a player
        if (entity.getType().name().equals("PLAYER")) {
            return true;
        }

        // Check if it's a hostile mob (you can expand this list)
        String entityType = entity.getType().name();
        return entityType.equals("ZOMBIE") ||
               entityType.equals("SKELETON") ||
               entityType.equals("CREEPER") ||
               entityType.equals("SPIDER") ||
               entityType.equals("ENDERMAN");
    }

    /**
     * Called when a new target is acquired
     */
    private void onTargetAcquired(Entity target) {
        // Could implement target acquisition logic
        // For example: play sound, update behavior tree parameters, etc.
    }

    /**
     * Called when the current target is lost
     */
    private void onTargetLost(Entity target) {
        // Could implement target loss logic
        // For example: play sound, return to patrol, etc.
    }

    /**
     * Get the current target
     */
    public Entity getCurrentTarget() {
        return currentTarget;
    }

    /**
     * Set a specific target for this agent
     */
    public void setTarget(Entity target) {
        this.currentTarget = target;
        if (target != null) {
            onTargetAcquired(target);
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
