package chef.sheesh.eyeAI.core.ml.algorithms;

import chef.sheesh.eyeAI.core.ml.models.Action;
import chef.sheesh.eyeAI.core.ml.models.EnhancedExperience;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DQNConvergenceTest {

    @Test
    @DisplayName("Should converge to >90% optimal actions in simple environment")
    void testDQNConvergence() {
        // Parameters for the test
        int stateSize = 5;
        int actionSize = Action.values().length;
        int hiddenSize = 10;
        double learningRate = 0.001;
        double discountFactor = 0.95;
        int batchSize = 32;
        int bufferCapacity = 1000;
        double epsilonStart = 1.0; // Start with high exploration
        int updateTargetEvery = 100;
        int trainingSteps = 10000; // Number of training steps
        int testEpisodes = 1000;   // Number of test episodes to evaluate performance
        
        // Create DQN instance
        DeepQNetwork dqn = new DeepQNetwork(stateSize, actionSize, hiddenSize, learningRate, 
                                           discountFactor, batchSize, bufferCapacity, 
                                           epsilonStart, updateTargetEvery, 0.6, 0.4, 0.001);
        
        // Simple environment: state 0 is best to move right (action 1), state 1 is best to move left (action 0)
        double[] state0 = {1.0, 0.0, 0.0, 0.0, 0.0};
        double[] state1 = {0.0, 1.0, 0.0, 0.0, 0.0};
        
        Random random = new Random(123); // Fixed seed for reproducibility
        
        // Training phase
        for (int step = 0; step < trainingSteps; step++) {
            // Decay epsilon over time
            double epsilon = Math.max(0.01, epsilonStart * Math.exp(-step / 1000.0));
            dqn.setExplorationRate(epsilon);
            
            // Select random state and action for training
            double[] state = random.nextBoolean() ? state0 : state1;
            int action = dqn.selectAction(state);
            
            // Create next state (optimal action leads to reward)
            double[] nextState = new double[stateSize];
            double reward = 0.0;
            boolean done = false;
            
            if (state == state0 && action == 1) { // Moving right from state 0 is optimal
                reward = 10.0;
                done = true;
            } else if (state == state1 && action == 0) { // Moving left from state 1 is optimal
                reward = 10.0;
                done = true;
            } else {
                // Small negative reward for suboptimal actions
                reward = -0.1;
            }
            
            // Create experience and train
            EnhancedExperience experience = new EnhancedExperience(state, action, reward, nextState, done);
            dqn.trainEnhanced(experience);
        }
        
        // Evaluation phase
        int optimalActions = 0;
        int totalActions = 0;
        
        // Set epsilon to 0 for pure exploitation
        dqn.setExplorationRate(0.0);
        
        for (int episode = 0; episode < testEpisodes; episode++) {
            // Select random state for testing
            double[] state = random.nextBoolean() ? state0 : state1;
            
            // Select action
            int action = dqn.selectAction(state);
            totalActions++;
            
            // Check if action is optimal
            if ((state == state0 && action == 1) || (state == state1 && action == 0)) {
                optimalActions++;
            }
        }
        
        // Calculate optimal action percentage
        double optimalPercentage = (double) optimalActions / totalActions;
        
        // Assert that >90% of actions are optimal
        assertTrue(optimalPercentage > 0.9, 
                  "DQN should converge to >90% optimal actions, but got " + (optimalPercentage * 100) + "%");
    }
}
