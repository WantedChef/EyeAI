package chef.sheesh.eyeAI.ai.behavior.nodes;

import chef.sheesh.eyeAI.ai.behavior.BehaviorTree;
import chef.sheesh.eyeAI.ai.core.emotions.Emotion;
import chef.sheesh.eyeAI.ai.core.emotions.EmotionSystem;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;

/**
 * A condition node that checks the value of an agent's emotion.
 */
public class EmotionConditionNode extends BehaviorTree {

    public enum Comparison {
        GREATER_THAN,
        LESS_THAN
    }

    private final Emotion emotion;
    private final double threshold;
    private final Comparison comparison;

    public EmotionConditionNode(Emotion emotion, double threshold, Comparison comparison) {
        this.emotion = emotion;
        this.threshold = threshold;
        this.comparison = comparison;
        this.name = "EmotionCondition";
        this.description = "Checks if " + emotion + " is " + comparison.name().toLowerCase().replace('_', ' ') + " " + threshold;
    }

    @Override
    public ExecutionResult execute(IFakePlayer fakePlayer) {
        EmotionSystem emotionSystem = fakePlayer.getEmotionSystem();
        if (emotionSystem == null) {
            return failure(); // No emotion system available
        }


        double value = emotionSystem.getEmotionValue(emotion);
        boolean conditionMet = switch (comparison) {
            case GREATER_THAN -> value > threshold;
            case LESS_THAN -> value < threshold;
        };

        return conditionMet ? success() : failure();
    }

    @Override
    public void reset() {
        // No state to reset
    }

    @Override
    public String getCategory() {
        return "Condition";
    }
}
