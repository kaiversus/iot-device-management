package com.iot.management.config;
import org.springframework.context.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
/** Optional Windows workaround: NIO2 uses IOCP instead of selector wakeup Unix sockets. */
@Configuration @ConditionalOnProperty(name="app.tomcat.nio2",havingValue="true")
public class LocalServerConfig {
 @Bean WebServerFactoryCustomizer<TomcatServletWebServerFactory> nio2() {
  return factory -> factory.setProtocol("org.apache.coyote.http11.Http11Nio2Protocol");
 }
}
