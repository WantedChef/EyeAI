package chef.sheesh.eyeAI.ai.behavior.trees;

import chef.sheesh.eyeAI.ai.behavior.BehaviorTree;
import chef.sheesh.eyeAI.ai.behavior.nodes.AttackNode;
import chef.sheesh.eyeAI.ai.behavior.nodes.EmotionConditionNode;
import chef.sheesh.eyeAI.ai.behavior.nodes.HasTargetCondition;
import chef.sheesh.eyeAI.ai.behavior.nodes.MoveToNode;
import chef.sheesh.eyeAI.ai.behavior.nodes.SequenceNode;
import chef.sheesh.eyeAI.ai.core.emotions.Emotion;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;

/**
 * A behavior tree for aggressive combat, influenced by emotions.
 * The agent will only engage if it has a target, is angry enough, and not too scared.
 */
public class AggressiveCombatBehaviorTree extends BehaviorTree {

    private final BehaviorTree root;

    public AggressiveCombatBehaviorTree(IFakePlayer agent) {
        // The root of our combat behavior is a sequence of actions and conditions.
        this.root = new SequenceNode(
            // Condition: Agent must have a target.
            new HasTargetCondition(),
            // Condition: Agent's ANGER must be greater than 0.5.
            new EmotionConditionNode(Emotion.ANGER, 0.5, EmotionConditionNode.Comparison.GREATER_THAN),
            // Condition: Agent's FEAR must be less than 0.4.
            new EmotionConditionNode(Emotion.FEAR, 0.4, EmotionConditionNode.Comparison.LESS_THAN),
            // Action: If conditions are met, move towards the target.
            new MoveToNode(),
            // Action: And then attack it.
            new AttackNode()
        );
        this.root.setName("EmotionallyDrivenAggressiveCombat");
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
        return "Combat";
    }
}
