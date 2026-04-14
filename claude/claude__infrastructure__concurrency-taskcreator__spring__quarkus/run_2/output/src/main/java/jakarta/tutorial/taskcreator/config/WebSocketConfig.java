package jakarta.tutorial.taskcreator.config;

import jakarta.enterprise.context.ApplicationScoped;

// WebSocket configuration in Quarkus is handled through annotations
// on the WebSocket endpoint itself, so this class is no longer needed
// but we keep it as an empty placeholder for now
@ApplicationScoped
public class WebSocketConfig {
    // Configuration is now handled via @ServerEndpoint annotation
}