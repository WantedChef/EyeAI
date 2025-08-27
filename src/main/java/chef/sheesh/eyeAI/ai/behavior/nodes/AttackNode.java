package chef.sheesh.eyeAI.ai.behavior.nodes;

import chef.sheesh.eyeAI.ai.behavior.BehaviorTree;
import chef.sheesh.eyeAI.ai.core.DecisionContext;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

/**
 * Action node that makes a fake player attack a target entity.
 * Handles combat logic and cooldowns.
 */
public class AttackNode extends BehaviorTree {

    private final Entity target;
    private long lastAttackTime = 0;
    private final long attackCooldown = 1000; // 1 second cooldown
    private int maxAttacks = -1; // -1 for unlimited
    private int attacksPerformed = 0;

    public AttackNode() {
        this(null);
    }

    public AttackNode(Entity target) {
        this.target = target;
        this.name = "AttackNode";
    }

    public AttackNode(Entity target, int maxAttacks) {
        this.target = target;
        this.maxAttacks = maxAttacks;
        this.name = "AttackNode";
    }

    @Override
    public ExecutionResult execute(IFakePlayer fakePlayer) {
        if (!(fakePlayer instanceof FakePlayer realFakePlayer)) {
            return failure();
        }

        // Resolve target: use provided target if present; otherwise use agent's current target
        Entity effectiveTarget = this.target != null ? this.target : fakePlayer.getTarget();

        // Check if target is still valid
        if (effectiveTarget == null || effectiveTarget.isDead()) {
            reset();
            return failure();
        }

        // Check if we've performed the maximum number of attacks
        if (maxAttacks > 0 && attacksPerformed >= maxAttacks) {
            reset();
            return success();
        }

        long currentTime = System.currentTimeMillis();

        // Check attack cooldown
        if (currentTime - lastAttackTime < attackCooldown) {
            return running(); // Still cooling down
        }

        @SuppressWarnings("unused") // TODO: Use context for decision making
        DecisionContext context = realFakePlayer.createDecisionContext();
        Location currentLocation = realFakePlayer.getLocation();
        Location targetLocation = effectiveTarget.getLocation();

        // Check if target is in range (simple distance check)
        double distance = currentLocation.distance(targetLocation);
        if (distance > 3.0) {
            return failure(); // Target too far, movement should handle this
        }

        // Check if we have line of sight (optional, can be expensive)
        if (!hasLineOfSight(realFakePlayer, effectiveTarget)) {
            return failure(); // No line of sight
        }

        // Perform attack
        realFakePlayer.performAttack(effectiveTarget);
        lastAttackTime = currentTime;
        attacksPerformed++;

        // Check if we've reached the attack limit
        if (maxAttacks > 0 && attacksPerformed >= maxAttacks) {
            reset();
            return success();
        }

        return running(); // Continue attacking
    }

    /**
     * Simple line of sight check (can be improved with ray tracing)
     */
    private boolean hasLineOfSight(FakePlayer fakePlayer, Entity target) {
        Location eyeLocation = fakePlayer.getLocation().add(0, 1.62, 0); // Eye height
        Location targetLocation = target.getLocation().add(0, target.getHeight() / 2, 0);

        // Simple distance-based approximation
        return eyeLocation.distance(targetLocation) < 4.0;
    }

    @Override
    public void reset() {
        attacksPerformed = 0;
        lastAttackTime = 0;
        markNotRunning();
    }

    @Override
    public String getDescription() {
        return String.format("Attack (%s, %d/%s)",
               (target != null ? target.getType().name() : "null"),
               attacksPerformed,
               (maxAttacks > 0 ? String.valueOf(maxAttacks) : "∞"));
    }

    @Override
    public String getCategory() {
        return "Action";
    }

    /**
     * Get the target entity
     */
    public Entity getTarget() {
        return target;
    }

    /**
     * Get the number of attacks performed
     */
    public int getAttacksPerformed() {
        return attacksPerformed;
    }

    /**
     * Set the maximum number of attacks
     */
    public void setMaxAttacks(int maxAttacks) {
        this.maxAttacks = maxAttacks;
    }
}
