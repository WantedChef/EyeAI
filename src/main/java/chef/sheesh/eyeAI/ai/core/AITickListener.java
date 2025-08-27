package chef.sheesh.eyeAI.ai.core;

import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Listener that handles AI tick events to coordinate fake player updates.
 */
public class AITickListener implements Listener {

    private final IFakePlayerManager fakePlayerManager;

    public AITickListener(IFakePlayerManager fakePlayerManager) {
        this.fakePlayerManager = fakePlayerManager;
    }

    @EventHandler
    public void onAITick(AITickEvent event) {
        // Tick all fake players on main thread
        fakePlayerManager.tickAll();
    }
}
