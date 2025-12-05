package com.dipanshushukla.realtimechatappmessageservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.dipanshushukla.realtimechatappmessageservice.dto.ChatRoomDTO;
import com.dipanshushukla.realtimechatappmessageservice.exception.BadRequestException;
import com.dipanshushukla.realtimechatappmessageservice.exception.handler.GlobalExceptionHandler;
import com.dipanshushukla.realtimechatappmessageservice.factory.MessageDataFactory;
import com.dipanshushukla.realtimechatappmessageservice.model.ChatRoomType;
import com.dipanshushukla.realtimechatappmessageservice.service.ChatRoomService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ChatRoomControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ChatRoomService chatRoomService;

    @InjectMocks
    private ChatRoomController chatRoomController;

    private ObjectMapper objectMapper = new ObjectMapper();
    private ChatRoomDTO chatRoomDTO;
    private final String USER_ID_HEADER = "X-User-Id";

    @BeforeEach
    void setUp() {
        // Wire up the controller with the GlobalExceptionHandler to test exception
        // routing
        mockMvc = MockMvcBuilders.standaloneSetup(chatRoomController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        chatRoomDTO = ChatRoomDTO.builder()
                .chatId(MessageDataFactory.DEFAULT_CHAT_ROOM_ID)
                .name("General Chat")
                .type(ChatRoomType.PUBLIC)
                .description("A general discussion room")
                .build();
    }

    @Test
    @DisplayName("POST /api/chat-rooms should return 201 Created")
    void createChatRoom_Success() throws Exception {
        when(chatRoomService.createChatRoom(any(ChatRoomDTO.class), eq(MessageDataFactory.DEFAULT_SENDER_ID)))
                .thenReturn(chatRoomDTO);

        mockMvc.perform(post("/rooms")
                .header(USER_ID_HEADER, MessageDataFactory.DEFAULT_SENDER_ID.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(chatRoomDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("General Chat"));

        verify(chatRoomService).createChatRoom(any(ChatRoomDTO.class), eq(MessageDataFactory.DEFAULT_SENDER_ID));
    }

    @Test
    @DisplayName("GET /api/chat-rooms should return paginated list of chat rooms")
    void getMyChatRooms_Success() throws Exception {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<ChatRoomDTO> pagedResponse = new PageImpl<>(List.of(chatRoomDTO), pageRequest, 1);
        when(chatRoomService.getMyChatRooms(eq(MessageDataFactory.DEFAULT_SENDER_ID), anyInt(), anyInt()))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/rooms/my")
                .header(USER_ID_HEADER, MessageDataFactory.DEFAULT_SENDER_ID.toString())
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].chatId").value(MessageDataFactory.DEFAULT_CHAT_ROOM_ID));
    }

    @Test
    @DisplayName("Should translate BadRequestException to 400 Bad Request via GlobalExceptionHandler")
    void createChatRoom_BadRequest_Returns400() throws Exception {
        when(chatRoomService.createChatRoom(any(ChatRoomDTO.class), eq(MessageDataFactory.DEFAULT_SENDER_ID)))
                .thenThrow(new BadRequestException("Invalid DM configuration"));

        mockMvc.perform(post("/rooms")
                .header(USER_ID_HEADER, MessageDataFactory.DEFAULT_SENDER_ID.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(chatRoomDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid DM configuration"));
    }

}
