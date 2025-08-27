package chef.sheesh.eyeAI.core.ml.models;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * A state representation that captures spatial information about the agent's environment.
 * It scans the nearby terrain and entities to build a local map.
 */
public class SpatialState implements IState, Serializable {
    private static final long serialVersionUID = 1L;

    private final HashMap<String, String> terrainMap = new HashMap<>(); // Key: "x,y,z", Value: Block type
    private final HashMap<UUID, Location> entityPositions = new HashMap<>();
    private final transient World world;
    private final Location center;

    public SpatialState(Location center, int radius, World world) {
        this.center = center;
        this.world = world;
        scanTerrain(center, radius);
        scanEntities(center, radius);
    }

    private void scanTerrain(Location center, int radius) {
        if (world == null) {
            return;
        }
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location loc = center.clone().add(x, y, z);
                    Block block = world.getBlockAt(loc);
                    String key = String.format("%d,%d,%d", x, y, z); // Relative coordinates
                    terrainMap.put(key, block.getType().name());
                }
            }
        }
    }

    private void scanEntities(Location center, int radius) {
        if (world == null) {
            return;
        }
        for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
            entityPositions.put(entity.getUniqueId(), entity.getLocation());
        }
    }

    @Override
    public double[] flatten() {
        // Placeholder: A real implementation would convert the maps to a fixed-size vector
        // using techniques like one-hot encoding for block types and relative positions for entities.
        List<Double> features = new ArrayList<>();
        features.add(center.getX());
        features.add(center.getY());
        features.add(center.getZ());
        features.add((double) terrainMap.size()); // Simple feature: number of scanned blocks
        features.add((double) entityPositions.size()); // Simple feature: number of nearby entities

        double[] flatState = new double[features.size()];
        for (int i = 0; i < features.size(); i++) {
            flatState[i] = features.get(i);
        }
        return flatState;
    }

    @Override
    public int getStateSize() {
        // Must match the size of the array from flatten()
        return 5; // Based on the placeholder implementation
    }

    @Override
    public IState copy() {
        return new SpatialState(this.center.clone(), 0, this.world); // Simplified copy
    }

    @Override
    public boolean isTerminal() {
        // This state representation doesn't track agent health, so it's never terminal by itself.
        return false;
    }
}
