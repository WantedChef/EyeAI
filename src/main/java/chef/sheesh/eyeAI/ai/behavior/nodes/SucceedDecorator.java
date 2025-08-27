package chef.sheesh.eyeAI.ai.behavior.nodes;

import chef.sheesh.eyeAI.ai.behavior.IBehaviorTree;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;

/**
 * Succeed decorator that always returns SUCCESS regardless of the child's result.
 * Useful for cleanup actions that should always be considered successful.
 */
public class SucceedDecorator extends DecoratorNode {

    public SucceedDecorator() {
        super();
    }

    public SucceedDecorator(IBehaviorTree child) {
        super(child);
    }

    @Override
    public ExecutionResult execute(IFakePlayer fakePlayer) {
        if (child == null) {
            return success();
        }

        // Execute child but ignore result
        child.execute(fakePlayer);
        return success();
    }

    @Override
    public String getDescription() {
        return String.format("Succeed (%s)",
               child != null ? child.getDescription() : "no child");
    }
}
