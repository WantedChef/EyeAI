package chef.sheesh.eyeAI.ai.fakeplayer.persistence;

import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FileBasedFakePlayerPersistence implements IFakePlayerPersistence {
    
    private final JavaPlugin plugin;
    private final Gson gson;
    private final File dataFolder;
    private final Map<UUID, FakePlayer> cachedPlayers = new ConcurrentHashMap<>();
    private boolean enabled = false;
    
    public FileBasedFakePlayerPersistence(JavaPlugin plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder()
            .registerTypeAdapter(Location.class, new LocationSerializer())
            .setPrettyPrinting()
            .create();
        this.dataFolder = new File(plugin.getDataFolder(), "fakeplayers");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }
    
    @Override
    public void save(FakePlayer fakePlayer) {
        if (!enabled) {
            return;
        }
        
        File file = new File(dataFolder, fakePlayer.getId().toString() + ".json");
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(fakePlayer, writer);
            cachedPlayers.put(fakePlayer.getId(), fakePlayer);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save fake player: " + fakePlayer.getId());
            e.printStackTrace();
        }
    }
    
    @Override
    public Optional<FakePlayer> load(UUID id) {
        if (!enabled) {
            return Optional.empty();
        }
        
        if (cachedPlayers.containsKey(id)) {
            return Optional.of(cachedPlayers.get(id));
        }
        
        File file = new File(dataFolder, id.toString() + ".json");
        if (!file.exists()) {
            return Optional.empty();
        }
        
        try (Reader reader = new FileReader(file)) {
            FakePlayer player = gson.fromJson(reader, FakePlayer.class);
            cachedPlayers.put(id, player);
            return Optional.of(player);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load fake player: " + id);
            e.printStackTrace();
            return Optional.empty();
        }
    }
    
    @Override
    public List<FakePlayer> loadAll() {
        if (!enabled) {
            return new ArrayList<>();
        }
        
        cachedPlayers.clear();
        List<FakePlayer> players = new ArrayList<>();
        
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                try (Reader reader = new FileReader(file)) {
                    FakePlayer player = gson.fromJson(reader, FakePlayer.class);
                    players.add(player);
                    cachedPlayers.put(player.getId(), player);
                } catch (IOException e) {
                    plugin.getLogger().severe("Failed to load fake player from file: " + file.getName());
                    e.printStackTrace();
                }
            }
        }
        
        return players;
    }
    
    @Override
    public void delete(UUID id) {
        if (!enabled) {
            return;
        }
        
        File file = new File(dataFolder, id.toString() + ".json");
        if (file.exists()) {
            file.delete();
        }
        cachedPlayers.remove(id);
    }
    
    @Override
    public boolean exists(UUID id) {
        if (!enabled) {
            return false;
        }
        
        return cachedPlayers.containsKey(id) || 
               new File(dataFolder, id.toString() + ".json").exists();
    }
    
    @Override
    public int count() {
        if (!enabled) {
            return 0;
        }
        
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".json"));
        return files != null ? files.length : 0;
    }
    
    @Override
    public void saveAll(List<FakePlayer> fakePlayers) {
        if (!enabled) {
            return;
        }
        
        for (FakePlayer player : fakePlayers) {
            save(player);
        }
    }
    
    @Override
    public void deleteAll() {
        if (!enabled) {
            return;
        }
        
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
        cachedPlayers.clear();
    }
    
    @Override
    public List<FakePlayer> findByName(String namePattern) {
        if (!enabled) {
            return new ArrayList<>();
        }
        
        String lowerPattern = namePattern.toLowerCase();
        return loadAll().stream()
            .filter(player -> player.getName().toLowerCase().contains(lowerPattern))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<FakePlayer> findByWorld(UUID worldId) {
        if (!enabled) {
            return new ArrayList<>();
        }
        
        return loadAll().stream()
            .filter(player -> {
                Location loc = player.getLocation();
                return loc != null && loc.getWorld() != null && 
                       loc.getWorld().getUID().equals(worldId);
            })
            .collect(Collectors.toList());
    }
    
    @Override
    public Optional<FakePlayerStatistics> getStatistics(UUID id) {
        // Statistics are stored in memory, not persisted separately
        return Optional.empty();
    }
    
    @Override
    public void updateStatistics(UUID id, FakePlayerStatistics stats) {
        // Statistics are managed in memory by FakePlayerManager
        // This method is a placeholder for future persistence implementation
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            loadAll(); // Preload all players when enabled
        }
    }
    
    private static class LocationSerializer implements com.google.gson.JsonSerializer<Location>, 
                                                   com.google.gson.JsonDeserializer<Location> {
        
        @Override
        public com.google.gson.JsonElement serialize(Location location, Type typeOfSrc, 
                                   com.google.gson.JsonSerializationContext context) {
            com.google.gson.JsonObject obj = new com.google.gson.JsonObject();
            obj.addProperty("world", location.getWorld().getName());
            obj.addProperty("x", location.getX());
            obj.addProperty("y", location.getY());
            obj.addProperty("z", location.getZ());
            obj.addProperty("yaw", location.getYaw());
            obj.addProperty("pitch", location.getPitch());
            return obj;
        }
        
        @Override
        public Location deserialize(com.google.gson.JsonElement json, Type typeOfT, 
                                  com.google.gson.JsonDeserializationContext context) {
            com.google.gson.JsonObject obj = json.getAsJsonObject();
            World world = Bukkit.getWorld(obj.get("world").getAsString());
            if (world == null) {
                world = Bukkit.getWorlds().get(0); // Fallback to first world
            }
            return new Location(
                world,
                obj.get("x").getAsDouble(),
                obj.get("y").getAsDouble(),
                obj.get("z").getAsDouble(),
                obj.get("yaw").getAsFloat(),
                obj.get("pitch").getAsFloat()
            );
        }
    }
}
