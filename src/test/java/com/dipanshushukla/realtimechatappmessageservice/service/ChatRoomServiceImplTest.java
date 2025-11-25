package com.dipanshushukla.realtimechatappmessageservice.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dipanshushukla.realtimechatappmessageservice.dto.ChatRoomDTO;
import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoom;
import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoomMember;
import com.dipanshushukla.realtimechatappmessageservice.entity.User;
import com.dipanshushukla.realtimechatappmessageservice.exception.BadRequestException;
import com.dipanshushukla.realtimechatappmessageservice.factory.MessageDataFactory;
import com.dipanshushukla.realtimechatappmessageservice.model.ChatRoomType;
import com.dipanshushukla.realtimechatappmessageservice.repository.ChatRoomMemberRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.ChatRoomRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.UserRepository;
import com.dipanshushukla.realtimechatappmessageservice.service.impl.ChatRoomServiceImpl;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceImplTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ChatRoomMemberRepository memberRepository;

    @InjectMocks
    private ChatRoomServiceImpl chatRoomService;

    private User creator;
    private ChatRoom chatRoom;

    @BeforeEach
    void setUp() {
        creator = MessageDataFactory.createValidUser();
        chatRoom = MessageDataFactory.createValidChatRoom();
    }

    @Test
    @DisplayName("Should create a Direct Message and add both members")
    void createChatRoom_DM_Success() {
        UUID recipientId = UUID.randomUUID();
        User recipient = new User();
        recipient.setUserId(recipientId);

        ChatRoomDTO requestDto = ChatRoomDTO.builder().type(ChatRoomType.DIRECT_MESSAGE).memberIds(Set.of(recipientId))
                .build();

        when(chatRoomRepository.findExistingDirectMessage(creator.getUserId(), recipientId))
                .thenReturn(Optional.empty());
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(chatRoom);
        when(userRepository.findById(creator.getUserId())).thenReturn(Optional.of(creator));
        when(userRepository.findById(recipientId)).thenReturn(Optional.of(recipient));

        ChatRoomDTO result = chatRoomService.createChatRoom(requestDto, creator.getUserId());

        assertNotNull(result);
        verify(chatRoomRepository).save(any(ChatRoom.class));

        verify(memberRepository, times(2)).save(any(ChatRoomMember.class)); // Verifies member saves occurred
    }

    @Test
    @DisplayName("Should prevent deletion of Direct Message rooms")
    void deleteChatRoom_DM_ThrowsException() {
        chatRoom.setType(ChatRoomType.DIRECT_MESSAGE);
        ChatRoomMember adminMember = ChatRoomMember.builder().admin(true).build();

        when(userRepository.findById(creator.getUserId())).thenReturn(Optional.of(creator));
        when(chatRoomRepository.findById(chatRoom.getChatId())).thenReturn(Optional.of(chatRoom));
        when(memberRepository.findByChatRoomAndUser(chatRoom, creator)).thenReturn(adminMember);

        assertThrows(BadRequestException.class,
                () -> chatRoomService.deleteChatRoom(chatRoom.getChatId(), creator.getUserId()));
    }

}
