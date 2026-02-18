package com.dipanshushukla.realtimechatappmessageservice.controller;

import java.security.Principal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dipanshushukla.realtimechatappmessageservice.dto.MessageDTO;
import com.dipanshushukla.realtimechatappmessageservice.dto.TypingEventDTO;
import com.dipanshushukla.realtimechatappmessageservice.redis.RedisMessagePublisher;
import com.dipanshushukla.realtimechatappmessageservice.service.KafkaProducerService;
import com.dipanshushukla.realtimechatappmessageservice.service.ULIDService;
import com.dipanshushukla.realtimechatappmessageservice.service.UserPresenceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class WebSocketChatController {

    private final RedisMessagePublisher redisMessagePublisher;
    private final ULIDService ulid;

    private final KafkaProducerService kafkaProducerService;
    private final UserPresenceService userPresenceService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Header("X-User-Id") String userId, MessageDTO dto) {

        UUID senderId = UUID.fromString(userId);

        if (dto.getMessageId() == null || dto.getMessageId().isEmpty()) {
            // assign ID at ingress
            dto.setMessageId(ulid.newIdString());
            dto.setUserId(senderId);
            dto.setTimestamp(Timestamp.from(Instant.now()));
        }

        log.info(dto.toString());

        // Publish to Kafka for asynchronous persistence
        kafkaProducerService.publish(dto);

        // FANOUT ONLY
        redisMessagePublisher.publish("chat-messages", dto);
    }

    @MessageMapping("/chat.typing")
    public void handleTyping(@Header("X-User-Id") String userId, TypingEventDTO event) {
        redisMessagePublisher.publish("chat-typing", event);
    }

    @MessageMapping("/heartbeat")
    public void receiveHeartbeat(Principal principal) {
        userPresenceService.markUserOnline(principal.getName());
    }
}
