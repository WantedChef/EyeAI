package chef.sheesh.eyeAI.ai.behavior.nodes;

import chef.sheesh.eyeAI.ai.behavior.IBehaviorTree;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;

/**
 * Selector composite node.
 * Executes children in order until one succeeds.
 * Returns SUCCESS if any child succeeds, FAILURE only if all children fail.
 */
public class SelectorNode extends CompositeNode {

    public SelectorNode() {
        super();
    }

    public SelectorNode(IBehaviorTree... children) {
        super(children);
    }

    @Override
    public ExecutionResult execute(IFakePlayer fakePlayer) {
        if (!(fakePlayer instanceof FakePlayer)) {
            return failure();
        }

        if (hasChildren()) {
            return failure();
        }

        // Execute current child
        IBehaviorTree currentChild = getCurrentChild();
        if (currentChild == null) {
            // All children failed
            resetToFirstChild();
            return failure();
        }

        ExecutionResult childResult = currentChild.execute(fakePlayer);

        return switch (childResult) {
            case SUCCESS -> {
                // Child succeeded, reset for next time and return success
                resetToFirstChild();
                yield success();
            }
            case FAILURE -> {
                // Child failed, try next child
                if (nextChild()) {
                    // All children failed
                    resetToFirstChild();
                    yield failure();
                }
                yield running();
            }
            case RUNNING ->
                // Child is still running
                    running();
        };
    }

    @Override
    public void reset() {
        super.reset();
    }

    @Override
    public String getDescription() {
        return String.format("Selector (%d children)", getChildCount());
    }
}
