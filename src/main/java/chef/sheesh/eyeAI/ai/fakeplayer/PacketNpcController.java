package chef.sheesh.eyeAI.ai.fakeplayer;

// ProtocolLib is not available, using fallback implementation

// Adventure API import
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controller for handling visual representation of fake players using armor stands.
 * Provides basic NPC functionality with visual representation.
 */
public class PacketNpcController implements IFakePlayerPacketController {

    private final JavaPlugin plugin;
    private final Map<UUID, ArmorStand> visualEntities = new ConcurrentHashMap<>();
    private final Map<UUID, FakePlayer> entityIdToFakePlayer = new ConcurrentHashMap<>();
    private boolean enabled = true;

    public PacketNpcController(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getLogger().info("PacketNpcController initialized - using armor stand visual representation");
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void queueUpdate(FakePlayer fakePlayer) {
        updateLocation(fakePlayer, fakePlayer.getLocation());
    }

    @Override
    public void flush() {
        // Flush any pending updates
    }

    @SuppressWarnings("unused")
    private void handleNpcInteraction(Player player, FakePlayer fakePlayer) {
        FakePlayerInteractEvent event = new FakePlayerInteractEvent(player, fakePlayer);
        plugin.getServer().getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            player.sendMessage("§e[NPC] §f" + fakePlayer.getName() + "§7: Hello! I'm an AI-controlled entity.");
        }
    }

    @Override
    public void createVisualNpc(FakePlayer fp) {
        if (!enabled) {
            return;
        }

        Location location = fp.getLocation();
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);

        armorStand.setInvisible(true);
        armorStand.setInvulnerable(true);
        armorStand.setGravity(false);
        armorStand.customName(Component.text(fp.getName()));
        armorStand.setCustomNameVisible(true);

        visualEntities.put(fp.getId(), armorStand);
        entityIdToFakePlayer.put(armorStand.getUniqueId(), fp); // Add to reverse map
        fp.setVisibleNpc(armorStand);

        plugin.getLogger().info("Created visual NPC for " + fp.getName() + " using armor stand");
    }

    @Override
    public Entity getOrCreateProxyEntity(FakePlayer fakePlayer) {
        ArmorStand stand = visualEntities.get(fakePlayer.getId());
        if (stand == null) {
            stand = createArmorStand(fakePlayer);
            visualEntities.put(fakePlayer.getId(), stand);
            entityIdToFakePlayer.put(stand.getUniqueId(), fakePlayer); // Also add to reverse map here
        }
        return stand;
    }

    @Override
    public void removeVisualNpc(FakePlayer fp) {
        ArmorStand armorStand = visualEntities.remove(fp.getId());
        if (armorStand != null) {
            entityIdToFakePlayer.remove(armorStand.getUniqueId()); // Remove from reverse map
            if (!armorStand.isDead()) {
                armorStand.remove();
            }
        }
        fp.setVisibleNpc(null);
    }

    @Override
    public void updateVisibility() {
        // Update visibility for all NPCs
    }

    public void cleanup() {
        for (ArmorStand armorStand : visualEntities.values()) {
            if (armorStand != null && !armorStand.isDead()) {
                armorStand.remove();
            }
        }
        visualEntities.clear();
        entityIdToFakePlayer.clear();
    }

    @Override
    public void updateLocation(FakePlayer fakePlayer, Location location) {
        if (!enabled) {
            return;
        }
        
        ArmorStand armorStand = visualEntities.get(fakePlayer.getId());
        if (armorStand != null && !armorStand.isDead()) {
            if (!armorStand.getLocation().equals(location)) {
                armorStand.teleport(location);
            }
        }
    }

    private ArmorStand createArmorStand(FakePlayer fakePlayer) {
        Location location = fakePlayer.getLocation();
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setInvisible(true);
        armorStand.setInvulnerable(true);
        armorStand.setGravity(false);
        armorStand.customName(Component.text(fakePlayer.getName()));
        armorStand.setCustomNameVisible(true);
        return armorStand;
    }

    @Override
    public Optional<FakePlayer> getFakePlayerByEntityId(UUID entityId) {
        return Optional.ofNullable(entityIdToFakePlayer.get(entityId));
    }
}
