package chef.sheesh.eyeAI.core.ml.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpatialStateTest {

    @Test
    @DisplayName("Should initialize SpatialState with correct default values")
    void testInitialization() {
        SpatialState spatialState = new SpatialState();
        
        // Check that all fields are initialized with zeros
        assertEquals(0.0, spatialState.getNearbyBlocks());
        assertEquals(0.0, spatialState.getNearbyEntities());
        assertEquals(0.0, spatialState.getHeightDifference());
        assertEquals(0.0, spatialState.getDistanceToCenter());
        assertEquals(0.0, spatialState.getTerrainDensity());
    }

    @Test
    @DisplayName("Should correctly flatten state to feature vector")
    void testFlatten() {
        SpatialState spatialState = new SpatialState();
        // Set some non-zero values
        spatialState.setNearbyBlocks(5.0);
        spatialState.setNearbyEntities(3.0);
        spatialState.setHeightDifference(2.0);
        spatialState.setDistanceToCenter(10.0);
        spatialState.setTerrainDensity(0.75);
        
        double[] flattened = spatialState.flatten();
        assertEquals(5, flattened.length);
        assertEquals(5.0, flattened[0]);
        assertEquals(3.0, flattened[1]);
        assertEquals(2.0, flattened[2]);
        assertEquals(10.0, flattened[3]);
        assertEquals(0.75, flattened[4]);
    }
}
