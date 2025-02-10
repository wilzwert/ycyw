package com.openclassrooms.ycyw.configuration;


import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

/**
 * @author Wilhelm Zwertvaegher
 * Date:06/02/2025
 * Time:10:53
 */

@Configuration
@EnableWebSocketSecurity
public class WebSocketSecurityConfiguration {

    @Bean
    AuthorizationManager<Message<?>> messageAuthorizationManager(MessageMatcherDelegatingAuthorizationManager.Builder messages) {
        messages
                .simpDestMatchers("/topic/support").hasRole("SUPPORT")
                .simpSubscribeDestMatchers("/topic/support").hasRole("SUPPORT")
                /*.simpSubscribeDestMatchers("/queue/messages").permitAll()
                .simpSubscribeDestMatchers("/user/queue/messages").permitAll()*/
                .anyMessage().permitAll();


        return messages.build();
    }

    /**
     *
     * @param context the app context
     * @return the ChannelInterceptor to disable CSRF checks on Websockets connections for this POC
     */
    @Bean
    public ChannelInterceptor csrfChannelInterceptor(ApplicationContext context) {
        return new ChannelInterceptor() {};
    }
}
