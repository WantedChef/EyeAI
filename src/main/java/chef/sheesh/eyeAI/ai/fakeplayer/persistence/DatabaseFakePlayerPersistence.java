package chef.sheesh.eyeAI.ai.fakeplayer.persistence;

import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayer;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerManager;
import chef.sheesh.eyeAI.ai.fakeplayer.FakePlayerState;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Database implementation of FakePlayerPersistence using SQLite
 */
public class DatabaseFakePlayerPersistence implements FakePlayerPersistence {

    private final Connection connection;
    private final FakePlayerManager fakePlayerManager;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public DatabaseFakePlayerPersistence(Connection connection, FakePlayerManager fakePlayerManager) {
        this.connection = connection;
        this.fakePlayerManager = fakePlayerManager;
        initializeTables();
    }

    private void initializeTables() {
        try (Statement stmt = connection.createStatement()) {
            // Create fake_players table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS fake_players (
                    id VARCHAR(36) PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    world VARCHAR(255) NOT NULL,
                    x DOUBLE NOT NULL,
                    y DOUBLE NOT NULL,
                    z DOUBLE NOT NULL,
                    yaw FLOAT NOT NULL,
                    pitch FLOAT NOT NULL,
                    health DOUBLE NOT NULL,
                    state VARCHAR(20) NOT NULL,
                    behavior_tree TEXT,
                    created_at BIGINT NOT NULL,
                    updated_at BIGINT NOT NULL
                )
                """);

            // Create fake_player_statistics table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS fake_player_statistics (
                    player_id VARCHAR(36) PRIMARY KEY,
                    total_ticks BIGINT NOT NULL DEFAULT 0,
                    distance_traveled BIGINT NOT NULL DEFAULT 0,
                    entities_attacked INTEGER NOT NULL DEFAULT 0,
                    damage_dealt INTEGER NOT NULL DEFAULT 0,
                    damage_taken INTEGER NOT NULL DEFAULT 0,
                    deaths INTEGER NOT NULL DEFAULT 0,
                    creation_time BIGINT NOT NULL,
                    last_active_time VARCHAR(20) NOT NULL,
                    total_play_time BIGINT NOT NULL DEFAULT 0,
                    interactions_with_players INTEGER NOT NULL DEFAULT 0,
                    behavior_tree_type VARCHAR(255) NOT NULL DEFAULT 'default',
                    pathfinding_attempts INTEGER NOT NULL DEFAULT 0,
                    pathfinding_successes INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY (player_id) REFERENCES fake_players(id) ON DELETE CASCADE
                )
                """);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database tables", e);
        }
    }

    @Override
    public void save(FakePlayer fakePlayer) {
        String sql = """
            INSERT OR REPLACE INTO fake_players
            (id, name, world, x, y, z, yaw, pitch, health, state, behavior_tree, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            Location loc = fakePlayer.getLocation();

            stmt.setString(1, fakePlayer.getId().toString());
            stmt.setString(2, fakePlayer.getName());
            stmt.setString(3, loc.getWorld().getName());
            stmt.setDouble(4, loc.getX());
            stmt.setDouble(5, loc.getY());
            stmt.setDouble(6, loc.getZ());
            stmt.setFloat(7, loc.getYaw());
            stmt.setFloat(8, loc.getPitch());
            stmt.setDouble(9, fakePlayer.getHealth());
            stmt.setString(10, fakePlayer.getState().name());
            stmt.setString(11, fakePlayer.getBehaviorTree() != null ?
                fakePlayer.getBehaviorTree().getDescription() : "none");
            stmt.setLong(12, fakePlayer.getLastActionTime()); // Reuse as creation time
            stmt.setLong(13, System.currentTimeMillis());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save fake player", e);
        }
    }

    @Override
    public Optional<FakePlayer> load(UUID id) {
        String sql = "SELECT * FROM fake_players WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    FakePlayerData data = createFakePlayerDataFromResultSet(rs);
                    return Optional.of(createFakePlayerFromData(data));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load fake player", e);
        }

        return Optional.empty();
    }

    @Override
    public List<FakePlayer> loadAll() {
        List<FakePlayer> fakePlayers = new ArrayList<>();
        String sql = "SELECT * FROM fake_players ORDER BY name";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                FakePlayerData data = createFakePlayerDataFromResultSet(rs);
                fakePlayers.add(createFakePlayerFromData(data));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load all fake players", e);
        }

        return fakePlayers;
    }

    @Override
    public void delete(UUID id) {
        String sql = "DELETE FROM fake_players WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete fake player", e);
        }
    }

    @Override
    public boolean exists(UUID id) {
        String sql = "SELECT COUNT(*) FROM fake_players WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check if fake player exists", e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM fake_players";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            return rs.next() ? rs.getInt(1) : 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to count fake players", e);
        }
    }

    @Override
    public void saveAll(List<FakePlayer> fakePlayers) {
        try {
            connection.setAutoCommit(false);

            for (FakePlayer fakePlayer : fakePlayers) {
                save(fakePlayer);
            }

            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                // Log rollback error
            }
            throw new RuntimeException("Failed to save all fake players", e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                // Log error
            }
        }
    }

    @Override
    public void deleteAll() {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DELETE FROM fake_players");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete all fake players", e);
        }
    }

    @Override
    public List<FakePlayer> findByName(String namePattern) {
        List<FakePlayer> matchingPlayers = new ArrayList<>();
        String sql = "SELECT * FROM fake_players WHERE name LIKE ? ORDER BY name";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + namePattern + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    FakePlayerData data = createFakePlayerDataFromResultSet(rs);
                    matchingPlayers.add(createFakePlayerFromData(data));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find fake players by name", e);
        }

        return matchingPlayers;
    }

    @Override
    public List<FakePlayer> findByWorld(String worldName) {
        List<FakePlayer> worldPlayers = new ArrayList<>();
        String sql = "SELECT * FROM fake_players WHERE world = ? ORDER BY name";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, worldName);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    FakePlayerData data = createFakePlayerDataFromResultSet(rs);
                    worldPlayers.add(createFakePlayerFromData(data));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find fake players by world", e);
        }

        return worldPlayers;
    }

    @Override
    public void updateStatistics(UUID id, FakePlayerStatistics stats) {
        String sql = """
            INSERT OR REPLACE INTO fake_player_statistics
            (player_id, total_ticks, distance_traveled, entities_attacked, damage_dealt,
             damage_taken, deaths, creation_time, last_active_time, total_play_time,
             interactions_with_players, behavior_tree_type, pathfinding_attempts, pathfinding_successes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id.toString());
            stmt.setLong(2, stats.getTotalTicks());
            stmt.setLong(3, stats.getDistanceTraveled());
            stmt.setInt(4, stats.getEntitiesAttacked());
            stmt.setInt(5, stats.getDamageDealt());
            stmt.setInt(6, stats.getDamageTaken());
            stmt.setInt(7, stats.getDeaths());
            stmt.setLong(8, stats.getCreationTime());
            stmt.setString(9, stats.getLastActiveTime().format(DATE_FORMATTER));
            stmt.setLong(10, stats.getTotalPlayTime());
            stmt.setInt(11, stats.getInteractionsWithPlayers());
            stmt.setString(12, stats.getBehaviorTreeType());
            stmt.setInt(13, stats.getPathfindingAttempts());
            stmt.setInt(14, stats.getPathfindingSuccesses());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update fake player statistics", e);
        }
    }

    @Override
    public Optional<FakePlayerStatistics> getStatistics(UUID id) {
        String sql = "SELECT * FROM fake_player_statistics WHERE player_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(createStatisticsFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get fake player statistics", e);
        }

        return Optional.empty();
    }

    /**
     * Create FakePlayerData from database ResultSet
     */
    private FakePlayerData createFakePlayerDataFromResultSet(ResultSet rs) throws SQLException {
        FakePlayerData data = new FakePlayerData();
        data.setId(UUID.fromString(rs.getString("id")));
        data.setName(rs.getString("name"));
        data.setWorld(rs.getString("world"));
        data.setX(rs.getDouble("x"));
        data.setY(rs.getDouble("y"));
        data.setZ(rs.getDouble("z"));
        data.setYaw(rs.getFloat("yaw"));
        data.setPitch(rs.getFloat("pitch"));
        data.setHealth(rs.getDouble("health"));
        data.setState(rs.getString("state"));
        data.setBehaviorTree(rs.getString("behavior_tree"));
        data.setCreatedAt(rs.getLong("created_at"));
        data.setUpdatedAt(rs.getLong("updated_at"));
        return data;
    }

    /**
     * Create FakePlayer from FakePlayerData
     */
    private FakePlayer createFakePlayerFromData(FakePlayerData data) {
        Location location = new Location(
            Bukkit.getWorld(data.getWorld()),
            data.getX(),
            data.getY(),
            data.getZ(),
            data.getYaw(),
            data.getPitch()
        );

        FakePlayer fakePlayer = new FakePlayer(data.getId(), data.getName(), location, fakePlayerManager);
        fakePlayer.setHealth(data.getHealth());
        fakePlayer.setState(FakePlayerState.valueOf(data.getState()));

        return fakePlayer;
    }

    /**
     * Data transfer object for FakePlayer database storage
     */
    @SuppressWarnings("unused") // Methods used by database serialization
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

    private FakePlayerStatistics createStatisticsFromResultSet(ResultSet rs) throws SQLException {
        FakePlayerStatistics stats = new FakePlayerStatistics(rs.getLong("creation_time"));
        stats.setTotalTicks(rs.getLong("total_ticks"));
        stats.setDistanceTraveled(rs.getLong("distance_traveled"));
        stats.setEntitiesAttacked(rs.getInt("entities_attacked"));
        stats.setDamageDealt(rs.getInt("damage_dealt"));
        stats.setDamageTaken(rs.getInt("damage_taken"));
        stats.setDeaths(rs.getInt("deaths"));
        stats.setLastActiveTime(LocalDateTime.parse(rs.getString("last_active_time"), DATE_FORMATTER));
        stats.setTotalPlayTime(rs.getLong("total_play_time"));
        stats.setInteractionsWithPlayers(rs.getInt("interactions_with_players"));
        stats.setBehaviorTreeType(rs.getString("behavior_tree_type"));
        stats.setPathfindingAttempts(rs.getInt("pathfinding_attempts"));
        stats.setPathfindingSuccesses(rs.getInt("pathfinding_successes"));

        return stats;
    }
}
