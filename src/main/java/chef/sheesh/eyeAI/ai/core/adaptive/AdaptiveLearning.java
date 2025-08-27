package chef.sheesh.eyeAI.ai.core.adaptive;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages adaptive learning for AI agents.
 * This system tracks player behavior and adjusts the AI's responses accordingly.
 */
public class AdaptiveLearning {

    private final Map<UUID, PlayerProfile> playerProfiles;

    public AdaptiveLearning() {
        this.playerProfiles = new ConcurrentHashMap<>();
    }

    /**
     * Gets the profile for a specific player.
     *
     * @param player The player.
     * @return The player's profile.
     */
    public PlayerProfile getProfile(Player player) {
        return playerProfiles.computeIfAbsent(player.getUniqueId(), PlayerProfile::new);
    }

    /**
     * Records an encounter between an AI and a player.
     *
     * @param player The player involved in the encounter.
     */
    public void recordEncounter(Player player) {
        getProfile(player).recordEncounter();
    }

    /**
     * Records that a player initiated an attack.
     *
     * @param player The player who attacked.
     */
    public void recordPlayerAttack(Player player) {
        getProfile(player).recordAttack();
    }

    /**
     * Records that a player fled from an encounter.
     *
     * @param player The player who fled.
     */
    public void recordPlayerFlee(Player player) {
        getProfile(player).recordFlee();
    }

    /**
     * Gets an aggression modifier based on a player's profile.
     * This can be used to influence the AI's behavior.
     *
     * @param player The player.
     * @return A modifier value (e.g., > 1.0 for more aggressive response).
     */
    public double getAggressionModifier(Player player) {
        double aggressionScore = getProfile(player).getAggressionScore();
        // Simple mapping: more aggressive player -> more defensive AI response (modifier < 1.0)
        // and vice-versa. This can be made more complex.
        return 1.0 - (aggressionScore - 0.5);
    }
}
