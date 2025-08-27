package chef.sheesh.eyeAI.ai.behavior.nodes;

import chef.sheesh.eyeAI.ai.behavior.IBehaviorTree;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Parallel composite node that executes all children concurrently.
 * Can succeed when a specified number of children succeed (successPolicy)
 * and fail when a specified number of children fail (failurePolicy).
 */
public class ParallelNode extends CompositeNode {

    public enum Policy {
        REQUIRE_ONE,    // Succeed/fail when at least one child succeeds/fails
        REQUIRE_ALL,    // Succeed/fail only when all children succeed/fail
        REQUIRE_MAJORITY // Succeed/fail when majority of children succeed/fail
    }

    private final Policy successPolicy;
    private final Policy failurePolicy;
    private final List<ExecutionResult> childResults;

    public ParallelNode(Policy successPolicy, Policy failurePolicy) {
        super();
        this.successPolicy = successPolicy;
        this.failurePolicy = failurePolicy;
        this.childResults = new ArrayList<>();
    }

    public ParallelNode(IBehaviorTree[] children, Policy successPolicy, Policy failurePolicy) {
        super(children);
        this.successPolicy = successPolicy;
        this.failurePolicy = failurePolicy;
        this.childResults = new ArrayList<>();
    }

    @Override
    public ExecutionResult execute(IFakePlayer fakePlayer) {
        if (hasChildren()) {
            return failure();
        }

        // Execute all children
        childResults.clear();
        int successCount = 0;
        int failureCount = 0;
        int runningCount = 0;

        for (IBehaviorTree child : children) {
            ExecutionResult result = child.execute(fakePlayer);
            childResults.add(result);

            switch (result) {
                case SUCCESS -> successCount++;
                case FAILURE -> failureCount++;
                case RUNNING -> runningCount++;
            }
        }

        // Check success policy
        if (shouldSucceed(successCount, failureCount, runningCount)) {
            return success();
        }

        // Check failure policy
        if (shouldFail(successCount, failureCount, runningCount)) {
            return failure();
        }

        // Still running
        return running();
    }

    private boolean shouldSucceed(int successCount, int failureCount, int runningCount) {
        int totalChildren = children.size();

        return switch (successPolicy) {
            case REQUIRE_ONE -> successCount >= 1;
            case REQUIRE_ALL -> successCount == totalChildren;
            case REQUIRE_MAJORITY -> successCount > totalChildren / 2;
        };
    }

    private boolean shouldFail(int successCount, int failureCount, int runningCount) {
        int totalChildren = children.size();

        return switch (failurePolicy) {
            case REQUIRE_ONE -> failureCount >= 1;
            case REQUIRE_ALL -> failureCount == totalChildren && runningCount == 0;
            case REQUIRE_MAJORITY -> failureCount > totalChildren / 2;
        };
    }

    @Override
    public void reset() {
        super.reset();
        childResults.clear();
    }

    @Override
    public String getDescription() {
        return String.format("Parallel (%d children, %s/%s)",
               getChildCount(), successPolicy.name(), failurePolicy.name());
    }

    /**
     * Get the results of the last execution for debugging
     */
    public List<ExecutionResult> getLastResults() {
        return new ArrayList<>(childResults);
    }
}
