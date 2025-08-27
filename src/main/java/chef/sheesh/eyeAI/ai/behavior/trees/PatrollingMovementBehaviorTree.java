package chef.sheesh.eyeAI.ai.behavior.trees;

import chef.sheesh.eyeAI.ai.behavior.BehaviorTree;
import chef.sheesh.eyeAI.ai.behavior.IBehaviorTree;
import chef.sheesh.eyeAI.ai.behavior.nodes.MoveToNode;
import chef.sheesh.eyeAI.ai.behavior.nodes.RepeatDecorator;
import chef.sheesh.eyeAI.ai.behavior.nodes.SequenceNode;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;
import org.bukkit.Location;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A behavior tree for patrolling a set of points indefinitely.
 * The agent will move to each point in the list in order, and then repeat.
 */
public class PatrollingMovementBehaviorTree extends BehaviorTree {

    private final BehaviorTree root;

    public PatrollingMovementBehaviorTree(IFakePlayer agent, List<Location> patrolPoints) {
        if (patrolPoints == null || patrolPoints.isEmpty()) {
            throw new IllegalArgumentException("Patrol points cannot be null or empty.");
        }

        // Create a sequence of MoveToNodes for each patrol point.
        List<IBehaviorTree> moveNodes = patrolPoints.stream()
                .map(MoveToNode::new)
                .collect(Collectors.toList());

        SequenceNode patrolSequence = new SequenceNode(moveNodes.toArray(new IBehaviorTree[0]));
        patrolSequence.setName("PatrolSequence");

        // Repeat the sequence indefinitely.
        this.root = new RepeatDecorator(patrolSequence, Integer.MAX_VALUE);
        this.root.setName("EndlessPatrol");
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
        return "Movement";
    }
}
