package com.DogProject.controller;

import com.DogProject.domain.ChatMessage;
import com.DogProject.entity.Member;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat/{roomId}")
    @SendTo("/topic/messages/{roomId}")
    public ChatMessage handleMessage(@DestinationVariable String roomId, ChatMessage message) {
        // 메시지 처리 로직
        return message;
    }

    @GetMapping("/chat/{id1}_{id2}")
    public String chatRoom(@PathVariable int id1, @PathVariable int id2, Model model, HttpSession session) {
        Member member = (Member) session.getAttribute("member");
        
        if (member == null) {
            return "redirect:/member/login";
        }

        // 현재 사용자가 채팅방의 참여자인지 확인
        if (member.getMIdx() != id1 && member.getMIdx() != id2) {
            return "redirect:/error";
        }

        // 자기 자신과의 채팅 방지
        if (id1 == id2) {
            return "redirect:/error";
        }

        // 채팅방 ID 정규화 (항상 작은 ID가 앞에 오도록)
        String roomId = id1 < id2 ? id1 + "_" + id2 : id2 + "_" + id1;
        if (!roomId.equals(id1 + "_" + id2)) {
            return "redirect:/chat/" + roomId;
        }

        // 현재 사용자 정보를 Map으로 변환
        Map<String, Object> currentMember = new HashMap<>();
        currentMember.put("mIdx", member.getMIdx());
        currentMember.put("mName", member.getName());

        model.addAttribute("currentMember", currentMember);
        model.addAttribute("roomId", roomId);
        
        return "chat";
    }
}