package com.dipanshushukla.realtimechatappmessageservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dipanshushukla.realtimechatappmessageservice.dto.MessageDTO;
import com.dipanshushukla.realtimechatappmessageservice.dto.TypingEventDTO;
import com.dipanshushukla.realtimechatappmessageservice.factory.MessageDataFactory;
import com.dipanshushukla.realtimechatappmessageservice.model.MessageStatus;
import com.dipanshushukla.realtimechatappmessageservice.model.MessageType;
import com.dipanshushukla.realtimechatappmessageservice.redis.RedisMessagePublisher;
import com.dipanshushukla.realtimechatappmessageservice.service.KafkaProducerService;
import com.dipanshushukla.realtimechatappmessageservice.service.ULIDService;
import com.dipanshushukla.realtimechatappmessageservice.service.UserPresenceService;

@ExtendWith(MockitoExtension.class)
class WebSocketChatControllerTest {

    @Mock
    private RedisMessagePublisher redisMessagePublisher;
    @Mock
    private ULIDService ulidService;
    @Mock
    private KafkaProducerService kafkaProducerService;
    @Mock
    private UserPresenceService userPresenceService;

    @InjectMocks
    private WebSocketChatController chatController;

    private MessageDTO messageDTO;
    private final String MOCK_ULID = "01ARZ3NDEKTSV4RRFFQ69G5FAV";

    @BeforeEach
    void setUp() {
        // Create an empty DTO to test the ingress mapping logic
        messageDTO = new MessageDTO();
        messageDTO.setContent("Hello Realtime!");
        messageDTO.setChatRoomId(MessageDataFactory.DEFAULT_CHAT_ROOM_ID);
    }

    @Test
    @DisplayName("sendMessage: Should assign defaults, generate ULID, and publish to Kafka/Redis")
    void sendMessage_AssignsDefaultsAndPublishes() {
        when(ulidService.newIdString()).thenReturn(MOCK_ULID);

        chatController.sendMessage(MessageDataFactory.DEFAULT_SENDER_ID.toString(), messageDTO);

        // Assert Defaults were correctly assigned
        assertEquals(MOCK_ULID, messageDTO.getMessageId());
        assertEquals(MessageDataFactory.DEFAULT_SENDER_ID, messageDTO.getUserId());
        assertNotNull(messageDTO.getTimestamp());
        assertEquals(MessageStatus.SENT, messageDTO.getStatus());
        assertEquals(MessageType.TEXT, messageDTO.getType());
        assertFalse(messageDTO.getEdited());

        // Verify routing to message brokers
        verify(kafkaProducerService).publish(messageDTO);
        verify(redisMessagePublisher).publish("chat-messages", messageDTO);
    }

    @Test
    @DisplayName("handleTyping: Should route event directly to Redis typing channel")
    void handleTyping_PublishesToRedis() {
        TypingEventDTO typingEvent = new TypingEventDTO();
        typingEvent.setChatId(MessageDataFactory.DEFAULT_CHAT_ROOM_ID.toString());
        typingEvent.setUserId(MessageDataFactory.DEFAULT_SENDER_ID.toString());
        typingEvent.setTyping(true);

        chatController.handleTyping(MessageDataFactory.DEFAULT_SENDER_ID.toString(), typingEvent);

        verify(redisMessagePublisher).publish(eq("chat-typing"), eq(typingEvent));
    }

    @Test
    @DisplayName("receiveHeartbeat: Should update user presence status")
    void receiveHeartbeat_UpdatesPresence() {
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn(MessageDataFactory.DEFAULT_SENDER_ID.toString());

        chatController.receiveHeartbeat(mockPrincipal);

        verify(userPresenceService).markUserOnline(MessageDataFactory.DEFAULT_SENDER_ID.toString());
    }

}
