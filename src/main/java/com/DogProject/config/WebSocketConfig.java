package com.DogProject.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Configuration
@EnableWebSocketMessageBroker  // WebSocket 메시지 브로커 활성화
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 1. 메시지 브로커 설정
        config.enableSimpleBroker("/topic", "/queue");  // 구독 요청 prefix: 클라이언트가 메시지를 받을 때 사용
        config.setApplicationDestinationPrefixes("/app");  // 메시지 발행 prefix: 클라이언트가 메시지를 보낼 때 사용
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 2. WebSocket 연결 엔드포인트 설정
        registry.addEndpoint("/ws")  // WebSocket 연결 시 사용할 엔드포인트 설정
               .setAllowedOrigins("http://edu.e-tops.co.kr", "http://localhost:8989")  // 허용할 도메인 설정
               .withSockJS()  // SockJS 사용 설정 (WebSocket을 지원하지 않는 브라우저를 위한 대체 옵션)
               .setStreamBytesLimit(512 * 1024)  // 스트림 바이트 제한 설정
               .setHttpMessageCacheSize(1000)  // HTTP 메시지 캐시 크기 설정
               .setDisconnectDelay(30 * 1000);  // 연결 종료 지연 시간 설정
    }
}