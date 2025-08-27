package chef.sheesh.eyeAI.ai.fakeplayer.events;

import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerManager;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerState;
import chef.sheesh.eyeAI.ai.fakeplayer.events.FakePlayerDamageEvent.DamageCause;
import chef.sheesh.eyeAI.ai.fakeplayer.events.FakePlayerInteractEvent.InteractionType;
import chef.sheesh.eyeAI.ai.fakeplayer.events.FakePlayerMoveEvent.MoveType;
import chef.sheesh.eyeAI.ai.fakeplayer.events.FakePlayerStateChangeEvent.StateChangeReason;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager for handling fake player events
 */
public class FakePlayerEventManager {

    private final JavaPlugin plugin;
    private final FakePlayerManager fakePlayerManager;
    private final Map<UUID, Location> lastLocations;
    private final Map<UUID, FakePlayerState> lastStates;
    private final Map<UUID, Long> lastMoveEvents;

    public FakePlayerEventManager(JavaPlugin plugin, FakePlayerManager fakePlayerManager) {
        this.plugin = plugin;
        this.fakePlayerManager = fakePlayerManager;
        this.lastLocations = new ConcurrentHashMap<>();
        this.lastStates = new ConcurrentHashMap<>();
        this.lastMoveEvents = new ConcurrentHashMap<>();
    }

    /**
     * Fire spawn event for a fake player
     */
    public void fireSpawnEvent(FakePlayer fakePlayer, Location spawnLocation) {
        FakePlayerSpawnEvent event = new FakePlayerSpawnEvent(fakePlayer, spawnLocation);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            plugin.getLogger().info("Fake player spawn cancelled: " + fakePlayer.getName());
        }
    }

    /**
     * Fire despawn event for a fake player
     */
    public void fireDespawnEvent(FakePlayer fakePlayer, FakePlayerDespawnEvent.DespawnReason reason) {
        FakePlayerState lastState = lastStates.get(fakePlayer.getId());
        FakePlayerDespawnEvent event = new FakePlayerDespawnEvent(fakePlayer, reason, lastState);
        Bukkit.getPluginManager().callEvent(event);
    }

    /**
     * Fire damage event for a fake player
     */
    public void fireDamageEvent(FakePlayer fakePlayer, Entity damager, DamageCause cause, double damage) {
        FakePlayerDamageEvent event = new FakePlayerDamageEvent(fakePlayer, damager, cause, damage);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return; // Damage cancelled
        }

        // Apply damage if not cancelled
        fakePlayer.damage(event.getDamage());
    }

    /**
     * Fire combat event for a fake player
     */
    public void fireCombatEvent(FakePlayer fakePlayer, Entity target,
                               FakePlayerCombatEvent.CombatType combatType, double damage) {
        FakePlayerCombatEvent event = new FakePlayerCombatEvent(fakePlayer, target, combatType, damage);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return; // Combat cancelled
        }

        // Execute combat if not cancelled
        if (combatType == FakePlayerCombatEvent.CombatType.ATTACK) {
            fakePlayer.performAttack(target);
        }
    }

    /**
     * Fire interaction event for a fake player
     */
    public void fireInteractEvent(FakePlayer fakePlayer, Player player, InteractionType interactionType) {
        FakePlayerInteractEvent event = new FakePlayerInteractEvent(fakePlayer, player, interactionType);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            // Send response message
            player.sendMessage(event.getResponseMessage());
        }
    }

    /**
     * Update fake player and fire appropriate events
     */
    public void updateFakePlayer(FakePlayer fakePlayer) {
        UUID fakePlayerId = fakePlayer.getId();
        Location currentLocation = fakePlayer.getLocation();
        FakePlayerState currentState = fakePlayer.getState();

        // Check for state changes
        FakePlayerState lastState = lastStates.get(fakePlayerId);
        if (lastState != null && lastState != currentState) {
            fireStateChangeEvent(fakePlayer, lastState, currentState, StateChangeReason.AI_DECISION);
        }

        // Check for movement
        Location lastLocation = lastLocations.get(fakePlayerId);
        if (lastLocation != null && !lastLocation.equals(currentLocation)) {
            double distance = lastLocation.distance(currentLocation);
            if (distance > 0.1) { // Only fire event for significant movement
                fireMoveEvent(fakePlayer, lastLocation, currentLocation, getMoveType(fakePlayer));
            }
        }

        // Update tracking data
        lastLocations.put(fakePlayerId, currentLocation.clone());
        lastStates.put(fakePlayerId, currentState);
    }

    /**
     * Fire state change event
     */
    private void fireStateChangeEvent(FakePlayer fakePlayer, FakePlayerState oldState,
                                    FakePlayerState newState, StateChangeReason reason) {
        FakePlayerStateChangeEvent event = new FakePlayerStateChangeEvent(fakePlayer, oldState, newState, reason);
        Bukkit.getPluginManager().callEvent(event);
    }

    /**
     * Fire move event
     */
    private void fireMoveEvent(FakePlayer fakePlayer, Location from, Location to, MoveType moveType) {
        // Rate limit move events to prevent spam
        Long lastMoveEvent = lastMoveEvents.get(fakePlayer.getId());
        long currentTime = System.currentTimeMillis();

        if (lastMoveEvent == null || currentTime - lastMoveEvent > 100) { // Max 10 events per second
            FakePlayerMoveEvent event = new FakePlayerMoveEvent(fakePlayer, from, to, moveType);
            Bukkit.getPluginManager().callEvent(event);
            lastMoveEvents.put(fakePlayer.getId(), currentTime);
        }
    }

    /**
     * Determine move type based on fake player state
     */
    private MoveType getMoveType(FakePlayer fakePlayer) {
        return switch (fakePlayer.getState()) {
            case MOVING -> MoveType.WALK;
            case ATTACKING -> MoveType.PURSUIT;
            case FLEEING -> MoveType.FLEE;
            case PATROLLING -> MoveType.PATROL;
            default -> MoveType.WALK;
        };
    }

    /**
     * Fire events for all active fake players
     */
    public void fireEventsForAllFakePlayers() {
        Collection<FakePlayer> fakePlayers = fakePlayerManager.getActiveFakePlayers();
        for (FakePlayer fakePlayer : fakePlayers) {
            updateFakePlayer(fakePlayer);
        }
    }

    /**
     * Handle player interaction with fake player
     */
    public void handlePlayerInteraction(Player player, FakePlayer fakePlayer, InteractionType interactionType) {
        fireInteractEvent(fakePlayer, player, interactionType);
    }

    /**
     * Handle fake player taking damage
     */
    public void handleDamage(FakePlayer fakePlayer, Entity damager, double damage) {
        DamageCause cause = determineDamageCause(damager);
        fireDamageEvent(fakePlayer, damager, cause, damage);
    }

    /**
     * Handle fake player attacking
     */
    public void handleAttack(FakePlayer fakePlayer, Entity target, double damage) {
        fireCombatEvent(fakePlayer, target, FakePlayerCombatEvent.CombatType.ATTACK, damage);
    }

    /**
     * Determine damage cause from damager entity
     */
    private DamageCause determineDamageCause(Entity damager) {
        if (damager == null) {
            return DamageCause.CUSTOM;
        }

        String damagerType = damager.getType().name();
        if (damagerType.contains("ZOMBIE") || damagerType.contains("SKELETON") ||
            damagerType.contains("CREEPER") || damagerType.contains("SPIDER")) {
            return DamageCause.ENTITY_ATTACK;
        }

        // Add more damage cause logic here
        return DamageCause.ENTITY_ATTACK; // Default
    }

    /**
     * Register event listeners
     */
    public void registerListeners() {
        // Register default event listeners
        Bukkit.getPluginManager().registerEvents(new FakePlayerEventListener(this), plugin);
    }

    /**
     * Clean up tracking data for despawned fake player
     */
    public void cleanupFakePlayer(UUID fakePlayerId) {
        lastLocations.remove(fakePlayerId);
        lastStates.remove(fakePlayerId);
        lastMoveEvents.remove(fakePlayerId);
    }

    /**
     * Get event statistics
     */
    public Map<String, Integer> getEventStatistics() {
        // This would track event firing statistics
        // For now, return empty map
        return new HashMap<>();
    }

    /**
     * Reset event tracking
     */
    public void resetEventTracking() {
        lastMoveEvents.clear();
    }
}
