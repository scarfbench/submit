package jakarta.tutorial.taskcreator;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final InfoEndpoint infoEndpoint;

    public WebSocketConfig(InfoEndpoint infoEndpoint) {
        this.infoEndpoint = infoEndpoint;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(infoEndpoint, "/wsinfo").setAllowedOrigins("*");
    }
}
