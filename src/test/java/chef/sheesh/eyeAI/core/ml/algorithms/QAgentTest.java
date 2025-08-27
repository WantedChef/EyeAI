package chef.sheesh.eyeAI.core.ml.algorithms;

import chef.sheesh.eyeAI.core.ml.models.Action;
import chef.sheesh.eyeAI.core.ml.models.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class QAgentTest {

    private QAgent qAgent;

    @BeforeEach
    void setUp() {
        qAgent = new QAgent(0.1, 0.9, 0.1);
    }

    @Test
    @DisplayName("Should select some action when exploiting a new state")
    void testExploitationOnNewState() {
        qAgent.setExplorationRate(0.0); // Force exploitation
        GameState state = mock(GameState.class);
        Action action = qAgent.decideAction(state);
        assertNotNull(action);
    }

    @Test
    @DisplayName("Should learn from experience correctly")
    void testLearning() {
        GameState state = mock(GameState.class);
        GameState nextState = mock(GameState.class);
        Action action = Action.ATTACK_ENTITY;
        double reward = 10.0;

        double initialQValue = qAgent.getQValue(state, action);
        assertEquals(0.0, initialQValue);

        qAgent.learn(state, action, reward, nextState);

        double learnedQValue = qAgent.getQValue(state, action);
        assertTrue(learnedQValue > 0.0);
    }

    @Test
    @DisplayName("Should handle null states gracefully")
    void testEdgeCases() {
        assertThrows(NullPointerException.class, () -> qAgent.decideAction(null));
    }
}
