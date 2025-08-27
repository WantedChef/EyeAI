package chef.sheesh.eyeAI.data.service;

import chef.sheesh.eyeAI.data.model.PlayerData;
import chef.sheesh.eyeAI.data.model.TransactionRecord;
import chef.sheesh.eyeAI.infra.cache.L1CacheManager;
import chef.sheesh.eyeAI.infra.cache.L2CacheManager;
import chef.sheesh.eyeAI.infra.config.DatabaseConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.plugin.java.JavaPlugin;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing player data with multi-level caching
 */
public class PlayerDataService {

    private final JavaPlugin plugin;
    private final L1CacheManager l1Cache;
    private final L2CacheManager l2Cache;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public PlayerDataService(JavaPlugin plugin, L1CacheManager l1Cache, L2CacheManager l2Cache,
                           DatabaseConfig databaseConfig) {
        this.plugin = plugin;
        this.l1Cache = l1Cache;
        this.l2Cache = l2Cache;
        // Note: Using DataStore interface instead of JdbcTemplate
        // This would need to be adapted to work with DataStore methods
        this.jdbcTemplate = null; // Placeholder - would need DataStore-based implementation
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }

    /**
     * Get player data with caching strategy
     */
    public CompletableFuture<PlayerData> getPlayerData(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            String playerName = "Unknown"; // In real implementation, get from UUID

            // Try L1 cache first
            PlayerData cachedData = l1Cache.getPlayerData(playerId);
            if (cachedData != null) {
                return cachedData;
            }

            // Try L2 cache
            cachedData = l2Cache.getPlayerData(playerId);
            if (cachedData != null) {
                // Update L1 cache
                l1Cache.cachePlayerData(cachedData);
                return cachedData;
            }

            // Load from database
            PlayerData dbData = loadFromDatabase(playerId);
            if (dbData != null) {
                // Update caches
                l1Cache.cachePlayerData(dbData);
                l2Cache.cachePlayerData(dbData);
                return dbData;
            }

            // Create new player data if not found
            PlayerData newPlayerData = PlayerData.createDefault(playerId, playerName);
            savePlayerData(newPlayerData); // This will also update caches
            return newPlayerData;
        });
    }

    /**
     * Save player data with caching
     */
    public CompletableFuture<Void> savePlayerData(PlayerData playerData) {
        return CompletableFuture.runAsync(() -> {
            try {
                // Update caches first (write-through)
                l1Cache.cachePlayerData(playerData);
                l2Cache.cachePlayerData(playerData);

                // Save to database asynchronously
                saveToDatabase(playerData);

            } catch (Exception e) {
                // Log the full stack trace for better debugging
                plugin.getLogger().severe("Failed to save player data for " + playerData.getPlayerId() + ": " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to save player data", e);
            }
        });
    }

    /**
     * Update player stats
     */
    public CompletableFuture<Void> updatePlayerStats(UUID playerId, boolean won, int kills, int deaths, int assists) {
        return getPlayerData(playerId).thenCompose(playerData -> {
            playerData.updateMatchStats(won, kills, deaths, assists);
            return savePlayerData(playerData);
        });
    }

    /**
     * Add currency to player
     */
    public CompletableFuture<Boolean> addCurrency(UUID playerId, String currencyType, double amount) {
        return getPlayerData(playerId).thenCompose(playerData -> {
            playerData.addCurrency(currencyType, amount);
            return savePlayerData(playerData).thenApply(v -> true);
        });
    }

    /**
     * Remove currency from player
     */
    public CompletableFuture<Boolean> removeCurrency(UUID playerId, String currencyType, double amount) {
        return getPlayerData(playerId).thenCompose(playerData -> {
            boolean success = playerData.removeCurrency(currencyType, amount);
            if (success) {
                return savePlayerData(playerData).thenApply(v -> true);
            }
            return CompletableFuture.completedFuture(false);
        });
    }

    /**
     * Get player currency balance
     */
    public CompletableFuture<Double> getCurrencyBalance(UUID playerId, String currencyType) {
        return getPlayerData(playerId).thenApply(playerData ->
            playerData.getCurrencyBalance(currencyType));
    }

    /**
     * Add transaction record
     */
    public CompletableFuture<Void> addTransaction(UUID playerId, TransactionRecord transaction) {
        return getPlayerData(playerId).thenCompose(playerData -> {
            playerData.getTransactionHistory().add(transaction);
            // Keep only last 100 transactions
            if (playerData.getTransactionHistory().size() > 100) {
                playerData.getTransactionHistory().remove(0);
            }
            return savePlayerData(playerData);
        });
    }

    /**
     * Update player settings
     */
    public CompletableFuture<Void> updatePlayerSettings(UUID playerId, String key, Object value) {
        return getPlayerData(playerId).thenCompose(playerData -> {
            try {
                JsonNode settings = playerData.getCustomSettings();
                if (settings == null) {
                    settings = objectMapper.createObjectNode();
                }

                if (settings instanceof com.fasterxml.jackson.databind.node.ObjectNode) {
                    ((com.fasterxml.jackson.databind.node.ObjectNode) settings).put(key, value.toString());
                }

                playerData.setCustomSettings(settings);
                return savePlayerData(playerData);

            } catch (Exception e) {
                plugin.getLogger().warning("Failed to update player settings: " + e.getMessage());
                return CompletableFuture.completedFuture(null);
            }
        });
    }

    /**
     * Invalidate all caches for a player
     */
    public void invalidatePlayerCaches(UUID playerId) {
        l1Cache.invalidatePlayerData(playerId);
        l2Cache.invalidatePlayerData(playerId);
    }

    /**
     * Load player data from database
     */
    private PlayerData loadFromDatabase(UUID playerId) {
        String sql = """
            SELECT p.*, ps.custom_settings, ps.currency_balances, ps.command_usage,
                   ps.feature_usage_time, ps.security_flags,
                   soc.friends, soc.blocked_players, soc.status_message, soc.online_status
            FROM player_data p
            LEFT JOIN player_settings ps ON p.player_id = ps.player_id
            LEFT JOIN player_social soc ON p.player_id = soc.player_id
            WHERE p.player_id = ?
            """;

        List<PlayerData> results = jdbcTemplate.query(sql, new PlayerDataRowMapper(), playerId.toString());

        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Save player data to database
     */
    private void saveToDatabase(PlayerData playerData) {
        // Update main player data
        String sql = """
            INSERT INTO player_data (
                player_id, player_name, level, experience, kills, deaths, kdr, assists,
                wins, losses, balance, tokens, scoreboard_enabled, preferred_language,
                first_join, last_seen, playtime, afk_time, average_session_length,
                sessions_count, last_ip_address, failed_login_attempts, last_failed_login
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                player_name = VALUES(player_name),
                level = VALUES(level),
                experience = VALUES(experience),
                kills = VALUES(kills),
                deaths = VALUES(deaths),
                kdr = VALUES(kdr),
                assists = VALUES(assists),
                wins = VALUES(wins),
                losses = VALUES(losses),
                balance = VALUES(balance),
                tokens = VALUES(tokens),
                scoreboard_enabled = VALUES(scoreboard_enabled),
                preferred_language = VALUES(preferred_language),
                last_seen = VALUES(last_seen),
                playtime = VALUES(playtime),
                afk_time = VALUES(afk_time),
                average_session_length = VALUES(average_session_length),
                sessions_count = VALUES(sessions_count),
                last_ip_address = VALUES(last_ip_address),
                failed_login_attempts = VALUES(failed_login_attempts),
                last_failed_login = VALUES(last_failed_login)
            """;

        jdbcTemplate.update(sql,
            playerData.getPlayerId().toString(),
            playerData.getPlayerName(),
            playerData.getLevel(),
            playerData.getExperience(),
            playerData.getKills(),
            playerData.getDeaths(),
            playerData.getKdr(),
            playerData.getAssists(),
            playerData.getWins(),
            playerData.getLosses(),
            playerData.getBalance(),
            playerData.getTokens(),
            playerData.isScoreboardEnabled(),
            playerData.getPreferredLanguage(),
            playerData.getFirstJoin(),
            playerData.getLastSeen(),
            playerData.getPlaytime(),
            playerData.getAfkTime(),
            playerData.getAverageSessionLength(),
            playerData.getSessionsCount(),
            playerData.getLastIpAddress(),
            playerData.getFailedLoginAttempts(),
            playerData.getLastFailedLogin()
        );

        // Update settings, social data, etc. (similar pattern)
        updatePlayerSettings(playerData);
        updatePlayerSocialData(playerData);
    }

    private void updatePlayerSettings(PlayerData playerData) {
        if (playerData.getCustomSettings() != null || playerData.getCurrencyBalances() != null) {
            String sql = """
                INSERT INTO player_settings (
                    player_id, custom_settings, currency_balances, command_usage,
                    feature_usage_time, security_flags
                ) VALUES (?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    custom_settings = VALUES(custom_settings),
                    currency_balances = VALUES(currency_balances),
                    command_usage = VALUES(command_usage),
                    feature_usage_time = VALUES(feature_usage_time),
                    security_flags = VALUES(security_flags)
                """;

            try {
                jdbcTemplate.update(sql,
                    playerData.getPlayerId().toString(),
                    playerData.getCustomSettings() != null ? playerData.getCustomSettings().toString() : null,
                    playerData.getCurrencyBalances() != null ? objectMapper.writeValueAsString(playerData.getCurrencyBalances()) : null,
                    playerData.getCommandUsage() != null ? objectMapper.writeValueAsString(playerData.getCommandUsage()) : null,
                    playerData.getFeatureUsageTime() != null ? objectMapper.writeValueAsString(playerData.getFeatureUsageTime()) : null,
                    playerData.getSecurityFlags() != null ? objectMapper.writeValueAsString(playerData.getSecurityFlags()) : null
                );
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to update player settings: " + e.getMessage());
            }
        }
    }

    private void updatePlayerSocialData(PlayerData playerData) {
        if (playerData.getFriends() != null || playerData.getBlockedPlayers() != null) {
            String sql = """
                INSERT INTO player_social (
                    player_id, friends, blocked_players, status_message, online_status
                ) VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    friends = VALUES(friends),
                    blocked_players = VALUES(blocked_players),
                    status_message = VALUES(status_message),
                    online_status = VALUES(online_status)
                """;

            try {
                jdbcTemplate.update(sql,
                    playerData.getPlayerId().toString(),
                    playerData.getFriends() != null ? objectMapper.writeValueAsString(playerData.getFriends()) : null,
                    playerData.getBlockedPlayers() != null ? objectMapper.writeValueAsString(playerData.getBlockedPlayers()) : null,
                    playerData.getStatusMessage(),
                    playerData.isOnlineStatus()
                );
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to update player social data: " + e.getMessage());
            }
        }
    }

    /**
     * Row mapper for player data
     */
    private class PlayerDataRowMapper implements RowMapper<PlayerData> {
        @Override
        public PlayerData mapRow(ResultSet rs, int rowNum) throws SQLException {
            UUID playerId = UUID.fromString(rs.getString("player_id"));

            PlayerData.PlayerDataBuilder builder = PlayerData.builder()
                .playerId(playerId)
                .playerName(rs.getString("player_name"))
                .level(rs.getInt("level"))
                .experience(rs.getLong("experience"))
                .kills(rs.getInt("kills"))
                .deaths(rs.getInt("deaths"))
                .kdr(rs.getDouble("kdr"))
                .assists(rs.getInt("assists"))
                .wins(rs.getInt("wins"))
                .losses(rs.getInt("losses"))
                .balance(rs.getDouble("balance"))
                .tokens(rs.getInt("tokens"))
                .scoreboardEnabled(rs.getBoolean("scoreboard_enabled"))
                .preferredLanguage(rs.getString("preferred_language"))
                .firstJoin(rs.getLong("first_join"))
                .lastSeen(rs.getLong("last_seen"))
                .playtime(rs.getLong("playtime"))
                .afkTime(rs.getLong("afk_time"))
                .averageSessionLength(rs.getDouble("average_session_length"))
                .sessionsCount(rs.getInt("sessions_count"))
                .lastIpAddress(rs.getString("last_ip_address"))
                .failedLoginAttempts(rs.getInt("failed_login_attempts"))
                .lastFailedLogin(rs.getLong("last_failed_login"))
                .onlineStatus(rs.getBoolean("online_status"))
                .statusMessage(rs.getString("status_message"));

            try {
                // Parse JSON fields
                String customSettings = rs.getString("custom_settings");
                if (customSettings != null) {
                    builder.customSettings(objectMapper.readTree(customSettings));
                }

                String currencyBalances = rs.getString("currency_balances");
                if (currencyBalances != null) {
                    builder.currencyBalances(objectMapper.readValue(currencyBalances, Map.class));
                }

                String friends = rs.getString("friends");
                if (friends != null) {
                    builder.friends(objectMapper.readValue(friends, List.class));
                }

                String blockedPlayers = rs.getString("blocked_players");
                if (blockedPlayers != null) {
                    builder.blockedPlayers(objectMapper.readValue(blockedPlayers, List.class));
                }

                String commandUsage = rs.getString("command_usage");
                if (commandUsage != null) {
                    builder.commandUsage(objectMapper.readValue(commandUsage, Map.class));
                }

                String featureUsageTime = rs.getString("feature_usage_time");
                if (featureUsageTime != null) {
                    builder.featureUsageTime(objectMapper.readValue(featureUsageTime, Map.class));
                }

                String securityFlags = rs.getString("security_flags");
                if (securityFlags != null) {
                    builder.securityFlags(objectMapper.readValue(securityFlags, Map.class));
                }

            } catch (Exception e) {
                plugin.getLogger().warning("Failed to parse JSON fields for player " + playerId + ": " + e.getMessage());
            }

            return builder.build();
        }
    }
}
