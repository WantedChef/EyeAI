package chef.sheesh.eyeAI.ai.fakeplayer;

import chef.sheesh.eyeAI.ai.core.SchedulerService;
import chef.sheesh.eyeAI.ai.core.DecisionContext;
import chef.sheesh.eyeAI.ai.fakeplayer.*;
import chef.sheesh.eyeAI.ai.fakeplayer.persistence.FileBasedFakePlayerPersistence;
import chef.sheesh.eyeAI.ai.fakeplayer.persistence.IFakePlayerPersistence;
import chef.sheesh.eyeAI.ai.fakeplayer.persistence.FakePlayerStatistics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;
import java.util.concurrent.TimeUnit;

/**
 * Manager for all fake players in the system.
 * Handles spawning, despawning, ticking, and coordination with other systems.
 */
public class FakePlayerManager implements IFakePlayerManager {

    // IFakePlayerManager interface implementation

    @Override
    public Optional<FakePlayer> getFakePlayer(String name) {
        return active.values().stream()
                .filter(fp -> fp.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public boolean hasActiveFakePlayers() {
        return !active.isEmpty();
    }

    @Override
    public int getActiveFakePlayerCount() {
        return active.size();
    }

    @Override
    public boolean despawnFakePlayer(FakePlayer fakePlayer) {
        return despawnFakePlayer(fakePlayer.getId());
    }

    private final JavaPlugin plugin;
    private final SchedulerService scheduler;
    private final Map<UUID, FakePlayer> active = new ConcurrentHashMap<>();
    // Getters
    private final IFakePlayerPacketController packetController;
    private final FakePlayerAI behaviorTreeFactory;
    private final IFakePlayerPersistence persistence;
    private final Map<UUID, FakePlayerStatistics> statistics = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastSaveTime = new ConcurrentHashMap<>();
    private static final long SAVE_INTERVAL = 300000; // 5 minutes
    private long lastVisualUpdate = 0;

    public FakePlayerManager(JavaPlugin plugin, SchedulerService scheduler) {
        this.plugin = plugin;
        this.scheduler = scheduler;
        this.packetController = new PacketNpcController(plugin);
        this.behaviorTreeFactory = new FakePlayerAI();
        this.persistence = new FileBasedFakePlayerPersistence(plugin);

        // Load existing fake players from persistence
        loadFromPersistence();
    }

    /**
     * Spawn a new fake player at the given location
     */
    public void spawnFakePlayer(String name, Location spawn) {
        UUID id = UUID.randomUUID();
        FakePlayer fp = new FakePlayer(id, name, spawn, this);
        active.put(id, fp);
        
        // Create behavior tree
        behaviorTreeFactory.createDefaultCombatBehavior(fp);
        
        // Format location for logging
        behaviorTreeFactory.formatLocation(spawn);
        
        // Save initial state
        FakePlayerStatistics stats = new FakePlayerStatistics(id, name);
        statistics.put(id, stats);

        // Create visual representation if enabled
        if (packetController.isEnabled()) {
            packetController.createVisualNpc(fp);
        }

        // Save to persistence
        persistence.save(fp);
        plugin.getLogger().info("Spawned FakePlayer: " + name + " at " + spawn);
    }

    /**
     * Despawn all fake players and save their data
     */
    public void despawnAll() {
        getLogger().info("Despawning all fake players...");
        List<FakePlayer> playersToDespawn = new ArrayList<>(active.values());
        for (FakePlayer player : playersToDespawn) {
            despawnFakePlayer(player.getId());
        }
        getLogger().info("All fake players despawned and data saved.");
    }

    /**
     * Despawn a specific fake player
     */
    public boolean despawnFakePlayer(UUID id) {
        FakePlayer fp = active.remove(id);
        if (fp != null) {
            // Save final statistics
            FakePlayerStatistics stats = statistics.remove(id);
            if (stats != null) {
                try {
                    // Persist latest statistics snapshot
                    persistence.updateStatistics(id, stats);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to persist statistics for despawned fake player " + fp.getName() + ": " + e.getMessage());
                }
            }

            packetController.removeVisualNpc(fp);
            plugin.getLogger().info("Despawned FakePlayer: " + fp.getName());
            return true;
        }
        return false;
    }


    /**
     * Get a fake player by ID
     */
    public Optional<FakePlayer> getFakePlayer(UUID id) {
        return Optional.ofNullable(active.get(id));
    }

    /**
     * Get all active fake players
     */
    public Collection<FakePlayer> getActiveFakePlayers() {
        return new ArrayList<>(active.values());
    }

    @Override
    public List<IFakePlayer> getAllFakePlayers() {
        return new ArrayList<>(active.values());
    }

    /**
     * Get the count of active fake players
     */
    public int getActiveCount() {
        return active.size();
    }


    /**
     * Create a damage event for fake player attacks
     */
    public void callFakeDamage(FakePlayer attacker, Entity target) {
        scheduler.runOnMain(() -> {
            Entity damageSource = packetController.getOrCreateProxyEntity(attacker);

            // Use modern constructor without deprecated parameters
            EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(
                damageSource, target, EntityDamageEvent.DamageCause.ENTITY_ATTACK,
                2.0
            );

            plugin.getServer().getPluginManager().callEvent(event);

            if (!event.isCancelled() && target instanceof org.bukkit.entity.LivingEntity) {
                ((org.bukkit.entity.LivingEntity) target).damage(event.getDamage());
            }
        });
    }

    /**
     * Create a decision context for a fake player
     */
    public DecisionContext createDecisionContext(FakePlayer fakePlayer) {
        Location location = fakePlayer.getLocation();
        
        // Holder for computed values we must gather on the main thread
        class EnvScan {
            List<Entity> entitiesOnly = Collections.emptyList();
            List<Player> nearbyPlayers = Collections.emptyList();
            long worldTime;
            boolean isDayTime;
            boolean onGround;
            double obstacleDensity;
            double lightLevel;
            boolean thundering;
            boolean storm;
            double terrainDifficulty;
            boolean nearWater;
            boolean nearLava;
            double elevation;
            double coverDensity;
        }

        EnvScan scan = new EnvScan();

        try {
            CompletableFuture<EnvScan> future = new CompletableFuture<>();

            // Schedule on main thread
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    // Nearby entities and players
                    List<Entity> nearby = location.getWorld().getNearbyEntities(location, 16, 16, 16)
                            .stream()
                            .filter(entity -> !entity.equals(fakePlayer.getVisibleNpc()))
                            .toList();

                    List<Player> players = nearby.stream()
                            .filter(e -> e instanceof Player)
                            .map(e -> (Player) e)
                            .toList();

                    List<Entity> entitiesOnly = nearby.stream()
                            .filter(e -> !(e instanceof Player))
                            .toList();

                    // World/time/env
                    long worldTime = location.getWorld().getTime();
                    boolean isDayTime = worldTime < 12300 || worldTime > 23850; // simplified
                    boolean onGround = location.getWorld().getBlockAt(location.clone().subtract(0, 1, 0)).getType().isSolid();

                    int obstacles = 0;
                    for (int x = -2; x <= 2; x++) {
                        for (int z = -2; z <= 2; z++) {
                            if (location.getWorld().getBlockAt(location.getBlockX() + x, location.getBlockY(), location.getBlockZ() + z)
                                    .getType().isSolid()) {
                                obstacles++;
                            }
                        }
                    }
                    double obstacleDensity = obstacles; // raw count; normalization handled by consumer

                    double lightLevel = location.getWorld().getBlockAt(location).getLightLevel() / 15.0;
                    boolean thundering = location.getWorld().isThundering();
                    boolean storm = location.getWorld().hasStorm();

                    // Very simple terrain difficulty heuristic based on surrounding height variance
                    int baseY = location.getBlockY();
                    int variance = 0;
                    for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                            int y = location.getWorld().getHighestBlockYAt(location.getBlockX() + x, location.getBlockZ() + z);
                            variance += Math.abs(y - baseY);
                        }
                    }
                    double terrainDifficulty = Math.min(1.0, variance / 20.0);

                    boolean nearWater = false;
                    boolean nearLava = false;
                    for (int x = -2; x <= 2; x++) {
                        for (int z = -2; z <= 2; z++) {
                            var type = location.getWorld().getBlockAt(location.getBlockX() + x, location.getBlockY(), location.getBlockZ() + z).getType();
                            String name = type.name();
                            if (name.contains("WATER")) {
                                nearWater = true;
                            }
                            if (name.contains("LAVA")) {
                                nearLava = true;
                            }
                        }
                    }

                    double elevation = location.getY();

                    // Naive cover density: count solid blocks 2 blocks above nearby positions
                    int cover = 0;
                    for (int x = -2; x <= 2; x++) {
                        for (int z = -2; z <= 2; z++) {
                            if (location.getWorld().getBlockAt(location.getBlockX() + x, location.getBlockY() + 2, location.getBlockZ() + z)
                                    .getType().isSolid()) {
                                cover++;
                            }
                        }
                    }
                    double coverDensity = cover;

                    EnvScan result = new EnvScan();
                    result.entitiesOnly = entitiesOnly;
                    result.nearbyPlayers = players;
                    result.worldTime = worldTime;
                    result.isDayTime = isDayTime;
                    result.onGround = onGround;
                    result.obstacleDensity = obstacleDensity;
                    result.lightLevel = lightLevel;
                    result.thundering = thundering;
                    result.storm = storm;
                    result.terrainDifficulty = terrainDifficulty;
                    result.nearWater = nearWater;
                    result.nearLava = nearLava;
                    result.elevation = elevation;
                    result.coverDensity = coverDensity;

                    future.complete(result);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            });

            scan = future.get(100, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            getLogger().warning("Failed to build decision context for " + fakePlayer.getName() + ": " + e.getMessage());
        }

        // Calculate threat level based on nearby hostile entities (safe: uses values computed on main thread)
        double threatLevel = calculateThreatLevel(fakePlayer, scan.entitiesOnly);

        return new DecisionContext(
            location,
            fakePlayer.getHealth(),
            scan.entitiesOnly,
            scan.nearbyPlayers,
            scan.worldTime,
            scan.isDayTime,
            Optional.empty(), // Current target (to be implemented in behavior trees)
            threatLevel,
            scan.onGround,
            scan.obstacleDensity,
            scan.lightLevel,
            scan.thundering,
            scan.storm,
            scan.terrainDifficulty,
            scan.nearWater,
            scan.nearLava,
            scan.elevation,
            scan.coverDensity
        );
    }

    /**
     * Calculate threat level based on nearby entities
     */
    private double calculateThreatLevel(FakePlayer fakePlayer, List<Entity> nearbyEntities) {
        return nearbyEntities.stream()
                .filter(this::isHostile)
                .mapToDouble(entity -> {
                    double distance = fakePlayer.getLocation().distance(entity.getLocation());
                    return Math.max(0, 10 - distance); // Closer = higher threat
                })
                .sum();
    }

    private boolean isHostile(Entity entity) {
        String typeName = entity.getType().name();
        return typeName.contains("ZOMBIE") ||
               typeName.contains("SKELETON") ||
               typeName.contains("CREEPER") ||
               typeName.contains("SPIDER") ||
               typeName.contains("ENDERMAN");
    }

    public java.util.logging.Logger getLogger() {
        return plugin.getLogger();
    }

    public SchedulerService getScheduler() {
        return scheduler;
    }

    // Explicit accessor to avoid Lombok processing issues
    public IFakePlayerPacketController getPacketController() {
        return packetController;
    }

    // IFakePlayerManager interface implementation methods

    @Override
    public void saveToPersistence() {
        try {
            // Save all active fake players
            persistence.saveAll(new ArrayList<>(active.values()));

            // Save all statistics
            saveAllStatistics();

            plugin.getLogger().info("Successfully saved " + active.size() + " fake players to persistence");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save fake players to persistence: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void loadFromPersistence() {
        try {
            List<FakePlayer> loadedPlayers = persistence.loadAll();
            int loadedCount = 0;

            for (FakePlayer loadedPlayer : loadedPlayers) {
                // Only load players that aren't already active
                if (!active.containsKey(loadedPlayer.getId())) {
                    active.put(loadedPlayer.getId(), loadedPlayer);

                    // Load statistics if available
                    Optional<FakePlayerStatistics> stats = persistence.getStatistics(loadedPlayer.getId());
                    stats.ifPresent(fakePlayerStatistics -> statistics.put(loadedPlayer.getId(), fakePlayerStatistics));

                    // Create visual representation if enabled
                    if (packetController.isEnabled()) {
                        packetController.createVisualNpc(loadedPlayer);
                    }

                    loadedCount++;
                }
            }

            plugin.getLogger().info("Successfully loaded " + loadedCount + " fake players from persistence");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load fake players from persistence: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Save statistics for all fake players
     */
    private void saveAllStatistics() {
        for (Map.Entry<UUID, FakePlayerStatistics> entry : statistics.entrySet()) {
            try {
                persistence.updateStatistics(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to save statistics for fake player " + entry.getKey() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Get statistics for a specific fake player
     */
    public Optional<FakePlayerStatistics> getStatistics(UUID id) {
        return Optional.ofNullable(statistics.get(id));
    }

    /**
     * Get all fake player statistics
     */
    public Map<UUID, FakePlayerStatistics> getAllStatistics() {
        return new HashMap<>(statistics);
    }

    /**
     * Force save all data immediately
     */
    public void forceSaveAll() {
        saveToPersistence();
        plugin.getLogger().info("Forced save of all fake player data completed");
    }

    /**
     * Create a fake player at the given location with the specified name
     */
    public IFakePlayer createFakePlayer(Location location, String name) {
        UUID id = UUID.randomUUID();
        FakePlayer fp = new FakePlayer(id, name, location, this);
        active.put(id, fp);
        
        // Create behavior tree
        behaviorTreeFactory.createDefaultCombatBehavior(fp);
        
        // Save initial state
        FakePlayerStatistics stats = new FakePlayerStatistics(id, name);
        statistics.put(id, stats);
        
        // Create visual representation if enabled
        if (packetController.isEnabled()) {
            packetController.createVisualNpc(fp);
        }
        
        // Save to persistence
        persistence.save(fp);
        
        return fp;
    }

    @Override
    public void tickAll() {
        boolean enabled = true;
        if (!enabled) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        
        // Process each fake player
        for (FakePlayer fp : active.values()) {
            // Skip if not loaded or invalid
            if (fp == null || fp.getLocation() == null || 
                fp.getLocation().getWorld() == null) {
                continue;
            }
            
            // Update AI behavior on main thread to avoid async entity access
            try {
                // Create decision context with proper entity scanning
                DecisionContext context = createDecisionContext(fp);
                
                // Update AI with the context
                behaviorTreeFactory.updateState(fp);
                fp.tick();
                
                // Update statistics
                FakePlayerStatistics stats = statistics.get(fp.getId());
                if (stats != null) {
                    stats.incrementTicks();
                    stats.setLastActiveTime(LocalDateTime.now());
                }
                
            } catch (Exception e) {
                plugin.getLogger().warning("Error ticking fake player " + fp.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Update NPC visuals periodically
        if (currentTime - lastVisualUpdate > 1000) {
            updateNpcVisuals();
            lastVisualUpdate = currentTime;
        }
    }

    private void updateNpcVisuals() {
        packetController.flush(); // Send batched packets
        packetController.updateVisibility(); // Update NPC visibility
    }

    /**
     * Save all fake players to persistence
     */
    public void saveAllFakePlayers() {
        for (FakePlayer fakePlayer : active.values()) {
            persistence.save(fakePlayer);
        }
    }

    /**
     * Load all fake players from persistence
     */
    public void loadAllFakePlayers() {
        List<FakePlayer> loadedPlayers = persistence.loadAll();
        for (FakePlayer fakePlayer : loadedPlayers) {
            active.put(fakePlayer.getId(), fakePlayer);
            // Initialize visual representation
            packetController.getOrCreateProxyEntity(fakePlayer);
        }
    }

    /**
     * Clear all fake players
     */
    public void clearAllFakePlayers() {
        List<UUID> ids = new ArrayList<>(active.keySet());
        for (UUID id : ids) {
            removeFakePlayer(id);
        }
    }

    /**
     * Remove a fake player by ID
     * @param id the fake player ID
     * @return true if removed successfully
     */
    public boolean removeFakePlayer(UUID id) {
        FakePlayer fakePlayer = active.remove(id);
        if (fakePlayer != null) {
            packetController.removeVisualNpc(fakePlayer);
            fakePlayer.setState(FakePlayerState.REMOVED);
            return true;
        }
        return false;
    }

    /**
     * Gets the FakePlayer associated with a given entity.
     * @param entity The entity to check.
     * @return An Optional containing the FakePlayer if the entity is a visual representation, otherwise empty.
     */
    public Optional<FakePlayer> getFakePlayerByEntity(Entity entity) {
        if (entity == null) {
            return Optional.empty();
        }
        return this.packetController.getFakePlayerByEntityId(entity.getUniqueId());
    }
}
