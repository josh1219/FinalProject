package com.DogProject.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    // 클라이언트로부터 메시지를 수신하고, 메시지를 '/topic/messages'로 전송한대이~
    @MessageMapping("/sendMessage")  // 클라이언트에서 "/app/sendMessage"로 메시지를 보내면 이 메서드가 호출된데이~
    @SendTo("/topic/messages")  // 클라이언트에게 전달될 경로
    public String sendMessage(String message) throws Exception {
        System.out.println("서버에서 수신된 메시지: " + message);
        return message;  // 수신된 메시지를 그대로 반환 (채팅 메시지)
    }
}

