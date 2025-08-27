package chef.sheesh.eyeAI.infra.cache;

import chef.sheesh.eyeAI.data.model.AIModel;
import chef.sheesh.eyeAI.data.model.PlayerData;
import chef.sheesh.eyeAI.data.model.TrainingData;
import chef.sheesh.eyeAI.infra.config.RedisConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;

import java.time.Duration;
import java.util.UUID;

/**
 * L2 Cache Manager using Redis for distributed caching
 */
public class L2CacheManager {

    private final JavaPlugin plugin;
    private final RedisConfig redisConfig;
    private final ObjectMapper objectMapper;

    public L2CacheManager(JavaPlugin plugin, RedisConfig redisConfig) {
        this.plugin = plugin;
        this.redisConfig = redisConfig;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules(); // Register Java 8 modules
    }

    // Player Data Operations
    public PlayerData getPlayerData(UUID playerId) {
        String key = "player:" + playerId.toString();

        return redisConfig.execute(jedis -> {
            String json = jedis.get(key);
            if (json == null) {
                return null;
            }

            try {
                return objectMapper.readValue(json, PlayerData.class);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to deserialize player data for " + playerId + ": " + e.getMessage());
                jedis.del(key); // Remove corrupted data
                return null;
            }
        });
    }

    public void cachePlayerData(PlayerData playerData) {
        String key = "player:" + playerData.getPlayerId().toString();

        redisConfig.execute(jedis -> {
            try {
                String json = objectMapper.writeValueAsString(playerData);
                jedis.setex(key, 1800, json); // 30 minutes
                return null;
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to serialize player data for " + playerData.getPlayerId() + ": " + e.getMessage());
                return null;
            }
        });
    }

    public void invalidatePlayerData(UUID playerId) {
        String key = "player:" + playerId.toString();
        redisConfig.execute(jedis -> jedis.del(key));
    }

    // AI Model Operations
    public AIModel getAIModel(String modelId) {
        String key = "aimodel:" + modelId;

        return redisConfig.execute(jedis -> {
            String json = jedis.get(key);
            if (json == null) {
                return null;
            }

            try {
                return objectMapper.readValue(json, AIModel.class);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to deserialize AI model " + modelId + ": " + e.getMessage());
                jedis.del(key);
                return null;
            }
        });
    }

    public void cacheAIModel(AIModel aiModel) {
        String key = "aimodel:" + aiModel.getModelId();

        redisConfig.execute(jedis -> {
            try {
                String json = objectMapper.writeValueAsString(aiModel);
                jedis.setex(key, 3600, json); // 1 hour
                return null;
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to serialize AI model " + aiModel.getModelId() + ": " + e.getMessage());
                return null;
            }
        });
    }

    public void invalidateAIModel(String modelId) {
        String key = "aimodel:" + modelId;
        redisConfig.execute(jedis -> jedis.del(key));
    }

    // Training Data Operations
    public TrainingData getTrainingData(String dataId) {
        String key = "training:" + dataId;

        return redisConfig.execute(jedis -> {
            String json = jedis.get(key);
            if (json == null) {
                return null;
            }

            try {
                return objectMapper.readValue(json, TrainingData.class);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to deserialize training data " + dataId + ": " + e.getMessage());
                jedis.del(key);
                return null;
            }
        });
    }

    public void cacheTrainingData(TrainingData trainingData) {
        String key = "training:" + trainingData.getDataId();

        redisConfig.execute(jedis -> {
            try {
                String json = objectMapper.writeValueAsString(trainingData);
                jedis.setex(key, 7200, json); // 2 hours
                return null;
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to serialize training data " + trainingData.getDataId() + ": " + e.getMessage());
                return null;
            }
        });
    }

    public void invalidateTrainingData(String dataId) {
        String key = "training:" + dataId;
        redisConfig.execute(jedis -> jedis.del(key));
    }

    // Session Management
    public void storeSession(String sessionId, Object sessionData) {
        String key = "session:" + sessionId;

        redisConfig.execute(jedis -> {
            try {
                String json = objectMapper.writeValueAsString(sessionData);
                jedis.setex(key, 86400, json); // 24 hours
                return null;
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to serialize session data: " + e.getMessage());
                return null;
            }
        });
    }

    public <T> T getSession(String sessionId, Class<T> sessionType) {
        String key = "session:" + sessionId;

        return redisConfig.execute(jedis -> {
            String json = jedis.get(key);
            if (json == null) {
                return null;
            }

            try {
                return objectMapper.readValue(json, sessionType);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to deserialize session data: " + e.getMessage());
                jedis.del(key);
                return null;
            }
        });
    }

    public void invalidateSession(String sessionId) {
        String key = "session:" + sessionId;
        redisConfig.execute(jedis -> jedis.del(key));
    }

    // Pub/Sub Operations
    public void publish(String channel, String message) {
        redisConfig.execute(jedis -> jedis.publish(channel, message));
    }

    public void publishPlayerUpdate(UUID playerId, PlayerData playerData) {
        String channel = "player_updates";
        String message = playerId.toString();

        // Cache the update first
        cachePlayerData(playerData);

        // Then publish
        publish(channel, message);
    }

    // Cache Management
    public void clearAllCaches() {
        redisConfig.execute(jedis -> {
            jedis.flushDB();
            return null;
        });
        plugin.getLogger().info("All L2 caches cleared");
    }

    public void clearPlayerCache() {
        redisConfig.execute(jedis -> {
            String[] keys = jedis.keys("player:*").toArray(new String[0]);
            if (keys.length > 0) {
                jedis.del(keys);
            }
            return null;
        });
        plugin.getLogger().info("Player L2 cache cleared");
    }

    public void clearAIModelCache() {
        redisConfig.execute(jedis -> {
            String[] keys = jedis.keys("aimodel:*").toArray(new String[0]);
            if (keys.length > 0) {
                jedis.del(keys);
            }
            return null;
        });
        plugin.getLogger().info("AI Model L2 cache cleared");
    }

    public void clearTrainingDataCache() {
        redisConfig.execute(jedis -> {
            String[] keys = jedis.keys("training:*").toArray(new String[0]);
            if (keys.length > 0) {
                jedis.del(keys);
            }
            return null;
        });
        plugin.getLogger().info("Training data L2 cache cleared");
    }

    // Cache Statistics
    public L2CacheStats getCacheStats() {
        return redisConfig.execute(jedis -> {
            long dbSize = jedis.dbSize();
            String info = jedis.info("memory");
            // Parse the info string to extract used_memory
            long usedMemory = 0L;
            if (info != null && !info.isEmpty()) {
                String[] lines = info.split("\r\n|\r|\n");
                for (String line : lines) {
                    if (line.startsWith("used_memory:")) {
                        usedMemory = Long.parseLong(line.substring("used_memory:".length()).trim());
                        break;
                    }
                }
            }
            String clientsInfo = jedis.info("clients");
            int connectedClients = 0;
            if (clientsInfo != null && !clientsInfo.isEmpty()) {
                String[] lines = clientsInfo.split("\r\n|\r|\n");
                for (String line : lines) {
                    if (line.startsWith("connected_clients:")) {
                        connectedClients = Integer.parseInt(line.substring("connected_clients:".length()).trim());
                        break;
                    }
                }
            }

            return new L2CacheStats(dbSize, usedMemory, connectedClients);
        });
    }

    public record L2CacheStats(
        long keyCount,
        long usedMemoryBytes,
        int connectedClients
    ) {}
}
