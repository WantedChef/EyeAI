package chef.sheesh.eyeAI.ai.behavior.nodes;

import chef.sheesh.eyeAI.ai.behavior.IBehaviorTree;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;

/**
 * Repeat decorator that repeats the child execution until it succeeds
 * or reaches a maximum number of attempts.
 */
public class RepeatDecorator extends DecoratorNode {

    private final int maxAttempts;
    private int currentAttempts = 0;

    public RepeatDecorator(int maxAttempts) {
        super();
        this.maxAttempts = maxAttempts;
    }

    public RepeatDecorator(IBehaviorTree child, int maxAttempts) {
        super(child);
        this.maxAttempts = maxAttempts;
    }

    @Override
    public ExecutionResult execute(IFakePlayer fakePlayer) {
        if (child == null) {
            return failure();
        }

        if (currentAttempts >= maxAttempts) {
            return failure(); // Max attempts reached
        }

        ExecutionResult childResult = child.execute(fakePlayer);

        switch (childResult) {
            case SUCCESS:
                // Reset for next time
                currentAttempts = 0;
                return success();

            case FAILURE:
                currentAttempts++;
                if (currentAttempts >= maxAttempts) {
                    return failure();
                }
                return running(); // Try again

            case RUNNING:
                return running();

            default:
                return failure();
        }
    }

    @Override
    public void reset() {
        super.reset();
        currentAttempts = 0;
    }

    @Override
    public String getDescription() {
        return String.format("Repeat (%s, %d/%d)",
               child != null ? child.getDescription() : "no child",
               currentAttempts, maxAttempts);
    }
}
