package chef.sheesh.eyeAI.ai.agents;

import chef.sheesh.eyeAI.ai.behavior.IBehaviorTree;
import chef.sheesh.eyeAI.ai.core.emotions.EmotionSystem;
import chef.sheesh.eyeAI.ai.core.personality.Personality;
import chef.sheesh.eyeAI.ai.core.personality.PersonalitySystem;
import chef.sheesh.eyeAI.ai.core.team.Team;
import chef.sheesh.eyeAI.ai.core.team.TeamRole;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerState;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Minimal implementation of IFakePlayer for use in agents when FakePlayerManager is not available
 */
public class MinimalFakePlayer implements IFakePlayer {

    private final UUID id;
    private final String name;
    private Location location;
    private double health;
    private FakePlayerState state;
    private IBehaviorTree behaviorTree;
    private final Map<String, Object> blackboard = new ConcurrentHashMap<>();
    private final EmotionSystem emotionSystem = new EmotionSystem();
    private final PersonalitySystem personalitySystem = new PersonalitySystem(Personality.STRATEGIC);
    private Entity target;
    private Team team;
    private TeamRole role = TeamRole.NONE;

    public MinimalFakePlayer(Location location, String name) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.location = location.clone();
        this.health = 20.0;
        this.state = FakePlayerState.IDLE;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Location getLocation() {
        return location.clone();
    }

    @Override
    public void setLocation(Location location) {
        this.location = location.clone();
    }

    @Override
    public double getHealth() {
        return health;
    }


    @Override
    public void setHealth(double health) {
        this.health = Math.max(0, Math.min(20, health));
        if (this.health <= 0) {
            this.state = FakePlayerState.DEAD;
        }
    }

    @Override
    public FakePlayerState getState() {
        return state;
    }

    @Override
    public void setState(FakePlayerState state) {
        this.state = state;
    }

    @Override
    public void setState(String state) {
        try {
            this.state = FakePlayerState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            this.state = FakePlayerState.IDLE;
        }
    }

    @Override
    public String getStateName() {
        return state.name();
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isAlive() {
        return health > 0;
    }

    @Override
    public boolean isInCombat() {
        return this.state == FakePlayerState.COMBAT || this.state == FakePlayerState.ATTACKING;
    }

    @Override
    public void moveTo(Location location) {
        this.location = location.clone();
        this.state = FakePlayerState.MOVING;
    }

    @Override
    public void attackEntity(Entity target) {
        this.state = FakePlayerState.ATTACKING;
        // Minimal implementation - no actual damage dealt
    }

    @Override
    public void attackNearby() {
        // Minimal implementation - no actual damage dealt
    }

    @Override
    public List<Entity> getNearbyEntities(double x, double y, double z) {
        // Minimal implementation - return empty list
        return new ArrayList<>();
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
    public String toString() {
        return "MinimalFakePlayer{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", health=" + health +
            ", state=" + state +
            ", location=" + location +
            '}';
    }
}
