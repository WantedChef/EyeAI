package chef.sheesh.eyeAI.core;

import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Simple dependency injection container for managing plugin services
 */
@RequiredArgsConstructor
public class ServiceManager {
    
    private final Plugin plugin;
    private final ConcurrentHashMap<Class<?>, Object> services = new ConcurrentHashMap<>();
    
    /**
     * Register a service instance
     */
    public <T> void registerService(@NotNull Class<T> serviceClass, @NotNull T instance) {
        services.put(serviceClass, instance);
        plugin.getLogger().info("Registered service: " + serviceClass.getSimpleName());
    }
    
    /**
     * Get a service instance
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public <T> T getService(@NotNull Class<T> serviceClass) {
        Object service = services.get(serviceClass);
        if (service == null) {
            throw new IllegalStateException("Service not registered: " + serviceClass.getSimpleName());
        }
        return (T) service;
    }
    
    /**
     * Check if a service is registered
     */
    public boolean hasService(@NotNull Class<?> serviceClass) {
        return services.containsKey(serviceClass);
    }
    
    /**
     * Shutdown all services gracefully
     */
    public void shutdown() {
        try {
            // Services that implement AutoCloseable will be closed
            for (Object service : services.values()) {
                if (service instanceof AutoCloseable) {
                    try {
                        ((AutoCloseable) service).close();
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.WARNING, "Error closing service: " + service.getClass().getSimpleName(), e);
                    }
                }
            }
            services.clear();
            plugin.getLogger().info("All services shutdown");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error during service shutdown", e);
        }
    }
}
