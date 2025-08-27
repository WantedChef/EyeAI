package chef.sheesh.eyeAI.ai.core.updates;

import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerManager;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages which AI agents are updated based on their location in loaded chunks.
 * This helps to optimize server performance by not ticking agents that are far away.
 */
public class ChunkUpdateManager {

    private final FakePlayerManager fakePlayerManager;

    public ChunkUpdateManager(FakePlayerManager fakePlayerManager) {
        this.fakePlayerManager = fakePlayerManager;
    }

    /**
     * Gets a list of active agents that are in loaded chunks and should be updated.
     *
     * @return A list of active fake players.
     */
    public List<IFakePlayer> getActiveAgents() {
        List<IFakePlayer> allAgents = fakePlayerManager.getAllFakePlayers();
        if (allAgents.isEmpty()) {
            return new ArrayList<>();
        }

        return allAgents.stream()
                .filter(this::isAgentInLoadedChunk)
                .collect(Collectors.toList());
    }

    /**
     * Checks if an agent is in a loaded chunk.
     *
     * @param agent The agent to check.
     * @return True if the agent is in a loaded chunk, false otherwise.
     */
    private boolean isAgentInLoadedChunk(IFakePlayer agent) {
        Location loc = agent.getLocation();
        if (loc == null || loc.getWorld() == null) {
            return false;
        }

        World world = loc.getWorld();
        Chunk chunk = loc.getChunk();

        return world.isChunkLoaded(chunk.getX(), chunk.getZ());
    }
}
