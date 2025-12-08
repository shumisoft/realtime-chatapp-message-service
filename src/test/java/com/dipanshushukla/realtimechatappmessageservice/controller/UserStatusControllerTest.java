package com.dipanshushukla.realtimechatappmessageservice.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
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

import com.dipanshushukla.realtimechatappmessageservice.dto.UserStatusDTO;
import com.dipanshushukla.realtimechatappmessageservice.exception.handler.GlobalExceptionHandler;
import com.dipanshushukla.realtimechatappmessageservice.model.OnlineStatusType;
import com.dipanshushukla.realtimechatappmessageservice.service.UserPresenceService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class UserStatusControllerTest {

	private MockMvc mockMvc;

	@Mock
	private UserPresenceService userPresenceService;

	@InjectMocks
	private UserStatusController userStatusController;

	private ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void setUp() {
		// Wire up standalone setup with the global exception handler
		mockMvc = MockMvcBuilders.standaloneSetup(userStatusController)
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	@DisplayName("POST /user-status/batch should return map of user statuses")
	void getUserStatuses_Success() throws Exception {
		// Arrange
		String userId = UUID.randomUUID().toString();
		List<String> requestPayload = List.of(userId);

		UserStatusDTO statusDto = UserStatusDTO.builder()
				.userId(userId)
				.status(OnlineStatusType.ONLINE)
				.build();

		// Mock the exact method call present in the controller
		when(userPresenceService.getUserStatuses(requestPayload)).thenReturn(Map.of(userId, statusDto));

		// Act & Assert
		mockMvc.perform(post("/user-status/batch")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestPayload)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.['" + userId + "'].status").value("ONLINE"));

		// Verify the service was called properly
		verify(userPresenceService).getUserStatuses(requestPayload);
	}

}
