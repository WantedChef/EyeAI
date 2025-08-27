package chef.sheesh.eyeAI.ai.behavior;

import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;

/**
 * Modern abstract base class for behavior tree nodes.
 * Provides unified execution model and common functionality.
 */
public abstract class BehaviorTree implements IBehaviorTree {

    protected String name = getClass().getSimpleName();
    protected String description = "";
    protected boolean isRunning = false;

    /**
     * Execute this behavior tree node
     * @param fakePlayer The fake player executing this behavior
     * @return The execution result
     */
    public abstract ExecutionResult execute(IFakePlayer fakePlayer);

    /**
     * Reset the state of this behavior tree node
     */
    public abstract void reset();

    /**
     * Get a human-readable description of this node
     */
    @Override
    public String getDescription() {
        return description.isEmpty() ? getClass().getSimpleName() : description;
    }

    /**
     * Set a custom description for this node
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Legacy tick method for backward compatibility
     */
    @Override
    public Status tick(FakePlayer fakePlayer) {
        ExecutionResult result = execute(fakePlayer);
        return switch (result) {
            case SUCCESS -> Status.SUCCESS;
            case FAILURE -> Status.FAILURE;
            case RUNNING -> Status.RUNNING;
        };
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the category of this node for debugging/visualization
     */
    @Override
    public String getCategory() {
        return "Action";
    }

    /**
     * Helper method to mark node as running
     */
    protected void markRunning() {
        isRunning = true;
    }

    /**
     * Helper method to mark node as not running
     */
    protected void markNotRunning() {
        isRunning = false;
    }

    /**
     * Utility method to create a simple success result
     */
    protected ExecutionResult success() {
        markNotRunning();
        return ExecutionResult.SUCCESS;
    }

    /**
     * Utility method to create a simple failure result
     */
    protected ExecutionResult failure() {
        markNotRunning();
        return ExecutionResult.FAILURE;
    }

    /**
     * Utility method to create a running result
     */
    protected ExecutionResult running() {
        markRunning();
        return ExecutionResult.RUNNING;
    }
}
