package chef.sheesh.eyeAI.commands;

import chef.sheesh.eyeAI.bootstrap.ChefAI;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerManager;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Command system for managing fake players
 */
public class FakePlayerCommand implements CommandExecutor {
    
    private final ChefAI plugin;
    private final FakePlayerManager fakePlayerManager;
    
    public FakePlayerCommand(ChefAI plugin, FakePlayerManager fakePlayerManager) {
        this.plugin = plugin;
        this.fakePlayerManager = fakePlayerManager;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                           @NotNull String label, @NotNull String[] args) {
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "spawn":
                return handleSpawn(sender, args);
            case "remove":
                return handleRemove(sender, args);
            case "list":
                return handleList(sender);
            case "info":
                return handleInfo(sender, args);
            case "save":
                return handleSave(sender);
            case "load":
                return handleLoad(sender);
            case "clear":
                return handleClear(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }
    
    private boolean handleSpawn(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        
        Player player = (Player) sender;
        String name = args.length >= 2 ? args[1] : "FakePlayer";
        
        Location spawnLocation = player.getLocation();
        IFakePlayer fakePlayer = fakePlayerManager.createFakePlayer(spawnLocation, name);
        
        sender.sendMessage(ChatColor.GREEN + "Spawned fake player: " + ChatColor.GOLD + fakePlayer.getName());
        sender.sendMessage(ChatColor.GRAY + "ID: " + fakePlayer.getId());
        
        return true;
    }
    
    private boolean handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /fakeplayer remove <id|name>");
            return true;
        }
        
        String identifier = args[1];
        
        try {
            // Try as UUID first
            UUID id = UUID.fromString(identifier);
            if (fakePlayerManager.removeFakePlayer(id)) {
                sender.sendMessage(ChatColor.GREEN + "Removed fake player with ID: " + identifier);
            } else {
                sender.sendMessage(ChatColor.RED + "No fake player found with ID: " + identifier);
            }
        } catch (IllegalArgumentException e) {
            // Try as name
            List<FakePlayer> players = fakePlayerManager.getActiveFakePlayers().stream()
                .filter(fp -> fp.getName().equalsIgnoreCase(identifier))
                .collect(Collectors.toList());
            
            if (players.isEmpty()) {
                sender.sendMessage(ChatColor.RED + "No fake player found with name: " + identifier);
            } else {
                FakePlayer fp = players.get(0);
                fakePlayerManager.removeFakePlayer(fp.getId());
                sender.sendMessage(ChatColor.GREEN + "Removed fake player: " + fp.getName());
            }
        }
        
        return true;
    }
    
    private boolean handleList(CommandSender sender) {
        List<FakePlayer> activePlayers = new ArrayList<>(fakePlayerManager.getActiveFakePlayers());
        
        if (activePlayers.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No active fake players.");
            return true;
        }
        
        sender.sendMessage(ChatColor.GOLD + "=== Active Fake Players (" + activePlayers.size() + ") ===");
        for (FakePlayer fp : activePlayers) {
            Location loc = fp.getLocation();
            String location = String.format("%.1f, %.1f, %.1f", 
                loc.getX(), loc.getY(), loc.getZ());
            
            sender.sendMessage(ChatColor.GRAY + "- " + ChatColor.WHITE + fp.getName() + 
                ChatColor.GRAY + " (" + fp.getId().toString().substring(0, 8) + "...)" +
                ChatColor.GRAY + " at " + ChatColor.WHITE + location);
        }
        
        return true;
    }
    
    private boolean handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /fakeplayer info <id|name>");
            return true;
        }
        
        String identifier = args[1];
        FakePlayer target = null;
        
        try {
            UUID id = UUID.fromString(identifier);
            target = fakePlayerManager.getActiveFakePlayers().stream()
                .filter(fp -> fp.getId().equals(id))
                .findFirst()
                .orElse(null);
        } catch (IllegalArgumentException e) {
            target = fakePlayerManager.getActiveFakePlayers().stream()
                .filter(fp -> fp.getName().equalsIgnoreCase(identifier))
                .findFirst()
                .orElse(null);
        }
        
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "No fake player found with identifier: " + identifier);
            return true;
        }
        
        Location loc = target.getLocation();
        sender.sendMessage(ChatColor.GOLD + "=== Fake Player Info ===");
        sender.sendMessage(ChatColor.GRAY + "Name: " + ChatColor.WHITE + target.getName());
        sender.sendMessage(ChatColor.GRAY + "ID: " + ChatColor.WHITE + target.getId());
        sender.sendMessage(ChatColor.GRAY + "Location: " + ChatColor.WHITE + 
            String.format("%.1f, %.1f, %.1f", loc.getX(), loc.getY(), loc.getZ()));
        sender.sendMessage(ChatColor.GRAY + "World: " + ChatColor.WHITE + loc.getWorld().getName());
        sender.sendMessage(ChatColor.GRAY + "Health: " + ChatColor.WHITE + target.getHealth());
        sender.sendMessage(ChatColor.GRAY + "State: " + ChatColor.WHITE + target.getState());
        
        return true;
    }
    
    private boolean handleSave(CommandSender sender) {
        fakePlayerManager.saveAllFakePlayers();
        sender.sendMessage(ChatColor.GREEN + "Saved all fake players to persistence.");
        return true;
    }
    
    private boolean handleLoad(CommandSender sender) {
        fakePlayerManager.loadAllFakePlayers();
        sender.sendMessage(ChatColor.GREEN + "Loaded all fake players from persistence.");
        return true;
    }
    
    private boolean handleClear(CommandSender sender) {
        int count = fakePlayerManager.getActiveFakePlayers().size();
        fakePlayerManager.clearAllFakePlayers();
        sender.sendMessage(ChatColor.GREEN + "Cleared all " + count + " fake players.");
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Fake Player Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/fakeplayer spawn [name]" + ChatColor.GRAY + " - Spawn a fake player");
        sender.sendMessage(ChatColor.YELLOW + "/fakeplayer remove <id|name>" + ChatColor.GRAY + " - Remove a fake player");
        sender.sendMessage(ChatColor.YELLOW + "/fakeplayer list" + ChatColor.GRAY + " - List all fake players");
        sender.sendMessage(ChatColor.YELLOW + "/fakeplayer info <id|name>" + ChatColor.GRAY + " - Get info about a fake player");
        sender.sendMessage(ChatColor.YELLOW + "/fakeplayer save" + ChatColor.GRAY + " - Save all fake players");
        sender.sendMessage(ChatColor.YELLOW + "/fakeplayer load" + ChatColor.GRAY + " - Load all fake players");
        sender.sendMessage(ChatColor.YELLOW + "/fakeplayer clear" + ChatColor.GRAY + " - Clear all fake players");
    }
}
