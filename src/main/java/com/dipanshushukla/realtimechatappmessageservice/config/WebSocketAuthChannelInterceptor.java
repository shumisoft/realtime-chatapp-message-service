package com.dipanshushukla.realtimechatappmessageservice.config;

import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null)
            return message;

        // ---------------------------
        // 1) HANDLE CONNECT FRAME
        // ---------------------------
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String authHeader = accessor.getFirstNativeHeader("Authorization");
            log.info("CONNECT received. Authorization Header={}", authHeader);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header. Rejecting connection.");
                return null; // BLOCK CONNECT
            }

            String token = authHeader.substring(7);

            try {
                // Validate the JWT signature via JWKS
                var jwt = jwtProcessor.process(token, null);

                String userId = jwt.getStringClaim("userId");
                String username = jwt.getStringClaim("username");

                log.info("JWT valid. userId={}, username={}", userId, username);

                // Set Principal so @MessageMapping can access it
                accessor.setUser((Principal) () -> username);

                // Store userId for later SEND frames if needed
                accessor.getSessionAttributes().put("userId", userId);

            } catch (Exception e) {
                log.error("Invalid JWT token: {}", e.getMessage());
                return null; // Reject connection
            }
        }

        // ---------------------------
        // 2) HANDLE SEND FRAME
        // ---------------------------
        if (StompCommand.SEND.equals(accessor.getCommand())) {

            Object userId = accessor.getSessionAttributes().get("userId");

            if (userId == null) {
                log.warn("SEND received with no authenticated user. Rejecting message.");
                return null; // block message
            }

            // Optionally forward X-User-Id to backend consumers
            accessor.addNativeHeader("X-User-Id", userId.toString());
        }

        return message;
    }
}
