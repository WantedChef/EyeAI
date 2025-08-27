package chef.sheesh.eyeAI.commands;

import chef.sheesh.eyeAI.bootstrap.ChefAI;
import chef.sheesh.eyeAI.ai.commands.EyeAICommand;
import chef.sheesh.eyeAI.ai.commands.EyeAdminCommand;
import chef.sheesh.eyeAI.ai.commands.EyeStatsCommand;
import chef.sheesh.eyeAI.ai.commands.EyeScoreboardCommand;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

/**
 * Manages all plugin commands registration
 */
@RequiredArgsConstructor
public class CommandManager {

    private final ChefAI plugin;
    
    /**
     * Register all plugin commands
     */
    public void registerCommands() {
        try {
            // Main command
            registerCommand("chefai", new EyeAICommand(plugin));
            
            // Admin command
            registerCommand("eyeadmin", new EyeAdminCommand(plugin));
            
            // Stats command
            registerCommand("eyestats", new EyeStatsCommand(plugin));
            
            // Scoreboard command
            registerCommand("eyescoreboard", new EyeScoreboardCommand(plugin));
            
            plugin.getLogger().info("All commands registered successfully");
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to register commands", e);
        }
    }
    
    /**
     * Register a single command
     */
    private void registerCommand(@NotNull String name, @NotNull BaseCommand executor) {
        PluginCommand command = plugin.getCommand(name);
        if (command != null) {
            command.setExecutor(executor);
        } else {
            plugin.getLogger().warning("Command " + name + " not found in plugin.yml");
        }
    }
}
