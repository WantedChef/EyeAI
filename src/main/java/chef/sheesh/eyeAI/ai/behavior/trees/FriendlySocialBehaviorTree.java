package chef.sheesh.eyeAI.ai.behavior.trees;

import chef.sheesh.eyeAI.ai.behavior.BehaviorTree;
import chef.sheesh.eyeAI.ai.behavior.nodes.ChatNode;
import chef.sheesh.eyeAI.ai.behavior.nodes.FindNearbyPlayerNode;
import chef.sheesh.eyeAI.ai.behavior.nodes.LookAtNode;
import chef.sheesh.eyeAI.ai.behavior.nodes.SequenceNode;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;

/**
 * A behavior tree for friendly social interaction.
 * The agent will find a nearby player, look at them, and greet them.
 */
public class FriendlySocialBehaviorTree extends BehaviorTree {

    private final BehaviorTree root;

    public FriendlySocialBehaviorTree(IFakePlayer agent) {
        this.root = new SequenceNode(
            new FindNearbyPlayerNode(10), // Find player within 10 blocks
            new LookAtNode("target_player"),
            new ChatNode("Hello, friend!")
        );
        this.root.setName("FriendlySocialSequence");
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
        return "Social";
    }
}
