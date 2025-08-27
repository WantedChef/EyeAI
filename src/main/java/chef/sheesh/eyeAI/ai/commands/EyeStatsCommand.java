package chef.sheesh.eyeAI.ai.commands;

import chef.sheesh.eyeAI.bootstrap.ChefAI;
import chef.sheesh.eyeAI.commands.BaseCommand;
import chef.sheesh.eyeAI.data.model.PlayerData;
import chef.sheesh.eyeAI.data.CachedPlayerDataManager;
import chef.sheesh.eyeAI.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.UUID;
import java.util.Collections;

/**
 * /eyestats command - shows player statistics
 */
public class EyeStatsCommand extends BaseCommand {
    
    public EyeStatsCommand(ChefAI plugin) {
        super(plugin);
    }
    
    @Override
    public boolean onCommand(@NotNull Player player, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!checkPermission(player, Permissions.COMMAND_STATS)) {
            return true;
        }
        
        UUID targetId;
        String targetName;
        
        if (args.length == 0) {
            // Show own stats
            targetId = player.getUniqueId();
            targetName = player.getName();
        } else {
            // Show other player's stats
            if (!checkPermission(player, Permissions.COMMAND_STATS_OTHER)) {
                return true;
            }
            
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            if (!target.hasPlayedBefore() && !target.isOnline()) {
                sendMessage(player, getConfigMessage("player-not-found"));
                return true;
            }
            
            targetId = target.getUniqueId();
            targetName = target.getName();
        }
        
        // Get player data and display stats
        CachedPlayerDataManager dataManager = plugin.getDataManager();
        dataManager.loadPlayerData(targetId).thenAccept(data -> {
            Bukkit.getScheduler().runTask(plugin, () -> displayStats(player, data, targetName));
        });
        
        return true;
    }
    
    private void displayStats(@NotNull Player sender, @NotNull PlayerData data, @NotNull String playerName) {
        sendRawMessage(sender, "&8&m                                                    ");
        sendRawMessage(sender, "&b&l" + playerName + "'s Statistics");
        sendRawMessage(sender, "");
        sendRawMessage(sender, "&7Level: &a" + data.getLevel());
        sendRawMessage(sender, "&7Experience: &e" + String.format("%,d", data.getExperience()));
        sendRawMessage(sender, "&7Kills: &c" + data.getKills());
        sendRawMessage(sender, "&7Deaths: &c" + data.getDeaths());
        sendRawMessage(sender, "&7K/D Ratio: &6" + String.format("%.2f", data.getKDRatio()));
        sendRawMessage(sender, "&7Balance: &6$" + String.format("%.2f", data.getBalance()));
        sendRawMessage(sender, "&7Tokens: &d" + data.getTokens());
        sendRawMessage(sender, "");
        
        // Format playtime
        long playtimeHours = data.getPlaytime() / (1000 * 60 * 60);
        long playtimeMinutes = (data.getPlaytime() % (1000 * 60 * 60)) / (1000 * 60);
        sendRawMessage(sender, "&7Playtime: &b" + playtimeHours + "h " + playtimeMinutes + "m");
        
        // Format dates
        long daysSinceJoin = (System.currentTimeMillis() - data.getFirstJoin()) / (1000 * 60 * 60 * 24);
        sendRawMessage(sender, "&7First Join: &f" + daysSinceJoin + " days ago");
        
        sendRawMessage(sender, "&8&m                                                    ");
    }
    
    @Override
    public List<String> onTabComplete(Player player, Command command, String alias, String[] args) {
        if (args.length == 1 && hasPermission(player, Permissions.COMMAND_STATS_OTHER)) {
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                .toList();
        }
        return List.of();
    }
    
    @Override
    public List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return null; // Return null to allow default player name completion
        }
        return Collections.emptyList();
    }
}
