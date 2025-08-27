package chef.sheesh.eyeAI.ai.fakeplayer;

import chef.sheesh.eyeAI.ai.behavior.IBehaviorTree;
import chef.sheesh.eyeAI.ai.core.DecisionContext;
import chef.sheesh.eyeAI.ai.core.emotions.EmotionSystem;
import chef.sheesh.eyeAI.ai.core.personality.PersonalitySystem;
import chef.sheesh.eyeAI.ai.core.personality.Personality;
import chef.sheesh.eyeAI.ai.core.team.Team;
import chef.sheesh.eyeAI.ai.core.team.TeamRole;
import chef.sheesh.eyeAI.ai.fakeplayer.ai.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Core simulation object representing a fake player in the game world.
 * This is a server-side only entity that doesn't use real Player objects.
 */
public class FakePlayer implements IFakePlayer {

    private final UUID id;
    private String name;
    private Location location;
    private double health = 20.0;
    private IBehaviorTree behaviorTree;
    private volatile FakePlayerState state = FakePlayerState.IDLE;
    private Entity visibleNpc; // Optional packet-based entity reference
    private long lastActionTime;
    private final FakePlayerManager manager;
    
    // AI Controller Components
    private final PathFinder pathfinder;
    private final MovementController movementController;
    private final TargetSelector targetSelector;
    private final CombatController combatController;
    private final BehaviorController behaviorController;

    // New Fields
    private final Map<String, Object> blackboard = new ConcurrentHashMap<>();
    private final EmotionSystem emotionSystem = new EmotionSystem();
    private final PersonalitySystem personalitySystem = new PersonalitySystem(Personality.STRATEGIC);
    private Entity target;
    private Team team;
    private TeamRole role = TeamRole.NONE;
    
    // Movement logging counter
    private static int movementLogCounter = 0;
    private static final int MOVEMENT_LOG_INTERVAL = 30000; // Only log every 30000 movements (~1 per 30 seconds)

    public FakePlayer(UUID id, String name, Location spawn, FakePlayerManager manager) {
        this.id = id;
        this.name = name;
        this.location = spawn.clone();
        this.manager = manager;
        this.lastActionTime = System.currentTimeMillis();
        
        // Initialize AI controllers
        this.pathfinder = new PathFinder(this);
        this.movementController = new MovementController(this);
        this.targetSelector = new TargetSelector(this);
        this.combatController = new CombatController(this);
        this.behaviorController = new BehaviorController(this);
    }
    
    @Override
    public FakePlayerManager getManager() {
        return manager;
    }

    public void tick() {
        try {
            updateLastActionTime();
            
            pathfinder.tick();
            movementController.tick();
            targetSelector.tick();
            combatController.tick();
            behaviorController.tick();

            if (behaviorTree != null) {
                behaviorTree.tick(this);
            }

            if (needsPacketUpdate()) {
                manager.getPacketController().queueUpdate(this);
            }

            updateState();

        } catch (Exception e) {
            Logger logger = manager.getLogger();
            if (logger != null) {
                logger.warning("FakePlayer tick error for " + name + ": " + e.getMessage());
            }
        }
    }

    public void moveTo(Location loc) {
        // Only log every MOVEMENT_LOG_INTERVAL movements to reduce console spam
        movementLogCounter++;
        if (movementLogCounter % MOVEMENT_LOG_INTERVAL == 0 && manager.getLogger() != null) {
            manager.getLogger().info("FakePlayer " + name + " moving to location: " + loc);
        }
        
        this.location = loc.clone();
        setState(FakePlayerState.MOVING);
        updateLastActionTime();
    }

    public void performAttack(Entity target) {
        setState(FakePlayerState.ATTACKING);
        manager.callFakeDamage(this, target);
        updateLastActionTime();
    }

    public DecisionContext createDecisionContext() {
        return manager.createDecisionContext(this);
    }

    private boolean needsPacketUpdate() {
        return (System.currentTimeMillis() - lastActionTime) > 50;
    }

    private void updateState() {
        if (health <= 0) {
            setState(FakePlayerState.DEAD);
            return;
        }

        DecisionContext context = createDecisionContext();

        if (context.isHealthCritical()) {
            setState(FakePlayerState.FLEEING);
        } else if (context.hasHostileNearby()) {
            setState(FakePlayerState.ATTACKING);
        } else if (state == FakePlayerState.MOVING) {
            if (movementController.hasReachedDestination()) {
                setState(FakePlayerState.IDLE);
            }
        }
    }

    public void setState(FakePlayerState state) {
        this.state = state;
        updateLastActionTime();
    }

    @Override
    public void setState(String state) {
        this.state = FakePlayerState.fromString(state);
        updateLastActionTime();
    }

    public String getStateName() {
        return state.name();
    }

    @Override
    public boolean isInCombat() {
        return this.state == FakePlayerState.COMBAT || this.state == FakePlayerState.ATTACKING;
    }

    @Override
    public boolean isAlive() {
        return this.state != FakePlayerState.DEAD && this.state != FakePlayerState.REMOVED;
    }

    // Explicit accessors to avoid Lombok issues in downstream modules
    public void setVisibleNpc(Entity entity) {
        this.visibleNpc = entity;
    }

    

    /**
     * Get nearby entities within the specified radius
     * WARNING: This method must be called from the main thread!
     */
    @Override
    public List<Entity> getNearbyEntities(double x, double y, double z) {
        if (location == null || location.getWorld() == null) {
            return new ArrayList<>();
        }
        
        // Ensure we're on the main thread
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("getNearbyEntities must be called from main thread!");
        }
        
        return new ArrayList<>(location.getWorld().getNearbyEntities(location, x, y, z));
    }

    @Override
    public void attackEntity(Entity target) {
        if (target instanceof LivingEntity) {
            ((LivingEntity) target).damage(5.0);
            this.state = FakePlayerState.COMBAT;
            manager.getLogger().info(String.format("%s attacked %s", this.name, target.getName()));
        }
    }

    @Override
    public void attackNearby() {
        List<Entity> nearby = new ArrayList<>(getNearbyEntities(5, 2, 5));
        if (!nearby.isEmpty()) {
            Entity target = nearby.get(0);
            attackEntity(target);
        }
        
        if (!nearby.isEmpty()) {
            this.state = FakePlayerState.COMBAT;
        } else if (this.state == FakePlayerState.COMBAT) {
            this.state = FakePlayerState.IDLE;
        }
    }

    public void attack(Player target) {
        performAttack(target);
    }

    public void kill() {
        setHealth(0);
        setState(FakePlayerState.DEAD);
    }

    public void remove() {
        setState(FakePlayerState.DEAD);
        manager.despawnFakePlayer(this);
    }

    public double getMaxHealth() {
        return 20.0;
    }

    public Entity getVisibleNpc() {
        return visibleNpc;
    }

    public long getLastActionTime() {
        return lastActionTime;
    }

    public IBehaviorTree getBehaviorTree() {
        return behaviorTree;
    }

    public void damage(double amount) {
        setHealth(health - amount);
    }

    public void heal(double amount) {
        setHealth(health + amount);
    }

    public void interact(Player target) {
        if (target != null) {
            this.state = FakePlayerState.INTERACTING;
            this.lastActionTime = System.currentTimeMillis();
        }
    }

    public void setCombatMode(boolean combatMode) {
        if (combatMode) {
            setState(FakePlayerState.COMBAT);
        } else if (getState() == FakePlayerState.COMBAT) {
            setState(FakePlayerState.IDLE);
        }
    }

    public void setMovementMode(boolean movementMode) {
        if (movementMode) {
            setState(FakePlayerState.MOVING);
        }
    }

    public void setInteractionMode(boolean interactionMode) {
        if (interactionMode) {
            setState(FakePlayerState.INTERACTING);
        }
    }

    @Override
    public Map<String, Object> getBlackboard() {
        return blackboard;
    }

    @Override
    public void setBlackboardValue(String key, Object value) {
        blackboard.put(key, value);
    }

    @Override
    public Object getBlackboardValue(String key) {
        return blackboard.get(key);
    }

    @Override
    public void chat(String message) {
        Logger.getLogger("Minecraft").info("[" + getName() + "]: " + message);
    }

    @Override
    public EmotionSystem getEmotionSystem() {
        return emotionSystem;
    }

    @Override
    public PersonalitySystem getPersonalitySystem() {
        return personalitySystem;
    }

    @Override
    public Entity getTarget() {
        return target;
    }

    @Override
    public void setTarget(Entity target) {
        this.target = target;
    }

    @Override
    public Team getTeam() {
        return team;
    }

    @Override
    public void setTeam(Team team) {
        this.team = team;
    }

    @Override
    public TeamRole getRole() {
        return role;
    }

    @Override
    public void setRole(TeamRole role) {
        this.role = role;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public double getHealth() {
        return health;
    }

    @Override
    public void setHealth(double health) {
        this.health = health;
    }

    @Override
    public FakePlayerState getState() {
        return state;
    }

    @Override
    public String toString() {
        return "FakePlayer{"
                + "id=" + id +
                ", name='" + name + "'" +
                ", health=" + health +
                ", state=" + state +
                ", location=" + location +
                '}';
    }

    public PathFinder getPathfinder() {
        return pathfinder;
    }

    public MovementController getMovementController() {
        return movementController;
    }

    public BehaviorController getBehaviorController() {
        return behaviorController;
    }

    public TargetSelector getTargetSelector() {
        return targetSelector;
    }

    public CombatController getCombatController() {
        return combatController;
    }

    private void updateLastActionTime() {
        this.lastActionTime = System.currentTimeMillis();
    }
}