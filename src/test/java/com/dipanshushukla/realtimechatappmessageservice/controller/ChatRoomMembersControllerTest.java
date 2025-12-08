package com.dipanshushukla.realtimechatappmessageservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.dipanshushukla.realtimechatappmessageservice.dto.ChatRoomMembersDTO;
import com.dipanshushukla.realtimechatappmessageservice.dto.UserDTO;
import com.dipanshushukla.realtimechatappmessageservice.exception.handler.GlobalExceptionHandler;
import com.dipanshushukla.realtimechatappmessageservice.factory.MessageDataFactory;
import com.dipanshushukla.realtimechatappmessageservice.service.ChatRoomMembersService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ChatRoomMembersControllerTest {
	private MockMvc mockMvc;

	@Mock
	private ChatRoomMembersService membersService;

	@InjectMocks
	private ChatRoomMembersController membersController;

	private ObjectMapper objectMapper = new ObjectMapper();
	private static final String USER_ID_HEADER = "X-User-Id";
	private UUID requesterId = MessageDataFactory.DEFAULT_SENDER_ID;
	private Long chatId = MessageDataFactory.DEFAULT_CHAT_ROOM_ID;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(membersController)
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	@DisplayName("GET /rooms/{chatId}/members should return 200 OK with member list")
	void getMembers_Success() throws Exception {
		// Correctly nesting UserDTO inside ChatRoomMembersDTO
		UserDTO userDto = UserDTO.builder()
				.userId(requesterId)
				.username("test_user")
				.build();

		ChatRoomMembersDTO member = ChatRoomMembersDTO.builder()
				.chatId(chatId)
				.userId(requesterId)
				.user(userDto)
				.admin(true)
				.build();

		when(membersService.getMembers(chatId, requesterId)).thenReturn(List.of(member));

		mockMvc.perform(get("/rooms/{chatId}/members", chatId)
				.header(USER_ID_HEADER, requesterId.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].userId").value(requesterId.toString()))
				.andExpect(jsonPath("$[0].user.username").value("test_user"))
				.andExpect(jsonPath("$[0].admin").value(true));
	}

	@Test
	@DisplayName("POST /rooms/members should return 200 OK")
	void addMember_Success() throws Exception {
		// Matching the @NotNull userId requirement
		ChatRoomMembersDTO dto = ChatRoomMembersDTO.builder()
				.userId(UUID.randomUUID())
				.build();

		mockMvc.perform(post("/rooms/{chatId}/members", chatId)
				.header(USER_ID_HEADER, requesterId.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.message").value("Member added."));

		verify(membersService).addMember(any(ChatRoomMembersDTO.class), eq(requesterId));
	}

	@Test
	@DisplayName("DELETE /rooms/{chatId}/members/{userId} should return 200 OK")
	void removeMember_Success() throws Exception {
		UUID targetUser = UUID.randomUUID();

		// Testing the actual removeMember endpoint you have defined
		mockMvc.perform(delete("/rooms/{chatId}/members/{userId}", chatId, targetUser)
				.header(USER_ID_HEADER, requesterId.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Member removed."));

		verify(membersService).removeMember(chatId, targetUser, requesterId);
	}
}
