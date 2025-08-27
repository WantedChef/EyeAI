package chef.sheesh.eyeAI.ai.fakeplayer.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Default event listener for fake player events
 */
public class FakePlayerEventListener implements Listener {

    @SuppressWarnings("unused")
    private final FakePlayerEventManager eventManager;

    public FakePlayerEventListener(FakePlayerEventManager eventManager) {
        this.eventManager = eventManager;
    }

    @EventHandler
    public void onFakePlayerSpawn(FakePlayerSpawnEvent event) {
        // Log spawn events
        event.getFakePlayer().getManager().getLogger().info(
            "FakePlayer spawned: " + event.getFakePlayer().getName() +
            " at " + event.getSpawnLocation().toString());
    }

    @EventHandler
    public void onFakePlayerDespawn(FakePlayerDespawnEvent event) {
        // Log despawn events
        event.getFakePlayer().getManager().getLogger().info(
            "FakePlayer despawned: " + event.getFakePlayer().getName() +
            " (Reason: " + event.getReason() + ")");
    }

    @EventHandler
    public void onFakePlayerDamage(FakePlayerDamageEvent event) {
        // Handle damage events
        if (event.getFakePlayer().getHealth() <= 0) {
            // Fake player died
            event.getFakePlayer().setState(chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerState.DEAD);
        }
    }

    @EventHandler
    public void onFakePlayerCombat(FakePlayerCombatEvent event) {
        // Handle combat events
        if (event.getCombatType() == FakePlayerCombatEvent.CombatType.ATTACK) {
            // Ensure fake player is in attacking state
            event.getFakePlayer().setState(chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerState.ATTACKING);
        }
    }

    @EventHandler
    public void onFakePlayerMove(FakePlayerMoveEvent event) {
        // Handle movement events
        // Could be used for anti-cheat integration or movement validation
    }

    @EventHandler
    public void onFakePlayerInteract(FakePlayerInteractEvent event) {
        // Handle interaction events
        // Default behavior is already handled in the event
    }

    @EventHandler
    public void onFakePlayerStateChange(FakePlayerStateChangeEvent event) {
        // Handle state change events
        if (event.isEnteringCombat()) {
            // Fake player entering combat
            event.getFakePlayer().getManager().getLogger().info(
                "FakePlayer entering combat: " + event.getFakePlayer().getName());
        } else if (event.isLeavingCombat()) {
            // Fake player leaving combat
            event.getFakePlayer().getManager().getLogger().info(
                "FakePlayer leaving combat: " + event.getFakePlayer().getName());
        } else if (event.isDying()) {
            // Fake player dying
            event.getFakePlayer().getManager().getLogger().info(
                "FakePlayer dying: " + event.getFakePlayer().getName());
        }
    }
}
