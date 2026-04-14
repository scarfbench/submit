/*
 * Copyright (c), Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v1.0, which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package jakarta.tutorial.taskcreator;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/* Notify the clients so that they can refresh the log textarea */
@Component
public class InfoEndpoint extends TextWebSocketHandler {

    private static final Logger log = Logger.getLogger("InfoEndpoint");
    /* Keep a list of clients */
    private static final Queue<WebSocketSession> sessions =
            new ConcurrentLinkedQueue<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("[InfoEndpoint] Connection opened");
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        log.info("[InfoEndpoint] Connection closed");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Handle incoming messages if needed
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.severe("[InfoEndpoint] WebSocket error: " + exception.getMessage());
    }

    /* Listen to Spring application events (replacement for CDI @Observes).
     * The clients use JavaScript to make AJAX requests to refresh
     * the log textarea. */
    @EventListener
    public void pushAlert(String event) {
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                try {
                    s.sendMessage(new TextMessage(event));
                    log.info("[InfoEndpoint] Event sent: " + event);
                } catch (IOException ex) {
                    log.warning("[InfoEndpoint] Failed to send message: " + ex.getMessage());
                }
            }
        }
    }
}
