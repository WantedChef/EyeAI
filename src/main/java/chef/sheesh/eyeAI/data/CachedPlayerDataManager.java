package chef.sheesh.eyeAI.data;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import chef.sheesh.eyeAI.data.model.PlayerData;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.logging.Level;

/**
 * Thread-safe LRU cache for player data with async-sync bridge
 * Provides immediate sync access to cached data for scoreboard performance
 */
public class CachedPlayerDataManager implements AutoCloseable {
    
    private final Plugin plugin;
    private final PlayerDataHandler dataHandler;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    // LRU Cache implementation
    private final Map<UUID, PlayerData> cache;
    private int maxCacheSize;
    
    // Cache statistics
    @Getter
    private long cacheHits = 0;
    @Getter
    private long cacheMisses = 0;
    @Getter
    private long totalRequests = 0;
    
    // Pending async operations
    private final Map<UUID, CompletableFuture<PlayerData>> pendingLoads = new ConcurrentHashMap<>();
    
    public CachedPlayerDataManager(@NotNull Plugin plugin) {
        this.plugin = plugin;
        this.dataHandler = new PlayerDataHandler(plugin);
        // Use default value initially, will be updated when config is available
        this.maxCacheSize = 1000;

        // LRU Cache with access-order
        this.cache = new LinkedHashMap<UUID, PlayerData>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<UUID, PlayerData> eldest) {
                if (size() > maxCacheSize) {
                    // Save evicted data async
                    dataHandler.savePlayerData(eldest.getValue());
                    return true;
                }
                return false;
            }
        };

        // Initialize database
        dataHandler.initialize();

        plugin.getLogger().info("CachedPlayerDataManager initialized with cache size: " + maxCacheSize);
    }

    /**
     * Update cache size from configuration
     */
    public void updateCacheSize(int newSize) {
        lock.writeLock().lock();
        try {
            this.maxCacheSize = newSize;
            plugin.getLogger().info("Cache size updated to: " + maxCacheSize);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Get player data synchronously from cache (for scoreboard performance)
     * Returns null if not cached - use loadPlayerData() to load async first
     */
    @Nullable
    public PlayerData getPlayerDataSync(@NotNull UUID playerId) {
        lock.readLock().lock();
        try {
            totalRequests++;
            PlayerData data = cache.get(playerId);
            if (data != null) {
                cacheHits++;
                return data;
            } else {
                cacheMisses++;
                return null;
            }
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Load player data async and cache it
     * Returns cached data immediately if available, otherwise loads from database
     */
    @NotNull
    public CompletableFuture<PlayerData> loadPlayerData(@NotNull UUID playerId) {
        // Check cache first
        PlayerData cached = getPlayerDataSync(playerId);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        
        // Check if already loading
        CompletableFuture<PlayerData> pending = pendingLoads.get(playerId);
        if (pending != null) {
            return pending;
        }
        
        // Load from database
        CompletableFuture<PlayerData> future = dataHandler.getPlayerData(playerId)
            .thenApply(data -> {
                // Update player name if online
                Player player = Bukkit.getPlayer(playerId);
                if (player != null) {
                    data.setPlayerName(player.getName());
                }
                
                // Cache the data
                cachePlayerData(data);
                
                // Remove from pending
                pendingLoads.remove(playerId);
                
                return data;
            })
            .exceptionally(throwable -> {
                plugin.getLogger().log(Level.SEVERE, "Failed to load player data for " + playerId, throwable);
                pendingLoads.remove(playerId);
                
                // Return default data
                Player player = Bukkit.getPlayer(playerId);
                PlayerData defaultData = PlayerData.builder()
                    .playerId(playerId)
                    .playerName(player != null ? player.getName() : "Unknown")
                    .level(1)
                    .experience(0)
                    .kills(0)
                    .deaths(0)
                    .balance(0.0)
                    .tokens(0)
                    .scoreboardEnabled(true)
                    .preferredLanguage("en")
                    .firstJoin(System.currentTimeMillis())
                    .lastSeen(System.currentTimeMillis())
                    .playtime(0)
                    .build();
                
                cachePlayerData(defaultData);
                return defaultData;
            });
        
        pendingLoads.put(playerId, future);
        return future;
    }
    
    /**
     * Update player data with a function and save async
     */
    @NotNull
    public CompletableFuture<Void> updatePlayerData(@NotNull UUID playerId, @NotNull Function<PlayerData, PlayerData> updater) {
        return loadPlayerData(playerId)
            .thenCompose(data -> {
                PlayerData updated = updater.apply(data);
                cachePlayerData(updated);
                return dataHandler.savePlayerData(updated);
            });
    }
    
    /**
     * Cache player data (thread-safe)
     */
    public void cachePlayerData(@NotNull PlayerData data) {
        lock.writeLock().lock();
        try {
            cache.put(data.getPlayerId(), data);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Remove player from cache and save data
     */
    public void uncachePlayer(@NotNull UUID playerId) {
        lock.writeLock().lock();
        try {
            PlayerData data = cache.remove(playerId);
            if (data != null) {
                // Save async before removing
                dataHandler.savePlayerData(data);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Get cache hit ratio
     */
    public double getCacheHitRatio() {
        if (totalRequests == 0) {
            return 0.0;
        }
        return (double) cacheHits / totalRequests;
    }
    
    /**
     * Get current cache size
     */
    public int getCacheSize() {
        lock.readLock().lock();
        try {
            return cache.size();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Clear cache and save all data
     */
    public void clearCache() {
        lock.writeLock().lock();
        try {
            // Save all cached data
            for (PlayerData data : cache.values()) {
                dataHandler.savePlayerData(data);
            }
            cache.clear();
            plugin.getLogger().info("Cache cleared and all data saved");
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Shutdown and save all cached data
     */
    public void shutdown() {
        try {
            // Cancel pending operations
            pendingLoads.values().forEach(future -> future.cancel(false));
            pendingLoads.clear();
            
            // Save all cached data
            clearCache();
            
            // Close database handler
            dataHandler.close();
            
            plugin.getLogger().info("CachedPlayerDataManager shutdown complete");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error during CachedPlayerDataManager shutdown", e);
        }
    }
    
    @Override
    public void close() {
        shutdown();
    }
}
