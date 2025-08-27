package chef.sheesh.eyeAI.ai.core;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

/**
 * Context object containing all information needed for AI decision making.
 * This is passed to behavior trees and decision-making components.
 */
public class DecisionContext {

    private final Location currentLocation;
    private final double health;
    private final List<Entity> nearbyEntities;
    private final List<Player> nearbyPlayers;
    private final long worldTime;
    private final boolean isDayTime;
    private final Optional<Entity> currentTarget;
    private final double threatLevel;

    public DecisionContext(Location currentLocation, double health, List<Entity> nearbyEntities,
                         List<Player> nearbyPlayers, long worldTime, boolean isDayTime,
                         Optional<Entity> currentTarget, double threatLevel) {
        this.currentLocation = currentLocation.clone();
        this.health = health;
        this.nearbyEntities = nearbyEntities;
        this.nearbyPlayers = nearbyPlayers;
        this.worldTime = worldTime;
        this.isDayTime = isDayTime;
        this.currentTarget = currentTarget;
        this.threatLevel = threatLevel;
    }

    // Getters
    public Location getCurrentLocation() {
        return currentLocation.clone();
    }

    public double getHealth() {
        return health;
    }

    public List<Entity> getNearbyEntities() {
        return nearbyEntities;
    }

    public List<Player> getNearbyPlayers() {
        return nearbyPlayers;
    }

    public long getWorldTime() {
        return worldTime;
    }

    public boolean isDayTime() {
        return isDayTime;
    }

    public Optional<Entity> getCurrentTarget() {
        return currentTarget;
    }

    public double getThreatLevel() {
        return threatLevel;
    }

    /**
     * Check if there are any hostile entities nearby
     */
    public boolean hasHostileNearby() {
        return nearbyEntities.stream()
                .anyMatch(entity -> isHostile(entity));
    }

    /**
     * Check if there are any players nearby
     */
    public boolean hasPlayersNearby() {
        return !nearbyPlayers.isEmpty();
    }

    /**
     * Check if health is low (below 30%)
     */
    public boolean isHealthLow() {
        return health < 6.0; // 30% of 20 health
    }

    /**
     * Check if health is critical (below 15%)
     */
    public boolean isHealthCritical() {
        return health < 3.0; // 15% of 20 health
    }

    /**
     * Find the closest entity
     */
    public Optional<Entity> getClosestEntity() {
        return nearbyEntities.stream()
                .min((e1, e2) -> Double.compare(
                    currentLocation.distance(e1.getLocation()),
                    currentLocation.distance(e2.getLocation())
                ));
    }

    /**
     * Find the closest player
     */
    public Optional<Player> getClosestPlayer() {
        return nearbyPlayers.stream()
                .min((p1, p2) -> Double.compare(
                    currentLocation.distance(p1.getLocation()),
                    currentLocation.distance(p2.getLocation())
                ));
    }

    private boolean isHostile(Entity entity) {
        // Simple check for hostile mobs - can be expanded
        return entity.getType().name().contains("ZOMBIE") ||
               entity.getType().name().contains("SKELETON") ||
               entity.getType().name().contains("CREEPER") ||
               entity.getType().name().contains("SPIDER");
    }
}

