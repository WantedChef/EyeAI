package chef.sheesh.eyeAI.integration.mem0;

public interface Mem0Service {
    /** Initialize network clients/resources if enabled. */
    void start();
    /** Gracefully release resources. */
    void shutdown();
    /** Whether Mem0 features are enabled via configuration. */
    boolean isEnabled();
}

