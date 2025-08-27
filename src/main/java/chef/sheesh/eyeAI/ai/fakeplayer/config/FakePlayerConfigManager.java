package chef.sheesh.eyeAI.ai.fakeplayer.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

/**
 * Manager for fake player configuration
 */
public class FakePlayerConfigManager {

    private final JavaPlugin plugin;
    private final File configFile;
    private FakePlayerConfig config;
    private YamlConfiguration yamlConfig;

    public FakePlayerConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "fake_players.yml");

        // Create data folder if it doesn't exist
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        loadConfig();
    }

    /**
     * Load configuration from file
     */
    public void loadConfig() {
        if (!configFile.exists()) {
            // Create default configuration
            config = new FakePlayerConfig();
            saveConfig();
            return;
        }

        yamlConfig = YamlConfiguration.loadConfiguration(configFile);
        config = new FakePlayerConfig(yamlConfig);

        plugin.getLogger().info("Loaded fake player configuration from " + configFile.getName());
    }

    /**
     * Save configuration to file
     */
    public void saveConfig() {
        if (yamlConfig == null) {
            yamlConfig = new YamlConfiguration();
        }

        // Clear existing configuration
        for (String key : yamlConfig.getKeys(false)) {
            yamlConfig.set(key, null);
        }

        // Save current configuration
        config.saveToConfig(yamlConfig);

        try {
            yamlConfig.save(configFile);
            plugin.getLogger().info("Saved fake player configuration to " + configFile.getName());
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save fake player configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Reload configuration from file
     */
    public void reloadConfig() {
        loadConfig();
        plugin.getLogger().info("Reloaded fake player configuration");
    }

    /**
     * Get the current configuration
     */
    public FakePlayerConfig getConfig() {
        return config;
    }

    /**
     * Set a new configuration
     */
    public void setConfig(FakePlayerConfig config) {
        this.config = config;
        saveConfig();
    }

    /**
     * Get configuration value by key path
     */
    public Object get(String path) {
        return yamlConfig.get(path);
    }

    /**
     * Get configuration value by key path with default
     */
    public Object get(String path, Object def) {
        return yamlConfig.get(path, def);
    }

    /**
     * Set configuration value
     */
    public void set(String path, Object value) {
        yamlConfig.set(path, value);
        config.loadFromConfig(yamlConfig);
        saveConfig();
    }

    /**
     * Check if configuration contains a path
     */
    public boolean contains(String path) {
        return yamlConfig.contains(path);
    }

    /**
     * Get string value
     */
    public String getString(String path) {
        return yamlConfig.getString(path);
    }

    /**
     * Get string value with default
     */
    public String getString(String path, String def) {
        return yamlConfig.getString(path, def);
    }

    /**
     * Get int value
     */
    public int getInt(String path) {
        return yamlConfig.getInt(path);
    }

    /**
     * Get int value with default
     */
    public int getInt(String path, int def) {
        return yamlConfig.getInt(path, def);
    }

    /**
     * Get double value
     */
    public double getDouble(String path) {
        return yamlConfig.getDouble(path);
    }

    /**
     * Get double value with default
     */
    public double getDouble(String path, double def) {
        return yamlConfig.getDouble(path, def);
    }

    /**
     * Get boolean value
     */
    public boolean getBoolean(String path) {
        return yamlConfig.getBoolean(path);
    }

    /**
     * Get boolean value with default
     */
    public boolean getBoolean(String path, boolean def) {
        return yamlConfig.getBoolean(path, def);
    }

    /**
     * Get long value
     */
    public long getLong(String path) {
        return yamlConfig.getLong(path);
    }

    /**
     * Get long value with default
     */
    public long getLong(String path, long def) {
        return yamlConfig.getLong(path, def);
    }

    /**
     * Create a backup of the current configuration
     */
    public void createBackup() {
        File backupFile = new File(configFile.getParent(), configFile.getName() + ".backup");
        try {
            if (configFile.exists()) {
                java.nio.file.Files.copy(configFile.toPath(), backupFile.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                plugin.getLogger().info("Created configuration backup: " + backupFile.getName());
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to create configuration backup: " + e.getMessage());
        }
    }

    /**
     * Restore configuration from backup
     */
    public boolean restoreBackup() {
        File backupFile = new File(configFile.getParent(), configFile.getName() + ".backup");
        if (!backupFile.exists()) {
            plugin.getLogger().warning("No backup file found to restore from");
            return false;
        }

        try {
            java.nio.file.Files.copy(backupFile.toPath(), configFile.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            loadConfig();
            plugin.getLogger().info("Restored configuration from backup");
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to restore configuration from backup: " + e.getMessage());
            return false;
        }
    }

    /**
     * Reset configuration to defaults
     */
    public void resetToDefaults() {
        config = new FakePlayerConfig();
        saveConfig();
        plugin.getLogger().info("Reset fake player configuration to defaults");
    }

    /**
     * Validate configuration values
     */
    public boolean validateConfig() {
        boolean valid = true;

        // Validate health values
        if (config.getDefaultHealth() <= 0 || config.getDefaultHealth() > config.getMaxHealth()) {
            plugin.getLogger().warning("Invalid defaultHealth value: " + config.getDefaultHealth());
            valid = false;
        }

        if (config.getMaxHealth() <= 0) {
            plugin.getLogger().warning("Invalid maxHealth value: " + config.getMaxHealth());
            valid = false;
        }

        // Validate speed values
        if (config.getMovementSpeed() <= 0 || config.getMovementSpeed() > 5.0) {
            plugin.getLogger().warning("Invalid movementSpeed value: " + config.getMovementSpeed());
            valid = false;
        }

        if (config.getFleeSpeed() <= 0 || config.getFleeSpeed() > 10.0) {
            plugin.getLogger().warning("Invalid fleeSpeed value: " + config.getFleeSpeed());
            valid = false;
        }

        // Validate range values
        if (config.getDetectionRange() <= 0 || config.getDetectionRange() > 100) {
            plugin.getLogger().warning("Invalid detectionRange value: " + config.getDetectionRange());
            valid = false;
        }

        if (config.getAttackRange() <= 0 || config.getAttackRange() > 10) {
            plugin.getLogger().warning("Invalid attackRange value: " + config.getAttackRange());
            valid = false;
        }

        // Validate performance values
        if (config.getMaxFakePlayers() <= 0 || config.getMaxFakePlayers() > 1000) {
            plugin.getLogger().warning("Invalid maxFakePlayers value: " + config.getMaxFakePlayers());
            valid = false;
        }

        if (config.getTickInterval() < 1 || config.getTickInterval() > 20) {
            plugin.getLogger().warning("Invalid tickInterval value: " + config.getTickInterval());
            valid = false;
        }

        return valid;
    }

    /**
     * Get the configuration file
     */
    public File getConfigFile() {
        return configFile;
    }

    /**
     * Check if configuration file exists
     */
    public boolean configFileExists() {
        return configFile.exists();
    }

    /**
     * Get configuration file size in bytes
     */
    public long getConfigFileSize() {
        return configFile.exists() ? configFile.length() : 0;
    }

    /**
     * Get last modified time of configuration file
     */
    public long getConfigLastModified() {
        return configFile.exists() ? configFile.lastModified() : 0;
    }

    /**
     * Get a summary of current configuration
     */
    public String getConfigSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("FakePlayer Configuration Summary:\n");
        summary.append("================================\n");
        summary.append(String.format("Health: %.1f/%.1f\n", config.getDefaultHealth(), config.getMaxHealth()));
        summary.append(String.format("Speeds: Move=%.1f, Flee=%.1f, Combat=%.1f\n",
            config.getMovementSpeed(), config.getFleeSpeed(), config.getCombatSpeed()));
        summary.append(String.format("Ranges: Detection=%.1f, Attack=%.1f, Flee=%.1f\n",
            config.getDetectionRange(), config.getAttackRange(), config.getFleeRange()));
        summary.append(String.format("Performance: MaxPlayers=%d, TickInterval=%d\n",
            config.getMaxFakePlayers(), config.getTickInterval()));
        summary.append(String.format("Features: Persistence=%s, VisualNPC=%s, AutoSpawn=%s\n",
            config.isEnablePersistence(), config.isEnableVisualNpc(), config.isEnableAutoSpawn()));
        summary.append(String.format("BehaviorTree: %s (Debug=%s)\n",
            config.getDefaultBehaviorTree(), config.isEnableBehaviorTreeDebug()));

        return summary.toString();
    }
}
