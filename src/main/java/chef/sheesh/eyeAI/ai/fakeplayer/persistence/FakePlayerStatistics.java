package chef.sheesh.eyeAI.ai.fakeplayer.persistence;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Statistics and metadata for fake players
 */
public class FakePlayerStatistics {

    private long totalTicks = 0;
    private long distanceTraveled = 0;
    private int entitiesAttacked = 0;
    private int damageDealt = 0;
    private int damageTaken = 0;
    private int deaths = 0;
    private long creationTime;
    private LocalDateTime lastActiveTime;
    private long totalPlayTime = 0; // in milliseconds
    private int interactionsWithPlayers = 0;
    private String behaviorTreeType = "default";
    private int pathfindingAttempts = 0;
    private int pathfindingSuccesses = 0;

    public FakePlayerStatistics() {
        this.creationTime = System.currentTimeMillis();
        this.lastActiveTime = LocalDateTime.now();
    }

    public FakePlayerStatistics(long creationTime) {
        this.creationTime = creationTime;
        this.lastActiveTime = LocalDateTime.now();
    }

    public FakePlayerStatistics(UUID id, String name) {
        this.creationTime = System.currentTimeMillis();
        this.lastActiveTime = LocalDateTime.now();
    }

    // Getters and setters
    public long getTotalTicks() {
        return totalTicks;
    }

    public void setTotalTicks(long totalTicks) {
        this.totalTicks = totalTicks;
    }

    public void incrementTicks() {
        this.totalTicks++;
    }

    public long getDistanceTraveled() {
        return distanceTraveled;
    }

    public void setDistanceTraveled(long distanceTraveled) {
        this.distanceTraveled = distanceTraveled;
    }

    public void addDistance(long distance) {
        this.distanceTraveled += distance;
    }

    public int getEntitiesAttacked() {
        return entitiesAttacked;
    }

    public void setEntitiesAttacked(int entitiesAttacked) {
        this.entitiesAttacked = entitiesAttacked;
    }

    public void incrementEntitiesAttacked() {
        this.entitiesAttacked++;
    }

    public int getDamageDealt() {
        return damageDealt;
    }

    public void setDamageDealt(int damageDealt) {
        this.damageDealt = damageDealt;
    }

    public void addDamageDealt(int damage) {
        this.damageDealt += damage;
    }

    public int getDamageTaken() {
        return damageTaken;
    }

    public void setDamageTaken(int damageTaken) {
        this.damageTaken = damageTaken;
    }

    public void addDamageTaken(int damage) {
        this.damageTaken += damage;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public void incrementDeaths() {
        this.deaths++;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public LocalDateTime getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime(LocalDateTime lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    public void updateLastActiveTime() {
        this.lastActiveTime = LocalDateTime.now();
    }

    public long getTotalPlayTime() {
        return totalPlayTime;
    }

    public void setTotalPlayTime(long totalPlayTime) {
        this.totalPlayTime = totalPlayTime;
    }

    public void addPlayTime(long milliseconds) {
        this.totalPlayTime += milliseconds;
    }

    public int getInteractionsWithPlayers() {
        return interactionsWithPlayers;
    }

    public void setInteractionsWithPlayers(int interactionsWithPlayers) {
        this.interactionsWithPlayers = interactionsWithPlayers;
    }

    public void incrementInteractionsWithPlayers() {
        this.interactionsWithPlayers++;
    }

    public String getBehaviorTreeType() {
        return behaviorTreeType;
    }

    public void setBehaviorTreeType(String behaviorTreeType) {
        this.behaviorTreeType = behaviorTreeType;
    }

    public int getPathfindingAttempts() {
        return pathfindingAttempts;
    }

    public void setPathfindingAttempts(int pathfindingAttempts) {
        this.pathfindingAttempts = pathfindingAttempts;
    }

    public void incrementPathfindingAttempts() {
        this.pathfindingAttempts++;
    }

    public int getPathfindingSuccesses() {
        return pathfindingSuccesses;
    }

    public void setPathfindingSuccesses(int pathfindingSuccesses) {
        this.pathfindingSuccesses = pathfindingSuccesses;
    }

    public void incrementPathfindingSuccesses() {
        this.pathfindingSuccesses++;
    }

    public double getPathfindingSuccessRate() {
        return pathfindingAttempts > 0 ? (double) pathfindingSuccesses / pathfindingAttempts : 0.0;
    }

    public double getAverageDamagePerAttack() {
        return entitiesAttacked > 0 ? (double) damageDealt / entitiesAttacked : 0.0;
    }

    public double getAverageDistancePerTick() {
        return totalTicks > 0 ? (double) distanceTraveled / totalTicks : 0.0;
    }
}
