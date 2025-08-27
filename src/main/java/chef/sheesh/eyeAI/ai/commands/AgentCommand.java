package chef.sheesh.eyeAI.ai.commands;

import chef.sheesh.eyeAI.ai.agents.*;
import chef.sheesh.eyeAI.bootstrap.ChefAI;
import chef.sheesh.eyeAI.commands.BaseCommand;
import chef.sheesh.eyeAI.utils.Permissions;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * /agent command - manage AI agents
 */
public class AgentCommand extends BaseCommand {

    public AgentCommand(ChefAI plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(Player player, Command command, String label, String[] args) {
        if (!checkPermission(player, Permissions.GUI_ADMIN)) {
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "spawn":
                handleSpawnCommand(player, Arrays.copyOfRange(args, 1, args.length));
                break;
            case "despawn":
                handleDespawnCommand(player, Arrays.copyOfRange(args, 1, args.length));
                break;
            case "list":
                handleListCommand(player);
                break;
            case "stats":
                handleStatsCommand(player);
                break;
            case "kill":
                handleKillCommand(player, Arrays.copyOfRange(args, 1, args.length));
                break;
            case "scenario":
                handleScenarioCommand(player, Arrays.copyOfRange(args, 1, args.length));
                break;
            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void handleSpawnCommand(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage("§cUsage: /agent spawn <type> [name]");
            return;
        }

        String type = args[0].toLowerCase();
        String name = args.length > 1 ? args[1] : type + "Agent";
        Location location = player.getLocation();

        IAgent agent = null;

        try {
            switch (type) {
                case "combat":
                    agent = AgentSystem.spawnCombatAgent(name, location);
                    break;
                case "patrol":
                    agent = AgentSystem.spawnPatrolAgent(name, location);
                    break;
                case "boss":
                    AgentFactory factory = new AgentFactory();
                    agent = factory.createBossAgent(name);
                    agent.spawn(location);
                    break;
                default:
                    player.sendMessage("§cUnknown agent type: " + type);
                    player.sendMessage("§cAvailable types: combat, patrol, boss");
                    return;
            }

            if (agent != null) {
                player.sendMessage("§aSuccessfully spawned " + agent.getType() + " agent: " + agent.getName());
                player.sendMessage("§7Agent ID: " + agent.getId());
            } else {
                player.sendMessage("§cFailed to spawn agent");
            }

        } catch (Exception e) {
            player.sendMessage("§cError spawning agent: " + e.getMessage());
        }
    }

    private void handleDespawnCommand(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage("§cUsage: /agent despawn <id|all|type>");
            return;
        }

        String target = args[0].toLowerCase();

        if (target.equals("all")) {
            AgentSystem.despawnAllAgents();
            player.sendMessage("§aDespawned all agents");
        } else if (target.equals("combat") || target.equals("patrol") || target.equals("guard") ||
                   target.equals("scout") || target.equals("trader") || target.equals("custom")) {
            IAgent.AgentType type = IAgent.AgentType.valueOf(target.toUpperCase());
            AgentSystem.getAgentManager().despawnAgentsByType(type);
            player.sendMessage("§aDespawned all " + type + " agents");
        } else {
            // Try to despawn by ID
            if (AgentSystem.despawnAgent(target)) {
                player.sendMessage("§aDespawned agent: " + target);
            } else {
                player.sendMessage("§cAgent not found: " + target);
            }
        }
    }

    private void handleListCommand(Player player) {
        var agents = AgentSystem.getAgentManager().getAllAgents();

        if (agents.isEmpty()) {
            player.sendMessage("§7No active agents");
            return;
        }

        player.sendMessage("§6Active Agents (" + agents.size() + "):");
        for (IAgent agent : agents) {
            String status = agent.isActive() ? "§aACTIVE" : "§cINACTIVE";
            player.sendMessage(String.format("§7- %s %s (%s) - %s",
                agent.getName(), agent.getType(), agent.getId(), status));
        }
    }

    private void handleStatsCommand(Player player) {
        AgentManager.AgentStats stats = AgentSystem.getStats();

        if (stats == null) {
            player.sendMessage("§cAgent system not initialized");
            return;
        }

        player.sendMessage("§6Agent Statistics:");
        player.sendMessage("§7Total: " + stats.getTotalAgents());
        player.sendMessage("§7Active: " + stats.getActiveAgents());
        player.sendMessage("§7Dead: " + stats.getDeadAgents());

        player.sendMessage("§6By Type:");
        for (IAgent.AgentType type : IAgent.AgentType.values()) {
            int count = stats.getAgentsByTypeCount().getOrDefault(type, 0);
            if (count > 0) {
                player.sendMessage("§7" + type + ": " + count);
            }
        }
    }

    private void handleKillCommand(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage("§cUsage: /agent kill <id>");
            return;
        }

        String id = args[0];
        IAgent agent = AgentSystem.getAgent(id);

        if (agent != null) {
            agent.kill();
            player.sendMessage("§aKilled agent: " + agent.getName());
        } else {
            player.sendMessage("§cAgent not found: " + id);
        }
    }

    private void handleScenarioCommand(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage("§cUsage: /agent scenario <name>");
            player.sendMessage("§7Available scenarios: defense, patrol, boss");
            return;
        }

        String scenarioName = args[0].toLowerCase();
        Location center = player.getLocation();

        AgentExamples examples = new AgentExamples(AgentSystem.getAgentManager());

        switch (scenarioName) {
            case "defense":
                var guards = examples.createDefensivePerimeter(center, 4);
                player.sendMessage("§aCreated defensive perimeter with " + guards.size() + " guards");
                break;
            case "patrol":
                for (int i = 0; i < 3; i++) {
                    examples.createPatrolAgentWithWaypoints(center.clone().add(i * 20, 0, 0), center.getWorld());
                }
                player.sendMessage("§aCreated patrol routes");
                break;
            case "boss":
                IAgent boss = examples.createBossAgent(center);
                if (boss != null) {
                    player.sendMessage("§aSpawned boss agent: " + boss.getName());
                } else {
                    player.sendMessage("§cFailed to spawn boss");
                }
                break;
            case "complex":
                examples.createComplexScenario(center);
                player.sendMessage("§aCreated complex scenario with multiple agent types");
                break;
            default:
                player.sendMessage("§cUnknown scenario: " + scenarioName);
                break;
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6Agent Management Commands:");
        player.sendMessage("§7/agent spawn <type> [name] - Spawn an agent");
        player.sendMessage("§7/agent despawn <id|all|type> - Despawn agents");
        player.sendMessage("§7/agent list - List all active agents");
        player.sendMessage("§7/agent stats - Show agent statistics");
        player.sendMessage("§7/agent kill <id> - Kill an agent");
        player.sendMessage("§7/agent scenario <name> - Create predefined scenarios");
        player.sendMessage("");
        player.sendMessage("§6Agent Types: §7combat, patrol, boss");
        player.sendMessage("§6Scenarios: §7defense, patrol, boss, complex");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                    @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return List.of();
        }

        if (args.length == 1) {
            return Arrays.asList("spawn", "despawn", "list", "stats", "kill", "scenario");
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            switch (subCommand) {
                case "spawn":
                    return Arrays.asList("combat", "patrol", "boss");
                case "despawn":
                    List<String> despawnOptions = new ArrayList<>();
                    despawnOptions.add("all");
                    despawnOptions.addAll(Arrays.asList("combat", "patrol", "guard", "scout", "trader", "custom"));
                    // Add agent IDs
                    AgentSystem.getAgentManager().getAllAgents().forEach(agent ->
                        despawnOptions.add(agent.getId()));
                    return despawnOptions;
                case "kill":
                    return AgentSystem.getAgentManager().getAllAgents().stream()
                        .map(IAgent::getId)
                        .toList();
                case "scenario":
                    return Arrays.asList("defense", "patrol", "boss", "complex");
            }
        }

        return List.of();
    }

    @Override
    public List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull Command command,
                                        @NotNull String alias, @NotNull String[] args) {
        return onTabComplete(sender, command, alias, args);
    }
}
