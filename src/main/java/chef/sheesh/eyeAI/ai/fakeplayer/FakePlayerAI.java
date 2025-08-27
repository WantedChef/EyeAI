package chef.sheesh.eyeAI.ai.fakeplayer;

import chef.sheesh.eyeAI.ai.behavior.IBehaviorTree;
import chef.sheesh.eyeAI.ai.core.DecisionContext;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.logging.Logger;

/**
 * AI behavior system for fake players
 */
public class FakePlayerAI implements IBehaviorTree {
    
    private final Random random = new Random();
    private static final int LOG_INTERVAL = 100; // Log every 100 ticks (5 seconds)
    private int tickCounter = 0;
    private FakePlayer fakePlayer;
    private boolean running = false;
    private String name = "FakePlayerAI";

    /**
     * Update the fake player's state and behavior
     */
    public void updateState(FakePlayer fakePlayer) {
        this.fakePlayer = fakePlayer;
        tickCounter++;

        // Build context via manager on the main thread
        DecisionContext context = fakePlayer.createDecisionContext();

        // Basic AI behavior
        if (shouldMove(context)) {
            moveToRandomLocation(fakePlayer);
        }

        if (shouldAttack(context)) {
            attackNearbyEntity(fakePlayer, context);
        }

        if (shouldInteract(context)) {
            interactWithNearbyPlayer(fakePlayer, context);
        }

        // Log AI decisions every 100 ticks (~5 seconds)
        if (tickCounter % LOG_INTERVAL == 0) {
            Logger.getLogger("Minecraft").info(String.format(
                "[EyeAI][AI] %s: %d entities, %d players nearby",
                fakePlayer.getName(),
                context.getNearbyEntities().size(),
                context.getNearbyPlayers().size()
            ));
        }
    }

    /**
     * Create default combat behavior for a fake player
     */
    public void createDefaultCombatBehavior(FakePlayer fakePlayer) {
        // Enable combat mode in the behavior controller
        fakePlayer.getBehaviorController().setCombatMode(true);
        
        // Set patrol mode as well so the player has something to do when not in combat
        fakePlayer.getBehaviorController().setPatrolMode(true);
    }

    private boolean shouldMove(DecisionContext context) {
        return random.nextDouble() < 0.3; // 30% chance to move
    }

    private boolean shouldAttack(DecisionContext context) {
        return !context.getNearbyEntities().isEmpty() &&
               random.nextDouble() < 0.2; // 20% chance to attack
    }

    private boolean shouldInteract(DecisionContext context) {
        return !context.getNearbyPlayers().isEmpty() &&
               random.nextDouble() < 0.1; // 10% chance to interact
    }

    private void moveToRandomLocation(FakePlayer fakePlayer) {
        Location current = fakePlayer.getLocation();
        double offsetX = (random.nextDouble() - 0.5) * 10;
        double offsetZ = (random.nextDouble() - 0.5) * 10;

        Location newLoc = current.clone().add(offsetX, 0, offsetZ);
        newLoc.setY(current.getWorld().getHighestBlockYAt(newLoc) + 1);

        fakePlayer.moveTo(newLoc);
    }

    private void attackNearbyEntity(FakePlayer fakePlayer, DecisionContext context) {
        if (!context.getNearbyEntities().isEmpty()) {
            Entity target = context.getNearbyEntities().get(0);
            fakePlayer.attackEntity(target);
        }
    }

    private void interactWithNearbyPlayer(FakePlayer fakePlayer, DecisionContext context) {
        if (!context.getNearbyPlayers().isEmpty()) {
            Player target = context.getNearbyPlayers().get(0);
            fakePlayer.interact(target);
        }
    }

    public String formatLocation(Location loc) {
        return String.format("%.1f, %.1f, %.1f", loc.getX(), loc.getY(), loc.getZ());
    }
    
    @Override
    public ExecutionResult execute(IFakePlayer fakePlayer) {
        if (!(fakePlayer instanceof FakePlayer)) {
            return ExecutionResult.FAILURE;
        }
        
        FakePlayer player = (FakePlayer) fakePlayer;
        this.fakePlayer = player;
        running = true;
        
        // Build context via manager on the main thread
        DecisionContext context = player.createDecisionContext();
        
        // Update behavior based on context
        updateState(player);
        
        running = false;
        return ExecutionResult.SUCCESS;
    }
    
    @Override
    public void reset() {
        tickCounter = 0;
        running = false;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public String getDescription() {
        return "Default AI behavior system for fake players with combat, movement, and interaction capabilities";
    }
}
