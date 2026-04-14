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

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/* WebSocket version of the dukeetf example */
@Component
public class ETFEndpoint extends TextWebSocketHandler {
    private static final Logger logger = Logger.getLogger("ETFEndpoint");
    /* Queue for all open WebSocket sessions */
    static Queue<WebSocketSession> queue = new ConcurrentLinkedQueue<>();

    /* PriceVolumeBean calls this method to send updates */
    public static void send(double price, int volume) {
        String msg = String.format("%.2f / %d", price, volume);
        try {
            /* Send updates to all open WebSocket sessions */
            for (WebSocketSession session : queue) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(msg));
                    logger.log(Level.INFO, "Sent: {0}", msg);
                }
            }
        } catch (IOException e) {
            logger.log(Level.INFO, e.toString());
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        /* Register this connection in the queue */
        queue.add(session);
        logger.log(Level.INFO, "Connection opened.");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        /* Remove this connection from the queue */
        queue.remove(session);
        logger.log(Level.INFO, "Connection closed.");
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        /* Remove this connection from the queue */
        queue.remove(session);
        logger.log(Level.INFO, exception.toString());
        logger.log(Level.INFO, "Connection error.");
    }

}
