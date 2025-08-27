package chef.sheesh.eyeAI.monitoring;

import chef.sheesh.eyeAI.monitoring.UltimateAIMonitor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.java.JavaPlugin;
import spark.Spark;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static spark.Spark.*;

/**
 * Web Dashboard for AI Monitoring System - Accessible on localhost
 */
@Slf4j
public class WebDashboard {

    private final UltimateAIMonitor monitor;
    private final JavaPlugin plugin;
    private final int port;
    private final AtomicLong lastUpdateTime = new AtomicLong(System.currentTimeMillis());

    // Real-time data storage
    private volatile AISystemStatus latestAIStatus;
    private volatile DatabaseStatus latestDBStatus;
    private volatile PerformanceMetrics latestPerformance;
    private volatile SystemResources latestResources;

    public WebDashboard(UltimateAIMonitor monitor, JavaPlugin plugin) {
        this.monitor = monitor;
        this.plugin = plugin;
        this.port = 8080; // Default port, could be configurable

        initializeWebServer();
        log.info("🌐 Web Dashboard initialized on localhost:{}", port);
    }

    /**
     * Initialize the web server with all endpoints
     */
    private void initializeWebServer() {
        // Configure Spark
        port(this.port);
        staticFiles.location("/public");
        webSocket("/ws", WebSocketHandler.class);

        // Main dashboard
        get("/", (req, res) -> {
            res.type("text/html");
            return generateMainDashboard();
        });

        // API endpoints for data
        get("/api/status", (req, res) -> {
            res.type("application/json");
            return generateStatusJson();
        });

        get("/api/ai-system", (req, res) -> {
            res.type("application/json");
            return latestAIStatus != null ? toJson(latestAIStatus) : "{}";
        });

        get("/api/database", (req, res) -> {
            res.type("application/json");
            return latestDBStatus != null ? toJson(latestDBStatus) : "{}";
        });

        get("/api/performance", (req, res) -> {
            res.type("application/json");
            return latestPerformance != null ? toJson(latestPerformance) : "{}";
        });

        get("/api/resources", (req, res) -> {
            res.type("application/json");
            return latestResources != null ? toJson(latestResources) : "{}";
        });

        // Detailed views
        get("/ai-details", (req, res) -> {
            res.type("text/html");
            return generateAIDetailsPage();
        });

        get("/database-viewer", (req, res) -> {
            res.type("text/html");
            return generateDatabaseViewerPage();
        });

        get("/performance", (req, res) -> {
            res.type("text/html");
            return generatePerformancePage();
        });

        get("/logs", (req, res) -> {
            res.type("text/html");
            return generateLogsPage();
        });

        log.info("🚀 Web Dashboard endpoints configured");
    }

    /**
     * Generate the main dashboard HTML
     */
    private String generateMainDashboard() {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>🚀 Ultimate AI Monitor - The Best Monitor Ever</title>
                <style>
                    """ + getCSS() + """
                </style>
            </head>
            <body>
                <div class="container">
                    <header>
                        <h1>🚀 Ultimate AI Monitor</h1>
                        <p>The Best Monitor Ever - Real-time AI System Monitoring</p>
                        <div class="status-indicator" id="overall-status">
                            <span class="status-dot" id="status-dot"></span>
                            <span id="status-text">Loading...</span>
                        </div>
                    </header>

                    <nav>
                        <a href="/">Dashboard</a>
                        <a href="/ai-details">AI System</a>
                        <a href="/database-viewer">Database</a>
                        <a href="/performance">Performance</a>
                        <a href="/logs">Logs</a>
                    </nav>

                    <div class="dashboard-grid">
                        <div class="card ai-status-card">
                            <h3>🤖 AI System Status</h3>
                            <div class="metric-grid">
                                <div class="metric">
                                    <span class="label">Training Active:</span>
                                    <span class="value" id="training-active">-</span>
                                </div>
                                <div class="metric">
                                    <span class="label">Learning Enabled:</span>
                                    <span class="value" id="learning-enabled">-</span>
                                </div>
                                <div class="metric">
                                    <span class="label">Training Progress:</span>
                                    <span class="value" id="training-progress">-</span>
                                </div>
                                <div class="metric">
                                    <span class="label">Current Step:</span>
                                    <span class="value" id="current-step">-</span>
                                </div>
                                <div class="metric">
                                    <span class="label">Experiences Processed:</span>
                                    <span class="value" id="experiences-processed">-</span>
                                </div>
                                <div class="metric">
                                    <span class="label">Buffer Size:</span>
                                    <span class="value" id="buffer-size">-</span>
                                </div>
                            </div>
                        </div>

                        <div class="card performance-card">
                            <h3>⚡ Performance Metrics</h3>
                            <div class="metric-grid">
                                <div class="metric">
                                    <span class="label">Memory Usage:</span>
                                    <span class="value" id="memory-usage">-</span>
                                </div>
                                <div class="metric">
                                    <span class="label">Training Batches/sec:</span>
                                    <span class="value" id="batches-per-sec">-</span>
                                </div>
                                <div class="metric">
                                    <span class="label">Avg Training Time:</span>
                                    <span class="value" id="avg-training-time">-</span>
                                </div>
                                <div class="metric">
                                    <span class="label">CPU Usage:</span>
                                    <span class="value" id="cpu-usage">-</span>
                                </div>
                            </div>
                        </div>

                        <div class="card database-card">
                            <h3>💾 Database Status</h3>
                            <div class="metric-grid">
                                <div class="metric">
                                    <span class="label">Models Stored:</span>
                                    <span class="value" id="models-count">-</span>
                                </div>
                                <div class="metric">
                                    <span class="label">Last Backup:</span>
                                    <span class="value" id="last-backup">-</span>
                                </div>
                                <div class="metric">
                                    <span class="label">Storage Used:</span>
                                    <span class="value" id="storage-used">-</span>
                                </div>
                                <div class="metric">
                                    <span class="label">Health Status:</span>
                                    <span class="value" id="db-health">-</span>
                                </div>
                            </div>
                        </div>

                        <div class="card alerts-card">
                            <h3>🚨 Active Alerts</h3>
                            <div id="alerts-container">
                                <p class="no-alerts">No active alerts</p>
                            </div>
                        </div>
                    </div>

                    <div class="charts-section">
                        <div class="chart-container">
                            <h3>📈 Real-time Charts</h3>
                            <canvas id="performanceChart" width="400" height="200"></canvas>
                        </div>
                    </div>
                </div>

                <script>
                    """ + getJavaScript() + """
                </script>
            </body>
            </html>
            """;
    }

    /**
     * Get CSS styles for the dashboard
     */
    private String getCSS() {
        return """
            * {
                margin: 0;
                padding: 0;
                box-sizing: border-box;
            }

            body {
                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                min-height: 100vh;
                color: #333;
            }

            .container {
                max-width: 1400px;
                margin: 0 auto;
                padding: 20px;
            }

            header {
                text-align: center;
                margin-bottom: 30px;
                color: white;
            }

            header h1 {
                font-size: 3rem;
                margin-bottom: 10px;
                text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
            }

            header p {
                font-size: 1.2rem;
                opacity: 0.9;
            }

            .status-indicator {
                display: inline-flex;
                align-items: center;
                gap: 10px;
                margin-top: 15px;
                padding: 10px 20px;
                background: rgba(255,255,255,0.1);
                border-radius: 25px;
                backdrop-filter: blur(10px);
            }

            .status-dot {
                width: 12px;
                height: 12px;
                border-radius: 50%;
                background: #4CAF50;
                animation: pulse 2s infinite;
            }

            @keyframes pulse {
                0% { opacity: 1; }
                50% { opacity: 0.5; }
                100% { opacity: 1; }
            }

            nav {
                display: flex;
                justify-content: center;
                gap: 20px;
                margin-bottom: 30px;
                flex-wrap: wrap;
            }

            nav a {
                color: white;
                text-decoration: none;
                padding: 10px 20px;
                background: rgba(255,255,255,0.1);
                border-radius: 25px;
                transition: all 0.3s ease;
                backdrop-filter: blur(10px);
            }

            nav a:hover {
                background: rgba(255,255,255,0.2);
                transform: translateY(-2px);
            }

            .dashboard-grid {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
                gap: 20px;
                margin-bottom: 30px;
            }

            .card {
                background: rgba(255,255,255,0.95);
                border-radius: 15px;
                padding: 20px;
                box-shadow: 0 8px 32px rgba(0,0,0,0.1);
                backdrop-filter: blur(10px);
                transition: transform 0.3s ease;
            }

            .card:hover {
                transform: translateY(-5px);
            }

            .card h3 {
                margin-bottom: 15px;
                color: #667eea;
                font-size: 1.3rem;
            }

            .metric-grid {
                display: grid;
                grid-template-columns: 1fr 1fr;
                gap: 10px;
            }

            .metric {
                display: flex;
                justify-content: space-between;
                align-items: center;
                padding: 8px 0;
                border-bottom: 1px solid #eee;
            }

            .metric:last-child {
                border-bottom: none;
            }

            .label {
                font-weight: 500;
                color: #666;
            }

            .value {
                font-weight: bold;
                color: #667eea;
                font-family: 'Courier New', monospace;
            }

            .charts-section {
                background: rgba(255,255,255,0.95);
                border-radius: 15px;
                padding: 20px;
                box-shadow: 0 8px 32px rgba(0,0,0,0.1);
                backdrop-filter: blur(10px);
            }

            .chart-container h3 {
                margin-bottom: 15px;
                color: #667eea;
            }

            .no-alerts {
                color: #666;
                font-style: italic;
                text-align: center;
                padding: 20px;
            }

            @media (max-width: 768px) {
                .dashboard-grid {
                    grid-template-columns: 1fr;
                }

                .metric-grid {
                    grid-template-columns: 1fr;
                }

                header h1 {
                    font-size: 2rem;
                }

                nav {
                    flex-direction: column;
                    align-items: center;
                }
            }
            """;
    }

    /**
     * Get JavaScript for real-time updates
     */
    private String getJavaScript() {
        return """
            // Real-time dashboard updates
            function updateDashboard() {
                fetch('/api/status')
                    .then(response => response.json())
                    .then(data => {
                        // Update AI status
                        document.getElementById('training-active').textContent = data.aiStatus?.trainingActive ? 'Yes' : 'No';
                        document.getElementById('learning-enabled').textContent = data.aiStatus?.learningEnabled ? 'Yes' : 'No';
                        document.getElementById('training-progress').textContent = Math.round((data.aiStatus?.trainingProgress || 0) * 100) + '%';
                        document.getElementById('current-step').textContent = data.aiStatus?.currentTrainingStep || 0;
                        document.getElementById('experiences-processed').textContent = (data.aiStatus?.totalExperiencesProcessed || 0).toLocaleString();
                        document.getElementById('buffer-size').textContent = (data.aiStatus?.experienceBufferSize || 0).toLocaleString();

                        // Update performance
                        document.getElementById('memory-usage').textContent = (data.systemResources?.memoryUsageMB || 0) + ' MB';
                        document.getElementById('batches-per-sec').textContent = (data.performanceMetrics?.trainingBatchesPerSecond || 0).toFixed(2);
                        document.getElementById('avg-training-time').textContent = (data.performanceMetrics?.averageTrainingTime || 0).toFixed(2) + ' ms';
                        document.getElementById('cpu-usage').textContent = (data.systemResources?.cpuUsagePercent || 0).toFixed(1) + '%';

                        // Update database
                        document.getElementById('models-count').textContent = data.databaseStatus?.totalModels || 0;
                        document.getElementById('last-backup').textContent = data.databaseStatus?.lastBackupTime ? new Date(data.databaseStatus.lastBackupTime).toLocaleString() : 'Never';
                        document.getElementById('storage-used').textContent = (data.databaseStatus?.storageUsedMB || 0) + ' MB';
                        document.getElementById('db-health').textContent = data.databaseStatus?.healthy ? 'Healthy' : 'Issues';

                        // Update overall status
                        const overallHealthy = data.aiStatus?.systemHealthy && data.databaseStatus?.healthy;
                        const statusDot = document.getElementById('status-dot');
                        const statusText = document.getElementById('status-text');

                        if (overallHealthy) {
                            statusDot.style.background = '#4CAF50';
                            statusText.textContent = 'All Systems Operational';
                        } else {
                            statusDot.style.background = '#f44336';
                            statusText.textContent = 'Issues Detected';
                        }

                        // Update alerts
                        updateAlerts(data.alerts || []);
                    })
                    .catch(error => {
                        console.error('Error updating dashboard:', error);
                    });
            }

            function updateAlerts(alerts) {
                const container = document.getElementById('alerts-container');
                if (alerts.length === 0) {
                    container.innerHTML = '<p class="no-alerts">No active alerts</p>';
                    return;
                }

                container.innerHTML = alerts.map(alert => `
                    <div class="alert alert-${alert.severity.toLowerCase()}">
                        <strong>${alert.severity}:</strong> ${alert.message}
                        <br><small>${new Date(alert.timestamp).toLocaleString()}</small>
                    </div>
                `).join('');
            }

            // Update every 2 seconds
            setInterval(updateDashboard, 2000);

            // Initial load
            updateDashboard();

            // WebSocket connection for real-time updates
            const ws = new WebSocket('ws://localhost:8080/ws');
            ws.onmessage = function(event) {
                const data = JSON.parse(event.data);
                if (data.type === 'update') {
                    updateDashboard();
                }
            };
            """;
    }

    /**
     * Generate AI details page
     */
    private String generateAIDetailsPage() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>AI System Details - Ultimate Monitor</title>
                <style>""" + getCSS() + """</style>
            </head>
            <body>
                <div class="container">
                    <header>
                        <h1>🤖 AI System Details</h1>
                        <a href="/">← Back to Dashboard</a>
                    </header>

                    <div class="dashboard-grid">
                        <div class="card">
                            <h3>Training Configuration</h3>
                            <div id="training-config">
                                Loading training configuration...
                            </div>
                        </div>

                        <div class="card">
                            <h3>Experience Buffer</h3>
                            <div id="experience-buffer">
                                Loading buffer statistics...
                            </div>
                        </div>

                        <div class="card">
                            <h3>Q-Learning Statistics</h3>
                            <div id="q-learning-stats">
                                Loading Q-learning data...
                            </div>
                        </div>

                        <div class="card">
                            <h3>Model Performance</h3>
                            <div id="model-performance">
                                Loading performance metrics...
                            </div>
                        </div>
                    </div>
                </div>

                <script>
                    async function loadAIDetails() {
                        try {
                            const response = await fetch('/api/ai-details');
                            const data = await response.json();

                            document.getElementById('training-config').innerHTML = `
                                <div class="metric"><span class="label">Max Steps:</span><span class="value">${data.trainingConfig?.maxSteps || 'N/A'}</span></div>
                                <div class="metric"><span class="label">Batch Size:</span><span class="value">${data.trainingConfig?.batchSize || 'N/A'}</span></div>
                                <div class="metric"><span class="label">Environments:</span><span class="value">${data.trainingConfig?.numEnvironments || 'N/A'}</span></div>
                                <div class="metric"><span class="label">Learning Rate:</span><span class="value">${data.trainingConfig?.learningRate || 'N/A'}</span></div>
                                <div class="metric"><span class="label">Multi-Agent:</span><span class="value">${data.trainingConfig?.useMultiAgent ? 'Yes' : 'No'}</span></div>
                            `;

                            document.getElementById('experience-buffer').innerHTML = `
                                <div class="metric"><span class="label">Current Size:</span><span class="value">${data.bufferStats?.currentSize || 0}</span></div>
                                <div class="metric"><span class="label">Max Capacity:</span><span class="value">${data.bufferStats?.maxCapacity || 0}</span></div>
                                <div class="metric"><span class="label">Utilization:</span><span class="value">${Math.round((data.bufferStats?.utilizationPercent || 0) * 100)}%</span></div>
                                <div class="metric"><span class="label">Samples/sec:</span><span class="value">${(data.bufferStats?.samplesPerSecond || 0).toFixed(2)}</span></div>
                            `;

                            document.getElementById('q-learning-stats').innerHTML = `
                                <div class="metric"><span class="label">Q-Table Size:</span><span class="value">${data.qStats?.tableSize || 0}</span></div>
                                <div class="metric"><span class="label">Update Count:</span><span class="value">${data.qStats?.updateCount || 0}</span></div>
                                <div class="metric"><span class="label">Avg Reward:</span><span class="value">${(data.qStats?.averageReward || 0).toFixed(4)}</span></div>
                            `;

                            document.getElementById('model-performance').innerHTML = `
                                <div class="metric"><span class="label">Accuracy:</span><span class="value">${Math.round((data.performance?.accuracy || 0) * 100)}%</span></div>
                                <div class="metric"><span class="label">Precision:</span><span class="value">${Math.round((data.performance?.precision || 0) * 100)}%</span></div>
                                <div class="metric"><span class="label">Recall:</span><span class="value">${Math.round((data.performance?.recall || 0) * 100)}%</span></div>
                                <div class="metric"><span class="label">F1 Score:</span><span class="value">${Math.round((data.performance?.f1Score || 0) * 100)}%</span></div>
                            `;
                        } catch (error) {
                            console.error('Error loading AI details:', error);
                        }
                    }

                    loadAIDetails();
                    setInterval(loadAIDetails, 5000);
                </script>
            </body>
            </html>
            """;
    }

    /**
     * Generate database viewer page
     */
    private String generateDatabaseViewerPage() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Database Viewer - Ultimate Monitor</title>
                <style>""" + getCSS() + """</style>
            </head>
            <body>
                <div class="container">
                    <header>
                        <h1>💾 Database Viewer</h1>
                        <a href="/">← Back to Dashboard</a>
                    </header>

                    <div class="dashboard-grid">
                        <div class="card">
                            <h3>AI Models</h3>
                            <div id="models-list">
                                Loading models...
                            </div>
                        </div>

                        <div class="card">
                            <h3>Training Data</h3>
                            <div id="training-data">
                                Loading training data...
                            </div>
                        </div>

                        <div class="card">
                            <h3>Model Files</h3>
                            <div id="model-files">
                                Loading model files...
                            </div>
                        </div>

                        <div class="card">
                            <h3>Storage Statistics</h3>
                            <div id="storage-stats">
                                Loading storage statistics...
                            </div>
                        </div>
                    </div>
                </div>

                <script>
                    async function loadDatabaseContent() {
                        try {
                            const response = await fetch('/api/database-content');
                            const data = await response.json();

                            // Models list
                            const modelsHtml = data.aiModels?.map(model => `
                                <div class="model-item">
                                    <strong>${model.modelName}</strong><br>
                                    <small>Type: ${model.modelType} | Version: ${model.modelVersion}</small><br>
                                    <small>Accuracy: ${Math.round(model.accuracy * 100)}% | Created: ${new Date(model.createdAt).toLocaleDateString()}</small>
                                </div>
                            `).join('') || '<p>No models found</p>';
                            document.getElementById('models-list').innerHTML = modelsHtml;

                            // Training data
                            const trainingHtml = data.trainingData?.slice(0, 10).map(td => `
                                <div class="training-item">
                                    <strong>Session ${td.sessionId}</strong><br>
                                    <small>Duration: ${td.duration}ms | Reward: ${td.totalReward?.toFixed(2)}</small>
                                </div>
                            `).join('') || '<p>No training data found</p>';
                            document.getElementById('training-data').innerHTML = trainingHtml;

                            // Model files
                            const filesHtml = data.persistedModels?.map(file => `
                                <div class="file-item">
                                    <strong>${file.getName()}</strong><br>
                                    <small>Size: ${Math.round(file.length() / 1024)}KB | Modified: ${new Date(file.lastModified()).toLocaleDateString()}</small>
                                </div>
                            `).join('') || '<p>No model files found</p>';
                            document.getElementById('model-files').innerHTML = filesHtml;

                            // Storage stats
                            document.getElementById('storage-stats').innerHTML = `
                                <div class="metric"><span class="label">Total Models:</span><span class="value">${data.aiModels?.length || 0}</span></div>
                                <div class="metric"><span class="label">Training Sessions:</span><span class="value">${data.trainingData?.length || 0}</span></div>
                                <div class="metric"><span class="label">Model Files:</span><span class="value">${data.persistedModels?.length || 0}</span></div>
                                <div class="metric"><span class="label">Total Storage:</span><span class="value">${Math.round((data.totalSizeBytes || 0) / (1024*1024))} MB</span></div>
                            `;
                        } catch (error) {
                            console.error('Error loading database content:', error);
                        }
                    }

                    loadDatabaseContent();
                    setInterval(loadDatabaseContent, 10000);
                </script>
            </body>
            </html>
            """;
    }

    /**
     * Generate performance page
     */
    private String generatePerformancePage() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Performance Metrics - Ultimate Monitor</title>
                <style>""" + getCSS() + """</style>
            </head>
            <body>
                <div class="container">
                    <header>
                        <h1>⚡ Performance Metrics</h1>
                        <a href="/">← Back to Dashboard</a>
                    </header>

                    <div class="charts-section">
                        <canvas id="cpuChart" width="800" height="300"></canvas>
                        <canvas id="memoryChart" width="800" height="300"></canvas>
                        <canvas id="trainingChart" width="800" height="300"></canvas>
                    </div>
                </div>

                <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
                <script>
                    // Performance charts would be implemented here
                    console.log('Performance charts loading...');
                </script>
            </body>
            </html>
            """;
    }

    /**
     * Generate logs page
     */
    private String generateLogsPage() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>System Logs - Ultimate Monitor</title>
                <style>""" + getCSS() + """</style>
            </head>
            <body>
                <div class="container">
                    <header>
                        <h1>📝 System Logs</h1>
                        <a href="/">← Back to Dashboard</a>
                    </header>

                    <div class="card">
                        <h3>Recent Log Entries</h3>
                        <div id="log-entries" style="height: 600px; overflow-y: auto; background: #f5f5f5; padding: 10px; border-radius: 5px; font-family: monospace;">
                            Loading logs...
                        </div>
                    </div>
                </div>

                <script>
                    async function loadLogs() {
                        try {
                            const response = await fetch('/api/logs?limit=100');
                            const logs = await response.json();
                            const logHtml = logs.map(log => `
                                <div style="margin: 5px 0; border-bottom: 1px solid #ddd; padding: 5px;">
                                    <span style="color: ${getLogColor(log.level)};">[${log.timestamp}] ${log.level}</span>
                                    <span style="color: #666;">[${log.logger}]</span>
                                    ${log.message}
                                </div>
                            `).join('');
                            document.getElementById('log-entries').innerHTML = logHtml;
                        } catch (error) {
                            console.error('Error loading logs:', error);
                        }
                    }

                    function getLogColor(level) {
                        switch(level.toLowerCase()) {
                            case 'error': return '#f44336';
                            case 'warn': return '#ff9800';
                            case 'info': return '#2196F3';
                            case 'debug': return '#666';
                            default: return '#333';
                        }
                    }

                    loadLogs();
                    setInterval(loadLogs, 5000);
                </script>
            </body>
            </html>
            """;
    }

    /**
     * Update methods called by the main monitor
     */
    public void updateAISystemStatus(AISystemStatus status) {
        this.latestAIStatus = status;
        lastUpdateTime.set(System.currentTimeMillis());
    }

    public void updateDatabaseStatus(DatabaseStatus status) {
        this.latestDBStatus = status;
    }

    public void updatePerformanceMetrics(PerformanceMetrics metrics) {
        this.latestPerformance = metrics;
    }

    public void updateSystemResources(SystemResources resources) {
        this.latestResources = resources;
    }

    public void refreshAll() {
        // This method is called periodically to refresh all data
        lastUpdateTime.set(System.currentTimeMillis());
    }

    /**
     * Shutdown the web server
     */
    public void shutdown() {
        Spark.stop();
        log.info("🛑 Web Dashboard shutdown complete");
    }

    /**
     * Simple JSON serialization (in a real implementation, use a proper JSON library)
     */
    private String toJson(Object obj) {
        // Placeholder - in reality, you'd use Jackson, Gson, etc.
        return "{}";
    }

    /**
     * Generate status JSON for API
     */
    private String generateStatusJson() {
        SystemOverview overview = monitor.getSystemOverview();
        return String.format("""
            {
                "aiStatus": {
                    "trainingActive": %s,
                    "learningEnabled": %s,
                    "trainingProgress": %.2f,
                    "currentTrainingStep": %d,
                    "totalExperiencesProcessed": %d,
                    "experienceBufferSize": %d,
                    "systemHealthy": %s
                },
                "databaseStatus": {
                    "totalModels": %d,
                    "healthy": %s,
                    "storageUsedMB": %d,
                    "lastBackupTime": %d
                },
                "performanceMetrics": {
                    "trainingBatchesPerSecond": %.2f,
                    "averageTrainingTime": %.2f
                },
                "systemResources": {
                    "memoryUsageMB": %d,
                    "cpuUsagePercent": %.1f
                },
                "alerts": []
            }
            """,
            overview.getAiStatus().isTrainingActive(),
            overview.getAiStatus().isLearningEnabled(),
            overview.getAiStatus().getTrainingProgress(),
            overview.getAiStatus().getCurrentTrainingStep(),
            overview.getAiStatus().getTotalExperiencesProcessed(),
            overview.getAiStatus().getExperienceBufferSize(),
            overview.getAiStatus().isSystemHealthy(),
            overview.getDatabaseStatus().getTotalModels(),
            overview.getDatabaseStatus().isHealthy(),
            overview.getDatabaseStatus().getStorageUsedMB(),
            overview.getDatabaseStatus().getLastBackupTime(),
            overview.getPerformanceMetrics().getTrainingBatchesPerSecond(),
            overview.getPerformanceMetrics().getAverageTrainingTime(),
            overview.getSystemResources().getMemoryUsageMB(),
            overview.getSystemResources().getCpuUsagePercent()
        );
    }
}
            """;
    }
    <parameter name="EmptyFile">false
