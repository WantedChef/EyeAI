package chef.sheesh.eyeAI.ai.commands;

import chef.sheesh.eyeAI.ai.core.IAIEngine;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerManager;
import chef.sheesh.eyeAI.ai.fakeplayer.IFakePlayer;
import chef.sheesh.eyeAI.bootstrap.ChefAI;
import chef.sheesh.eyeAI.core.ai.AIManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Main command handler for AI system management.
 * Provides commands for spawning, managing, and monitoring fake players.
 */
@SuppressWarnings("deprecation")
public class AICommands implements CommandExecutor, TabCompleter {

    private final FakePlayerManager fakePlayerManager;
    private final AIManager aiManager;

    public AICommands(IAIEngine aiEngine, FakePlayerManager fakePlayerManager) {
        this.fakePlayerManager = fakePlayerManager;
        this.aiManager = ChefAI.get().getAIManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("ai.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use AI commands.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "spawn":
                return handleSpawn(sender, args);
            case "despawn":
                return handleDespawn(sender, args);
            case "list":
                return handleList(sender);
            case "gui":
                return handleGui(sender);
            case "stats":
                return handleStats(sender);
            case "save":
                return handleSave(sender);
            case "load":
                return handleLoad(sender);
            case "clear":
                return handleClear(sender);
            case "info":
                return handleInfo(sender, args);
            case "move":
                return handleMove(sender, args);
            case "tp":
                return handleTeleport(sender, args);
            case "training":
                return handleTraining(sender);
            case "status":
                return handleStatus(sender);
            case "start":
                return handleStartTraining(sender);
            case "pause":
                return handlePauseTraining(sender);
            case "stop":
                return handleStopTraining(sender);
            default:
                sender.sendMessage(ChatColor.RED + "Unknown command. Use /ai help for help.");
                return false;
        }
    }

    private boolean handleSpawn(CommandSender sender, String[] args) {
        Location spawnLocation;

        if (sender instanceof Player player) {
            spawnLocation = player.getLocation();
        } else {
            // For console, use the server's spawn location
            try {
                spawnLocation = ChefAI.get().getServer().getWorlds().get(0).getSpawnLocation();
                if (spawnLocation == null) {
                    sender.sendMessage(ChatColor.RED + "Server spawn location is not available.");
                    return true;
                }
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "Failed to get server spawn location: " + e.getMessage());
                return true;
            }
        }

        String name = args.length >= 2 ? args[1] : "FakePlayer" + (int) (Math.random() * 1000);

        try {
            IFakePlayer fakePlayer = fakePlayerManager.createFakePlayer(spawnLocation, name);
            sender.sendMessage(ChatColor.GREEN + "Spawned fake player: " + ChatColor.GOLD + fakePlayer.getName());
            sender.sendMessage(ChatColor.GRAY + "ID: " + fakePlayer.getId());
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Failed to spawn fake player: " + e.getMessage());
        }
        return true;
    }

    private boolean handleDespawn(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /ai despawn <id|name>");
            return true;
        }

        String identifier = args[1];

        try {
            UUID id = UUID.fromString(identifier);
            if (fakePlayerManager.removeFakePlayer(id)) {
                sender.sendMessage(ChatColor.GREEN + "Removed fake player with ID: " + identifier);
            } else {
                sender.sendMessage(ChatColor.RED + "No fake player found with ID: " + identifier);
            }
        } catch (IllegalArgumentException e) {
            Optional<FakePlayer> player = fakePlayerManager.getActiveFakePlayers().stream()
                    .filter(fp -> fp.getName().equalsIgnoreCase(identifier))
                    .findFirst();

            if (player.isPresent()) {
                fakePlayerManager.removeFakePlayer(player.get().getId());
                sender.sendMessage(ChatColor.GREEN + "Removed fake player: " + player.get().getName());
            } else {
                sender.sendMessage(ChatColor.RED + "No fake player found with name: " + identifier);
            }
        }
        return true;
    }

    private boolean handleList(CommandSender sender) {
        List<IFakePlayer> activePlayers = new ArrayList<>(fakePlayerManager.getActiveFakePlayers());

        // Also include training fake players managed by the simulation engine
        if (aiManager != null && aiManager.hasSimEngine()) {
            chef.sheesh.eyeAI.core.sim.FakePlayerEngine simEngine = aiManager.sim();
            if (simEngine != null) {
                activePlayers.addAll(simEngine.getTrainingFakePlayers());
            }
        }

        if (activePlayers.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No active fake players.");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "=== Active Fake Players (" + activePlayers.size() + ") ===");
        for (IFakePlayer fp : activePlayers) {
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
            sender.sendMessage(ChatColor.RED + "Usage: /ai info <id|name>");
            return true;
        }

        String identifier = args[1];
        Optional<FakePlayer> target = findFakePlayer(identifier);

        if (target.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No fake player found with identifier: " + identifier);
            return true;
        }

        IFakePlayer fp = target.get();
        Location loc = fp.getLocation();

        sender.sendMessage(ChatColor.GOLD + "=== Fake Player Info ===");
        sender.sendMessage(ChatColor.GRAY + "Name: " + ChatColor.WHITE + fp.getName());
        sender.sendMessage(ChatColor.GRAY + "ID: " + ChatColor.WHITE + fp.getId());
        sender.sendMessage(ChatColor.GRAY + "Location: " + ChatColor.WHITE +
                String.format("%.1f, %.1f, %.1f", loc.getX(), loc.getY(), loc.getZ()));
        sender.sendMessage(ChatColor.GRAY + "World: " + ChatColor.WHITE + loc.getWorld().getName());
        sender.sendMessage(ChatColor.GRAY + "Health: " + ChatColor.WHITE + fp.getHealth());
        sender.sendMessage(ChatColor.GRAY + "State: " + ChatColor.WHITE + fp.getStateName());

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

    private boolean handleMove(CommandSender sender, String[] args) {
        if (args.length < 5) {
            sender.sendMessage(ChatColor.RED + "Usage: /ai move <id|name> <x> <y> <z>");
            return true;
        }

        String identifier = args[1];
        Optional<FakePlayer> target = findFakePlayer(identifier);

        if (target.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No fake player found with identifier: " + identifier);
            return true;
        }

        try {
            double x = Double.parseDouble(args[2]);
            double y = Double.parseDouble(args[3]);
            double z = Double.parseDouble(args[4]);

            Location newLoc = target.get().getLocation().clone();
            newLoc.setX(x);
            newLoc.setY(y);
            newLoc.setZ(z);

            target.get().setLocation(newLoc);
            sender.sendMessage(ChatColor.GREEN + "Moved fake player " + target.get().getName() +
                    " to " + String.format("%.1f, %.1f, %.1f", x, y, z));
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid coordinates provided.");
        }

        return true;
    }

    private boolean handleTeleport(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /ai tp <id|name>");
            return true;
        }

        String identifier = args[1];
        Optional<FakePlayer> target = findFakePlayer(identifier);

        if (target.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No fake player found with identifier: " + identifier);
            return true;
        }

        Location loc = target.get().getLocation();
        player.teleport(loc);
        sender.sendMessage(ChatColor.GREEN + "Teleported to fake player: " + target.get().getName());

        return true;
    }

    private Optional<FakePlayer> findFakePlayer(String identifier) {
        try {
            UUID id = UUID.fromString(identifier);
            return fakePlayerManager.getActiveFakePlayers().stream()
                    .filter(fp -> fp.getId().equals(id))
                    .findFirst();
        } catch (IllegalArgumentException e) {
            return fakePlayerManager.getActiveFakePlayers().stream()
                    .filter(fp -> fp.getName().equalsIgnoreCase(identifier))
                    .findFirst();
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== AI Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/ai spawn [name]" + ChatColor.GRAY + " - Spawn a fake player");
        sender.sendMessage(ChatColor.YELLOW + "/ai despawn <id|name>" + ChatColor.GRAY + " - Remove a fake player");
        sender.sendMessage(ChatColor.YELLOW + "/ai list" + ChatColor.GRAY + " - List all fake players");
        sender.sendMessage(ChatColor.YELLOW + "/ai info <id|name>" + ChatColor.GRAY + " - Get fake player info");
        sender.sendMessage(ChatColor.YELLOW + "/ai save" + ChatColor.GRAY + " - Save fake players");
        sender.sendMessage(ChatColor.YELLOW + "/ai load" + ChatColor.GRAY + " - Load fake players");
        sender.sendMessage(ChatColor.YELLOW + "/ai clear" + ChatColor.GRAY + " - Clear all fake players");
        sender.sendMessage(ChatColor.YELLOW + "/ai move <id|name> <x> <y> <z>" + ChatColor.GRAY + " - Move fake player");
        sender.sendMessage(ChatColor.YELLOW + "/ai tp <id|name>" + ChatColor.GRAY + " - Teleport to fake player");
        sender.sendMessage(ChatColor.YELLOW + "/ai training" + ChatColor.GRAY + " - Show training status");
        sender.sendMessage(ChatColor.YELLOW + "/ai status" + ChatColor.GRAY + " - Show AI system status");
        sender.sendMessage(ChatColor.YELLOW + "/ai start" + ChatColor.GRAY + " - Start AI training");
        sender.sendMessage(ChatColor.YELLOW + "/ai pause" + ChatColor.GRAY + " - Pause AI training");
        sender.sendMessage(ChatColor.YELLOW + "/ai stop" + ChatColor.GRAY + " - Stop AI training");
    }

    private boolean handleGui(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "GUI functionality coming soon!");
        return true;
    }

    private boolean handleStats(CommandSender sender) {
        List<IFakePlayer> activePlayers = new ArrayList<>(fakePlayerManager.getActiveFakePlayers());
        sender.sendMessage(ChatColor.GOLD + "=== AI System Stats ===");
        sender.sendMessage(ChatColor.GRAY + "Active fake players: " + ChatColor.WHITE + activePlayers.size());
        return true;
    }

    private boolean handleTraining(CommandSender sender) {
        if (aiManager == null) {
            sender.sendMessage(ChatColor.RED + "AI Manager not available");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "=== AI/ML Training Status ===");

        // Training status
        String trainingStatus = "Unknown";
        try {
            trainingStatus = aiManager.getTrainingStatus();
        } catch (Exception e) {
            trainingStatus = "Error: " + e.getMessage();
        }
        sender.sendMessage(ChatColor.GRAY + "Training: " + ChatColor.WHITE + trainingStatus);

        // ML Core status
        boolean hasMLCore = false;
        try {
            hasMLCore = aiManager.hasMLCore();
        } catch (Exception e) {
            // Ignore
        }

        if (hasMLCore) {
            sender.sendMessage(ChatColor.GRAY + "ML Core: " + ChatColor.GREEN + "Active");

            // Get ML statistics if available
            try {
                if (aiManager.ml() != null) {
                    var stats = aiManager.ml().getStatistics();

                    sender.sendMessage(ChatColor.GRAY + "Experiences Processed: " + ChatColor.WHITE + stats.totalExperiencesProcessed);
                    sender.sendMessage(ChatColor.GRAY + "Training Batches: " + ChatColor.WHITE + stats.totalTrainingBatches);
                    sender.sendMessage(ChatColor.GRAY + "Average Reward: " + ChatColor.WHITE + String.format("%.3f", stats.averageReward));
                    sender.sendMessage(ChatColor.GRAY + "Buffer Size: " + ChatColor.WHITE + stats.experienceBufferSize);
                }
            } catch (Exception e) {
                sender.sendMessage(ChatColor.GRAY + "Detailed stats: " + ChatColor.YELLOW + "Unavailable");
            }
        } else {
            sender.sendMessage(ChatColor.GRAY + "ML Core: " + ChatColor.RED + "Not Available");
        }

        return true;
    }

    private boolean handleStatus(CommandSender sender) {
        if (aiManager == null) {
            sender.sendMessage(ChatColor.RED + "AI Manager not available");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "=== AI System Status ===");

        // Components status
        sender.sendMessage(ChatColor.GRAY + "Components:");

        boolean hasMLCore = false;
        boolean hasSimEngine = false;
        boolean hasScheduler = false;
        try {
            hasMLCore = aiManager.hasMLCore();
            hasSimEngine = aiManager.hasSimEngine();
            hasScheduler = aiManager.hasScheduler();
        } catch (Exception e) {
            // Ignore
        }

        sender.sendMessage(ChatColor.GRAY + "  - ML Core: " + (hasMLCore ? ChatColor.GREEN + "✓" : ChatColor.RED + "✗"));
        sender.sendMessage(ChatColor.GRAY + "  - Sim Engine: " + (hasSimEngine ? ChatColor.GREEN + "✓" : ChatColor.RED + "✗"));
        sender.sendMessage(ChatColor.GRAY + "  - Scheduler: " + (hasScheduler ? ChatColor.GREEN + "✓" : ChatColor.RED + "✗"));

        // Active fake players
        int activeFakePlayers = fakePlayerManager.getActiveFakePlayers().size();
        sender.sendMessage(ChatColor.GRAY + "Active Fake Players: " + ChatColor.WHITE + activeFakePlayers);

        // Overall system status
        boolean systemHealthy = hasMLCore && hasSimEngine && hasScheduler;
        sender.sendMessage(ChatColor.GRAY + "System Health: " + (systemHealthy ? ChatColor.GREEN + "Healthy" : ChatColor.YELLOW + "Degraded"));

        return true;
    }

    private boolean handleStartTraining(CommandSender sender) {
        if (aiManager == null) {
            sender.sendMessage(ChatColor.RED + "AI Manager not available");
            return true;
        }

        try {
            aiManager.startTraining();
            sender.sendMessage(ChatColor.GREEN + "AI training started successfully");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Failed to start AI training: " + e.getMessage());
        }

        return true;
    }

    private boolean handlePauseTraining(CommandSender sender) {
        if (aiManager == null) {
            sender.sendMessage(ChatColor.RED + "AI Manager not available");
            return true;
        }

        try {
            // Check if scheduler exists and is running
            if (aiManager.scheduler() != null && aiManager.scheduler().isRunning()) {
                aiManager.scheduler().stop(); // Using stop as pause since there's no specific pause method
                sender.sendMessage(ChatColor.YELLOW + "AI training paused");
            } else {
                sender.sendMessage(ChatColor.RED + "AI training is not currently running");
            }
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Failed to pause AI training: " + e.getMessage());
        }

        return true;
    }

    private boolean handleStopTraining(CommandSender sender) {
        if (aiManager == null) {
            sender.sendMessage(ChatColor.RED + "AI Manager not available");
            return true;
        }

        try {
            aiManager.stopTraining();
            sender.sendMessage(ChatColor.GREEN + "AI training stopped successfully");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Failed to stop AI training: " + e.getMessage());
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("ai.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return Arrays.asList("spawn", "despawn", "list", "info", "save", "load", "clear", "move", "tp", "gui", "stats", "training", "status", "start", "pause", "stop");
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "despawn", "info", "tp", "move":
                    return fakePlayerManager.getActiveFakePlayers().stream()
                            .map(IFakePlayer::getName)
                            .collect(Collectors.toList());
                default:
                    return Collections.emptyList();
            }
        }

        return Collections.emptyList();
    }
}