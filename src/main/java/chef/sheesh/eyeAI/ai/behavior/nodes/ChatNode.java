package chef.sheesh.eyeAI.ai.behavior.nodes;

import chef.sheesh.eyeAI.ai.behavior.BehaviorTree;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;

/**
 * A node that makes the agent send a chat message.
 */
public class ChatNode extends BehaviorTree {

    public ChatNode(String message) {
    }

    @Override
    public ExecutionResult execute(IFakePlayer fakePlayer) {
        // This assumes a chat method exists on the IFakePlayer interface.
        // We will need to add it.
        // fakePlayer.chat(message);
        return success();
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
