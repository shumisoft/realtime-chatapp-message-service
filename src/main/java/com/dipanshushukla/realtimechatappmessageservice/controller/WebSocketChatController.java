package com.dipanshushukla.realtimechatappmessageservice.controller;

import java.util.UUID;

import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dipanshushukla.realtimechatappmessageservice.dto.MessageDTO;
import com.dipanshushukla.realtimechatappmessageservice.redis.RedisPublisher;
import com.dipanshushukla.realtimechatappmessageservice.service.MessageService;
import com.dipanshushukla.realtimechatappmessageservice.service.ULIDService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class WebSocketChatController {

    private final MessageService messageService;
    private final RedisPublisher redisPublisher;
    private final ULIDService ulid;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Header("X-User-Id") String userId, MessageDTO dto) {

        UUID senderId = UUID.fromString(userId);

        // assign ID at ingress
        dto.setMessageId(ulid.newId());
        dto.setUserId(senderId);

        // persist async = enqueue
        redisPublisher.publish("chat-messages", dto);
    }
}
