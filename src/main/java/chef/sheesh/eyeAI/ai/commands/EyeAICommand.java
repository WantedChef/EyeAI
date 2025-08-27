package chef.sheesh.eyeAI.ai.commands;

import chef.sheesh.eyeAI.bootstrap.ChefAI;
import chef.sheesh.eyeAI.commands.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * Main /chefai command - placeholder implementation
 */
public class EyeAICommand extends BaseCommand {
    
    public EyeAICommand(ChefAI plugin) {
        super(plugin);
    }
    
    @Override
    public boolean onCommand(Player player, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "help", "?" -> showHelp(player);
            case "version", "ver" -> showVersion(player);
            case "reload" -> reloadPlugin(player);
            default -> player.sendMessage("§cUnknown command. Use /chefai help for help.");
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(Player player, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("help", "version", "reload");
        }
        return Collections.emptyList();
    }
    
    @Override
    public List<String> getTabCompletions(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("help", "version", "reload");
        }
        return Collections.emptyList();
    }
    
    private void showHelp(Player player) {
        player.sendMessage("§a=== EyeAI Help ===");
        player.sendMessage("§7/chefai §f- Show this help message");
        player.sendMessage("§7/chefai reload §f- Reload the plugin");
        player.sendMessage("§7/ai §f- Manage AI training (start/stop/status)");
        player.sendMessage("§7/ai help §f- Show AI training commands");
    }
    
    private void showVersion(Player player) {
        player.sendMessage("§aEyeAI Plugin v" + plugin.getPluginMeta().getVersion());
    }
    
    private void reloadPlugin(Player player) {
        try {
            plugin.reloadConfig();
            player.sendMessage("§aPlugin configuration reloaded!");
        } catch (Exception e) {
            player.sendMessage("§cError reloading plugin: " + e.getMessage());
            plugin.getLogger().severe("Error reloading plugin: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
