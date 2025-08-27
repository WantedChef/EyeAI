package chef.sheesh.eyeAI.ai.behavior.nodes;

import chef.sheesh.eyeAI.ai.core.DecisionContext;

/**
 * Condition that checks if the fake player has a target to attack.
 */
public class HasTargetCondition extends ConditionNode {

    @Override
    protected boolean evaluate(DecisionContext context) {
        return context.getCurrentTarget().isPresent() ||
               context.hasHostileNearby();
    }

    @Override
    public String getDescription() {
        return "HasTarget";
    }
}

