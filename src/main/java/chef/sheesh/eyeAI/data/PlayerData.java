package chef.sheesh.eyeAI.data;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Player data model containing all player-related information
 */
@Data
@Builder
public class PlayerData {
    
    @NotNull
    private final UUID playerId;
    
    @NotNull
    private String playerName;
    
    // Stats
    private int level;
    private long experience;
    private int kills;
    private int deaths;
    
    // Economy
    private double balance;
    private int tokens;
    
    // Preferences
    private boolean scoreboardEnabled;
    @NotNull
    @Builder.Default
    private String preferredLanguage = "en";
    
    // Timestamps
    private long firstJoin;
    private long lastSeen;
    private long playtime;
    
    /**
     * Calculate K/D ratio
     */
    public double getKDRatio() {
        if (deaths == 0) {
            return kills;
        }
        return (double) kills / deaths;
    }
    
    /**
     * Add experience and handle level ups
     */
    public void addExperience(long amount) {
        this.experience += amount;
        
        // Simple level calculation (1000 XP per level)
        int newLevel = (int) (experience / 1000) + 1;
        if (newLevel > level) {
            this.level = newLevel;
        }
    }
    
    /**
     * Add to balance
     */
    public void addBalance(double amount) {
        this.balance += amount;
    }
    
    /**
     * Remove from balance (returns true if successful)
     */
    public boolean removeBalance(double amount) {
        if (balance >= amount) {
            this.balance -= amount;
            return true;
        }
        return false;
    }
    
    /**
     * Update last seen timestamp
     */
    public void updateLastSeen() {
        this.lastSeen = System.currentTimeMillis();
    }
    
    /**
     * Add playtime in milliseconds
     */
    public void addPlaytime(long milliseconds) {
        this.playtime += milliseconds;
    }
}
