package chef.sheesh.eyeAI.ai.behavior.trees;

import chef.sheesh.eyeAI.ai.behavior.BehaviorTree;
import chef.sheesh.eyeAI.ai.behavior.nodes.SelectorNode;
import chef.sheesh.eyeAI.ai.behavior.trees.team.LeaderBehaviorTree;
import chef.sheesh.eyeAI.ai.behavior.trees.team.SupportBehaviorTree;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;
import org.bukkit.Location;

import java.util.List;

/**
 * The root behavior tree for an AI agent.
 * It uses a selector to choose between different high-level behaviors
 * like fleeing, fighting, or patrolling.
 */
public class AgentBehaviorTree extends BehaviorTree {

    private final BehaviorTree root;

    public AgentBehaviorTree(IFakePlayer agent, List<Location> patrolPoints) {
        // The root of the agent's behavior is a selector that tries behaviors in order of priority.
        this.root = new SelectorNode(
            // 1. Highest priority: Flee if scared.
            new FleeBehaviorTree(agent),

            // 2. Team behaviors
            new LeaderBehaviorTree(agent),
            new SupportBehaviorTree(agent),

            // 3. Next priority: Fight if angry and has a target.
            new AggressiveCombatBehaviorTree(agent),

            // 4. Default behavior: Patrol a set of points.
            new PatrollingMovementBehaviorTree(agent, patrolPoints)
        );
        this.root.setName("AgentRootSelector");
    }

    @Override
    public ExecutionResult execute(IFakePlayer fakePlayer) {
        return root.execute(fakePlayer);
    }

    @Override
    public void reset() {
        root.reset();
    }

    @Override
    public String getCategory() {
        return "Meta";
    }
}
