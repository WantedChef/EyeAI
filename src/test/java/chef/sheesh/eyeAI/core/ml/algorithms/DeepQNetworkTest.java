package chef.sheesh.eyeAI.core.ml.algorithms;

import chef.sheesh.eyeAI.core.ml.models.Action;
import chef.sheesh.eyeAI.core.ml.models.EnhancedExperience;
import chef.sheesh.eyeAI.core.ml.models.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeepQNetworkTest {

    private DeepQNetwork dqn;
    private static final int STATE_SIZE = 5;
    private static final int ACTION_SIZE = Action.values().length;
    private static final int HIDDEN_SIZE = 10;
    private static final double LEARNING_RATE = 0.001;
    private static final double DISCOUNT_FACTOR = 0.95;
    private static final int BATCH_SIZE = 32;
    private static final int BUFFER_CAPACITY = 1000;
    private static final double EPSILON_START = 0.1;
    private static final int UPDATE_TARGET_EVERY = 100;
    private static final double ALPHA = 0.6;
    private static final double BETA_START = 0.4;
    private static final double BETA_INCREMENT = 0.001;

    @BeforeEach
    void setUp() {
        dqn = new DeepQNetwork(STATE_SIZE, ACTION_SIZE, HIDDEN_SIZE, LEARNING_RATE, DISCOUNT_FACTOR,
                BATCH_SIZE, BUFFER_CAPACITY, EPSILON_START, UPDATE_TARGET_EVERY, ALPHA, BETA_START, BETA_INCREMENT);
    }

    @Test
    @DisplayName("Should initialize DQN with correct parameters")
    void testInitialization() {
        assertNotNull(dqn);
        // We can't directly access private fields, but we can verify the behavior
        // by checking if the network can make predictions
        double[] testState = new double[STATE_SIZE];
        double[] qValues = dqn.predictQValues(testState);
        assertEquals(ACTION_SIZE, qValues.length);
    }

    @Test
    @DisplayName("Should select valid action index when exploiting")
    void testExploitationActionSelection() {
        double[] testState = new double[STATE_SIZE];
        // Force exploitation by setting epsilon to 0
        // We need to use reflection to access the private epsilon field
        try {
            java.lang.reflect.Field epsilonField = DeepQNetwork.class.getDeclaredField("epsilon");
            epsilonField.setAccessible(true);
            epsilonField.set(dqn, 0.0);
        } catch (Exception e) {
            fail("Failed to set epsilon field for testing");
        }
        
        int action = dqn.selectAction(testState);
        assertTrue(action >= 0 && action < ACTION_SIZE);
    }

    @Test
    @DisplayName("Should select valid action index when exploring")
    void testExplorationActionSelection() {
        double[] testState = new double[STATE_SIZE];
        // Force exploration by setting epsilon to 1
        try {
            java.lang.reflect.Field epsilonField = DeepQNetwork.class.getDeclaredField("epsilon");
            epsilonField.setAccessible(true);
            epsilonField.set(dqn, 1.0);
        } catch (Exception e) {
            fail("Failed to set epsilon field for testing");
        }
        
        int action = dqn.selectAction(testState);
        assertTrue(action >= 0 && action < ACTION_SIZE);
    }

    @Test
    @DisplayName("Should return exploration rate")
    void testGetExplorationRate() {
        double rate = dqn.getExplorationRate();
        assertEquals(EPSILON_START, rate);
    }

    @Test
    @DisplayName("Should update exploration rate")
    void testSetExplorationRate() {
        double newRate = 0.05;
        dqn.setExplorationRate(newRate);
        assertEquals(newRate, dqn.getExplorationRate());
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
        
        EnhancedExperience experience = new EnhancedExperience(state, 0, 10.0, nextState, false);
        
        // Training should not throw exceptions
        assertDoesNotThrow(() -> dqn.trainEnhanced(experience));
    }
}
