package chef.sheesh.eyeAI.ai.fakeplayer;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.util.Set;
import java.util.UUID;

/**
 * CommandSender-like wrapper for FakePlayer to allow command execution and permission checks.
 * This provides a bridge between the simulation and Bukkit's command system.
 */
public class FakePlayerWrapper implements CommandSender {

    private final FakePlayer fakePlayer;
    private final Server server;

    public FakePlayerWrapper(FakePlayer fakePlayer, Server server) {
        this.fakePlayer = fakePlayer;
        this.server = server;
    }

    @Override
    public void sendMessage(String message) {
        // Fake players don't receive messages, but we could log them
        fakePlayer.getManager().getLogger().info("[FakePlayer " + fakePlayer.getName() + "] " + message);
    }

    @Override
    public void sendMessage(String... messages) {
        for (String message : messages) {
            sendMessage(message);
        }
    }

    @Override
    public void sendMessage(net.kyori.adventure.text.Component component) {
        // Convert component to plain text for logging
        sendMessage(net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(component));
    }



    @Override
    public void sendMessage(UUID uuid, String message) {
        sendMessage(message); // UUID is ignored for fake players
    }

    @Override
    public void sendMessage(UUID uuid, String... messages) {
        for (String message : messages) {
            sendMessage(uuid, message);
        }
    }

    @Override
    public Server getServer() {
        return server;
    }

    @Override
    public String getName() {
        return fakePlayer.getName();
    }

    @Override
    public boolean isPermissionSet(String permission) {
        // Fake players have all permissions for simulation purposes
        return true;
    }

    @Override
    public boolean isPermissionSet(Permission permission) {
        return true;
    }

    @Override
    public boolean hasPermission(String permission) {
        // Grant all permissions to fake players
        return true;
    }

    @Override
    public boolean hasPermission(Permission permission) {
        return true;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String permission, boolean value) {
        // Return dummy attachment
        return new PermissionAttachment(plugin, this);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return new PermissionAttachment(plugin, this);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String permission, boolean value, int ticks) {
        return new PermissionAttachment(plugin, this);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        return new PermissionAttachment(plugin, this);
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {
        // No-op
    }

    @Override
    public void recalculatePermissions() {
        // No-op
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return Set.of(); // Return empty set
    }

    @Override
    public boolean isOp() {
        // Fake players are not operators
        return false;
    }

    @Override
    public void setOp(boolean op) {
        // Cannot change op status of fake players
    }

    /**
     * Get the underlying fake player
     */
    public FakePlayer getFakePlayer() {
        return fakePlayer;
    }

    /**
     * Execute a command as this fake player
     */
    public boolean executeCommand(String command) {
        return server.dispatchCommand(this, command);
    }

    /**
     * Get the current location of the fake player
     */
    public Location getLocation() {
        return fakePlayer.getLocation();
    }

    @Override
    public net.kyori.adventure.text.Component name() {
        return Component.text(getName());
    }

    @Override
    public org.bukkit.command.CommandSender.Spigot spigot() {
        return new org.bukkit.command.CommandSender.Spigot() {
            @Override
            public void sendMessage(net.md_5.bungee.api.chat.BaseComponent component) {
                FakePlayerWrapper.this.sendMessage(net.kyori.adventure.text.Component.text(component.toPlainText()));
            }
            
            @Override
            public void sendMessage(net.md_5.bungee.api.chat.BaseComponent... components) {
                if (components.length > 0) {
                    FakePlayerWrapper.this.sendMessage(net.kyori.adventure.text.Component.text(components[0].toPlainText()));
                }
            }
        };
    }
}
