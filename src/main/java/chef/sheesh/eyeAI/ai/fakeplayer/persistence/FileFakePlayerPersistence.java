package chef.sheesh.eyeAI.ai.fakeplayer.persistence;

import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * File-based implementation of FakePlayerPersistence using JSON files
 */
public class FileFakePlayerPersistence implements FakePlayerPersistence {

    private final File dataFolder;
    private final Gson gson;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public FileFakePlayerPersistence(File dataFolder) {
        this.dataFolder = new File(dataFolder, "fake_players");
        if (!this.dataFolder.exists()) {
            this.dataFolder.mkdirs();
        }

        this.gson = new GsonBuilder()
            .registerTypeAdapter(UUID.class, new UUIDAdapter())
            .registerTypeAdapter(Location.class, new LocationAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .setPrettyPrinting()
            .create();
    }

    @Override
    public void save(FakePlayer fakePlayer) {
        File playerFile = new File(dataFolder, fakePlayer.getId().toString() + ".json");

        try (FileWriter writer = new FileWriter(playerFile)) {
            FakePlayerData data = new FakePlayerData(fakePlayer);
            gson.toJson(data, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save fake player to file", e);
        }
    }

    @Override
    public Optional<FakePlayer> load(UUID id) {
        File playerFile = new File(dataFolder, id.toString() + ".json");

        if (!playerFile.exists()) {
            return Optional.empty();
        }

        try (FileReader reader = new FileReader(playerFile)) {
            @SuppressWarnings("unused") // TODO: Implement proper FakePlayer reconstruction
            FakePlayerData data = gson.fromJson(reader, FakePlayerData.class);
            // Note: This would need a proper factory method in FakePlayerManager
            // to reconstruct the FakePlayer object properly
            return Optional.empty(); // Placeholder
        } catch (Exception e) {
            throw new RuntimeException("Failed to load fake player from file", e);
        }
    }

    @Override
    public List<FakePlayer> loadAll() {
        List<FakePlayer> fakePlayers = new ArrayList<>();
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".json"));

        if (files != null) {
            for (File file : files) {
                try (FileReader reader = new FileReader(file)) {
                    @SuppressWarnings("unused") // TODO: Implement proper FakePlayer reconstruction
                    FakePlayerData data = gson.fromJson(reader, FakePlayerData.class);
                    // Again, this would need proper reconstruction
                    // fakePlayers.add(reconstructFakePlayer(data));
                } catch (Exception e) {
                    // Log error but continue loading other players
                    System.err.println("Failed to load fake player from " + file.getName() + ": " + e.getMessage());
                }
            }
        }

        return fakePlayers;
    }

    @Override
    public void delete(UUID id) {
        File playerFile = new File(dataFolder, id.toString() + ".json");
        if (playerFile.exists()) {
            playerFile.delete();
        }

        // Also delete statistics file
        File statsFile = new File(dataFolder, id.toString() + "_stats.json");
        if (statsFile.exists()) {
            statsFile.delete();
        }
    }

    @Override
    public boolean exists(UUID id) {
        File playerFile = new File(dataFolder, id.toString() + ".json");
        return playerFile.exists();
    }

    @Override
    public int count() {
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".json") && !name.contains("_stats"));
        return files != null ? files.length : 0;
    }

    @Override
    public void saveAll(List<FakePlayer> fakePlayers) {
        for (FakePlayer fakePlayer : fakePlayers) {
            save(fakePlayer);
        }
    }

    @Override
    public void deleteAll() {
        File[] files = dataFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    file.delete();
                }
            }
        }
    }

    @Override
    public List<FakePlayer> findByName(String namePattern) {
        List<FakePlayer> matchingPlayers = new ArrayList<>();
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".json") && !name.contains("_stats"));

        if (files != null) {
            for (File file : files) {
                try (FileReader reader = new FileReader(file)) {
                    FakePlayerData data = gson.fromJson(reader, FakePlayerData.class);
                    if (data.getName().toLowerCase().contains(namePattern.toLowerCase())) {
                        // matchingPlayers.add(reconstructFakePlayer(data));
                    }
                } catch (Exception e) {
                    // Log error but continue
                }
            }
        }

        return matchingPlayers;
    }

    @Override
    public List<FakePlayer> findByWorld(String worldName) {
        List<FakePlayer> worldPlayers = new ArrayList<>();
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".json") && !name.contains("_stats"));

        if (files != null) {
            for (File file : files) {
                try (FileReader reader = new FileReader(file)) {
                    FakePlayerData data = gson.fromJson(reader, FakePlayerData.class);
                    if (worldName.equals(data.getWorld())) {
                        // worldPlayers.add(reconstructFakePlayer(data));
                    }
                } catch (Exception e) {
                    // Log error but continue
                }
            }
        }

        return worldPlayers;
    }

    @Override
    public void updateStatistics(UUID id, FakePlayerStatistics stats) {
        File statsFile = new File(dataFolder, id.toString() + "_stats.json");

        try (FileWriter writer = new FileWriter(statsFile)) {
            gson.toJson(stats, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save fake player statistics to file", e);
        }
    }

    @Override
    public Optional<FakePlayerStatistics> getStatistics(UUID id) {
        File statsFile = new File(dataFolder, id.toString() + "_stats.json");

        if (!statsFile.exists()) {
            return Optional.empty();
        }

        try (FileReader reader = new FileReader(statsFile)) {
            FakePlayerStatistics stats = gson.fromJson(reader, FakePlayerStatistics.class);
            return Optional.of(stats);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load fake player statistics from file", e);
        }
    }

    /**
     * Data transfer object for FakePlayer serialization
     */
    @SuppressWarnings({"unused", "UnusedAssignment"}) // Methods used by JSON serialization
    private static class FakePlayerData {
        private UUID id;
        private String name;
        private String world;
        private double x, y, z;
        private float yaw, pitch;
        private double health;
        private String state;
        private String behaviorTree;
        private long createdAt;
        private long updatedAt;

        public FakePlayerData() {}

        public FakePlayerData(FakePlayer fakePlayer) {
            this.id = fakePlayer.getId();
            this.name = fakePlayer.getName();
            Location loc = fakePlayer.getLocation();
            this.world = loc.getWorld().getName();
            this.x = loc.getX();
            this.y = loc.getY();
            this.z = loc.getZ();
            this.yaw = loc.getYaw();
            this.pitch = loc.getPitch();
            this.health = fakePlayer.getHealth();
            this.state = fakePlayer.getState().name();
            this.behaviorTree = fakePlayer.getBehaviorTree() != null ?
                fakePlayer.getBehaviorTree().getDescription() : "none";
            this.createdAt = fakePlayer.getLastActionTime();
            this.updatedAt = System.currentTimeMillis();
        }

        // Getters and setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getWorld() { return world; }
        public void setWorld(String world) { this.world = world; }

        public double getX() { return x; }
        public void setX(double x) { this.x = x; }

        public double getY() { return y; }
        public void setY(double y) { this.y = y; }

        public double getZ() { return z; }
        public void setZ(double z) { this.z = z; }

        public float getYaw() { return yaw; }
        public void setYaw(float yaw) { this.yaw = yaw; }

        public float getPitch() { return pitch; }
        public void setPitch(float pitch) { this.pitch = pitch; }

        public double getHealth() { return health; }
        public void setHealth(double health) { this.health = health; }

        public String getState() { return state; }
        public void setState(String state) { this.state = state; }

        public String getBehaviorTree() { return behaviorTree; }
        public void setBehaviorTree(String behaviorTree) { this.behaviorTree = behaviorTree; }

        public long getCreatedAt() { return createdAt; }
        public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

        public long getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    }

    /**
     * Custom adapter for UUID serialization
     */
    private static class UUIDAdapter implements JsonSerializer<UUID>, JsonDeserializer<UUID> {
        @Override
        public JsonElement serialize(UUID src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }

        @Override
        public UUID deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return UUID.fromString(json.getAsString());
        }
    }

    /**
     * Custom adapter for Location serialization
     */
    private static class LocationAdapter implements JsonSerializer<Location>, JsonDeserializer<Location> {
        @Override
        public JsonElement serialize(Location src, Type typeOfSrc, JsonSerializationContext context) {
            Map<String, Object> locationData = new HashMap<>();
            locationData.put("world", src.getWorld().getName());
            locationData.put("x", src.getX());
            locationData.put("y", src.getY());
            locationData.put("z", src.getZ());
            locationData.put("yaw", src.getYaw());
            locationData.put("pitch", src.getPitch());
            return context.serialize(locationData);
        }

        @Override
        public Location deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Map<String, Object> locationData = context.deserialize(json, Map.class);
            return new Location(
                Bukkit.getWorld((String) locationData.get("world")),
                ((Number) locationData.get("x")).doubleValue(),
                ((Number) locationData.get("y")).doubleValue(),
                ((Number) locationData.get("z")).doubleValue(),
                ((Number) locationData.get("yaw")).floatValue(),
                ((Number) locationData.get("pitch")).floatValue()
            );
        }
    }

    /**
     * Custom adapter for LocalDateTime serialization
     */
    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.format(DATE_FORMATTER));
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return LocalDateTime.parse(json.getAsString(), DATE_FORMATTER);
        }
    }
}
