package chef.sheesh.eyeAI.ai.commands;

import chef.sheesh.eyeAI.bootstrap.ChefAI;
import chef.sheesh.eyeAI.commands.BaseCommand;
import chef.sheesh.eyeAI.ui.AdminGui;
import chef.sheesh.eyeAI.utils.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * /eyeadmin command - opens admin GUI
 */
public class EyeAdminCommand extends BaseCommand {

    public EyeAdminCommand(ChefAI plugin) {
        super(plugin);
    }
    
    @Override
    public boolean onCommand(Player player, Command command, String label, String[] args) {
        if (!checkPermission(player, Permissions.GUI_ADMIN)) {
            return true;
        }
        
        new AdminGui(plugin).open(player);
        return true;
    }
    
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return List.of();
    }
    
    @Override
    public List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return List.of(); // No tab completions for this command
    }
}
