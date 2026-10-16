package com.iot.management;
import com.iot.management.config.WebSocketConfig;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
class WebSocketAuthorizationTest {
 ChannelInterceptor interceptor() {
  ChannelRegistration r=mock(ChannelRegistration.class);new WebSocketConfig().configureClientInboundChannel(r);
  ArgumentCaptor<ChannelInterceptor> c=ArgumentCaptor.forClass(ChannelInterceptor.class);verify(r).interceptors(c.capture());return c.getValue();
 }
 org.springframework.messaging.Message<byte[]> message(String role,String destination,StompCommand command) {
  StompHeaderAccessor h=StompHeaderAccessor.create(command);
  if(role!=null)h.setUser(new UsernamePasswordAuthenticationToken("test", "unused",List.of(new SimpleGrantedAuthority(role))));
  h.setDestination(destination);h.setLeaveMutable(true);
  return MessageBuilder.createMessage(new byte[0],h.getMessageHeaders());
 }
 @Test void protectsSubscriptionsAndRejectsClientPublishing() {
  ChannelInterceptor c=interceptor();
  assertThat(c.preSend(message("ROLE_USER","/user/queue/data",StompCommand.SUBSCRIBE),null)).isNotNull();
  assertThat(c.preSend(message("ROLE_ADMIN","/topic/alerts",StompCommand.SUBSCRIBE),null)).isNotNull();
  assertThatThrownBy(() -> c.preSend(message("ROLE_USER","/topic/data",StompCommand.SUBSCRIBE),null)).isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
  assertThatThrownBy(() -> c.preSend(message("ROLE_USER","/queue/data",StompCommand.SUBSCRIBE),null)).isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
  assertThatThrownBy(() -> c.preSend(message(null,"/user/queue/data",StompCommand.SUBSCRIBE),null)).isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
  assertThatThrownBy(() -> c.preSend(message("ROLE_ADMIN","/topic/data",StompCommand.SEND),null)).isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
 }
}
