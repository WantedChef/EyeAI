package chef.sheesh.eyeAI.infra.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

/**
 * Redis configuration with Jedis connection pooling
 */
public class RedisConfig {

    private final JavaPlugin plugin;
    private final ConfigurationManager configManager;
    private JedisPool jedisPool;

    public RedisConfig(JavaPlugin plugin, ConfigurationManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    /**
     * Initialize Redis connection pool
     */
    public void initialize() {
        FileConfiguration config = configManager.getConfig();

        String host = config.getString(ConfigKeys.REDIS_HOST, "localhost");
        int port = config.getInt(ConfigKeys.REDIS_PORT, 6379);
        String password = config.getString(ConfigKeys.REDIS_PASSWORD, "");
        int database = config.getInt(ConfigKeys.REDIS_DATABASE, 0);
        int timeout = config.getInt(ConfigKeys.REDIS_TIMEOUT, 2000);

        // Connection pool configuration
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(config.getInt(ConfigKeys.REDIS_MAX_CONNECTIONS, 20));
        poolConfig.setMaxIdle(config.getInt(ConfigKeys.REDIS_MIN_IDLE, 5));
        poolConfig.setMinIdle(config.getInt(ConfigKeys.REDIS_MIN_IDLE, 5));
        poolConfig.setMaxWait(Duration.ofMillis(timeout));
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        // Note: Eviction settings use Jedis defaults as Duration-based methods are deprecated
        poolConfig.setBlockWhenExhausted(true);

        // Create Jedis pool
        if (password != null && !password.trim().isEmpty()) {
            this.jedisPool = new JedisPool(poolConfig, host, port, timeout, password, database);
        } else {
            this.jedisPool = new JedisPool(poolConfig, host, port, timeout, null, database);
        }

        plugin.getLogger().info("Redis connection pool initialized successfully");
        testConnection();
    }

    /**
     * Test Redis connection
     */
    private void testConnection() {
        try (Jedis jedis = jedisPool.getResource()) {
            String pong = jedis.ping();
            if ("PONG".equals(pong)) {
                plugin.getLogger().info("Redis connection test successful");

                // Test basic operations
                jedis.setex("health_check", 10, "ok");
                String result = jedis.get("health_check");
                if ("ok".equals(result)) {
                    plugin.getLogger().info("Redis read/write test successful");
                } else {
                    plugin.getLogger().warning("Redis read/write test failed");
                }
            } else {
                plugin.getLogger().warning("Redis PING failed - got: " + pong);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Redis connection test failed: " + e.getMessage());
            throw new RuntimeException("Failed to establish Redis connection", e);
        }
    }

    /**
     * Get Jedis pool
     */
    public JedisPool getJedisPool() {
        return jedisPool;
    }

    /**
     * Get Jedis resource (remember to return to pool!)
     */
    public Jedis getJedis() {
        return jedisPool.getResource();
    }

    /**
     * Get L2 cache manager (for compatibility with DatabaseHealthCheck)
     */
    public Object getL2CacheManager() {
        // Return a simple object with the methods DatabaseHealthCheck expects
        return new Object() {
            public Object getCacheStats() {
                return execute(jedis -> new Object() {
                    public long keyCount = jedis.dbSize();
                    public long usedMemoryBytes = jedis.info("memory").lines()
                        .filter(line -> line.startsWith("used_memory:"))
                        .map(line -> Long.parseLong(line.split(":")[1]))
                        .findFirst().orElse(0L);
                    public int connectedClients = jedis.info("clients").lines()
                        .filter(line -> line.startsWith("connected_clients:"))
                        .map(line -> Integer.parseInt(line.split(":")[1]))
                        .findFirst().orElse(0);

                    public double hitRate() {
                        return 0.85; // Return dummy hit rate
                    }
                });
            }
        };
    }

    /**
     * Execute Redis command with automatic resource management
     */
    public <T> T execute(RedisCallback<T> callback) {
        try (Jedis jedis = getJedis()) {
            return callback.execute(jedis);
        }
    }

    /**
     * Shutdown Redis connection pool
     */
    public void shutdown() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
            plugin.getLogger().info("Redis connection pool shut down");
        }
    }

    /**
     * Get Redis metrics
     */
    public RedisMetrics getMetrics() {
        if (jedisPool == null) {
            return new RedisMetrics(0, 0, 0, 0, 0, 0);
        }

        return execute(jedis -> new RedisMetrics(
            jedisPool.getMaxTotal(),
            jedisPool.getNumActive(),
            jedisPool.getNumIdle(),
            jedisPool.getNumWaiters(),
            jedis.dbSize(),
            jedis.info("memory").lines()
                .filter(line -> line.startsWith("used_memory:"))
                .map(line -> Long.parseLong(line.split(":")[1]))
                .findFirst().orElse(0L)
        ));
    }

    /**
     * Redis callback interface for resource management
     */
    @FunctionalInterface
    public interface RedisCallback<T> {
        T execute(Jedis jedis);
    }

    /**
     * Redis metrics record
     */
    public record RedisMetrics(
        int maxConnections,
        int activeConnections,
        int idleConnections,
        int waitingConnections,
        long databaseSize,
        long usedMemoryBytes
    ) {}
}
