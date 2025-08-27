package chef.sheesh.eyeAI.ai.behavior.nodes;

import chef.sheesh.eyeAI.ai.behavior.BehaviorTree;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;
import org.bukkit.entity.Entity;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

/**
 * A node that makes the agent look at a target entity.
 * The target is retrieved from the blackboard.
 */
public class LookAtNode extends BehaviorTree {

    private final String targetKey;

    public LookAtNode(String targetKey) {
        this.targetKey = targetKey;
    }

    @Override
    public ExecutionResult execute(IFakePlayer fakePlayer) {
        Object targetObject = fakePlayer.getBlackboardValue(targetKey);
        if (targetObject instanceof Entity target) {
            // Resolve locations
            Location playerLoc = fakePlayer.getLocation();
            if (playerLoc == null || target == null) {
                return failure();
            }

            // Ensure same world to avoid invalid direction calculations
            if (playerLoc.getWorld() != null && target.getWorld() != null && !playerLoc.getWorld().equals(target.getWorld())) {
                return failure();
            }

            Location targetLoc = (target instanceof LivingEntity living)
                    ? living.getEyeLocation()
                    : target.getLocation();

            // Compute direction vector and rotate the fake player to face the target
            Vector toTarget = targetLoc.toVector().subtract(playerLoc.toVector());
            if (toTarget.lengthSquared() == 0) {
                return failure();
            }

            playerLoc.setDirection(toTarget);
            fakePlayer.setLocation(playerLoc);

            // In a real implementation, this would send packets to rotate the NPC's head.
            // For now, we just log the action.
            Logger.getLogger("Minecraft").info("FakePlayer " + fakePlayer.getName() + " is looking at " + target.getName());
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
        return super.getCategory();
    }
}
