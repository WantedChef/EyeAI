package chef.sheesh.eyeAI.ai.behavior.nodes;

import chef.sheesh.eyeAI.ai.behavior.BehaviorTree;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

/**
 * A node that finds a safe location for the agent to flee to.
 * The safe location is calculated by moving away from the current target.
 */
public class FindSafeLocationNode extends BehaviorTree {

    private static final int FLEE_DISTANCE = 15;
    private static final int HEADROOM_BLOCKS = 2;
    private static final int MAX_ANGLE_ATTEMPTS = 7; // 0, +/-30, +/-60, 180
    private static final double ANGLE_STEP_DEGREES = 30.0;
    private static final int[] RADII = new int[] { FLEE_DISTANCE, FLEE_DISTANCE + 6, FLEE_DISTANCE + 12 };

    public FindSafeLocationNode() {
        this.name = "FindSafeLocation";
        this.description = "Finds a location to flee to, away from the current threat.";
    }

    @Override
    public ExecutionResult execute(IFakePlayer fakePlayer) {
        Entity target = fakePlayer.getTarget();
        if (target == null) {
            // No target to flee from
            return failure();
        }

        Location agentLocation = fakePlayer.getLocation();
        if (agentLocation == null || agentLocation.getWorld() == null) {
            return failure();
        }
        Location targetLocation = target.getLocation();

        // Calculate a horizontal vector pointing away from the target
        Vector fleeVector = agentLocation.toVector().subtract(targetLocation.toVector());
        fleeVector.setY(0); // horizontal only
        if (fleeVector.lengthSquared() == 0) {
            // If the vector is zero (e.g., same XZ), create a random vector
            fleeVector = new Vector(Math.random() - 0.5, 0, Math.random() - 0.5);
        }
        fleeVector.normalize();

        // Find a safe candidate with retries (angle offsets and radii)
        Location safe = findSafeFleeLocation(agentLocation, fleeVector);
        if (safe == null) {
            // As a last resort, try on-top-of-highest-block in the raw flee direction
            Location fallback = agentLocation.clone().add(fleeVector.clone().multiply(FLEE_DISTANCE));
            safe = projectToTopSafe(agentLocation.getWorld(), fallback);
        }

        if (safe == null) {
            return failure();
        }

        // Store the flee location in the agent's blackboard
        fakePlayer.setBlackboardValue("flee_location", safe);

        return success();
    }

    @Override
    public void reset() {
        // No state to reset
    }

    @Override
    public String getCategory() {
        return super.getCategory();
    }

    // ===== Helpers =====

    private Location findSafeFleeLocation(Location origin, Vector baseDirection) {
        World world = origin.getWorld();
        if (world == null) {
            return null;
        }

        // Try base direction with angle offsets and increasing radii
        int[] angleMultipliers = new int[] { 0, 1, -1, 2, -2, 6, -6 }; // 6 ~= 180/30
        int attempts = 0;
        for (int mult : angleMultipliers) {
            double angleDeg = mult * ANGLE_STEP_DEGREES;
            Vector dir = rotateY(baseDirection, Math.toRadians(angleDeg));
            for (int r : RADII) {
                if (++attempts > MAX_ANGLE_ATTEMPTS * RADII.length) {
                    break;
                }
                Location candidate = origin.clone().add(dir.clone().multiply(r));
                Location projected = projectToTopSafe(world, candidate);
                if (projected != null && isLocationSafe(world, projected)) {
                    return projected;
                }
            }
        }
        return null;
    }

    private Location projectToTopSafe(World world, Location locXZ) {
        if (world == null) {
            return null;
        }
        // Keep XZ, find a reasonable Y using highest solid block at XZ
        Block highest = world.getHighestBlockAt(locXZ);
        Location above = highest.getLocation().add(0, 1, 0);
        // Ensure within world border
        WorldBorder border = world.getWorldBorder();
        if (!border.isInside(above)) {
            return null;
        }
        // Validate not on dangerous surface and enough headroom
        if (isLocationSafe(world, above)) {
            return centerOnBlock(above);
        }
        return null;
    }

    private boolean isLocationSafe(World world, Location loc) {
        if (world == null) {
            return false;
        }
        // Check headroom blocks are not solid
        for (int i = 0; i < HEADROOM_BLOCKS; i++) {
            Block head = world.getBlockAt(loc.getBlockX(), loc.getBlockY() + i, loc.getBlockZ());
            if (head.getType().isSolid()) {
                return false;
            }
            if (isHarmful(head.getType())) {
                return false;
            }
        }
        // Check the ground block
        Block ground = world.getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
        if (!ground.getType().isSolid()) {
            return false;
        }
        if (isHarmful(ground.getType())) {
            return false;
        }
        // Basic liquid check at feet
        Block feet = world.getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        return !isLiquid(feet.getType());
    }

    private boolean isHarmful(Material m) {
        // Keep list conservative for broader API compatibility
        return m == Material.LAVA || m == Material.FIRE || m == Material.CACTUS;
    }

    private boolean isLiquid(Material m) {
        return m == Material.WATER || m == Material.LAVA;
    }

    private Location centerOnBlock(Location loc) {
        return new Location(loc.getWorld(), loc.getBlockX() + 0.5, loc.getY(), loc.getBlockZ() + 0.5, loc.getYaw(), loc.getPitch());
    }

    private Vector rotateY(Vector v, double radians) {
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double x = v.getX();
        double z = v.getZ();
        double rx = x * cos - z * sin;
        double rz = x * sin + z * cos;
        return new Vector(rx, 0, rz).normalize();
    }
}
