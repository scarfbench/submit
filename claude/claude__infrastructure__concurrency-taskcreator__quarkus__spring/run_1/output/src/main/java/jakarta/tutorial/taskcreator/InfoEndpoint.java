package jakarta.tutorial.taskcreator;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@ServerEndpoint("/wsinfo")
public class InfoEndpoint {
    private static final Logger log = LoggerFactory.getLogger(InfoEndpoint.class);
    private static final Queue<Session> sessions = new ConcurrentLinkedQueue<>();

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        log.info("[InfoEndpoint] Connection opened");
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
    }

    @OnMessage
    public void onMessage(String msg) {
    }

    @EventListener
    public void pushAlert(String event) {
        for (Session s : sessions) {
            if (s.isOpen()) {
                try {
                    s.getBasicRemote().sendText(event);
                } catch (IOException e) {
                    log.error("Error sending WebSocket message", e);
                }
            }
        }
    }
}
