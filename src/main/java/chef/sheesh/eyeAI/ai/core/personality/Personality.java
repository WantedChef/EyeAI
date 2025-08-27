package chef.sheesh.eyeAI.ai.core.personality;

/**
 * Represents the personality of an AI agent.
 * Each personality has a set of traits that influence behavior.
 */
public enum Personality {
    AGGRESSIVE(1.5, 0.5, 0.8, 0.2),
    DEFENSIVE(0.5, 1.5, 0.5, 1.2),
    STRATEGIC(1.0, 1.0, 1.5, 0.8),
    CHAOTIC(1.2, 1.2, 0.2, 1.5);

    private final double aggressionModifier;
    private final double defensiveModifier;
    private final double strategicModifier;
    private final double chaoticModifier;

    Personality(double aggressionModifier, double defensiveModifier, double strategicModifier, double chaoticModifier) {
        this.aggressionModifier = aggressionModifier;
        this.defensiveModifier = defensiveModifier;
        this.strategicModifier = strategicModifier;
        this.chaoticModifier = chaoticModifier;
    }

    public double getAggressionModifier() {
        return aggressionModifier;
    }

    public double getDefensiveModifier() {
        return defensiveModifier;
    }

    public double getStrategicModifier() {
        return strategicModifier;
    }

    public double getChaoticModifier() {
        return chaoticModifier;
    }
}
