package chef.sheesh.eyeAI.ai.fakeplayer;

import chef.sheesh.eyeAI.ai.core.emotions.EmotionSystem;
import chef.sheesh.eyeAI.ai.core.personality.PersonalitySystem;
import chef.sheesh.eyeAI.ai.core.team.Team;
import chef.sheesh.eyeAI.ai.core.team.TeamRole;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Interface for fake player functionality
 */
public interface IFakePlayer {
    
    UUID getId();
    
    String getName();
    
    Location getLocation();
    
    void setLocation(Location location);
    
    double getHealth();
    
    void setHealth(double health);
    
    FakePlayerState getState();
    
    void setState(FakePlayerState state);
    
    void setState(String state);
    
    String getStateName();
    
    boolean isInCombat();
    
    boolean isAlive();
    
    List<Entity> getNearbyEntities(double x, double y, double z);
    
    void moveTo(Location location);
    
    void attackEntity(Entity target);
    
    void attackNearby();

    Map<String, Object> getBlackboard();

    void setBlackboardValue(String key, Object value);

    Object getBlackboardValue(String key);

    void chat(String message);

    EmotionSystem getEmotionSystem();

    PersonalitySystem getPersonalitySystem();

    Entity getTarget();

    void setTarget(Entity target);

    Team getTeam();

    void setTeam(Team team);

    TeamRole getRole();

    void setRole(TeamRole role);
}