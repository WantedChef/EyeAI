package chef.sheesh.eyeAI.ai.behavior.nodes;

import chef.sheesh.eyeAI.ai.behavior.IBehaviorTree;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;

/**
 * Timeout decorator that fails if the child takes longer than the specified time.
 */
public class TimeoutDecorator extends DecoratorNode {

    private final long timeoutMillis;
    private long startTime = 0;

    public TimeoutDecorator(long timeoutMillis) {
        super();
        this.timeoutMillis = timeoutMillis;
    }

    public TimeoutDecorator(IBehaviorTree child, long timeoutMillis) {
        super(child);
        this.timeoutMillis = timeoutMillis;
    }

    @Override
    public ExecutionResult execute(IFakePlayer fakePlayer) {
        if (child == null) {
            return failure();
        }

        long currentTime = System.currentTimeMillis();

        // Initialize start time on first execution
        if (startTime == 0) {
            startTime = currentTime;
        }

        // Check timeout
        if (currentTime - startTime >= timeoutMillis) {
            child.reset(); // Reset child on timeout
            return failure();
        }

        ExecutionResult childResult = child.execute(fakePlayer);

        if (childResult.isSuccess() || childResult.isFailure()) {
            // Child completed, reset timer
            startTime = 0;
        }

        return childResult;
    }

    @Override
    public void reset() {
        super.reset();
        startTime = 0;
    }

    @Override
    public String getDescription() {
        return String.format("Timeout (%s, %dms)",
               child != null ? child.getDescription() : "no child",
               timeoutMillis);
    }
}
