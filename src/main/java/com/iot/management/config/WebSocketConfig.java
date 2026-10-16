package com.iot.management.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Client kết nối tại  /ws  (SockJS + STOMP), sau đó subscribe:
 *   /topic/data    -> điểm dữ liệu mới cho biểu đồ
 *   /topic/alerts  -> cảnh báo vượt ngưỡng
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
    }
    @Override
    public void configureClientInboundChannel(org.springframework.messaging.simp.config.ChannelRegistration registration) {
        registration.interceptors(new org.springframework.messaging.support.ChannelInterceptor() {
            @Override public org.springframework.messaging.Message<?> preSend(org.springframework.messaging.Message<?> message, org.springframework.messaging.MessageChannel channel) {
                org.springframework.messaging.simp.stomp.StompHeaderAccessor h=org.springframework.messaging.support.MessageHeaderAccessor.getAccessor(message, org.springframework.messaging.simp.stomp.StompHeaderAccessor.class);
                if (h==null) return message;
                if (h.getUser()==null) throw new org.springframework.security.access.AccessDeniedException("Đăng nhập để dùng realtime");
                if (h.getCommand()==org.springframework.messaging.simp.stomp.StompCommand.SEND) throw new org.springframework.security.access.AccessDeniedException("Dùng API để gửi dữ liệu");
                if (h.getCommand()==org.springframework.messaging.simp.stomp.StompCommand.SUBSCRIBE) {
                    String dest=h.getDestination();
                    boolean admin=h.getUser() instanceof org.springframework.security.core.Authentication && ((org.springframework.security.core.Authentication) h.getUser()).getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                    if (!("/user/queue/data".equals(dest) || "/user/queue/alerts".equals(dest) || (admin && ("/topic/data".equals(dest) || "/topic/alerts".equals(dest))))) throw new org.springframework.security.access.AccessDeniedException("Không có quyền xem luồng dữ liệu");
                }
                return message;
            }
        });
    }
}
