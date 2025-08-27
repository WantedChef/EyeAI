package chef.sheesh.eyeAI.ai.behavior.nodes;

import chef.sheesh.eyeAI.ai.behavior.IBehaviorTree;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;

/**
 * Inverter decorator that inverts the result of its child.
 * SUCCESS becomes FAILURE, FAILURE becomes SUCCESS, RUNNING remains RUNNING.
 */
public class InvertDecorator extends DecoratorNode {

    public InvertDecorator() {
        super();
    }

    public InvertDecorator(IBehaviorTree child) {
        super(child);
    }

    @Override
    public ExecutionResult execute(IFakePlayer fakePlayer) {
        if (child == null) {
            return failure();
        }

        ExecutionResult childResult = child.execute(fakePlayer);

        return switch (childResult) {
            case SUCCESS -> failure();
            case FAILURE -> success();
            case RUNNING -> running();
        };
    }

    @Override
    public String getDescription() {
        return String.format("Invert (%s)",
               child != null ? child.getDescription() : "no child");
    }
}
