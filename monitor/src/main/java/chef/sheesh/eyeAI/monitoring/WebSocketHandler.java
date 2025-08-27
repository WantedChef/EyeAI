package chef.sheesh.eyeAI.monitoring;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket Handler for real-time updates
 */
@Slf4j
@WebSocket
public class WebSocketHandler {

    private static final Gson gson = new Gson();
    private static final Set<Session> sessions = ConcurrentHashMap.newKeySet();
    private static UltimateAIMonitor monitor;

    /**
     * Set the monitor instance for broadcasting updates
     */
    public static void setMonitor(UltimateAIMonitor monitorInstance) {
        monitor = monitorInstance;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        sessions.add(session);
        log.debug("WebSocket client connected: {}", session.getRemoteAddress());
        broadcastSystemStatus();
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        sessions.remove(session);
        log.debug("WebSocket client disconnected: {} - {}", session.getRemoteAddress(), reason);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        try {
            WebSocketMessage wsMessage = gson.fromJson(message, WebSocketMessage.class);
            handleMessage(session, wsMessage);
        } catch (Exception e) {
            log.error("Error handling WebSocket message", e);
        }
    }

    /**
     * Handle incoming WebSocket messages
     */
    private void handleMessage(Session session, WebSocketMessage message) {
        switch (message.getType()) {
            case "subscribe":
                handleSubscribe(session, message);
                break;
            case "unsubscribe":
                handleUnsubscribe(session, message);
                break;
            case "request_update":
                broadcastSystemStatus();
                break;
            default:
                log.warn("Unknown WebSocket message type: {}", message.getType());
        }
    }

    /**
     * Handle subscription to specific data types
     */
    private void handleSubscribe(Session session, WebSocketMessage message) {
        // Implementation for subscribing to specific data streams
        log.debug("Client subscribed to: {}", message.getData());
    }

    /**
     * Handle unsubscription from data types
     */
    private void handleUnsubscribe(Session session, WebSocketMessage message) {
        // Implementation for unsubscribing from data streams
        log.debug("Client unsubscribed from: {}", message.getData());
    }

    /**
     * Broadcast system status to all connected clients
     */
    public static void broadcastSystemStatus() {
        if (monitor == null) return;

        SystemOverview overview = monitor.getSystemOverview();
        WebSocketMessage message = new WebSocketMessage(
            "status_update",
            gson.toJson(overview),
            System.currentTimeMillis()
        );

        broadcast(message);
    }

    /**
     * Broadcast alert to all connected clients
     */
    public static void broadcastAlert(Alert alert) {
        WebSocketMessage message = new WebSocketMessage(
            "alert",
            gson.toJson(alert),
            alert.getTimestamp()
        );

        broadcast(message);
    }

    /**
     * Broadcast message to all connected clients
     */
    private static void broadcast(WebSocketMessage message) {
        String jsonMessage = gson.toJson(message);

        sessions.forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.getRemote().sendString(jsonMessage);
                } catch (IOException e) {
                    log.error("Error broadcasting to WebSocket client", e);
                    sessions.remove(session);
                }
            } else {
                sessions.remove(session);
            }
        });
    }

    /**
     * Get number of connected clients
     */
    public static int getConnectedClients() {
        return sessions.size();
    }

    // Data classes
    public static class WebSocketMessage {
        private String type;
        private String data;
        private long timestamp;

        public WebSocketMessage() {}

        public WebSocketMessage(String type, String data, long timestamp) {
            this.type = type;
            this.data = data;
            this.timestamp = timestamp;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getData() { return data; }
        public void setData(String data) { this.data = data; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}
