package com.game.server.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;

@Component
public class GameSocketHandler extends TextWebSocketHandler {
    private final Map<String, List<WebSocketSession>> rooms = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String roomId = (String) session.getAttributes().get("roomId");
        rooms.computeIfAbsent(roomId, k -> new ArrayList<>()).add(session);
        System.out.println("🔗 WebSocket connected: " + roomId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        for (List<WebSocketSession> sessions : rooms.values()) {
            for (WebSocketSession s : sessions) {
                if (s.isOpen() && !s.equals(session)) {
                    s.sendMessage(message);
                }
            }
        }
    }
}