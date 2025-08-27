package chef.sheesh.eyeAI.ai.agents;

import chef.sheesh.eyeAI.ai.behavior.IBehaviorTree;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

/**
 * Abstract base class for all agents, providing common functionality.
 */
@SuppressWarnings("unused")
public abstract class AbstractAgent implements IAgent {

    protected final String id;
    protected String name;
    protected IFakePlayer fakePlayer;
    protected IBehaviorTree behaviorTree;
    protected AgentState state;
    protected AgentConfig config;
    protected double health;
    protected long lastTickTime;

    public AbstractAgent(AgentConfig config) {
        this.id = UUID.randomUUID().toString();
        this.config = config;
        this.name = config.getName();
        this.state = AgentState.INACTIVE;
        this.health = config.getMaxHealth();
        this.lastTickTime = System.currentTimeMillis();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Location getLocation() {
        return fakePlayer != null ? fakePlayer.getLocation() : null;
    }

    @Override
    public World getWorld() {
        Location location = getLocation();
        return location != null ? location.getWorld() : null;
    }

    @Override
    public IFakePlayer getFakePlayer() {
        return fakePlayer;
    }

    @Override
    public IBehaviorTree getBehaviorTree() {
        return behaviorTree;
    }

    @Override
    public void setBehaviorTree(IBehaviorTree behaviorTree) {
        this.behaviorTree = behaviorTree;
    }

    @Override
    public AgentState getState() {
        return state;
    }

    @Override
    public void setState(AgentState state) {
        this.state = state;
    }

    @Override
    public boolean isActive() {
        return state == AgentState.ACTIVE && fakePlayer != null && fakePlayer.isAlive();
    }

    @Override
    public boolean spawn(Location location) {
        if (state != AgentState.INACTIVE && state != AgentState.DEAD) {
            return false;
        }

        try {
            // Create the fake player
            this.fakePlayer = createFakePlayer(location);
            this.state = AgentState.SPAWNING;

            // Initialize behavior tree if set
            if (behaviorTree != null) {
                behaviorTree.reset();
            }

            onSpawn(location);
            this.state = AgentState.ACTIVE;
            return true;

        } catch (Exception e) {
            this.state = AgentState.INACTIVE;
            return false;
        }
    }

    @Override
    public void despawn() {
        if (state == AgentState.INACTIVE || state == AgentState.DEAD) {
            return;
        }

        this.state = AgentState.DESPAWNING;
        onDespawn();

        if (fakePlayer != null) {
            // Note: IFakePlayer interface doesn't have despawn() method
            // Use alternative approach or cast to FakePlayer if needed
            fakePlayer = null;
        }

        this.state = AgentState.INACTIVE;
    }

    @Override
    public void tick() {
        if (!isActive()) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastTickTime;
        lastTickTime = currentTime;

        try {
            // Execute behavior tree if available
            if (behaviorTree != null) {
                IBehaviorTree.ExecutionResult result = behaviorTree.execute(fakePlayer);
                onBehaviorTreeResult(result);
            }

            // Update fake player
            if (fakePlayer != null) {
                // Note: IFakePlayer interface doesn't have tick() method
                // Use alternative approach or cast to FakePlayer if needed
                // fakePlayer.tick();
            }

            // Custom agent tick logic
            onTick(deltaTime);

        } catch (Exception e) {
            // Log error and potentially despawn agent
            onTickError(e);
        }
    }

    @Override
    public void reset() {
        this.health = config.getMaxHealth();
        this.state = AgentState.INACTIVE;

        if (behaviorTree != null) {
            behaviorTree.reset();
        }

        onReset();
    }

    @Override
    public boolean isAlive() {
        return health > 0 && fakePlayer != null && fakePlayer.isAlive();
    }

    @Override
    public double getHealth() {
        return health / config.getMaxHealth();
    }

    @Override
    public void setHealth(double health) {
        this.health = Math.max(0, Math.min(config.getMaxHealth(), health * config.getMaxHealth()));
        if (this.health <= 0) {
            kill();
        }
    }

    @Override
    public void damage(double amount) {
        if (!isAlive()) {
            return;
        }

        this.health = Math.max(0, this.health - amount);
        onDamage(amount);

        if (this.health <= 0) {
            kill();
        }
    }

    @Override
    public void heal(double amount) {
        if (!isAlive()) {
            return;
        }

        this.health = Math.min(config.getMaxHealth(), this.health + amount);
        onHeal(amount);
    }

    @Override
    public void kill() {
        this.health = 0;
        this.state = AgentState.DEAD;
        onDeath();

        if (fakePlayer != null) {
            // Note: IFakePlayer interface doesn't have kill() method
            // Use alternative approach or cast to FakePlayer if needed
            // fakePlayer.kill();
        }
    }

    @Override
    public AgentConfig getConfig() {
        return config;
    }

    @Override
    public void setConfig(AgentConfig config) {
        this.config = config;
    }

    // Abstract methods that subclasses must implement

    /**
     * Create the fake player for this agent
     */
    protected abstract IFakePlayer createFakePlayer(Location location);

    /**
     * Called when the agent is spawned
     */
    protected abstract void onSpawn(Location location);

    /**
     * Called when the agent is despawned
     */
    protected abstract void onDespawn();

    /**
     * Called every tick for custom agent logic
     */
    protected abstract void onTick(long deltaTime);

    /**
     * Called when an error occurs during tick
     */
    protected abstract void onTickError(Exception e);

    /**
     * Called when the agent is reset
     */
    protected abstract void onReset();

    /**
     * Called when the agent takes damage
     */
    protected abstract void onDamage(double amount);

    /**
     * Called when the agent is healed
     */
    protected abstract void onHeal(double amount);

    /**
     * Called when the agent dies
     */
    protected abstract void onDeath();

    /**
     * Called when behavior tree execution returns a result
     */
    protected abstract void onBehaviorTreeResult(IBehaviorTree.ExecutionResult result);

    /**
     * Get the type of this agent
     */
    @Override
    public abstract AgentType getType();
}
