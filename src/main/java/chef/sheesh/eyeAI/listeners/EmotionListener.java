package chef.sheesh.eyeAI.listeners;

import chef.sheesh.eyeAI.ai.core.emotions.Emotion;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Optional;

/**
 * Listener for game events that should affect an AI's emotional state.
 */
public class EmotionListener implements Listener {

    private final FakePlayerManager fakePlayerManager;

    public EmotionListener(FakePlayerManager fakePlayerManager) {
        this.fakePlayerManager = fakePlayerManager;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity damaged = event.getEntity();

        // Case 1: A fake player was damaged
        fakePlayerManager.getFakePlayerByEntity(damaged).ifPresent(fakePlayer -> {
            fakePlayer.getEmotionSystem().increaseEmotion(Emotion.ANGER, 0.2);
            fakePlayer.getEmotionSystem().increaseEmotion(Emotion.FEAR, 0.3);
            fakePlayer.getEmotionSystem().decreaseEmotion(Emotion.JOY, 0.1);
        });

        // Case 2: A fake player was the damager
        fakePlayerManager.getFakePlayerByEntity(damager).ifPresent(fakePlayer -> {
            fakePlayer.getEmotionSystem().increaseEmotion(Emotion.JOY, 0.15);
            fakePlayer.getEmotionSystem().increaseEmotion(Emotion.ANGER, 0.05);
        });
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity deceased = event.getEntity();
        Player killer = deceased.getKiller(); // killer can be null

        // Case 1: A fake player died
        fakePlayerManager.getFakePlayerByEntity(deceased).ifPresent(fakePlayer -> {
            fakePlayer.getEmotionSystem().setEmotionValue(Emotion.SADNESS, 1.0);
            fakePlayer.getEmotionSystem().setEmotionValue(Emotion.JOY, 0.0);
            fakePlayer.getEmotionSystem().setEmotionValue(Emotion.ANGER, 0.1);
            fakePlayer.getEmotionSystem().setEmotionValue(Emotion.FEAR, 0.2);
        });

        // Case 2: A fake player got a kill
        if (killer != null) {
            fakePlayerManager.getFakePlayerByEntity(killer).ifPresent(fakePlayer -> {
                fakePlayer.getEmotionSystem().increaseEmotion(Emotion.JOY, 0.5);
                fakePlayer.getEmotionSystem().decreaseEmotion(Emotion.FEAR, 0.2);
            });
        }
    }
}
