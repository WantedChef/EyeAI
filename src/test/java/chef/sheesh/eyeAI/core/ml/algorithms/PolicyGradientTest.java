package chef.sheesh.eyeAI.core.ml.algorithms;

import chef.sheesh.eyeAI.core.ml.models.Action;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class PolicyGradientTest {

    private PolicyGradient policyGradient;
    private static final int STATE_SIZE = 5;
    private static final int ACTION_SIZE = Action.values().length;
    private static final int HIDDEN_SIZE = 10;
    private static final double LEARNING_RATE = 0.001;
    private static final int BATCH_SIZE = 32;
    private static final double DISCOUNT_FACTOR = 0.95;
    private static final double GAE_LAMBDA = 0.95;
    private static final double CLIP_EPSILON = 0.2;

    @BeforeEach
    void setUp() {
        policyGradient = new PolicyGradient(STATE_SIZE, ACTION_SIZE, HIDDEN_SIZE, LEARNING_RATE,
                                           BATCH_SIZE, DISCOUNT_FACTOR, GAE_LAMBDA, CLIP_EPSILON);
    }

    @Test
    @DisplayName("Should initialize PolicyGradient with correct parameters")
    void testInitialization() {
        assertNotNull(policyGradient);
        // We can't directly access private fields, but we can verify the behavior
        // by checking if the network can make predictions
        double[] testState = new double[STATE_SIZE];
        double[] probabilities = policyGradient.predictActionProbabilities(testState);
        assertEquals(ACTION_SIZE, probabilities.length);
        
        // Verify probabilities sum to 1
        double sum = 0;
        for (double prob : probabilities) {
            sum += prob;
        }
        assertEquals(1.0, sum, 0.001);
    }

    @Test
    @DisplayName("Should select valid action index")
    void testActionSelection() {
        double[] testState = new double[STATE_SIZE];
        int action = policyGradient.selectAction(testState);
        assertTrue(action >= 0 && action < ACTION_SIZE);
    }

    @Test
    @DisplayName("Should handle training with experiences")
    void testTraining() {
        // Create mock experiences
        double[] state = new double[STATE_SIZE];
        double[] nextState = new double[STATE_SIZE];
        
        // Fill with random values
        Random rand = new Random();
        for (int i = 0; i < STATE_SIZE; i++) {
            state[i] = rand.nextDouble() * 10;
            nextState[i] = rand.nextDouble() * 10;
        }
        
        // Training should not throw exceptions
        assertDoesNotThrow(() -> policyGradient.train(state, 0, 10.0));
    }
}
