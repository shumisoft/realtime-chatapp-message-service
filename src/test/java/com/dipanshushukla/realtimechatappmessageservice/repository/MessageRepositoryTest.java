package com.dipanshushukla.realtimechatappmessageservice.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoom;
import com.dipanshushukla.realtimechatappmessageservice.entity.Message;
import com.dipanshushukla.realtimechatappmessageservice.entity.User;
import com.dipanshushukla.realtimechatappmessageservice.factory.MessageDataFactory;

@DataJpaTest
class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private ChatRoomRepository chatRoomRepository;
    @Autowired
    private UserRepository userRepository;

    private ChatRoom savedRoom;
    private User savedUser;

    @BeforeEach
    void setUp() {
        // Save relationships first
        ChatRoom room = MessageDataFactory.createValidChatRoom();
        room.setChatId(null);
        savedRoom = chatRoomRepository.save(room);

        User user = MessageDataFactory.createValidUser();
        savedUser = userRepository.save(user);
    }

    @Test
    @DisplayName("Should save message and fetch it paginated by ChatRoom")
    void saveAndFetchPaginatedMessages() {
        // Arrange
        Message message = MessageDataFactory.createValidMessage(savedRoom, savedUser);
        messageRepository.save(message);

        PageRequest pageRequest = PageRequest.of(0, 10);

        // Act
        // Utilizing the exact custom query from your MessageService!
        Page<Message> pagedMessages = messageRepository.findByChatRoomOrderByTimestampDesc(savedRoom, pageRequest);

        // Assert
        assertNotNull(pagedMessages);
        assertEquals(1, pagedMessages.getTotalElements());
        assertEquals(MessageDataFactory.DEFAULT_CONTENT, pagedMessages.getContent().get(0).getContent());
        assertEquals(savedRoom.getChatId(), pagedMessages.getContent().get(0).getChatRoom().getChatId());
    }

}
