-- Initial database schema for EyeAI
-- This migration creates all the necessary tables for the application

-- Main data store table for generic key-value storage
CREATE TABLE data_store (
    data_key VARCHAR(255) PRIMARY KEY,
    data_value TEXT NOT NULL,
    data_type VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Player data table for comprehensive player statistics
CREATE TABLE player_data (
    player_id VARCHAR(36) PRIMARY KEY,
    player_name VARCHAR(16) NOT NULL,
    level INTEGER DEFAULT 1,
    experience BIGINT DEFAULT 0,
    kills INTEGER DEFAULT 0,
    deaths INTEGER DEFAULT 0,
    kdr DOUBLE PRECISION DEFAULT 0.0,
    assists INTEGER DEFAULT 0,
    wins INTEGER DEFAULT 0,
    losses INTEGER DEFAULT 0,
    balance DOUBLE PRECISION DEFAULT 0.0,
    tokens INTEGER DEFAULT 0,
    scoreboard_enabled BOOLEAN DEFAULT TRUE,
    preferred_language VARCHAR(5) DEFAULT 'en',
    first_join BIGINT NOT NULL,
    last_seen BIGINT NOT NULL,
    playtime BIGINT DEFAULT 0,
    afk_time BIGINT DEFAULT 0,
    average_session_length DOUBLE PRECISION DEFAULT 0.0,
    sessions_count INTEGER DEFAULT 0,
    last_ip_address VARCHAR(45),
    failed_login_attempts INTEGER DEFAULT 0,
    last_failed_login BIGINT DEFAULT 0
);

-- AI models table for storing trained AI models
CREATE TABLE ai_models (
    model_id VARCHAR(255) PRIMARY KEY,
    model_name VARCHAR(255) NOT NULL,
    model_type VARCHAR(50) NOT NULL,
    model_data TEXT NOT NULL,
    version VARCHAR(20) NOT NULL,
    accuracy DOUBLE PRECISION DEFAULT 0.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Training data table for AI model training
CREATE TABLE training_data (
    data_id VARCHAR(255) PRIMARY KEY,
    model_id VARCHAR(255),
    input_data TEXT NOT NULL,
    output_data TEXT,
    reward DOUBLE PRECISION DEFAULT 0.0,
    timestamp BIGINT NOT NULL,
    FOREIGN KEY (model_id) REFERENCES ai_models(model_id) ON DELETE SET NULL
);

-- AI decisions table for tracking AI decision-making
CREATE TABLE ai_decisions (
    decision_id VARCHAR(255) PRIMARY KEY,
    model_id VARCHAR(255) NOT NULL,
    input_state TEXT NOT NULL,
    action_taken TEXT NOT NULL,
    reward_received DOUBLE PRECISION DEFAULT 0.0,
    next_state TEXT,
    timestamp BIGINT NOT NULL,
    FOREIGN KEY (model_id) REFERENCES ai_models(model_id) ON DELETE CASCADE
);

-- Transaction records table for player economy tracking
CREATE TABLE transactions (
    transaction_id VARCHAR(255) PRIMARY KEY,
    player_id VARCHAR(36) NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    amount DOUBLE PRECISION NOT NULL,
    currency_type VARCHAR(10) DEFAULT 'COINS',
    description VARCHAR(255),
    timestamp BIGINT NOT NULL,
    FOREIGN KEY (player_id) REFERENCES player_data(player_id) ON DELETE CASCADE
);

-- Player settings table for customizable player preferences
CREATE TABLE player_settings (
    player_id VARCHAR(36) PRIMARY KEY,
    custom_settings TEXT,
    currency_balances TEXT,
    command_usage TEXT,
    feature_usage_time TEXT,
    security_flags TEXT,
    FOREIGN KEY (player_id) REFERENCES player_data(player_id) ON DELETE CASCADE
);

-- Player social features table
CREATE TABLE player_social (
    player_id VARCHAR(36) PRIMARY KEY,
    friends TEXT,
    blocked_players TEXT,
    status_message VARCHAR(255),
    online_status BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (player_id) REFERENCES player_data(player_id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_player_data_name ON player_data(player_name);
CREATE INDEX idx_player_data_last_seen ON player_data(last_seen);
CREATE INDEX idx_ai_models_type ON ai_models(model_type);
CREATE INDEX idx_training_data_model ON training_data(model_id);
CREATE INDEX idx_transactions_player ON transactions(player_id);
CREATE INDEX idx_data_store_type ON data_store(data_type);
CREATE INDEX idx_data_store_created ON data_store(created_at);

-- Create updated_at trigger function for H2
CREATE TRIGGER update_data_store_updated_at
    BEFORE UPDATE ON data_store
    FOR EACH ROW
    CALL "org.h2.tools.TriggerAdapter";

-- Create updated_at trigger function for ai_models
CREATE TRIGGER update_ai_models_updated_at
    BEFORE UPDATE ON ai_models
    FOR EACH ROW
    CALL "org.h2.tools.TriggerAdapter";
