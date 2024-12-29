package com.DogProject.service;

import com.DogProject.dto.ChatDTO;
import com.DogProject.entity.Chat;
import com.DogProject.entity.Member;
import com.DogProject.repository.ChatRepository;
import com.DogProject.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;
    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    // 채팅 메시지 저장
    public Chat saveChat(String content, int senderId, int receiverId) {
        Member sender = memberRepository.findById(senderId).orElseThrow();
        Member receiver = memberRepository.findById(receiverId).orElseThrow();

        Chat chat = new Chat();
        chat.setContent(content);
        chat.setSender(sender);
        chat.setReceiver(receiver);
        chat.setSendTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        chat.setRead(false);  // 새 메시지는 기본적으로 읽지 않음 상태

        Chat savedChat = chatRepository.save(chat);
        
        // 수신자에게 알림 전송
        notificationService.notifyUnreadMessages(receiverId);
        
        return savedChat;
    }

    // 사용자의 모든 채팅방 목록 조회 (각 상대방과의 최신 메시지만)
    public List<Chat> getLatestChatsByUserId(int userId) {
        List<ChatDTO> chatDTOs = chatRepository.findLatestChatsByUserId(userId);
        return chatDTOs.stream()
            .map(dto -> {
                Member sender = memberRepository.findById(dto.getSender()).orElseThrow();
                Member receiver = memberRepository.findById(dto.getReceiver()).orElseThrow();
                
                Chat chat = new Chat();
                chat.setCIdx(dto.getCIdx());
                chat.setContent(dto.getContent());
                chat.setSendTime(dto.getSendTime());
                chat.setSender(sender);
                chat.setReceiver(receiver);
                chat.setRead(dto.getIsRead());
                chat.setUnreadCount(dto.getUnreadCount());
                
                return chat;
            })
            .collect(Collectors.toList());
    }

    // 두 사용자 간의 채팅 내역 조회
    public List<Chat> getChatsBetweenUsers(int user1Id, int user2Id) {
        Member user1 = memberRepository.findById(user1Id).orElseThrow();
        Member user2 = memberRepository.findById(user2Id).orElseThrow();
        return chatRepository.findChatsBetweenUsers(user1, user2);
    }

    // 채팅방 삭제
    @Transactional
    public void deleteRooms(List<String> roomIds) {
        if (roomIds != null && !roomIds.isEmpty()) {
            for (String roomId : roomIds) {
                chatRepository.deleteByRoomId(roomId);
            }
        }
    }

    // 메시지를 읽음으로 표시
    @Transactional
    public void markMessagesAsRead(Member receiver, Member sender) {
        chatRepository.markAsRead(receiver, sender);
        // 읽음 처리 후 알림 업데이트
        notificationService.notifyUnreadMessages(receiver.getMIdx());
    }
}
