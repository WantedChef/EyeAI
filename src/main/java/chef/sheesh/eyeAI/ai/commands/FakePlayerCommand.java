package chef.sheesh.eyeAI.ai.commands;

import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerManager;
import chef.sheesh.eyeAI.ai.fakeplayer.config.FakePlayerConfig;
import chef.sheesh.eyeAI.ai.fakeplayer.config.FakePlayerConfigManager;
import chef.sheesh.eyeAI.ai.fakeplayer.monitoring.PerformanceMonitor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Main command handler for fake player management
 */
public class FakePlayerCommand implements CommandExecutor, TabCompleter {

    private final FakePlayerManager fakePlayerManager;
    private final FakePlayerConfigManager configManager;
    private final PerformanceMonitor performanceMonitor;

    public FakePlayerCommand(FakePlayerManager fakePlayerManager,
                           FakePlayerConfigManager configManager,
                           PerformanceMonitor performanceMonitor) {
        this.fakePlayerManager = fakePlayerManager;
        this.configManager = configManager;
        this.performanceMonitor = performanceMonitor;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "spawn" -> handleSpawn(sender, Arrays.copyOfRange(args, 1, args.length));
            case "despawn" -> handleDespawn(sender, Arrays.copyOfRange(args, 1, args.length));
            case "list" -> handleList(sender);
            case "info" -> handleInfo(sender, Arrays.copyOfRange(args, 1, args.length));
            case "config" -> handleConfig(sender, Arrays.copyOfRange(args, 1, args.length));
            case "stats" -> handleStats(sender);
            case "performance" -> handlePerformance(sender);
            case "reload" -> handleReload(sender);
            case "save" -> handleSave(sender);
            case "clear" -> handleClear(sender);
            default -> sendHelp(sender);
        }

        return true;
    }

    /**
     * Handle spawn command
     */
    private void handleSpawn(CommandSender sender, String[] args) {
        if (!sender.hasPermission("fakeplayer.admin.spawn")) {
            sender.sendMessage("§cYou don't have permission to spawn fake players.");
            return;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /fakeplayer spawn <name> [world] [x] [y] [z]");
            return;
        }

        String name = args[0];

        // Check if fake player already exists
        Optional<FakePlayer> existing = fakePlayerManager.getFakePlayer(name);
        if (existing.isPresent()) {
            sender.sendMessage("§cA fake player with name '" + name + "' already exists.");
            return;
        }

        org.bukkit.Location spawnLocation;

        if (sender instanceof Player player) {
            // Use player's location if no coordinates provided
            if (args.length == 1) {
                spawnLocation = player.getLocation();
            } else {
                // Parse coordinates
                spawnLocation = parseLocation(player.getLocation(), Arrays.copyOfRange(args, 1, args.length));
                if (spawnLocation == null) {
                    sender.sendMessage("§cInvalid coordinates. Usage: [world] [x] [y] [z]");
                    return;
                }
            }
        } else {
            if (args.length < 4) {
                sender.sendMessage("§cConsole must specify world and coordinates. Usage: /fakeplayer spawn <name> <world> <x> <y> <z>");
                return;
            }
            spawnLocation = parseLocation(null, Arrays.copyOfRange(args, 1, args.length));
            if (spawnLocation == null) {
                sender.sendMessage("§cInvalid coordinates or world.");
                return;
            }
        }

        // Spawn the fake player
        fakePlayerManager.spawnFakePlayer(name, spawnLocation);
        sender.sendMessage("§aSuccessfully spawned fake player '" + name + "' at " + formatLocation(spawnLocation));
    }

    /**
     * Handle despawn command
     */
    private void handleDespawn(CommandSender sender, String[] args) {
        if (!sender.hasPermission("fakeplayer.admin.despawn")) {
            sender.sendMessage("§cYou don't have permission to despawn fake players.");
            return;
        }

        if (args.length == 0) {
            sender.sendMessage("§cUsage: /fakeplayer despawn <name|all>");
            return;
        }

        String target = args[0].toLowerCase();

        if (target.equals("all")) {
            int count = fakePlayerManager.getActiveFakePlayerCount();
            fakePlayerManager.despawnAll();
            sender.sendMessage("§aDespawned " + count + " fake players.");
        } else {
            Optional<FakePlayer> fakePlayer = fakePlayerManager.getFakePlayer(target);
            if (fakePlayer.isPresent()) {
                boolean success = fakePlayerManager.despawnFakePlayer(fakePlayer.get().getId());
                if (success) {
                    sender.sendMessage("§aSuccessfully despawned fake player '" + target + "'");
                } else {
                    sender.sendMessage("§cFailed to despawn fake player '" + target + "'");
                }
            } else {
                sender.sendMessage("§cFake player '" + target + "' not found.");
            }
        }
    }

    /**
     * Handle list command
     */
    private void handleList(CommandSender sender) {
        if (!sender.hasPermission("fakeplayer.admin.list")) {
            sender.sendMessage("§cYou don't have permission to list fake players.");
            return;
        }

        java.util.Collection<FakePlayer> fakePlayers = fakePlayerManager.getActiveFakePlayers();

        if (fakePlayers.isEmpty()) {
            sender.sendMessage("§eNo active fake players.");
            return;
        }

        sender.sendMessage("§6=== Active Fake Players (" + fakePlayers.size() + ") ===");
        for (FakePlayer fp : fakePlayers) {
            org.bukkit.Location loc = fp.getLocation();
            sender.sendMessage(String.format("§e%s §7- %s (%.1f HP) at %s",
                fp.getName(),
                fp.getState().name(),
                fp.getHealth(),
                formatLocation(loc)));
        }
    }

    /**
     * Handle info command
     */
    private void handleInfo(CommandSender sender, String[] args) {
        if (!sender.hasPermission("fakeplayer.admin.info")) {
            sender.sendMessage("§cYou don't have permission to view fake player info.");
            return;
        }

        if (args.length == 0) {
            sender.sendMessage("§cUsage: /fakeplayer info <name>");
            return;
        }

        String name = args[0];
        Optional<FakePlayer> fakePlayer = fakePlayerManager.getFakePlayer(name);

        if (fakePlayer.isEmpty()) {
            sender.sendMessage("§cFake player '" + name + "' not found.");
            return;
        }

        FakePlayer fp = fakePlayer.get();
        sender.sendMessage("§6=== Fake Player Info: " + fp.getName() + " ===");
        sender.sendMessage("§eID: §f" + fp.getId());
        sender.sendMessage("§eState: §f" + fp.getState().name());
        sender.sendMessage("§eHealth: §f" + String.format("%.1f/%.1f", fp.getHealth(), fp.getMaxHealth()));
        sender.sendMessage("§eLocation: §f" + formatLocation(fp.getLocation()));
        sender.sendMessage("§eBehavior Tree: §f" + (fp.getBehaviorTree() != null ? fp.getBehaviorTree().getDescription() : "None"));
        sender.sendMessage("§eLast Action: §f" + fp.getLastActionTime() + "ms ago");

        // Show statistics if available
        Optional<chef.sheesh.eyeAI.ai.fakeplayer.persistence.FakePlayerStatistics> stats =
            fakePlayerManager.getStatistics(fp.getId());
        if (stats.isPresent()) {
            chef.sheesh.eyeAI.ai.fakeplayer.persistence.FakePlayerStatistics s = stats.get();
            sender.sendMessage("§eStatistics:");
            sender.sendMessage("  §7Ticks Alive: §f" + s.getTotalTicks());
            sender.sendMessage("  §7Entities Attacked: §f" + s.getEntitiesAttacked());
            sender.sendMessage("  §7Damage Dealt: §f" + s.getDamageDealt());
            sender.sendMessage("  §7Damage Taken: §f" + s.getDamageTaken());
            sender.sendMessage("  §7Distance Traveled: §f" + s.getDistanceTraveled() + " blocks");
        }
    }

    /**
     * Handle config command
     */
    private void handleConfig(CommandSender sender, String[] args) {
        if (!sender.hasPermission("fakeplayer.admin.config")) {
            sender.sendMessage("§cYou don't have permission to manage configuration.");
            return;
        }

        if (args.length == 0) {
            sendConfigHelp(sender);
            return;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "get" -> {
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /fakeplayer config get <key>");
                    return;
                }
                String key = args[1];
                Object value = configManager.get(key);
                sender.sendMessage("§e" + key + ": §f" + (value != null ? value.toString() : "null"));
            }
            case "set" -> {
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /fakeplayer config set <key> <value>");
                    return;
                }
                String key = args[1];
                String value = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                configManager.set(key, value);
                sender.sendMessage("§aSet " + key + " to " + value);
            }
            case "list" -> {
                FakePlayerConfig config = configManager.getConfig();
                sender.sendMessage("§6=== Configuration Summary ===");
                sender.sendMessage(configManager.getConfigSummary());
            }
            default -> sendConfigHelp(sender);
        }
    }

    /**
     * Handle stats command
     */
    private void handleStats(CommandSender sender) {
        if (!sender.hasPermission("fakeplayer.admin.stats")) {
            sender.sendMessage("§cYou don't have permission to view statistics.");
            return;
        }

        java.util.Map<java.util.UUID, chef.sheesh.eyeAI.ai.fakeplayer.persistence.FakePlayerStatistics> allStats =
            fakePlayerManager.getAllStatistics();

        sender.sendMessage("§6=== Fake Player Statistics ===");
        sender.sendMessage("§eTotal Fake Players: §f" + allStats.size());

        if (!allStats.isEmpty()) {
            long totalTicks = allStats.values().stream().mapToLong(chef.sheesh.eyeAI.ai.fakeplayer.persistence.FakePlayerStatistics::getTotalTicks).sum();
            long totalDamageDealt = allStats.values().stream().mapToLong(chef.sheesh.eyeAI.ai.fakeplayer.persistence.FakePlayerStatistics::getDamageDealt).sum();
            long totalDistance = allStats.values().stream().mapToLong(chef.sheesh.eyeAI.ai.fakeplayer.persistence.FakePlayerStatistics::getDistanceTraveled).sum();

            sender.sendMessage("§eTotal Ticks: §f" + totalTicks);
            sender.sendMessage("§eTotal Damage Dealt: §f" + totalDamageDealt);
            sender.sendMessage("§eTotal Distance: §f" + totalDistance + " blocks");
        }
    }

    /**
     * Handle performance command
     */
    private void handlePerformance(CommandSender sender) {
        if (!sender.hasPermission("fakeplayer.admin.performance")) {
            sender.sendMessage("§cYou don't have permission to view performance data.");
            return;
        }

        sender.sendMessage("§6=== Performance Report ===");
        sender.sendMessage(performanceMonitor.getPerformanceReport());
    }

    /**
     * Handle reload command
     */
    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("fakeplayer.admin.reload")) {
            sender.sendMessage("§cYou don't have permission to reload configuration.");
            return;
        }

        configManager.reloadConfig();
        sender.sendMessage("§aConfiguration reloaded successfully.");
    }

    /**
     * Handle save command
     */
    private void handleSave(CommandSender sender) {
        if (!sender.hasPermission("fakeplayer.admin.save")) {
            sender.sendMessage("§cYou don't have permission to save data.");
            return;
        }

        fakePlayerManager.forceSaveAll();
        sender.sendMessage("§aAll fake player data saved successfully.");
    }

    /**
     * Handle clear command
     */
    private void handleClear(CommandSender sender) {
        if (!sender.hasPermission("fakeplayer.admin.clear")) {
            sender.sendMessage("§cYou don't have permission to clear data.");
            return;
        }

        // Note: This would need to be implemented in FakePlayerManager
        sender.sendMessage("§eClear command not yet implemented in FakePlayerManager.");
    }

    /**
     * Parse location from command arguments
     */
    private org.bukkit.Location parseLocation(org.bukkit.Location baseLocation, String[] args) {
        try {
            if (args.length == 3) {
                // x y z (use player's world)
                double x = Double.parseDouble(args[0]);
                double y = Double.parseDouble(args[1]);
                double z = Double.parseDouble(args[2]);
                return new org.bukkit.Location(baseLocation.getWorld(), x, y, z);
            } else if (args.length == 4) {
                // world x y z
                org.bukkit.World world = org.bukkit.Bukkit.getWorld(args[0]);
                if (world == null) {
                    return null;
                }
                double x = Double.parseDouble(args[1]);
                double y = Double.parseDouble(args[2]);
                double z = Double.parseDouble(args[3]);
                return new org.bukkit.Location(world, x, y, z);
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    /**
     * Format location for display
     */
    private String formatLocation(org.bukkit.Location location) {
        return String.format("%.1f, %.1f, %.1f in %s",
            location.getX(), location.getY(), location.getZ(), location.getWorld().getName());
    }

    /**
     * Send help message
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== FakePlayer Commands ===");
        sender.sendMessage("§e/fakeplayer spawn <name> [world] [x] [y] [z] §7- Spawn a fake player");
        sender.sendMessage("§e/fakeplayer despawn <name|all> §7- Despawn fake player(s)");
        sender.sendMessage("§e/fakeplayer list §7- List all active fake players");
        sender.sendMessage("§e/fakeplayer info <name> §7- Show detailed info about a fake player");
        sender.sendMessage("§e/fakeplayer config §7- Manage configuration");
        sender.sendMessage("§e/fakeplayer stats §7- Show global statistics");
        sender.sendMessage("§e/fakeplayer performance §7- Show performance report");
        sender.sendMessage("§e/fakeplayer reload §7- Reload configuration");
        sender.sendMessage("§e/fakeplayer save §7- Force save all data");
        sender.sendMessage("§e/fakeplayer clear §7- Clear all persistent data");
    }

    /**
     * Send config help
     */
    private void sendConfigHelp(CommandSender sender) {
        sender.sendMessage("§6=== Config Commands ===");
        sender.sendMessage("§e/fakeplayer config get <key> §7- Get configuration value");
        sender.sendMessage("§e/fakeplayer config set <key> <value> §7- Set configuration value");
        sender.sendMessage("§e/fakeplayer config list §7- Show configuration summary");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First argument - subcommands
            String[] subCommands = {"spawn", "despawn", "list", "info", "config", "stats", "performance", "reload", "save", "clear"};
            completions.addAll(Arrays.asList(subCommands));
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "despawn" -> {
                    // Add "all" option
                    completions.add("all");
                    // Add existing fake player names
                    fakePlayerManager.getActiveFakePlayers().forEach(fp -> completions.add(fp.getName()));
                }
                case "info" -> {
                    // Add existing fake player names
                    fakePlayerManager.getActiveFakePlayers().forEach(fp -> completions.add(fp.getName()));
                }
                case "config" -> {
                    completions.addAll(Arrays.asList("get", "set", "list"));
                }
            }
        } else if (args.length == 3 && "config".equals(args[0]) && "get".equals(args[1])) {
            // Config keys for get command
            completions.addAll(Arrays.asList(
                "maxFakePlayers", "detectionRange", "attackRange", "movementSpeed",
                "enablePersistence", "enableVisualNpc", "saveStatistics"
            ));
        }

        // Filter completions based on current input
        if (args.length > 0) {
            String current = args[args.length - 1].toLowerCase();
            completions.removeIf(s -> !s.toLowerCase().startsWith(current));
        }

        return completions;
    }
}
