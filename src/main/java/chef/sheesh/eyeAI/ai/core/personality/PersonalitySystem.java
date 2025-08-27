package chef.sheesh.eyeAI.ai.core.personality;

/**
 * Manages the personality of an AI agent.
 */
public class PersonalitySystem {

    private Personality personality;

    public PersonalitySystem(Personality personality) {
        this.personality = personality;
    }

    public Personality getPersonality() {
        return personality;
    }

    public void setPersonality(Personality personality) {
        this.personality = personality;
    }
}
