package com.mstrust.exam.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/* ---------------------------------------------------
 * Cấu hình WebSocket cho real-time communication
 * - STOMP protocol với Simple Broker
 * - SockJS fallback support
 * - Topics: /topic/exam/{examId}, /topic/monitoring
 * - Application prefix: /app
 * @author: K24DTCN210-NVMANH (21/11/2025 01:46)
 * --------------------------------------------------- */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /* ---------------------------------------------------
     * Cấu hình Message Broker cho WebSocket
     * - Simple Broker: /topic (broadcast), /queue (point-to-point)
     * - Application prefix: /app (client gửi message)
     * @param config MessageBrokerRegistry để cấu hình broker
     * @author: K24DTCN210-NVMANH (21/11/2025 01:46)
     * --------------------------------------------------- */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker cho các destination prefix
        config.enableSimpleBroker("/topic", "/queue");
        
        // Set prefix cho messages từ client
        config.setApplicationDestinationPrefixes("/app");
        
        // Set prefix cho user-specific messages (optional)
        config.setUserDestinationPrefix("/user");
    }

    /* ---------------------------------------------------
     * Đăng ký STOMP endpoints
     * - Endpoint: /ws (WebSocket connection)
     * - SockJS fallback cho browsers không support WebSocket
     * - CORS: Allow all origins (cần tùy chỉnh trong production)
     * @param registry StompEndpointRegistry để đăng ký endpoints
     * @author: K24DTCN210-NVMANH (21/11/2025 01:46)
     * --------------------------------------------------- */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Allow all origins, cần restrict trong production
                .withSockJS(); // Enable SockJS fallback
    }
}
