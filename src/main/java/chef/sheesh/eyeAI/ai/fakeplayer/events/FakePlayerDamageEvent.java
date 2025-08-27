package chef.sheesh.eyeAI.ai.fakeplayer.events;

import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;

/**
 * Event fired when a fake player takes damage
 */
public class FakePlayerDamageEvent extends FakePlayerEvent implements Cancellable {

    private boolean cancelled = false;
    private final Entity damager;
    private final DamageCause cause;
    private double damage;
    private double originalDamage;

    public FakePlayerDamageEvent(FakePlayer fakePlayer, Entity damager, DamageCause cause, double damage) {
        super(fakePlayer);
        this.damager = damager;
        this.cause = cause;
        this.damage = damage;
        this.originalDamage = damage;
    }

    /**
     * Get the entity that caused the damage
     */
    public Entity getDamager() {
        return damager;
    }

    /**
     * Get the cause of damage
     */
    public DamageCause getCause() {
        return cause;
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
        this.damage = Math.max(0, damage);
    }

    /**
     * Get the original damage amount
     */
    public double getOriginalDamage() {
        return originalDamage;
    }

    /**
     * Check if damage was modified
     */
    public boolean isDamageModified() {
        return damage != originalDamage;
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
     * Causes of damage
     */
    public enum DamageCause {
        ENTITY_ATTACK,      // Attacked by another entity
        PROJECTILE,         // Hit by projectile
        FALL,              // Fall damage
        FIRE,              // Fire damage
        LAVA,              // Lava damage
        DROWNING,          // Drowning damage
        SUFFOCATION,       // Suffocation damage
        LIGHTNING,         // Lightning damage
        MAGIC,             // Magic damage
        POISON,            // Poison damage
        WITHER,            // Wither damage
        FALLING_BLOCK,     // Falling block damage
        THORNS,            // Thorns damage
        DRAGON_BREATH,     // Dragon breath damage
        FLY_INTO_WALL,     // Flying into wall damage
        HOT_FLOOR,         // Hot floor damage
        CRAMMING,          // Cramming damage
        DRYOUT,            // Dryout damage
        FREEZE,            // Freeze damage
        SONIC_BOOM,        // Sonic boom damage
        CUSTOM             // Custom damage cause
    }
}
