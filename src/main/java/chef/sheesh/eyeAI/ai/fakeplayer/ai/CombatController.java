package chef.sheesh.eyeAI.ai.fakeplayer.ai;

import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerState;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

/**
 * Combat controller for fake players
 */
public class CombatController {

    private final FakePlayer fakePlayer;
    private Entity currentTarget;
    private long lastAttackTime = 0;
    private static final long ATTACK_COOLDOWN = 1000; // 1 second between attacks
    private static final double ATTACK_RANGE = 4.0;
    private boolean inCombat = false;

    public CombatController(FakePlayer fakePlayer) {
        this.fakePlayer = fakePlayer;
    }

    /**
     * Update combat logic
     */
    public void tick() {
        // Get target from target selector
        Entity target = fakePlayer.getTargetSelector().getCurrentTarget();

        if (target != null && isValidCombatTarget(target)) {
            this.currentTarget = target;
            this.inCombat = true;

            // Try to attack if in range and cooldown is ready
            if (isInAttackRange(target) && canAttack()) {
                performAttack(target);
            } else {
                // Move closer to target
                moveTowardsTarget(target);
            }
        } else {
            // No valid target, exit combat
            exitCombat();
        }
    }

    /**
     * Engage a specific target
     */
    public void engageTarget(Entity target) {
        if (target != null && isValidCombatTarget(target)) {
            this.currentTarget = target;
            this.inCombat = true;
            fakePlayer.getTargetSelector().setTarget(target);
            fakePlayer.setState(FakePlayerState.ATTACKING);
        }
    }

    /**
     * Perform attack on target
     */
    private void performAttack(Entity target) {
        if (!canAttack() || !isInAttackRange(target)) {
            return;
        }

        // Perform the attack
        fakePlayer.performAttack(target);
        this.lastAttackTime = System.currentTimeMillis();

        // Face the target
        faceTarget(target);
    }

    /**
     * Move towards combat target
     */
    private void moveTowardsTarget(Entity target) {
        Location targetLoc = target.getLocation();

        // Calculate optimal attack position (slightly closer than attack range)
        Location playerLoc = fakePlayer.getLocation();
        double distance = playerLoc.distance(targetLoc);

        if (distance > ATTACK_RANGE - 1.0) {
            // Move closer
            fakePlayer.getMovementController().moveTowards(targetLoc);
        }
    }

    /**
     * Face the target
     */
    private void faceTarget(Entity target) {
        // TODO: Implement yaw/pitch calculation to face target
        // This would require updating FakePlayer location with yaw/pitch
    }

    /**
     * Check if can attack (cooldown ready)
     */
    private boolean canAttack() {
        return (System.currentTimeMillis() - lastAttackTime) >= ATTACK_COOLDOWN;
    }

    /**
     * Check if target is in attack range
     */
    private boolean isInAttackRange(Entity target) {
        if (target == null) {
            return false;
        }

        double distance = fakePlayer.getLocation().distance(target.getLocation());
        return distance <= ATTACK_RANGE;
    }

    /**
     * Check if target is valid for combat
     */
    private boolean isValidCombatTarget(Entity target) {
        if (target == null || !target.isValid() || target.isDead()) {
            return false;
        }

        if (!(target instanceof LivingEntity livingTarget)) {
            return false;
        }

        return livingTarget.getHealth() > 0;
    }

    /**
     * Exit combat mode
     */
    private void exitCombat() {
        this.inCombat = false;
        this.currentTarget = null;

        if (fakePlayer.getState() == FakePlayerState.ATTACKING) {
            fakePlayer.setState(FakePlayerState.IDLE);
        }
    }

    /**
     * Force exit combat
     */
    public void stopCombat() {
        exitCombat();
        fakePlayer.getTargetSelector().clearTarget();
    }

    /**
     * Check if currently in combat
     */
    public boolean isInCombat() {
        return inCombat && currentTarget != null && isValidCombatTarget(currentTarget);
    }

    /**
     * Get current combat target
     */
    public Entity getCurrentTarget() {
        return currentTarget;
    }

    /**
     * Set attack cooldown
     */
    public void setAttackCooldown(long cooldown) {
        // Allow customization of attack speed
    }

    /**
     * Get distance to current target
     */
    public double getDistanceToTarget() {
        if (currentTarget == null) {
            return Double.MAX_VALUE;
        }

        return fakePlayer.getLocation().distance(currentTarget.getLocation());
    }

    private void resetCombat() {
        exitCombat();
        fakePlayer.getTargetSelector().clearTarget();
    }

    private boolean hasLineOfSight(Entity target) {
        // Implement line of sight check
        return true;
    }

    private void attack(Entity target) {
        // Check if target is still valid
        if (target == null || !target.isValid()) {
            resetCombat();
            return;
        }

        // Check if target is dead
        if (target instanceof LivingEntity livingTarget && livingTarget.isDead()) {
            resetCombat();
            return;
        }

        // Check if target is too far away
        double distance = fakePlayer.getLocation().distance(target.getLocation());
        if (distance > ATTACK_RANGE * 2) {
            resetCombat();
            return;
        }

        // Check if we have line of sight
        if (!hasLineOfSight(target)) {
            return;
        }

        // Check attack cooldown
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastAttackTime) < ATTACK_COOLDOWN) {
            return;
        }

        // Perform attack
        fakePlayer.performAttack(target);
        lastAttackTime = currentTime;
    }
}
