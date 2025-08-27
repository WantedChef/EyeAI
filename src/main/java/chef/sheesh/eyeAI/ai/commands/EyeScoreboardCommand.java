package chef.sheesh.eyeAI.ai.commands;

import chef.sheesh.eyeAI.bootstrap.ChefAI;
import chef.sheesh.eyeAI.commands.BaseCommand;
import chef.sheesh.eyeAI.scoreboard.ScoreboardManager;
import chef.sheesh.eyeAI.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * /eyescoreboard command - toggle scoreboard display
 */
public class EyeScoreboardCommand extends BaseCommand {
    
    public EyeScoreboardCommand(ChefAI plugin) {
        super(plugin);
    }
    
    @Override
    public boolean onCommand(@NotNull Player player, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!checkPermission(player, Permissions.COMMAND_SCOREBOARD)) {
            return true;
        }
        
        if (args.length == 0) {
            // Toggle own scoreboard
            plugin.getScoreboardManager().toggleScoreboard(player);
            return true;
        }
        
        // Handle subcommands
        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "toggle", "t" -> {
                if (args.length > 1) {
                    // Toggle specific player's scoreboard
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        player.sendMessage("§cPlayer not found: " + args[1]);
                        return true;
                    }
                    plugin.getScoreboardManager().toggleScoreboard(target);
                    player.sendMessage("§aToggled scoreboard for " + target.getName());
                } else {
                    // Toggle own scoreboard
                    plugin.getScoreboardManager().toggleScoreboard(player);
                }
            }
            case "show", "s" -> {
                if (args.length > 1) {
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        player.sendMessage("§cPlayer not found: " + args[1]);
                        return true;
                    }
                    plugin.getScoreboardManager().showScoreboard(target);
                    player.sendMessage("§aShowing scoreboard for " + target.getName());
                } else {
                    plugin.getScoreboardManager().showScoreboard(player);
                }
            }
            case "hide", "h" -> {
                if (args.length > 1) {
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        player.sendMessage("§cPlayer not found: " + args[1]);
                        return true;
                    }
                    plugin.getScoreboardManager().hideScoreboard(target);
                    player.sendMessage("§aHid scoreboard for " + target.getName());
                } else {
                    plugin.getScoreboardManager().hideScoreboard(player);
                }
            }
            default -> player.sendMessage("§cUnknown subcommand. Use /eyescoreboard for help.");
        }
        
        return true;
    }
    
    @Override
    public List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1 && sender instanceof Player player && hasPermission(player, Permissions.COMMAND_SCOREBOARD_OTHER)) {
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                .toList();
        }
        return List.of();
    }
}
