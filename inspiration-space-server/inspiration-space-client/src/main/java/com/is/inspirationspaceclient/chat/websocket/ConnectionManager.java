package com.is.inspirationspaceclient.chat.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * websocket连接管理器
 */
@Component
public class ConnectionManager {
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void register(Long userId, WebSocketSession session) {
        sessions.put(userId, session);
    }

    public void unregister(Long userId) {
        sessions.remove(userId);
    }

    public boolean isOnline(Long userId) {
        return sessions.containsKey(userId);
    }

    public void sendIfOnline(Long userId, java.util.function.Consumer<WebSocketSession> consumer) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            consumer.accept(session);
        }
    }
}
