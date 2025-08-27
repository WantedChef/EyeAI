package chef.sheesh.eyeAI.core.ml.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SocialMatrixTest {

    private SocialMatrix socialMatrix;
    private UUID agent1;
    private UUID agent2;
    private UUID agent3;

    @BeforeEach
    void setUp() {
        socialMatrix = new SocialMatrix();
        agent1 = UUID.randomUUID();
        agent2 = UUID.randomUUID();
        agent3 = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should initialize with zero relationships")
    void testInitialization() {
        assertEquals(0.0, socialMatrix.getRelationship(agent1, agent2));
        assertEquals(0.0, socialMatrix.getRelationship(agent2, agent3));
    }

    @Test
    @DisplayName("Should update relationships bidirectionally")
    void testUpdateRelationships() {
        double relationshipValue = 0.5;
        socialMatrix.updateRelationship(agent1, agent2, relationshipValue);
        
        assertEquals(relationshipValue, socialMatrix.getRelationship(agent1, agent2));
        assertEquals(relationshipValue, socialMatrix.getRelationship(agent2, agent1));
    }

    @Test
    @DisplayName("Should decay relationships over time")
    void testDecayRelationships() {
        double initialRelationship = 0.8;
        double decayFactor = 0.9;
        socialMatrix.updateRelationship(agent1, agent2, initialRelationship);
        
        socialMatrix.decayRelationships(decayFactor);
        
        // Relationship should be decayed but not zero
        double expectedDecayedValue = initialRelationship * decayFactor;
        assertEquals(expectedDecayedValue, socialMatrix.getRelationship(agent1, agent2));
    }

    @Test
    @DisplayName("Should calculate social influence correctly")
    void testCalculateSocialInfluence() {
        // Set up relationships
        socialMatrix.updateRelationship(agent1, agent2, 0.5);
        socialMatrix.updateRelationship(agent2, agent3, 0.3);
        
        // Calculate influence
        double influence = socialMatrix.calculateSocialInfluence(agent1, agent3, 2);
        
        // With maxDepth=2, influence should be 0.5 * 0.3 = 0.15
        assertEquals(0.15, influence);
    }
}
