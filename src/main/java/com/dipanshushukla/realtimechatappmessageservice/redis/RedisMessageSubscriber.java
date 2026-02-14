package com.dipanshushukla.realtimechatappmessageservice.redis;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.dipanshushukla.realtimechatappmessageservice.dto.MessageDTO;
import com.dipanshushukla.realtimechatappmessageservice.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisMessageSubscriber implements MessageListener {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {

        try {
            MessageDTO dto = objectMapper.readValue(message.getBody(), MessageDTO.class);

            // broadcast to room
            messagingTemplate.convertAndSend(
                    "/topic/rooms/" + dto.getChatRoomId(),
                    dto);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
