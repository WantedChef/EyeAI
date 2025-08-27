package chef.sheesh.eyeAI.ai.behavior.nodes;

import chef.sheesh.eyeAI.ai.behavior.IBehaviorTree;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;

/**
 * Sequence composite node.
 * Executes children in order, returns SUCCESS only if all children succeed.
 * Returns FAILURE if any child fails, RUNNING if any child is running.
 */
public class SequenceNode extends CompositeNode {

    public SequenceNode() {
        super();
    }

    public SequenceNode(IBehaviorTree... children) {
        super(children);
    }

    @Override
    public ExecutionResult execute(IFakePlayer fakePlayer) {
        if (!(fakePlayer instanceof FakePlayer)) {
            return failure();
        }

        if (hasChildren()) {
            return success();
        }

        // Execute current child
        IBehaviorTree currentChild = getCurrentChild();
        if (currentChild == null) {
            // All children completed successfully
            resetToFirstChild();
            return success();
        }

        ExecutionResult childResult = currentChild.execute(fakePlayer);

        return switch (childResult) {
            case SUCCESS -> {
                // Move to next child
                if (nextChild()) {
                    // All children completed successfully
                    resetToFirstChild();
                    yield success();
                }
                yield running();
            }
            case FAILURE -> {
                // Sequence failed, reset and return failure
                resetToFirstChild();
                yield failure();
            }
            case RUNNING ->
                // Child is still running, return running
                    running();
        };
    }

    @Override
    public void reset() {
        super.reset();
    }

    @Override
    public String getDescription() {
        return String.format("Sequence (%d children)", getChildCount());
    }
}
