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
package jakarta.tutorial.web.websocketbot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import jakarta.tutorial.web.websocketbot.decoders.MessageDecoder;
import jakarta.tutorial.web.websocketbot.encoders.ChatMessageEncoder;
import jakarta.tutorial.web.websocketbot.encoders.InfoMessageEncoder;
import jakarta.tutorial.web.websocketbot.encoders.JoinMessageEncoder;
import jakarta.tutorial.web.websocketbot.encoders.UsersMessageEncoder;
import jakarta.tutorial.web.websocketbot.messages.ChatMessage;
import jakarta.tutorial.web.websocketbot.messages.InfoMessage;
import jakarta.tutorial.web.websocketbot.messages.JoinMessage;
import jakarta.tutorial.web.websocketbot.messages.Message;
import jakarta.tutorial.web.websocketbot.messages.UsersMessage;

@Component
public class BotEndpoint extends TextWebSocketHandler {
    private static final Logger logger = Logger.getLogger("BotEndpoint");
    
    private final BotBean botbean;
    private final ExecutorService executorService;
    private final MessageDecoder messageDecoder;
    private final JoinMessageEncoder joinMessageEncoder;
    private final ChatMessageEncoder chatMessageEncoder;
    private final InfoMessageEncoder infoMessageEncoder;
    private final UsersMessageEncoder usersMessageEncoder;
    
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    public BotEndpoint(BotBean botbean) {
        this.botbean = botbean;
        this.executorService = Executors.newCachedThreadPool();
        this.messageDecoder = new MessageDecoder();
        this.joinMessageEncoder = new JoinMessageEncoder();
        this.chatMessageEncoder = new ChatMessageEncoder();
        this.infoMessageEncoder = new InfoMessageEncoder();
        this.usersMessageEncoder = new UsersMessageEncoder();
    }
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        logger.log(Level.INFO, "Connection opened.");
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        String payload = textMessage.getPayload();
        logger.log(Level.INFO, "Received: {0}", payload);
        
        try {
            Message msg = messageDecoder.decode(payload);
            
            if (msg instanceof JoinMessage) {
                JoinMessage jmsg = (JoinMessage) msg;
                session.getAttributes().put("name", jmsg.getName());
                session.getAttributes().put("active", true);
                logger.log(Level.INFO, "Received: {0}", jmsg.toString());
                sendAll(new InfoMessage(jmsg.getName() + " has joined the chat"));
                sendAll(new ChatMessage("Duke", jmsg.getName(), "Hi there!!"));
                sendAll(new UsersMessage(this.getUserList()));
                
            } else if (msg instanceof ChatMessage) {
                final ChatMessage cmsg = (ChatMessage) msg;
                logger.log(Level.INFO, "Received: {0}", cmsg.toString());
                sendAll(cmsg);
                if (cmsg.getTarget().compareTo("Duke") == 0) {
                    executorService.submit(() -> {
                        String resp = botbean.respond(cmsg.getMessage());
                        try {
                            sendAll(new ChatMessage("Duke", cmsg.getName(), resp));
                        } catch (Exception e) {
                            logger.log(Level.SEVERE, "Error sending bot response", e);
                        }
                    });
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error handling message", e);
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        session.getAttributes().put("active", false);
        if (session.getAttributes().containsKey("name")) {
            String name = session.getAttributes().get("name").toString();
            sendAll(new InfoMessage(name + " has left the chat"));
            sendAll(new UsersMessage(this.getUserList()));
        }
        logger.log(Level.INFO, "Connection closed.");
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.log(Level.INFO, "Connection error ({0})", exception.toString());
    }
    
    public synchronized void sendAll(Object msg) throws Exception {
        String encodedMsg = encodeMessage(msg);
        for (WebSocketSession s : sessions.values()) {
            if (s.isOpen()) {
                try {
                    s.sendMessage(new TextMessage(encodedMsg));
                    logger.log(Level.INFO, "Sent: {0}", encodedMsg);
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Error sending message to session", e);
                }
            }
        }
    }
    
    private String encodeMessage(Object msg) throws Exception {
        if (msg instanceof JoinMessage) {
            return joinMessageEncoder.encode((JoinMessage) msg);
        } else if (msg instanceof ChatMessage) {
            return chatMessageEncoder.encode((ChatMessage) msg);
        } else if (msg instanceof InfoMessage) {
            return infoMessageEncoder.encode((InfoMessage) msg);
        } else if (msg instanceof UsersMessage) {
            return usersMessageEncoder.encode((UsersMessage) msg);
        }
        throw new IllegalArgumentException("Unknown message type: " + msg.getClass());
    }
    
    public List<String> getUserList() {
        List<String> users = new ArrayList<>();
        users.add("Duke");
        for (WebSocketSession s : sessions.values()) {
            if (s.isOpen() && Boolean.TRUE.equals(s.getAttributes().get("active"))) {
                users.add(s.getAttributes().get("name").toString());
            }
        }
        return users;
    }
}
