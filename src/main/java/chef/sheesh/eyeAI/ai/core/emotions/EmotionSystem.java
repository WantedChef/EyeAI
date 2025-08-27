package chef.sheesh.eyeAI.ai.core.emotions;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the emotional state of an AI agent.
 * The emotional state is represented by a set of values for different emotions.
 * These values can influence the agent's behavior.
 */
public class EmotionSystem {

    private final Map<Emotion, Double> emotions;

    public EmotionSystem() {
        this.emotions = new ConcurrentHashMap<>();
        for (Emotion emotion : Emotion.values()) {
            emotions.put(emotion, 0.0);
        }
    }

    /**
     * Gets the value of a specific emotion.
     *
     * @param emotion The emotion to query.
     * @return The value of the emotion, from 0.0 to 1.0.
     */
    public double getEmotionValue(Emotion emotion) {
        return emotions.getOrDefault(emotion, 0.0);
    }

    /**
     * Sets the value of a specific emotion.
     *
     * @param emotion The emotion to set.
     * @param value The new value, clamped between 0.0 and 1.0.
     */
    public void setEmotionValue(Emotion emotion, double value) {
        emotions.put(emotion, Math.max(0.0, Math.min(1.0, value)));
    }

    /**
     * Increases an emotion by a certain amount.
     *
     * @param emotion The emotion to increase.
     * @param amount The amount to increase by.
     */
    public void increaseEmotion(Emotion emotion, double amount) {
        setEmotionValue(emotion, getEmotionValue(emotion) + amount);
    }

    /**
     * Decreases an emotion by a certain amount.
     *
     * @param emotion The emotion to decrease.
     * @param amount The amount to decrease by.
     */
    public void decreaseEmotion(Emotion emotion, double amount) {
        setEmotionValue(emotion, getEmotionValue(emotion) - amount);
    }

    /**
     * Gets the dominant emotion.
     *
     * @return The emotion with the highest value.
     */
    public Emotion getDominantEmotion() {
        return emotions.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(Emotion.JOY); // Default emotion
    }
}
