package chef.sheesh.eyeAI.ai.behavior;

import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;

/**
 * Modern behavior tree interface with unified execution model
 */
public interface IBehaviorTree {

    /**
     * Execute the behavior tree for a fake player
     * @param fakePlayer The fake player to execute for
     * @return ExecutionResult indicating success/failure/running
     */
    ExecutionResult execute(IFakePlayer fakePlayer);

    /**
     * Reset the behavior tree state
     */
    void reset();

    /**
     * Check if the behavior tree is currently running
     */
    boolean isRunning();

    /**
     * Get the name of this behavior tree
     */
    String getName();

    /**
     * Set the name of this behavior tree
     */
    void setName(String name);

    /**
     * Get a human-readable description of this behavior tree
     */
    String getDescription();

    /**
     * Get the category of this node for debugging/visualization
     */
    default String getCategory() {
        return "Node";
    }

    /**
     * Unified execution result enum
     */
    enum ExecutionResult {
        SUCCESS,
        FAILURE,
        RUNNING;

        /**
         * Convert to boolean success indicator
         */
        public boolean isSuccess() {
            return this == SUCCESS;
        }

        /**
         * Convert to boolean failure indicator
         */
        public boolean isFailure() {
            return this == FAILURE;
        }

        /**
         * Convert to boolean running indicator
         */
        public boolean isRunning() {
            return this == RUNNING;
        }
    }

    /**
     * Legacy compatibility methods
     */
    default Status tick(FakePlayer fakePlayer) {
        ExecutionResult result = execute(fakePlayer);
        return switch (result) {
            case SUCCESS -> Status.SUCCESS;
            case FAILURE -> Status.FAILURE;
            case RUNNING -> Status.RUNNING;
        };
    }

    /**
     * Legacy status enum for backward compatibility
     */
    enum Status {
        SUCCESS,
        FAILURE,
        RUNNING
    }
}
