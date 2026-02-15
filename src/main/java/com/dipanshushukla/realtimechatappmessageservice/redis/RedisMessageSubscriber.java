package com.dipanshushukla.realtimechatappmessageservice.redis;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.dipanshushukla.realtimechatappmessageservice.dto.MessageDTO;
import com.dipanshushukla.realtimechatappmessageservice.dto.TypingEventDTO;
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
            String channel = new String(message.getChannel());
            if (channel.equals("chat-messages")) {
                MessageDTO dto = objectMapper.readValue(message.getBody(), MessageDTO.class);
                messagingTemplate.convertAndSend("/topic/rooms/" + dto.getChatRoomId(), dto);
            } else if (channel.equals("chat-typing")) {
                TypingEventDTO typingDto = objectMapper.readValue(message.getBody(), TypingEventDTO.class);
                // Send to a sub-topic specifically for typing
                messagingTemplate.convertAndSend("/topic/rooms/" + typingDto.getChatId() + "/typing", typingDto);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
