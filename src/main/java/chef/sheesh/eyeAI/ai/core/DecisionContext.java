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
    // Environment/terrain getters
    // Precomputed environment/terrain flags to avoid Bukkit access off-main
    private final boolean onGround;
    private final double obstacleDensity;
    private final double lightLevel; // 0.0 - 1.0
    private final boolean thundering;
    private final boolean storm;
    private final double terrainDifficulty;
    private final boolean nearWater;
    private final boolean nearLava;
    private final double elevation; // Y coordinate
    private final double coverDensity;

    public DecisionContext(Location currentLocation, double health, List<Entity> nearbyEntities,
                         List<Player> nearbyPlayers, long worldTime, boolean isDayTime,
                         Optional<Entity> currentTarget, double threatLevel,
                         boolean onGround, double obstacleDensity, double lightLevel,
                         boolean thundering, boolean storm, double terrainDifficulty,
                         boolean nearWater, boolean nearLava, double elevation, double coverDensity) {
        this.currentLocation = currentLocation.clone();
        this.health = health;
        this.nearbyEntities = nearbyEntities;
        this.nearbyPlayers = nearbyPlayers;
        this.worldTime = worldTime;
        this.isDayTime = isDayTime;
        this.currentTarget = currentTarget;
        this.threatLevel = threatLevel;
        this.onGround = onGround;
        this.obstacleDensity = obstacleDensity;
        this.lightLevel = lightLevel;
        this.thundering = thundering;
        this.storm = storm;
        this.terrainDifficulty = terrainDifficulty;
        this.nearWater = nearWater;
        this.nearLava = nearLava;
        this.elevation = elevation;
        this.coverDensity = coverDensity;
    }

    // Getters
    public Location getCurrentLocation() {
        return currentLocation.clone();
    }

    /**
     * Check if there are any hostile entities nearby
     */
    public boolean hasHostileNearby() {
        return nearbyEntities.stream()
                .anyMatch(this::isHostile);
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

    public boolean hasStorm() {
        return storm;
    }

    // Explicit getters to avoid Lombok dependency issues in other modules
    public boolean isOnGround() { return onGround; }
    public double getObstacleDensity() { return obstacleDensity; }
    public List<Entity> getNearbyEntities() { return nearbyEntities; }
    public List<Player> getNearbyPlayers() { return nearbyPlayers; }
    public long getWorldTime() { return worldTime; }
    public double getLightLevel() { return lightLevel; }
    public boolean isThundering() { return thundering; }
    public double getTerrainDifficulty() { return terrainDifficulty; }
    public boolean isNearWater() { return nearWater; }
    public boolean isNearLava() { return nearLava; }
    public double getElevation() { return elevation; }
    public double getCoverDensity() { return coverDensity; }
    public Optional<Entity> getCurrentTarget() { return currentTarget; }
    public double getHealth() { return health; }

}

