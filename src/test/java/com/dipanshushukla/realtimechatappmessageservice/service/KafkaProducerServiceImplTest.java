package com.dipanshushukla.realtimechatappmessageservice.service;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.dipanshushukla.realtimechatappmessageservice.dto.MessageDTO;
import com.dipanshushukla.realtimechatappmessageservice.factory.MessageDataFactory;
import com.dipanshushukla.realtimechatappmessageservice.service.impl.KafkaProducerServiceImpl;

@ExtendWith(MockitoExtension.class)
class KafkaProducerServiceImplTest {

    @Mock
    private KafkaTemplate<String, MessageDTO> kafkaTemplate;

    @InjectMocks
    private KafkaProducerServiceImpl kafkaProducerService;

    private MessageDTO mockMessageDTO;

    @BeforeEach
    void setUp() {
        mockMessageDTO = MessageDataFactory.createValidMessageDTO();
    }

    @Test
    @DisplayName("Should successfully publish MessageDTO to Kafka with ChatRoomId as Key")
    void publish_ShouldSendToKafkaTopic() {
        // Act
        kafkaProducerService.publish(mockMessageDTO);

        // Assert: Verify it uses the correct topic, parses the Long ID to String for
        // the key, and passes the DTO
        verify(kafkaTemplate).send("chat-messages", String.valueOf(MessageDataFactory.DEFAULT_CHAT_ROOM_ID),
                mockMessageDTO);
    }

}
