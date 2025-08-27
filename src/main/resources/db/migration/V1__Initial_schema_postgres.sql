-- PostgreSQL-specific schema additions
-- This file contains PostgreSQL-specific features and optimizations

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create updated_at trigger function for PostgreSQL
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers for automatic timestamp updates
CREATE TRIGGER update_data_store_updated_at
    BEFORE UPDATE ON data_store
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_ai_models_updated_at
    BEFORE UPDATE ON ai_models
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Create additional PostgreSQL-specific indexes
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_player_data_kdr ON player_data(kdr);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_player_data_balance ON player_data(balance);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_transactions_timestamp ON transactions(timestamp);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_ai_decisions_timestamp ON ai_decisions(timestamp);

-- Create partial indexes for better performance
CREATE INDEX idx_active_players ON player_data(last_seen) WHERE last_seen > (EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - INTERVAL '7 days')) * 1000);
CREATE INDEX idx_online_players ON player_social(player_id) WHERE online_status = true;

-- Create views for common queries
CREATE VIEW player_stats AS
SELECT
    pd.player_id,
    pd.player_name,
    pd.level,
    pd.kills,
    pd.deaths,
    pd.kdr,
    pd.wins,
    pd.losses,
    pd.balance,
    pd.tokens,
    pd.playtime,
    pd.last_seen,
    CASE WHEN pd.wins + pd.losses > 0 THEN ROUND((pd.wins::float / (pd.wins + pd.losses)) * 100, 2) ELSE 0 END as win_rate
FROM player_data pd;

CREATE VIEW ai_model_performance AS
SELECT
    am.model_id,
    am.model_name,
    am.model_type,
    am.accuracy,
    COUNT(td.data_id) as training_samples,
    AVG(td.reward) as avg_reward,
    MAX(td.timestamp) as last_trained
FROM ai_models am
LEFT JOIN training_data td ON am.model_id = td.model_id
GROUP BY am.model_id, am.model_name, am.model_type, am.accuracy;

-- Grant permissions (adjust as needed for your setup)
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO eyeai_user;
-- GRANT USAGE ON ALL SEQUENCES IN SCHEMA public TO eyeai_user;
