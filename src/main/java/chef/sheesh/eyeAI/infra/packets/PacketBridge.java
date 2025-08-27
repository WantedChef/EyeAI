package chef.sheesh.eyeAI.infra.packets;

import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public final class PacketBridge {
    private final Plugin plugin;
    private boolean protocolLibAvailable;
    private boolean packetEventsAvailable;

    public PacketBridge(Plugin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        // Detect integrations safely at runtime
        this.protocolLibAvailable = Bukkit.getPluginManager().getPlugin("ProtocolLib") != null;
        this.packetEventsAvailable = Bukkit.getPluginManager().getPlugin("packetevents") != null
                || Bukkit.getPluginManager().getPlugin("PacketEvents") != null;

        plugin.getLogger().info("PacketBridge: ProtocolLib=" + protocolLibAvailable + ", PacketEvents=" + packetEventsAvailable);

        if (protocolLibAvailable) {
            plugin.getLogger().info("ProtocolLib detected - advanced packet features available.");
            // TODO: Initialize ProtocolLib listeners when dependency is available
        }

        if (packetEventsAvailable) {
            plugin.getLogger().info("PacketEvents detected - high-throughput packet handling available.");
            // TODO: Initialize PacketEvents listeners when dependency is available
        }

        if (!protocolLibAvailable && !packetEventsAvailable) {
            plugin.getLogger().log(Level.INFO, "No packet framework available. Using basic Bukkit API for packet operations.");
        }
    }

    public void shutdown() {
        // Cleanup packet listeners if any were registered
    }

    public boolean isPacketEventsEnabled() { return packetEventsAvailable; }
    public boolean isProtocolLibEnabled() { return protocolLibAvailable; }

    public void sendMovement(Player player, Location target) {
        // Safe fallback using Bukkit API
        if (player == null || target == null) {
            return;
        }
        try {
            player.teleportAsync(target);
        } catch (NoSuchMethodError ignored) {
            player.teleport(target);
        }
    }
}
