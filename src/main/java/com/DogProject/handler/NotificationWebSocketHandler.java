package com.DogProject.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.DogProject.repository.ChatRepository;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationWebSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(NotificationWebSocketHandler.class);
    private static final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    private final ChatRepository chatRepository;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = extractUserId(session);
        if (userId != null) {
            sessions.put(userId, session);
            logger.info("새로운 알림 웹소켓 연결: {}", userId);
            
            // 초기 알림 수 전송
            try {
                int unreadCount = chatRepository.countUnreadMessages(Integer.parseInt(userId));
                String message = String.format("{\"unreadCount\": %d}", unreadCount);
                session.sendMessage(new TextMessage(message));
                logger.info("초기 알림 수 전송 완료: userId={}, unreadCount={}", userId, unreadCount);
            } catch (IOException e) {
                logger.error("초기 알림 수 전송 실패: {}", e.getMessage());
            }
        } else {
            logger.error("사용자 ID를 찾을 수 없음");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userId = extractUserId(session);
        if (userId != null) {
            sessions.remove(userId);
            logger.info("알림 웹소켓 연결 종료: {}", userId);
        }
    }

    private String extractUserId(WebSocketSession session) {
        try {
            URI uri = session.getUri();
            if (uri != null) {
                String query = uri.getQuery();
                if (query != null && query.contains("userId=")) {
                    return query.split("userId=")[1].split("&")[0];
                }
            }
        } catch (Exception e) {
            logger.error("사용자 ID 추출 중 오류: {}", e.getMessage());
        }
        return null;
    }

    public void sendNotification(String userId, int unreadCount) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String message = String.format("{\"unreadCount\": %d}", unreadCount);
                session.sendMessage(new TextMessage(message));
                logger.info("알림 전송 완료: userId={}, unreadCount={}", userId, unreadCount);
            } catch (IOException e) {
                logger.error("알림 전송 실패: {}", e.getMessage());
            }
        } else {
            logger.debug("세션을 찾을 수 없거나 닫힘: userId={}", userId);
        }
    }
}
