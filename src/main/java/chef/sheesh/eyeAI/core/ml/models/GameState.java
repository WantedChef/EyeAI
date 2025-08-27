package chef.sheesh.eyeAI.core.ml.models;

import com.google.common.base.Objects;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the state of the game from the perspective of an AI agent.
 * Implements IState to be compatible with deep learning algorithms.
 *
 * @param x              Getters for all fields Agent's position
 * @param health         Agent's vitals
 * @param nearbyEntities Nearby entities (e.g., players, mobs)
 * @param inventory      Agent's inventory
 * @param timeOfDay      Environmental factors
 */
public record GameState(double x, double y, double z, double health, int hunger, List<Object> nearbyEntities,
                        Map<String, Integer> inventory, long timeOfDay, String weather,
                        int lightLevel) implements Serializable, IState {

    @Serial
    private static final long serialVersionUID = 2L; // Updated version UID

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GameState gameState = (GameState) o;
        return Double.compare(gameState.x, x) == 0 && Double.compare(gameState.y, y) == 0 && Double.compare(gameState.z, z) == 0 && Double.compare(gameState.health, health) == 0 && hunger == gameState.hunger && timeOfDay == gameState.timeOfDay && lightLevel == gameState.lightLevel && Objects.equal(nearbyEntities, gameState.nearbyEntities) && Objects.equal(inventory, gameState.inventory);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(x, y, z, health, hunger, nearbyEntities, inventory, timeOfDay, weather, lightLevel);
    }

    // --- IState Implementation ---

    @Override
    public double[] flatten() {
        // This is a simplified flattening. A real implementation would need more sophisticated feature engineering.
        List<Double> features = new ArrayList<>();
        features.add(x);
        features.add(y);
        features.add(z);
        features.add(health);
        features.add((double) hunger);
        features.add((double) timeOfDay);
        features.add((double) lightLevel);
        // Weather to numeric (simple example)
        features.add(weather.equalsIgnoreCase("SUNNY") ? 1.0 : 0.0);
        // Add entity and inventory features (e.g., counts)
        features.add((double) nearbyEntities.size());
        features.add((double) inventory.size());

        // Convert to double array
        double[] flatState = new double[features.size()];
        for (int i = 0; i < features.size(); i++) {
            flatState[i] = features.get(i);
        }
        return flatState;
    }

    @Override
    public int getStateSize() {
        // This must match the number of features in flatten()
        return 10; // Based on the simplified implementation above
    }

    @Override
    public IState copy() {
        return new GameState(x, y, z, health, hunger, new ArrayList<>(nearbyEntities), new HashMap<>(inventory), timeOfDay, weather, lightLevel);
    }

    @Override
    public boolean isTerminal() {
        // A state is terminal if the agent is dead.
        return this.health <= 0;
    }

    /**
     * Returns a hash code representing this game state.
     * This is used for Q-table lookups and state identification.
     */
    public long getStateHash() {
        return (x + "," + y + "," + z + "," + health + "," + hunger + "," + timeOfDay + "," + weather + "," + lightLevel).hashCode();
    }
}
