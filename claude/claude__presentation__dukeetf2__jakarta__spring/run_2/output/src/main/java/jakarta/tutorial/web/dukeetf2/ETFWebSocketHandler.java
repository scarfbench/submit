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
package jakarta.tutorial.web.dukeetf2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/* WebSocket version of the dukeetf example - Spring WebSocket implementation */
@Component
public class ETFWebSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ETFWebSocketHandler.class);

    /* Queue for all open WebSocket sessions */
    private static final Queue<WebSocketSession> sessions = new ConcurrentLinkedQueue<>();

    /* PriceVolumeService calls this method to send updates */
    public static void send(double price, int volume) {
        String msg = String.format("%.2f / %d", price, volume);
        try {
            /* Send updates to all open WebSocket sessions */
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(msg));
                    logger.info("Sent: {}", msg);
                }
            }
        } catch (IOException e) {
            logger.error("Error sending message", e);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        /* Register this connection in the queue */
        sessions.add(session);
        logger.info("Connection opened.");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        /* Remove this connection from the queue */
        sessions.remove(session);
        logger.info("Connection closed.");
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        /* Remove this connection from the queue */
        sessions.remove(session);
        logger.error("Connection error", exception);
    }
}
