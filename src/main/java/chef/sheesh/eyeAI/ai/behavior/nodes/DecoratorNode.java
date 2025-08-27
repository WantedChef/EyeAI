package chef.sheesh.eyeAI.ai.behavior.nodes;

import chef.sheesh.eyeAI.ai.behavior.BehaviorTree;
import chef.sheesh.eyeAI.ai.behavior.IBehaviorTree;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;
import lombok.Getter;

/**
 * Abstract base class for decorator nodes.
 * Decorators modify the behavior of their child node.
 */
@Getter
public abstract class DecoratorNode extends BehaviorTree {

    /**
     * -- GETTER --
     *  Get the child node
     */
    protected IBehaviorTree child;

    public DecoratorNode() {
        super();
    }

    public DecoratorNode(IBehaviorTree child) {
        this.child = child;
    }

    /**
     * Set the child node
     */
    public void setChild(IBehaviorTree child) {
        this.child = child;
    }

    @Override
    public void reset() {
        if (child != null) {
            child.reset();
        }
        markNotRunning();
    }

    @Override
    public String getCategory() {
        return "Decorator";
    }

    @Override
    public String getDescription() {
        return String.format("%s (%s)",
               getClass().getSimpleName(),
               child != null ? child.getDescription() : "no child");
    }

    /**
     * Abstract method to be implemented by concrete decorators
     */
    @Override
    public abstract ExecutionResult execute(IFakePlayer fakePlayer);
}
