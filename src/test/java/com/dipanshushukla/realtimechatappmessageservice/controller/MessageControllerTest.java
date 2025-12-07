package com.dipanshushukla.realtimechatappmessageservice.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.dipanshushukla.realtimechatappmessageservice.dto.MessageDTO;
import com.dipanshushukla.realtimechatappmessageservice.exception.ResourceNotFoundException;
import com.dipanshushukla.realtimechatappmessageservice.exception.handler.GlobalExceptionHandler;
import com.dipanshushukla.realtimechatappmessageservice.factory.MessageDataFactory;
import com.dipanshushukla.realtimechatappmessageservice.service.MessageService;

@ExtendWith(MockitoExtension.class)
class MessageControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private MessageController messageController;

    private MessageDTO messageDTO;
    private final String USER_ID_HEADER = "X-User-Id";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(messageController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        messageDTO = MessageDataFactory.createValidMessageDTO();
    }

    @Test
    @DisplayName("GET /api/messages/{chatId} should return 200 OK with paginated messages")
    void getMessagesFromChatRoom_Success() throws Exception {

        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<MessageDTO> pagedResponse = new PageImpl<>(List.of(messageDTO), pageRequest, 1);

        when(messageService.getMessagesFromChatRoom(eq(MessageDataFactory.DEFAULT_CHAT_ROOM_ID),
                eq(MessageDataFactory.DEFAULT_SENDER_ID), anyInt(), anyInt()))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/rooms/{chatId}/messages", MessageDataFactory.DEFAULT_CHAT_ROOM_ID)
                .header(USER_ID_HEADER, MessageDataFactory.DEFAULT_SENDER_ID.toString())
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].messageId").value(MessageDataFactory.DEFAULT_MESSAGE_ID))
                .andExpect(jsonPath("$.content[0].content").value(MessageDataFactory.DEFAULT_CONTENT));
    }

    @Test
    @DisplayName("GET /messages/{messageId} should return 404 when message not found")
    void getMessage_NotFound_Returns404() throws Exception {
        String invalidMessageId = "INVALID_ID";

        when(messageService.getMessage(eq(invalidMessageId), eq(MessageDataFactory.DEFAULT_SENDER_ID)))
                .thenThrow(new ResourceNotFoundException("Message not found"));

        // FIX 1: URL path corrected to single slash
        mockMvc.perform(get("/messages/{messageId}", invalidMessageId)
                .header(USER_ID_HEADER, MessageDataFactory.DEFAULT_SENDER_ID.toString()))
                .andExpect(status().isNotFound())
                // FIX 2: Assert against JSON field "message" instead of raw response string
                .andExpect(jsonPath("$.message").value("Message not found"));
    }

}
