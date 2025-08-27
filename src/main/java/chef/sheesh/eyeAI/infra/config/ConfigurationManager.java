package chef.sheesh.eyeAI.infra.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public final class ConfigurationManager {
    private final JavaPlugin plugin;
    private final String configPath;
    private FileConfiguration config;

    public ConfigurationManager(JavaPlugin plugin, String configPath) {
        this.plugin = plugin;
        this.configPath = configPath;
        ensurePresent();
        this.config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), configPath));
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public boolean getBoolean(String path, boolean def) {
        return config.getBoolean(path, def);
    }

    public int getInt(String path, int def) {
        return config.getInt(path, def);
    }

    public double getDouble(String path, double def) {
        return config.getDouble(path, def);
    }

    public String getString(String path, String def) {
        return config.getString(path, def);
    }

    private void ensurePresent() {
        File dataDir = plugin.getDataFolder();
        File cfgFile = new File(dataDir, configPath);
        if (cfgFile.exists()) {
            return;
        }
        try {
            File parent = cfgFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            try (InputStream in = plugin.getResource(configPath)) {
                if (in != null) {
                    Files.copy(in, cfgFile.toPath());
                } else {
                    // Create empty file if no embedded resource was found
                    if (!cfgFile.exists()) {
                        cfgFile.createNewFile();
                    }
                }
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to create default config for " + configPath + ": " + e.getMessage());
        }
    }
}

