package chef.sheesh.eyeAI.core.management;

/**
 * Represents the complexity level of the AI.
 * This can be adjusted based on server load.
 */
public enum AIComplexityLevel {
    /**
     * Lowest complexity. Simple behaviors, low tick rate.
     */
    LOW,
    /**
     * Medium complexity. Standard behaviors.
     */
    MEDIUM,
    /**
     * High complexity. Advanced behaviors, full tick rate.
     */
    HIGH;
}
