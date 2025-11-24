package com.dipanshushukla.realtimechatappmessageservice.factory;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import com.dipanshushukla.realtimechatappmessageservice.dto.MessageDTO;
import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoom;
import com.dipanshushukla.realtimechatappmessageservice.entity.Message;
import com.dipanshushukla.realtimechatappmessageservice.entity.User;
import com.dipanshushukla.realtimechatappmessageservice.model.MessageStatus;
import com.dipanshushukla.realtimechatappmessageservice.model.MessageType;

public class MessageDataFactory {
    public static final String DEFAULT_MESSAGE_ID = "01ARZ3NDEKTSV4RRFFQ69G5FAV"; // Sample ULID
    public static final Long DEFAULT_CHAT_ROOM_ID = 100L;
    public static final UUID DEFAULT_SENDER_ID = UUID.randomUUID();
    public static final String DEFAULT_CONTENT = "Test WebSocket Message";
    public static final Timestamp DEFAULT_TIMESTAMP = Timestamp.from(Instant.now());

    public static ChatRoom createValidChatRoom() {
        return ChatRoom.builder()
                .chatId(DEFAULT_CHAT_ROOM_ID)
                .name("Test Chat")
                .build();
    }

    public static User createValidUser() {
        return User.builder()
                .userId(DEFAULT_SENDER_ID)
                .username("test_user")
                .email("test@example.com")
                .build();
    }

    public static Message createValidMessage(ChatRoom chatRoom, User user) {
        return Message.builder()
                .messageId(DEFAULT_MESSAGE_ID)
                .chatRoom(chatRoom)
                .user(user)
                .content(DEFAULT_CONTENT)
                .timestamp(DEFAULT_TIMESTAMP)
                .type(MessageType.TEXT)
                .status(MessageStatus.SENT)
                .edited(false)
                .build();
    }

    public static MessageDTO createValidMessageDTO() {
        return MessageDTO.builder()
                .messageId(DEFAULT_MESSAGE_ID)
                .chatRoomId(DEFAULT_CHAT_ROOM_ID)
                .userId(DEFAULT_SENDER_ID)
                .content(DEFAULT_CONTENT)
                .timestamp(DEFAULT_TIMESTAMP)
                .type(MessageType.TEXT)
                .status(MessageStatus.SENT)
                .edited(false)
                .build();
    }

}
