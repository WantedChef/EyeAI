package chef.sheesh.eyeAI.core.ml.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents the state of the game from the perspective of an AI agent.
 * Implements IState to be compatible with deep learning algorithms.
 */
public class GameState implements Serializable, IState {

    private static final long serialVersionUID = 2L; // Updated version UID

    // Agent's position
    private final double x;
    private final double y;
    private final double z;

    // Agent's vitals
    private final double health;
    private final int hunger;

    // Nearby entities (e.g., players, mobs)
    private final List<Object> nearbyEntities;

    // Agent's inventory
    private final Map<String, Integer> inventory;

    // Environmental factors
    private final long timeOfDay;
    private final String weather;
    private final int lightLevel;

    public GameState(double x, double y, double z, double health, int hunger,
                     List<Object> nearbyEntities, Map<String, Integer> inventory,
                     long timeOfDay, String weather, int lightLevel) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.health = health;
        this.hunger = hunger;
        this.nearbyEntities = nearbyEntities;
        this.inventory = inventory;
        this.timeOfDay = timeOfDay;
        this.weather = weather;
        this.lightLevel = lightLevel;
    }

    // Getters for all fields
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public double getHealth() { return health; }
    public int getHunger() { return hunger; }
    public List<Object> getNearbyEntities() { return nearbyEntities; }
    public Map<String, Integer> getInventory() { return inventory; }
    public long getTimeOfDay() { return timeOfDay; }
    public String getWeather() { return weather; }
    public int getLightLevel() { return lightLevel; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GameState gameState = (GameState) o;
        return Double.compare(gameState.x, x) == 0 &&
               Double.compare(gameState.y, y) == 0 &&
               Double.compare(gameState.z, z) == 0 &&
               Double.compare(gameState.health, health) == 0 &&
               hunger == gameState.hunger &&
               timeOfDay == gameState.timeOfDay &&
               lightLevel == gameState.lightLevel &&
               com.google.common.base.Objects.equal(nearbyEntities, gameState.nearbyEntities) &&
               com.google.common.base.Objects.equal(inventory, gameState.inventory) &&
               com.google.common.base.Objects.equal(weather, weather);
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(x, y, z, health, hunger, nearbyEntities, inventory, timeOfDay, weather, lightLevel);
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
        return new GameState(x, y, z, health, hunger, new ArrayList<>(nearbyEntities), new java.util.HashMap<>(inventory), timeOfDay, weather, lightLevel);
    }

    @Override
    public boolean isTerminal() {
        // A state is terminal if the agent is dead.
        return this.health <= 0;
    }
}
