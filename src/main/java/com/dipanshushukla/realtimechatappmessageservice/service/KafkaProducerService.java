package com.dipanshushukla.realtimechatappmessageservice.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.dipanshushukla.realtimechatappmessageservice.dto.MessageDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, MessageDTO> kafkaTemplate;
    private static final String TOPIC = "chat-messages";

    public void publish(MessageDTO dto) {
        Long key = dto.getChatRoomId();

        kafkaTemplate.send(TOPIC, String.valueOf(key), dto);

        log.info("Message sent to Kafka topic.");
    }
}
