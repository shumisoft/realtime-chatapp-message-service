package com.dipanshushukla.realtimechatappmessageservice.config;

import java.security.Principal;
import java.util.UUID;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import com.dipanshushukla.realtimechatappmessageservice.service.ChatRoomMembersService;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;
    private final ChatRoomMembersService memberService;

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
                accessor.setUser((Principal) () -> userId);

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

        // ---------------------------
        // 3) HANDLE SUBSCRIBE FRAME (Add this)
        // ---------------------------
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            String userId = (String) accessor.getSessionAttributes().get("userId");

            if (destination != null && destination.startsWith("/topic/rooms/")) {
                String[] parts = destination.split("/");
                String roomIdStr = parts[3];

                try {
                    Long roomUUID = Long.parseLong(roomIdStr);
                    UUID userUUID = UUID.fromString(userId);

                    memberService.ensureMember(roomUUID, userUUID);

                    log.info("Subscription authorized for user {} to room {}", userId, roomIdStr);
                } catch (Exception e) {
                    log.error("Invalid room ID or subscription format: {}", destination);
                    return null;
                }
            }
        }

        return message;
    }

}
