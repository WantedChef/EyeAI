package chef.sheesh.eyeAI.ai.behavior.nodes;

import chef.sheesh.eyeAI.ai.behavior.BehaviorTree;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * A node that finds the nearest player and stores it in the blackboard.
 * Succeeds if a player is found within the specified radius, fails otherwise.
 */
public class FindNearbyPlayerNode extends BehaviorTree {

    private final double radius;

    public FindNearbyPlayerNode(double radius) {
        this.radius = radius;
    }

    @Override
    public ExecutionResult execute(IFakePlayer fakePlayer) {
        List<Entity> nearbyEntities = fakePlayer.getNearbyEntities(radius, radius, radius);
        Player nearestPlayer = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Entity entity : nearbyEntities) {
            if (entity instanceof Player) {
                double distance = entity.getLocation().distance(fakePlayer.getLocation());
                if (distance < nearestDistance) {
                    nearestPlayer = (Player) entity;
                    nearestDistance = distance;
                }
            }
        }

        if (nearestPlayer != null) {
            fakePlayer.setBlackboardValue("target_player", nearestPlayer);
            return success();
        } else {
            return failure();
        }
    }

    @Override
    public void reset() {
        // No state to reset
    }

    @Override
    public String getCategory() {
        return "Condition";
    }
}
