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

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

/* Notify the clients so that they can refresh the log textarea */
@Component
@ServerEndpoint("/wsinfo")
public class InfoEndpoint {

    private static final Logger log = Logger.getLogger("InfoEndpoint");
    /* Keep a list of clients */
    private static final Queue<Session> sessions =
            new ConcurrentLinkedQueue<>();

    @OnOpen
    public void onOpen(Session session) {
        log.info("[InfoEndpoint] Connection opened");
        sessions.add(session);
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
    }

    @OnError
    public void onError(Session session, Throwable t) { }

    @OnMessage
    public void onMessage(String msg) { }

    /* Listen for Spring application events fired from the service and notify clients.
     * The clients use JavaScript to make requests to refresh the log textarea. */
    @EventListener
    public void handleTaskEvent(TaskEvent event) {
        pushAlert(event.getMessage());
    }

    private void pushAlert(String event) {
        for (Session s : sessions) {
            if (s.isOpen())
                try {
                    s.getBasicRemote().sendText(event);
                    log.info("[InfoEndpoint] Event sent");
                } catch (IOException ex) { }
        }
    }
}
