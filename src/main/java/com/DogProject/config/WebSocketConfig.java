package com.DogProject.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 메시지 브로커를 설정 (브라우저와 서버 간의 메시지 라우팅 설정)
        config.enableSimpleBroker("/topic");  // 클라이언트가 구독하는 메시지 경로 1
        config.setApplicationDestinationPrefixes("/app");  // 서버로 전송되는 메시지 경로 2
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // STOMP 엔드포인트를 등록하고, SockJS를 사용하여 연결하도록 설정
        registry.addEndpoint("/chat").withSockJS();  // /chat 경로에서 연결 3
    }
}
