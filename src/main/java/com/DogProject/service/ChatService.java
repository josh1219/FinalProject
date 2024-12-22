package com.DogProject.service;

import com.DogProject.entity.Chat;
import com.DogProject.entity.Member;
import com.DogProject.repository.ChatRepository;
import com.DogProject.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final MemberRepository memberRepository;

    // 채팅 메시지 저장
    public Chat saveChat(String content, int senderId, int receiverId) {
        Member sender = memberRepository.findById(senderId).orElseThrow();
        Member receiver = memberRepository.findById(receiverId).orElseThrow();

        Chat chat = new Chat();
        chat.setContent(content);
        chat.setSender(sender);
        chat.setReceiver(receiver);
        chat.setSendTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        return chatRepository.save(chat);
    }

    // 두 사용자 간의 채팅 내역 조회
    public List<Chat> getChatsBetweenUsers(int user1Id, int user2Id) {
        Member user1 = memberRepository.findById(user1Id).orElseThrow();
        Member user2 = memberRepository.findById(user2Id).orElseThrow();
        return chatRepository.findChatsBetweenUsers(user1, user2);
    }

    // 사용자의 모든 채팅방 목록 조회 (각 상대방과의 최신 메시지만)
    public List<Chat> getLatestChatsByUserId(int userId) {
        System.out.println("\n===== 채팅방 목록 조회 =====");
        System.out.println("사용자 ID: " + userId);
        
        List<Chat> chatRooms = chatRepository.findLatestChatsByUserId(userId);
        System.out.println("조회된 채팅방 수: " + chatRooms.size());
        
        for (Chat chat : chatRooms) {
            System.out.println("\n채팅방 정보:");
            System.out.println("- 채팅 ID: " + chat.getCIdx());
            System.out.println("- 보낸 사람: " + chat.getSender().getName());
            System.out.println("- 받는 사람: " + chat.getReceiver().getName());
            System.out.println("- 마지막 메시지: " + chat.getContent());
            System.out.println("- 시간: " + chat.getSendTime());
        }
        
        return chatRooms;
    }
}
