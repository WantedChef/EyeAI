package chef.sheesh.eyeAI.ai.fakeplayer.events;

import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;

/**
 * Event fired when a fake player engages in combat
 */
public class FakePlayerCombatEvent extends FakePlayerEvent implements Cancellable {

    private boolean cancelled = false;
    private final Entity target;
    private final CombatType combatType;
    private double damage;

    public FakePlayerCombatEvent(FakePlayer fakePlayer, Entity target, CombatType combatType, double damage) {
        super(fakePlayer);
        this.target = target;
        this.combatType = combatType;
        this.damage = damage;
    }

    /**
     * Get the target entity
     */
    public Entity getTarget() {
        return target;
    }

    /**
     * Get the type of combat
     */
    public CombatType getCombatType() {
        return combatType;
    }

    /**
     * Get the damage amount
     */
    public double getDamage() {
        return damage;
    }

    /**
     * Set the damage amount
     */
    public void setDamage(double damage) {
        this.damage = damage;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Types of combat interactions
     */
    public enum CombatType {
        ATTACK,     // Fake player attacking target
        DEFEND,     // Fake player defending against attack
        FLEE,       // Fake player fleeing from combat
        PURSUE      // Fake player pursuing target
    }
}
