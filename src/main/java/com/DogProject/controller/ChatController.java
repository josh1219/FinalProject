package com.DogProject.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatController {

    @MessageMapping("/sendMessage")
    @SendTo("/topic/messages")
    public String sendMessage(String message) throws Exception {
        System.out.println("서버에서 수신된 메시지: " + message);
        return message;
    }

    @GetMapping("/chat")
    public String chatPage() {
        return "chat";
    }
}