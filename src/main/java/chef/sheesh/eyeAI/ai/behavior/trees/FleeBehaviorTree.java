package chef.sheesh.eyeAI.ai.behavior.trees;

import chef.sheesh.eyeAI.ai.behavior.BehaviorTree;
import chef.sheesh.eyeAI.ai.behavior.nodes.EmotionConditionNode;
import chef.sheesh.eyeAI.ai.behavior.nodes.FindSafeLocationNode;
import chef.sheesh.eyeAI.ai.behavior.nodes.HasTargetCondition;
import chef.sheesh.eyeAI.ai.behavior.nodes.MoveToNode;
import chef.sheesh.eyeAI.ai.behavior.nodes.SequenceNode;
import chef.sheesh.eyeAI.ai.core.emotions.Emotion;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;

/**
 * A behavior tree for fleeing from a threat, driven by fear.
 */
public class FleeBehaviorTree extends BehaviorTree {

    private final BehaviorTree root;

    public FleeBehaviorTree(IFakePlayer agent) {
        // The root of our flee behavior is a sequence of actions and conditions.
        this.root = new SequenceNode(
            // Condition: Agent must have a target to flee from.
            new HasTargetCondition(),
            // Condition: Agent's FEAR must be greater than 0.6.
            new EmotionConditionNode(Emotion.FEAR, 0.6, EmotionConditionNode.Comparison.GREATER_THAN),
            // Action: Find a safe location away from the target.
            new FindSafeLocationNode(),
            // Action: Move to the safe location stored in the blackboard.
            new MoveToNode("flee_location")
        );
        this.root.setName("FearDrivenFlee");
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
