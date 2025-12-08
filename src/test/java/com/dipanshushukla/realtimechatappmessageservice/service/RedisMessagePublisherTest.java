package com.dipanshushukla.realtimechatappmessageservice.service;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import com.dipanshushukla.realtimechatappmessageservice.redis.RedisMessagePublisher;

@ExtendWith(MockitoExtension.class)
class RedisMessagePublisherTest {

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	@InjectMocks
	private RedisMessagePublisher redisMessagePublisher;

	@Test
	@DisplayName("Should successfully convert and send message to Redis channel")
	void publish_ShouldSendToRedisChannel() {
		// Arrange
		String targetChannel = "chat_room_100";
		Object mockMessage = new Object();

		// Act
		redisMessagePublisher.publish(targetChannel, mockMessage);

		// Assert
		verify(redisTemplate).convertAndSend(targetChannel, mockMessage);
	}

}
