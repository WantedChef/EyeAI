package chef.sheesh.eyeAI.infra.config;

public final class ConfigKeys {
    private ConfigKeys() {}

    // Storage configuration keys
    public static final String STORAGE_PROVIDER = "storage.provider";
    public static final String STORAGE_H2_FILE = "storage.h2.file";
    public static final String STORAGE_POSTGRES_HOST = "storage.postgres.host";
    public static final String STORAGE_POSTGRES_PORT = "storage.postgres.port";
    public static final String STORAGE_POSTGRES_DATABASE = "storage.postgres.database";
    public static final String STORAGE_POSTGRES_USER = "storage.postgres.user";
    public static final String STORAGE_POSTGRES_PASSWORD = "storage.postgres.password";

    // MySQL Configuration
    public static final String MYSQL_HOST = "mysql.host";
    public static final String MYSQL_PORT = "mysql.port";
    public static final String MYSQL_DATABASE = "mysql.database";
    public static final String MYSQL_USERNAME = "mysql.username";
    public static final String MYSQL_PASSWORD = "mysql.password";
    public static final String MYSQL_USE_SSL = "mysql.useSSL";
    public static final String MYSQL_CONNECTION_TIMEOUT = "mysql.connectionTimeout";
    public static final String MYSQL_MAX_POOL_SIZE = "mysql.maxPoolSize";
    public static final String MYSQL_MIN_IDLE = "mysql.minIdle";

    // Redis Configuration
    public static final String REDIS_HOST = "redis.host";
    public static final String REDIS_PORT = "redis.port";
    public static final String REDIS_PASSWORD = "redis.password";
    public static final String REDIS_DATABASE = "redis.database";
    public static final String REDIS_TIMEOUT = "redis.timeout";
    public static final String REDIS_MAX_CONNECTIONS = "redis.maxConnections";
    public static final String REDIS_MIN_IDLE = "redis.minIdle";

    // Caching Configuration
    public static final String CACHE_L1_MAX_SIZE = "cache.l1.maxSize";
    public static final String CACHE_L1_EXPIRE_MINUTES = "cache.l1.expireMinutes";
    public static final String CACHE_L2_EXPIRE_MINUTES = "cache.l2.expireMinutes";
    public static final String CACHE_ENABLED = "cache.enabled";
    public static final String CACHE_METRICS_ENABLED = "cache.metrics.enabled";

    // AI Storage Configuration
    public static final String AI_MODEL_STORAGE_PATH = "ai.model.storagePath";
    public static final String AI_MODEL_CACHE_SIZE = "ai.model.cacheSize";
    public static final String AI_TRAINING_COMPRESSION_ENABLED = "ai.training.compressionEnabled";
    public static final String AI_DECISION_RETENTION_DAYS = "ai.decision.retentionDays";

    // Security Configuration
    public static final String SECURITY_ENCRYPTION_KEY = "security.encryption.key";
    public static final String SECURITY_BACKUP_ENCRYPTION_KEY = "security.backup.encryption.key";

    // Backup Configuration
    public static final String BACKUP_ENABLED = "backup.enabled";
    public static final String BACKUP_MYSQL_SCHEDULE = "backup.mysql.schedule";
    public static final String BACKUP_REDIS_SCHEDULE = "backup.redis.schedule";
    public static final String BACKUP_AI_SCHEDULE = "backup.ai.schedule";
    public static final String BACKUP_RETENTION_DAYS = "backup.retention.days";
    public static final String BACKUP_LOCATION = "backup.location";

    // Training configuration keys
    public static final String TRAINING_ENABLED = "training.enabled";
    public static final String TRAINING_FAKE_PLAYERS = "training.fakePlayers";
    public static final String TRAINING_BATCH_SIZE = "training.batchSize";
    public static final String TRAINING_EPSILON_START = "training.epsilon.start";
    public static final String TRAINING_EPSILON_MIN = "training.epsilon.min";
    public static final String TRAINING_EPSILON_ADAPTIVE = "training.epsilon.adaptive";
    public static final String TRAINING_EPSILON_DECAY = "training.epsilon.decay";
    public static final String TRAINING_SAFETY_MIN_TPS = "training.safety.minTPS";

    // UI configuration keys
    public static final String UI_ENABLE_DASHBOARD = "ui.enableDashboard";

    // Monitoring Configuration
    public static final String MONITORING_ENABLED = "monitoring.enabled";
    public static final String MONITORING_METRICS_ENABLED = "monitoring.metrics.enabled";
    public static final String MONITORING_HEALTH_CHECK_INTERVAL = "monitoring.healthCheck.interval";
}
