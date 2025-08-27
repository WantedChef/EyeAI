package chef.sheesh.eyeAI.ai.core.tactics;

import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.List;

/**
 * An engine for making tactical decisions.
 * This class provides methods for complex behaviors like flanking and kiting.
 */
public class TacticEngine {

    /**
     * Finds a flanking position around a target.
     *
     * @param agent The agent that is flanking.
     * @param target The target to flank.
     * @return A suitable flanking position, or null if none is found.
     */
    public Location findFlankingPosition(IFakePlayer agent, Entity target) {
        // Placeholder implementation. A real implementation would be more complex.
        Location agentLoc = agent.getLocation();
        Location targetLoc = target.getLocation();
        // Get the vector from target to agent, rotate it 90 degrees, and go 5 blocks out.
        double x = agentLoc.getX() - targetLoc.getX();
        double z = agentLoc.getZ() - targetLoc.getZ();
        double newX = -z;
        double newZ = x;
        return targetLoc.clone().add(newX, 0, newZ);
    }

    /**
     * Finds a kiting position away from a target.
     *
     * @param agent The agent that is kiting.
     * @param target The target to kite from.
     * @return A suitable kiting position, or null if none is found.
     */
    public Location findKitingPosition(IFakePlayer agent, Entity target) {
        // Placeholder implementation.
        Location agentLoc = agent.getLocation();
        Location targetLoc = target.getLocation();
        // Get the vector from target to agent, and go 10 blocks further.
        double x = agentLoc.getX() - targetLoc.getX();
        double z = agentLoc.getZ() - targetLoc.getZ();
        return agentLoc.clone().add(x, 0, z);
    }

    /**
     * Selects the best target for crowd control.
     *
     * @param agent The agent.
     * @param targets A list of potential targets.
     * @return The best target for crowd control.
     */
    public Entity selectCrowdControlTarget(IFakePlayer agent, List<Entity> targets) {
        // Placeholder: just return the first target.
        return targets.isEmpty() ? null : targets.get(0);
    }

    /**
     * Decides if the agent should use a health potion.
     *
     * @param agent The agent.
     * @return True if the agent should use a health potion, false otherwise.
     */
    public boolean shouldUseHealthPotion(IFakePlayer agent) {
        // Placeholder: use a potion if health is below 50%.
        return agent.getHealth() < 10.0;
    }
}
