package chef.sheesh.eyeAI.data.model;

import com.fasterxml.jackson.databind.JsonNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Enhanced player data model with comprehensive stats and settings
 */
public class PlayerData {
    @NotNull private final UUID playerId;
    @NotNull private String playerName;

    // Enhanced Stats
    private int level;
    private long experience;
    private int kills;
    private int deaths;
    private double kdr;
    private int assists;
    private int wins;
    private int losses;
    private double winRate;

    // Advanced Economy
    private double balance;
    private int tokens;
    private Map<String, Double> currencyBalances;
    private List<TransactionRecord> transactionHistory;

    // Preferences & Settings
    private boolean scoreboardEnabled;
    private String preferredLanguage;
    private JsonNode customSettings;
    private List<String> unlockedFeatures;

    // Social Features
    private List<UUID> friends;
    private List<UUID> blockedPlayers;
    private String statusMessage;
    private boolean onlineStatus;

    // Enhanced Timestamps
    private long firstJoin;
    private long lastSeen;
    private long playtime;
    private long afkTime;
    private Map<String, Long> featureUsageTime;

    // Performance Metrics
    private double averageSessionLength;
    private int sessionsCount;
    private Map<String, Integer> commandUsage;

    // Security & Anti-Cheat
    private String lastIpAddress;
    private List<String> knownIpAddresses;
    private int failedLoginAttempts;
    private long lastFailedLogin;
    private Map<String, Object> securityFlags;

    // Constructor for backwards compatibility
    public PlayerData(@NotNull UUID playerId, @NotNull String playerName, int level, long experience,
                     int kills, int deaths, double balance, int tokens, boolean scoreboardEnabled,
                     String preferredLanguage, long firstJoin, long lastSeen, long playtime) {
        this.playerId = playerId;
        this.playerName = playerName != null ? playerName : "Unknown";
        this.level = level;
        this.experience = experience;
        this.kills = kills;
        this.deaths = deaths;
        this.kdr = deaths > 0 ? (double) kills / deaths : kills;
        this.balance = balance;
        this.tokens = tokens;
        this.scoreboardEnabled = scoreboardEnabled;
        this.preferredLanguage = preferredLanguage != null ? preferredLanguage : "en";
        this.firstJoin = firstJoin > 0 ? firstJoin : System.currentTimeMillis();
        this.lastSeen = lastSeen > 0 ? lastSeen : System.currentTimeMillis();
        this.playtime = Math.max(0, playtime);
        this.onlineStatus = true;
    }

    // Default constructor for builder pattern
    public PlayerData() {
        // Initialize with default values
        this.playerId = UUID.randomUUID();
        this.playerName = "Unknown";
        this.level = 1;
        this.experience = 0L;
        this.kills = 0;
        this.deaths = 0;
        this.kdr = 0.0;
        this.assists = 0;
        this.wins = 0;
        this.losses = 0;
        this.winRate = 0.0;
        this.balance = 0.0;
        this.tokens = 0;
        this.scoreboardEnabled = true;
        this.preferredLanguage = "en";
        this.onlineStatus = false;
        this.firstJoin = System.currentTimeMillis();
        this.lastSeen = System.currentTimeMillis();
        this.playtime = 0L;
        this.afkTime = 0L;
        this.averageSessionLength = 0.0;
        this.sessionsCount = 0;
        this.failedLoginAttempts = 0;
        this.lastFailedLogin = 0L;
    }

    public PlayerData(@NotNull UUID playerId) {
        this.playerId = playerId;
    }

    // Builder pattern implementation
    public static class PlayerDataBuilder {
        private UUID playerId;
        private String playerName;
        private int level = 1;
        private long experience = 0L;
        private int kills = 0;
        private int deaths = 0;
        private double kdr = 0.0;
        private int assists = 0;
        private int wins = 0;
        private int losses = 0;
        private double winRate = 0.0;
        private double balance = 0.0;
        private int tokens = 0;
        private Map<String, Double> currencyBalances = Map.of();
        private List<TransactionRecord> transactionHistory = List.of();
        private boolean scoreboardEnabled = true;
        private String preferredLanguage = "en";
        private JsonNode customSettings;
        private List<String> unlockedFeatures = List.of();
        private List<UUID> friends = List.of();
        private List<UUID> blockedPlayers = List.of();
        private String statusMessage = "";
        private boolean onlineStatus = true;
        private long firstJoin = System.currentTimeMillis();
        private long lastSeen = System.currentTimeMillis();
        private long playtime = 0L;
        private long afkTime = 0L;
        private Map<String, Long> featureUsageTime = Map.of();
        private double averageSessionLength = 0.0;
        private int sessionsCount = 0;
        private Map<String, Integer> commandUsage = Map.of();
        private String lastIpAddress = "";
        private List<String> knownIpAddresses = List.of();
        private int failedLoginAttempts = 0;
        private long lastFailedLogin = 0L;
        private Map<String, Object> securityFlags = Map.of();

        public PlayerDataBuilder playerId(UUID playerId) {
            this.playerId = playerId;
            return this;
        }

        public PlayerDataBuilder playerName(String playerName) {
            this.playerName = playerName;
            return this;
        }

        public PlayerDataBuilder level(int level) {
            this.level = level;
            return this;
        }

        public PlayerDataBuilder experience(long experience) {
            this.experience = experience;
            return this;
        }

        public PlayerDataBuilder kills(int kills) {
            this.kills = kills;
            return this;
        }

        public PlayerDataBuilder deaths(int deaths) {
            this.deaths = deaths;
            return this;
        }

        public PlayerDataBuilder kdr(double kdr) {
            this.kdr = kdr;
            return this;
        }

        public PlayerDataBuilder assists(int assists) {
            this.assists = assists;
            return this;
        }

        public PlayerDataBuilder wins(int wins) {
            this.wins = wins;
            return this;
        }

        public PlayerDataBuilder losses(int losses) {
            this.losses = losses;
            return this;
        }

        public PlayerDataBuilder winRate(double winRate) {
            this.winRate = winRate;
            return this;
        }

        public PlayerDataBuilder balance(double balance) {
            this.balance = balance;
            return this;
        }

        public PlayerDataBuilder tokens(int tokens) {
            this.tokens = tokens;
            return this;
        }

        public PlayerDataBuilder currencyBalances(Map<String, Double> currencyBalances) {
            this.currencyBalances = currencyBalances;
            return this;
        }

        public PlayerDataBuilder transactionHistory(List<TransactionRecord> transactionHistory) {
            this.transactionHistory = transactionHistory;
            return this;
        }

        public PlayerDataBuilder scoreboardEnabled(boolean scoreboardEnabled) {
            this.scoreboardEnabled = scoreboardEnabled;
            return this;
        }

        public PlayerDataBuilder preferredLanguage(String preferredLanguage) {
            this.preferredLanguage = preferredLanguage;
            return this;
        }

        public PlayerDataBuilder customSettings(JsonNode customSettings) {
            this.customSettings = customSettings;
            return this;
        }

        public PlayerDataBuilder unlockedFeatures(List<String> unlockedFeatures) {
            this.unlockedFeatures = unlockedFeatures;
            return this;
        }

        public PlayerDataBuilder friends(List<UUID> friends) {
            this.friends = friends;
            return this;
        }

        public PlayerDataBuilder blockedPlayers(List<UUID> blockedPlayers) {
            this.blockedPlayers = blockedPlayers;
            return this;
        }

        public PlayerDataBuilder statusMessage(String statusMessage) {
            this.statusMessage = statusMessage;
            return this;
        }

        public PlayerDataBuilder onlineStatus(boolean onlineStatus) {
            this.onlineStatus = onlineStatus;
            return this;
        }

        public PlayerDataBuilder firstJoin(long firstJoin) {
            this.firstJoin = firstJoin;
            return this;
        }

        public PlayerDataBuilder lastSeen(long lastSeen) {
            this.lastSeen = lastSeen;
            return this;
        }

        public PlayerDataBuilder playtime(long playtime) {
            this.playtime = playtime;
            return this;
        }

        public PlayerDataBuilder afkTime(long afkTime) {
            this.afkTime = afkTime;
            return this;
        }

        public PlayerDataBuilder featureUsageTime(Map<String, Long> featureUsageTime) {
            this.featureUsageTime = featureUsageTime;
            return this;
        }

        public PlayerDataBuilder averageSessionLength(double averageSessionLength) {
            this.averageSessionLength = averageSessionLength;
            return this;
        }

        public PlayerDataBuilder sessionsCount(int sessionsCount) {
            this.sessionsCount = sessionsCount;
            return this;
        }

        public PlayerDataBuilder commandUsage(Map<String, Integer> commandUsage) {
            this.commandUsage = commandUsage;
            return this;
        }

        public PlayerDataBuilder lastIpAddress(String lastIpAddress) {
            this.lastIpAddress = lastIpAddress;
            return this;
        }

        public PlayerDataBuilder knownIpAddresses(List<String> knownIpAddresses) {
            this.knownIpAddresses = knownIpAddresses;
            return this;
        }

        public PlayerDataBuilder failedLoginAttempts(int failedLoginAttempts) {
            this.failedLoginAttempts = failedLoginAttempts;
            return this;
        }

        public PlayerDataBuilder lastFailedLogin(long lastFailedLogin) {
            this.lastFailedLogin = lastFailedLogin;
            return this;
        }

        public PlayerDataBuilder securityFlags(Map<String, Object> securityFlags) {
            this.securityFlags = securityFlags;
            return this;
        }

        public PlayerData build() {
            PlayerData playerData = new PlayerData(playerId);
            playerData.playerName = this.playerName;
            playerData.level = this.level;
            playerData.experience = this.experience;
            playerData.kills = this.kills;
            playerData.deaths = this.deaths;
            playerData.kdr = this.kdr;
            playerData.assists = this.assists;
            playerData.wins = this.wins;
            playerData.losses = this.losses;
            playerData.winRate = this.winRate;
            playerData.balance = this.balance;
            playerData.tokens = this.tokens;
            playerData.currencyBalances = this.currencyBalances;
            playerData.transactionHistory = this.transactionHistory;
            playerData.scoreboardEnabled = this.scoreboardEnabled;
            playerData.preferredLanguage = this.preferredLanguage;
            playerData.customSettings = this.customSettings;
            playerData.unlockedFeatures = this.unlockedFeatures;
            playerData.friends = this.friends;
            playerData.blockedPlayers = this.blockedPlayers;
            playerData.statusMessage = this.statusMessage;
            playerData.onlineStatus = this.onlineStatus;
            playerData.firstJoin = this.firstJoin;
            playerData.lastSeen = this.lastSeen;
            playerData.playtime = this.playtime;
            playerData.afkTime = this.afkTime;
            playerData.featureUsageTime = this.featureUsageTime;
            playerData.averageSessionLength = this.averageSessionLength;
            playerData.sessionsCount = this.sessionsCount;
            playerData.commandUsage = this.commandUsage;
            playerData.lastIpAddress = this.lastIpAddress;
            playerData.knownIpAddresses = this.knownIpAddresses;
            playerData.failedLoginAttempts = this.failedLoginAttempts;
            playerData.lastFailedLogin = this.lastFailedLogin;
            playerData.securityFlags = this.securityFlags;
            return playerData;
        }
    }

    public static PlayerDataBuilder builder() {
        return new PlayerDataBuilder();
    }

    // Getters and Setters
    public UUID getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getExperience() {
        return experience;
    }

    public void setExperience(long experience) {
        this.experience = experience;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public double getKdr() {
        return kdr;
    }

    public double getKDRatio() {
        return kdr;
    }

    public void setKdr(double kdr) {
        this.kdr = kdr;
    }

    public int getAssists() {
        return assists;
    }

    public void setAssists(int assists) {
        this.assists = assists;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public double getWinRate() {
        return winRate;
    }

    public void setWinRate(double winRate) {
        this.winRate = winRate;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public int getTokens() {
        return tokens;
    }

    public void setTokens(int tokens) {
        this.tokens = tokens;
    }

    public Map<String, Double> getCurrencyBalances() {
        return currencyBalances;
    }

    public void setCurrencyBalances(Map<String, Double> currencyBalances) {
        this.currencyBalances = currencyBalances;
    }

    public List<TransactionRecord> getTransactionHistory() {
        return transactionHistory;
    }

    public void setTransactionHistory(List<TransactionRecord> transactionHistory) {
        this.transactionHistory = transactionHistory;
    }

    public boolean isScoreboardEnabled() {
        return scoreboardEnabled;
    }

    public void setScoreboardEnabled(boolean scoreboardEnabled) {
        this.scoreboardEnabled = scoreboardEnabled;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public JsonNode getCustomSettings() {
        return customSettings;
    }

    public void setCustomSettings(JsonNode customSettings) {
        this.customSettings = customSettings;
    }

    public List<String> getUnlockedFeatures() {
        return unlockedFeatures;
    }

    public void setUnlockedFeatures(List<String> unlockedFeatures) {
        this.unlockedFeatures = unlockedFeatures;
    }

    public List<UUID> getFriends() {
        return friends;
    }

    public void setFriends(List<UUID> friends) {
        this.friends = friends;
    }

    public List<UUID> getBlockedPlayers() {
        return blockedPlayers;
    }

    public void setBlockedPlayers(List<UUID> blockedPlayers) {
        this.blockedPlayers = blockedPlayers;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public boolean isOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(boolean onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public long getFirstJoin() {
        return firstJoin;
    }

    public void setFirstJoin(long firstJoin) {
        this.firstJoin = firstJoin;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public long getPlaytime() {
        return playtime;
    }

    public void setPlaytime(long playtime) {
        this.playtime = playtime;
    }

    public long getAfkTime() {
        return afkTime;
    }

    public void setAfkTime(long afkTime) {
        this.afkTime = afkTime;
    }

    public Map<String, Long> getFeatureUsageTime() {
        return featureUsageTime;
    }

    public void setFeatureUsageTime(Map<String, Long> featureUsageTime) {
        this.featureUsageTime = featureUsageTime;
    }

    public double getAverageSessionLength() {
        return averageSessionLength;
    }

    public void setAverageSessionLength(double averageSessionLength) {
        this.averageSessionLength = averageSessionLength;
    }

    public int getSessionsCount() {
        return sessionsCount;
    }

    public void setSessionsCount(int sessionsCount) {
        this.sessionsCount = sessionsCount;
    }

    public Map<String, Integer> getCommandUsage() {
        return commandUsage;
    }

    public void setCommandUsage(Map<String, Integer> commandUsage) {
        this.commandUsage = commandUsage;
    }

    public String getLastIpAddress() {
        return lastIpAddress;
    }

    public void setLastIpAddress(String lastIpAddress) {
        this.lastIpAddress = lastIpAddress;
    }

    public List<String> getKnownIpAddresses() {
        return knownIpAddresses;
    }

    public void setKnownIpAddresses(List<String> knownIpAddresses) {
        this.knownIpAddresses = knownIpAddresses;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public long getLastFailedLogin() {
        return lastFailedLogin;
    }

    public void setLastFailedLogin(long lastFailedLogin) {
        this.lastFailedLogin = lastFailedLogin;
    }

    public Map<String, Object> getSecurityFlags() {
        return securityFlags;
    }

    public void setSecurityFlags(Map<String, Object> securityFlags) {
        this.securityFlags = securityFlags;
    }

    /**
     * Create default player data with all enhanced fields initialized
     */
    public static PlayerData createDefault(UUID playerId, String playerName) {
        return PlayerData.builder()
            .playerId(playerId)
            .playerName(playerName != null ? playerName : "Unknown")
            .level(1)
            .experience(0L)
            .kills(0)
            .deaths(0)
            .kdr(0.0)
            .assists(0)
            .wins(0)
            .losses(0)
            .winRate(0.0)
            .balance(0.0)
            .tokens(0)
            .currencyBalances(Map.of())
            .transactionHistory(List.of())
            .scoreboardEnabled(true)
            .preferredLanguage("en")
            .customSettings(null)
            .unlockedFeatures(List.of())
            .friends(List.of())
            .blockedPlayers(List.of())
            .statusMessage("")
            .onlineStatus(true)
            .firstJoin(System.currentTimeMillis())
            .lastSeen(System.currentTimeMillis())
            .playtime(0L)
            .afkTime(0L)
            .featureUsageTime(Map.of())
            .averageSessionLength(0.0)
            .sessionsCount(0)
            .commandUsage(Map.of())
            .lastIpAddress("")
            .knownIpAddresses(List.of())
            .failedLoginAttempts(0)
            .lastFailedLogin(0L)
            .securityFlags(Map.of())
            .build();
    }

    /**
     * Calculate KDR (Kill/Death Ratio)
     */
    public double calculateKDR() {
        return deaths > 0 ? (double) kills / deaths : kills;
    }

    /**
     * Calculate win rate
     */
    public double calculateWinRate() {
        int totalGames = wins + losses;
        return totalGames > 0 ? (double) wins / totalGames : 0.0;
    }

    /**
     * Add currency to player's balance
     */
    public void addCurrency(String currencyType, double amount) {
        if (currencyBalances == null) {
            currencyBalances = new java.util.HashMap<>();
        }
        currencyBalances.put(currencyType, currencyBalances.getOrDefault(currencyType, 0.0) + amount);
    }

    /**
     * Remove currency from player's balance
     */
    public boolean removeCurrency(String currencyType, double amount) {
        if (currencyBalances == null) {
            return false;
        }

        double current = currencyBalances.getOrDefault(currencyType, 0.0);
        if (current < amount) {
            return false;
        }

        currencyBalances.put(currencyType, current - amount);
        return true;
    }

    /**
     * Get currency balance
     */
    public double getCurrencyBalance(String currencyType) {
        return currencyBalances != null ? currencyBalances.getOrDefault(currencyType, 0.0) : 0.0;
    }

    /**
     * Update player stats after a match
     */
    public void updateMatchStats(boolean won, int matchKills, int matchDeaths, int matchAssists) {
        if (won) {
            wins++;
        } else {
            losses++;
        }

        kills += matchKills;
        deaths += matchDeaths;
        assists += matchAssists;
        kdr = calculateKDR();
        winRate = calculateWinRate();
    }
}
