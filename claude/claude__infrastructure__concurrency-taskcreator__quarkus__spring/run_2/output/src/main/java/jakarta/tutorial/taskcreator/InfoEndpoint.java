package jakarta.tutorial.taskcreator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class InfoEndpoint extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(InfoEndpoint.class);
    private static final Queue<WebSocketSession> sessions = new ConcurrentLinkedQueue<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        log.info("[InfoEndpoint] Connection opened");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Handle incoming messages if needed
    }

    @EventListener
    public void pushAlert(TaskUpdateEvent event) {
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                try {
                    s.sendMessage(new TextMessage(event.getMessage()));
                } catch (IOException e) {
                    log.error("Error sending WebSocket message", e);
                }
            }
        }
    }
}
