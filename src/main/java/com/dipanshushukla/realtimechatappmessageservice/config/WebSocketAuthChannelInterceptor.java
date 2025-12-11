package com.dipanshushukla.realtimechatappmessageservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

@Configuration
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String userId = accessor.getFirstNativeHeader("X-User-Id");
            accessor.getSessionAttributes().put("userId", userId);
        }

        if (StompCommand.SEND.equals(accessor.getCommand())) {
            String userId = (String) accessor.getSessionAttributes().get("userId");
            accessor.addNativeHeader("X-User-Id", userId);
        }

        return message;
    }
}
