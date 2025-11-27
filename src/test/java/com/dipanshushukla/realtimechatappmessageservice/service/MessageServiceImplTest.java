package com.dipanshushukla.realtimechatappmessageservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

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

import com.dipanshushukla.realtimechatappmessageservice.dto.MessageDTO;
import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoom;
import com.dipanshushukla.realtimechatappmessageservice.entity.Message;
import com.dipanshushukla.realtimechatappmessageservice.entity.User;
import com.dipanshushukla.realtimechatappmessageservice.exception.BadRequestException;
import com.dipanshushukla.realtimechatappmessageservice.factory.MessageDataFactory;
import com.dipanshushukla.realtimechatappmessageservice.model.ChatRoomType;
import com.dipanshushukla.realtimechatappmessageservice.model.MessageStatus;
import com.dipanshushukla.realtimechatappmessageservice.repository.ChatRoomMemberRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.ChatRoomRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.MessageRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.UserRepository;
import com.dipanshushukla.realtimechatappmessageservice.service.impl.MessageServiceImpl;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @Mock
    private MessageRepository messageRepository;
    @Mock
    private ChatRoomRepository chatRoomRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ChatRoomMemberRepository chatRoomMembersRepository;

    @InjectMocks
    private MessageServiceImpl messageService;

    private User user;
    private ChatRoom chatRoom;
    private Message message;
    private MessageDTO messageDTO;

    @BeforeEach
    void setUp() {
        user = MessageDataFactory.createValidUser();
        chatRoom = MessageDataFactory.createValidChatRoom();
        message = MessageDataFactory.createValidMessage(chatRoom, user);
        messageDTO = MessageDataFactory.createValidMessageDTO();
    }

    private void mockEnsureMemberSuccess() {
        when(chatRoomRepository.findById(MessageDataFactory.DEFAULT_CHAT_ROOM_ID)).thenReturn(Optional.of(chatRoom));
        when(userRepository.findById(MessageDataFactory.DEFAULT_SENDER_ID)).thenReturn(Optional.of(user));
        when(chatRoomMembersRepository.existsByChatRoomAndUser(chatRoom, user)).thenReturn(true);
    }

    @Test
    @DisplayName("Should successfully create a message if user is a member")
    void createMessage_Success() {
        mockEnsureMemberSuccess();
        when(messageRepository.save(any(Message.class))).thenReturn(message);

        MessageDTO result = messageService.createMessage(messageDTO, MessageDataFactory.DEFAULT_SENDER_ID);

        assertNotNull(result);
        assertEquals(MessageDataFactory.DEFAULT_MESSAGE_ID, result.getMessageId());
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException if user is not a member of the room")
    void createMessage_NotMember_ThrowsException() {
        when(chatRoomRepository.findById(MessageDataFactory.DEFAULT_CHAT_ROOM_ID)).thenReturn(Optional.of(chatRoom));
        when(userRepository.findById(MessageDataFactory.DEFAULT_SENDER_ID)).thenReturn(Optional.of(user));
        when(chatRoomMembersRepository.existsByChatRoomAndUser(chatRoom, user)).thenReturn(false);

        assertThrows(BadRequestException.class,
                () -> messageService.createMessage(messageDTO, MessageDataFactory.DEFAULT_SENDER_ID));
    }

    @Test
    @DisplayName("Should mark messages as read when fetching from a Direct Message room")
    void getMessagesFromChatRoom_DM_MarksAsRead() {
        mockEnsureMemberSuccess();
        chatRoom.setType(ChatRoomType.DIRECT_MESSAGE);
        Page<Message> pagedMessages = new PageImpl<>(List.of(message));

        when(messageRepository.findByChatRoomOrderByTimestampDesc(eq(chatRoom), any(PageRequest.class)))
                .thenReturn(pagedMessages);

        Page<MessageDTO> result = messageService.getMessagesFromChatRoom(MessageDataFactory.DEFAULT_CHAT_ROOM_ID,
                MessageDataFactory.DEFAULT_SENDER_ID, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(messageRepository).markMessagesAsRead(MessageDataFactory.DEFAULT_CHAT_ROOM_ID,
                MessageDataFactory.DEFAULT_SENDER_ID);
    }

    @Test
    @DisplayName("Should update status to READ if requester is the owner")
    void updateMessageStatus_Success() {
        when(messageRepository.findById(MessageDataFactory.DEFAULT_MESSAGE_ID)).thenReturn(Optional.of(message));

        messageService.updateMessageStatus(MessageDataFactory.DEFAULT_MESSAGE_ID, MessageDataFactory.DEFAULT_SENDER_ID);

        assertEquals(MessageStatus.READ, message.getStatus());
        verify(messageRepository).save(message);
    }

}
