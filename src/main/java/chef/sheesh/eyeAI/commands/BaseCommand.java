package chef.sheesh.eyeAI.commands;

import chef.sheesh.eyeAI.bootstrap.ChefAI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Base class for all plugin commands with common functionality
 */
public abstract class BaseCommand implements CommandExecutor, TabCompleter {

    protected final ChefAI plugin;
    protected final MiniMessage miniMessage = MiniMessage.miniMessage();

    protected BaseCommand(ChefAI plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        
        try {
            return onCommand(player, command, label, args);
        } catch (Exception e) {
            plugin.getLogger().severe("Error executing command: " + e.getMessage());
            e.printStackTrace();
            sendMessage(sender, "&cAn error occurred while executing this command.");
            return true;
        }
    }
    
    public abstract boolean onCommand(Player player, Command command, String label, String[] args);
    
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (sender instanceof Player player) {
            return onTabComplete(player, command, alias, args);
        }
        return Collections.emptyList();
    }
    
    public List<String> onTabComplete(Player player, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
    
    /**
     * Sends a message to a command sender
     * @param sender The command sender to send the message to
     * @param message The message to send (supports color codes with &)
     */
    protected void sendMessage(@NotNull CommandSender sender, @NotNull String message) {
        if (message != null && !message.isEmpty()) {
            Component component = miniMessage.deserialize(getPrefix() + message);
            sender.sendMessage(component);
        }
    }
    
    /**
     * Send a message without prefix
     */
    protected void sendRawMessage(@NotNull CommandSender sender, @NotNull String message) {
        Component component = miniMessage.deserialize(message);
        sender.sendMessage(component);
    }
    
    /**
     * Check if sender has permission
     */
    protected boolean hasPermission(@NotNull CommandSender sender, @NotNull String permission) {
        return sender.hasPermission(permission);
    }
    
    /**
     * Check permission and send error if not allowed
     */
    protected boolean checkPermission(@NotNull CommandSender sender, @NotNull String permission) {
        if (!hasPermission(sender, permission)) {
            sendMessage(sender, getConfigMessage("no-permission"));
            return false;
        }
        return true;
    }
    
    /**
     * Check if sender is a player
     */
    protected boolean requirePlayer(@NotNull CommandSender sender) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, "&cThis command can only be used by players.");
            return false;
        }
        return true;
    }
    
    /**
     * Get message from config
     */
    protected String getConfigMessage(@NotNull String key) {
        try {
            // For now, return a default message since ChefAI doesn't have ConfigManager yet
            return "&cMessage not found: " + key;
        } catch (Exception e) {
            return "&cError loading message: " + key;
        }
    }

    /**
     * Get prefix from config
     */
    protected String getPrefix() {
        try {
            // For now, return a default prefix since ChefAI doesn't have ConfigManager yet
            return "&8[&bEyeAI&8] ";
        } catch (Exception e) {
            return "&8[&bEyeAI&8] ";
        }
    }
    
    /**
     * Get tab completions for the command
     */
    @Nullable
    public abstract List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args);
}
