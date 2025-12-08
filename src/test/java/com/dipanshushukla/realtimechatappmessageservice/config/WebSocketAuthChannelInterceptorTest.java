package com.dipanshushukla.realtimechatappmessageservice.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import com.dipanshushukla.realtimechatappmessageservice.exception.BadRequestException;
import com.dipanshushukla.realtimechatappmessageservice.factory.MessageDataFactory;
import com.dipanshushukla.realtimechatappmessageservice.service.ChatRoomMembersService;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;

@ExtendWith(MockitoExtension.class)
class WebSocketAuthChannelInterceptorTest {

	@Mock
	private ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

	@Mock
	private ChatRoomMembersService memberService;

	@InjectMocks
	private WebSocketAuthChannelInterceptor interceptor;

	private MessageChannel mockChannel;
	private static final String VALID_TOKEN = "valid.jwt.token";

	@BeforeEach
	void setUp() {
		mockChannel = mock(MessageChannel.class);
	}

	private Message<?> createMockMessage(StompCommand command, Map<String, Object> sessionAttributes) {
		StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
		if (sessionAttributes != null) {
			accessor.setSessionAttributes(sessionAttributes);
		}
		accessor.setLeaveMutable(true);
		return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
	}

	@Test
	@DisplayName("CONNECT: Should block connection if Authorization header is missing")
	void preSend_Connect_MissingAuth_ReturnsNull() {
		Message<?> message = createMockMessage(StompCommand.CONNECT, new HashMap<>());
		Message<?> result = interceptor.preSend(message, mockChannel);
		assertNull(result, "Message should be blocked (null) due to missing Auth header");
	}

	@Test
	@DisplayName("CONNECT: Should allow connection and set session attributes for valid JWT")
	void preSend_Connect_ValidAuth_ReturnsMessage() throws Exception {
		StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
		accessor.setNativeHeader("Authorization", "Bearer " + VALID_TOKEN);
		accessor.setSessionAttributes(new HashMap<>());
		accessor.setLeaveMutable(true);

		Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.claim("userId", MessageDataFactory.DEFAULT_SENDER_ID.toString())
				.claim("username", "testuser")
				.build();

		when(jwtProcessor.process(eq(VALID_TOKEN), any())).thenReturn(claims);

		Message<?> result = interceptor.preSend(message, mockChannel);

		assertNotNull(result);
		StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
		assertEquals(MessageDataFactory.DEFAULT_SENDER_ID.toString(),
				resultAccessor.getSessionAttributes().get("userId"));
		assertNotNull(resultAccessor.getUser());
	}

	@Test
	@DisplayName("SEND: Should block message if session userId is missing")
	void preSend_Send_MissingSessionUser_ReturnsNull() {
		Message<?> message = createMockMessage(StompCommand.SEND, new HashMap<>());
		Message<?> result = interceptor.preSend(message, mockChannel);
		assertNull(result, "SEND should be blocked if user is not authenticated in session");
	}

	@Test
	@DisplayName("SEND: Should append X-User-Id header for valid session")
	void preSend_Send_ValidSession_AppendsHeader() {
		Map<String, Object> attrs = new HashMap<>();
		attrs.put("userId", MessageDataFactory.DEFAULT_SENDER_ID.toString());
		Message<?> message = createMockMessage(StompCommand.SEND, attrs);

		Message<?> result = interceptor.preSend(message, mockChannel);

		assertNotNull(result);
		StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
		assertEquals(MessageDataFactory.DEFAULT_SENDER_ID.toString(), resultAccessor.getFirstNativeHeader("X-User-Id"));
	}

	@Test
	@DisplayName("SUBSCRIBE: Should allow if user is a valid member of the room")
	void preSend_Subscribe_ValidMember_ReturnsMessage() {
		Map<String, Object> attrs = new HashMap<>();
		attrs.put("userId", MessageDataFactory.DEFAULT_SENDER_ID.toString());
		StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
		accessor.setDestination("/topic/rooms/" + MessageDataFactory.DEFAULT_CHAT_ROOM_ID);
		accessor.setSessionAttributes(attrs);
		Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

		Message<?> result = interceptor.preSend(message, mockChannel);

		assertNotNull(result);
		verify(memberService).ensureMember(MessageDataFactory.DEFAULT_CHAT_ROOM_ID,
				MessageDataFactory.DEFAULT_SENDER_ID);
	}

	@Test
	@DisplayName("SUBSCRIBE: Should block if user is NOT a member of the room")
	void preSend_Subscribe_NotAMember_ReturnsNull() {
		Map<String, Object> attrs = new HashMap<>();
		attrs.put("userId", MessageDataFactory.DEFAULT_SENDER_ID.toString());
		StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
		accessor.setDestination("/topic/rooms/" + MessageDataFactory.DEFAULT_CHAT_ROOM_ID);
		accessor.setSessionAttributes(attrs);
		Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

		doThrow(new BadRequestException("Not a member")).when(memberService).ensureMember(any(), any());

		Message<?> result = interceptor.preSend(message, mockChannel);

		assertNull(result, "SUBSCRIBE should be blocked if user fails membership validation");
	}

}
