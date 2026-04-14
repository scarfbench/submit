package jakarta.tutorial.taskcreator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@ServerEndpoint("/wsinfo")
public class InfoWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(InfoWebSocketHandler.class);
    private static final Set<Session> sessions = ConcurrentHashMap.newKeySet();

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        log.info("[InfoWebSocketHandler] Connection opened");
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // no-op
    }

    public void onEvent(@Observes String event) {
        // Broadcast any String events
        for (Session s : sessions) {
            if (s.isOpen()) {
                try {
                    s.getBasicRemote().sendText(event);
                } catch (IOException e) {
                    log.error("Failed to send message to WebSocket session", e);
                }
            }
        }
    }
}
