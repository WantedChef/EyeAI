package chef.sheesh.eyeAI.ai.fakeplayer.events;

import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;

/**
 * Event fired when a fake player moves
 */
public class FakePlayerMoveEvent extends FakePlayerEvent implements Cancellable {

    private boolean cancelled = false;
    private final Location from;
    private Location to;
    private final MoveType moveType;

    public FakePlayerMoveEvent(FakePlayer fakePlayer, Location from, Location to, MoveType moveType) {
        super(fakePlayer);
        this.from = from.clone();
        this.to = to.clone();
        this.moveType = moveType;
    }

    /**
     * Get the starting location
     */
    public Location getFrom() {
        return from.clone();
    }

    /**
     * Get the destination location
     */
    public Location getTo() {
        return to.clone();
    }

    /**
     * Set the destination location
     */
    public void setTo(Location to) {
        this.to = to.clone();
    }

    /**
     * Get the type of movement
     */
    public MoveType getMoveType() {
        return moveType;
    }

    /**
     * Get the distance of the move
     */
    public double getDistance() {
        return from.distance(to);
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
     * Types of movement
     */
    public enum MoveType {
        WALK,           // Normal walking movement
        SPRINT,         // Sprinting movement
        SNEAK,          // Sneaking movement
        FLEE,           // Fleeing movement
        PATROL,         // Patrol pattern movement
        PURSUIT,        // Pursuing target movement
        PATHFINDING,    // Movement using pathfinding
        TELEPORT        // Teleportation movement
    }
}
