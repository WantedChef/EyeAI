package chef.sheesh.eyeAI.ai.behavior.nodes;

import chef.sheesh.eyeAI.ai.core.DecisionContext;

/**
 * Condition that checks if the fake player's health is low.
 */
public class HealthLowCondition extends ConditionNode {

    private final double threshold;

    public HealthLowCondition() {
        this.threshold = 6.0; // 30% of 20 health
    }

    public HealthLowCondition(double threshold) {
        this.threshold = threshold;
    }

    @Override
    protected boolean evaluate(DecisionContext context) {
        return context.getHealth() < threshold;
    }

    @Override
    public String getDescription() {
        return "HealthLow (< " + threshold + ")";
    }
}

