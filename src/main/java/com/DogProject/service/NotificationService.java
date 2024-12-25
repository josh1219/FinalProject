package com.DogProject.service;

import com.DogProject.handler.NotificationWebSocketHandler;
import com.DogProject.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationWebSocketHandler notificationWebSocketHandler;
    private final ChatRepository chatRepository;

    public void notifyUnreadMessages(int userId) {
        int unreadCount = chatRepository.countUnreadMessages(userId);
        notificationWebSocketHandler.sendNotification(String.valueOf(userId), unreadCount);
    }
}
