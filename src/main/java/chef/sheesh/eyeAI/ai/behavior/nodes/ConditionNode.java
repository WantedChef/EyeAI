package chef.sheesh.eyeAI.ai.behavior.nodes;

import chef.sheesh.eyeAI.ai.behavior.BehaviorTree;
import chef.sheesh.eyeAI.ai.core.DecisionContext;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;

/**
 * Modern abstract base class for condition nodes.
 * Condition nodes evaluate game state and return SUCCESS or FAILURE.
 */
public abstract class ConditionNode extends BehaviorTree {

    @Override
    public final ExecutionResult execute(IFakePlayer fakePlayer) {
        if (!(fakePlayer instanceof FakePlayer)) {
            return failure();
        }

        DecisionContext context = ((FakePlayer) fakePlayer).createDecisionContext();
        return evaluate(context) ? success() : failure();
    }

    /**
     * Evaluate the condition based on the current decision context
     * @param context The current decision context
     * @return true if condition is met, false otherwise
     */
    protected abstract boolean evaluate(DecisionContext context);

    @Override
    public void reset() {
        // Conditions don't have state to reset
        markNotRunning();
    }

    @Override
    public String getCategory() {
        return "Condition";
    }

    @Override
    public String getDescription() {
        return getClass().getSimpleName();
    }
}

