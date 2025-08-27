package chef.sheesh.eyeAI.bootstrap;

import chef.sheesh.eyeAI.ai.commands.AICommands;
import chef.sheesh.eyeAI.ai.core.AIEngine;
import chef.sheesh.eyeAI.ai.core.SchedulerService;
import chef.sheesh.eyeAI.ai.movement.IMovementEngine;
import chef.sheesh.eyeAI.ai.movement.MovementEngine;
import chef.sheesh.eyeAI.ai.movement.NavGraph;
import chef.sheesh.eyeAI.core.ai.AIManager;
import chef.sheesh.eyeAI.data.CachedPlayerDataManager;
import chef.sheesh.eyeAI.infra.config.ConfigurationManager;
import chef.sheesh.eyeAI.infra.diagnostic.Diagnostic;
import chef.sheesh.eyeAI.infra.events.EventBus;
import chef.sheesh.eyeAI.infra.packets.PacketBridge;
import chef.sheesh.eyeAI.scoreboard.ScoreboardManager;
import chef.sheesh.eyeAI.ui.AdminGui;
import chef.sheesh.eyeAI.listeners.EmotionListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class ChefAI extends JavaPlugin {
    private static ChefAI instance;

    private PacketBridge packets;
    private AIManager aiManager;
    private AIEngine aiEngine;
    private CachedPlayerDataManager dataManager;
    private ScoreboardManager scoreboardManager;

    public static ChefAI get() { return instance; }

    @Override
    public void onEnable() {
        instance = this;
        Diagnostic.bootstrapLogger(getLogger());

        ConfigurationManager config = new ConfigurationManager(this, "config/chefai.yml");
        EventBus eventBus = new EventBus();

        // Initialize data manager
        this.dataManager = new CachedPlayerDataManager(this);

        // Update cache size from configuration
        this.dataManager.updateCacheSize(config.getInt("cache.l1.maxSize", 1000));

        // Initialize scoreboard manager
        this.scoreboardManager = new ScoreboardManager(Bukkit.getScoreboardManager(), "EyeAI Scoreboard");

        this.packets = new PacketBridge(this);
        this.packets.init();

        this.aiManager = new AIManager(eventBus, config, packets, this);
        this.aiManager.enable();
        
        // Initialize AIEngine with proper MovementEngine and NavGraph
        NavGraph navGraph = new NavGraph(Bukkit.getWorlds().get(0)); // Use first world for now
        SchedulerService schedulerService = new SchedulerService(this);
        IMovementEngine movementEngine = new MovementEngine(navGraph, schedulerService);
        this.aiEngine = new AIEngine(this, config, movementEngine, navGraph);
        this.aiEngine.enable(); // Start the AI engine

        // Register commands from plugin.yml
        registerCommandsFromYml();

        // Register EmotionListener
        EmotionListener emotionListener = new EmotionListener(this.aiEngine.getFakePlayerManagerInternal());
        getServer().getPluginManager().registerEvents(emotionListener, this);
        getLogger().info("EmotionListener registered.");

        getLogger().info("CHEF-AI enabled.");
    }

    @Override
    public void onDisable() {
        if (aiManager != null) {
            aiManager.disable();
        }
        if (packets != null) {
            packets.shutdown();
        }
        Diagnostic.flush();
    }

    /**
     * Get the data manager
     */
    public CachedPlayerDataManager getDataManager() {
        return dataManager;
    }

    /**
     * Get the scoreboard manager
     */
    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    /**
     * Get the AI manager
     */
    public AIManager getAIManager() {
        return aiManager;
    }

    /**
     * Get the AI engine
     */
    public AIEngine getAIEngine() {
        return aiEngine;
    }

    /**
     * Register commands defined in plugin.yml with proper error handling
     */
    private void registerCommandsFromYml() {
        // Register chefai command
        if (getCommand("chefai") != null) {
            getCommand("chefai").setExecutor((sender, cmd, label, args) -> {
                if (!(sender instanceof org.bukkit.entity.Player p)) {
                    sender.sendMessage("This command can only be used by players!");
                    return true;
                }
                new AdminGui(this).open(p);
                return true;
            });
            getLogger().info("Successfully registered chefai command");
        } else {
            getLogger().warning("Failed to register chefai command - command not found in plugin.yml");
        }

        // Register eyeadmin command
        if (getCommand("eyeadmin") != null) {
            getCommand("eyeadmin").setExecutor((sender, cmd, label, args) -> {
                if (!(sender instanceof org.bukkit.entity.Player p)) {
                    sender.sendMessage("This command can only be used by players!");
                    return true;
                }
                new AdminGui(this).open(p);
                return true;
            });
            getLogger().info("Successfully registered eyeadmin command");
        } else {
            getLogger().warning("Failed to register eyeadmin command - command not found in plugin.yml");
        }

        // Register eyestats command
        if (getCommand("eyestats") != null) {
            getCommand("eyestats").setExecutor((sender, cmd, label, args) -> {
                // TODO: Implement stats command
                sender.sendMessage("Stats command not yet implemented");
                return true;
            });
            getLogger().info("Successfully registered eyestats command");
        } else {
            getLogger().warning("Failed to register eyestats command - command not found in plugin.yml");
        }

        // Register eyescoreboard command
        if (getCommand("eyescoreboard") != null) {
            getCommand("eyescoreboard").setExecutor((sender, cmd, label, args) -> {
                // TODO: Implement scoreboard toggle command
                sender.sendMessage("Scoreboard command not yet implemented");
                return true;
            });
            getLogger().info("Successfully registered eyescoreboard command");
        } else {
            getLogger().warning("Failed to register eyescoreboard command - command not found in plugin.yml");
        }

        // Register ai command
        if (getCommand("ai") != null) {
            getCommand("ai").setExecutor(new AICommands(aiEngine, aiEngine.getFakePlayerManagerInternal()));
            getLogger().info("Successfully registered ai command");
        } else {
            getLogger().warning("Failed to register ai command - command not found in plugin.yml");
        }
    }
}
