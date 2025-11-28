package com.dipanshushukla.realtimechatappmessageservice.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoom;
import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoomMember;
import com.dipanshushukla.realtimechatappmessageservice.entity.User;
import com.dipanshushukla.realtimechatappmessageservice.factory.MessageDataFactory;
import com.dipanshushukla.realtimechatappmessageservice.model.ChatRoomMemberId;

@DataJpaTest
class ChatRoomMemberRepositoryTest {

    @Autowired
    private ChatRoomMemberRepository memberRepository;
    @Autowired
    private ChatRoomRepository chatRoomRepository;
    @Autowired
    private UserRepository userRepository;

    private ChatRoom savedRoom;
    private User savedUser;

    @BeforeEach
    void setUp() {
        // We MUST save the parent entities first to avoid Foreign Key Constraint
        // violations!
        ChatRoom room = MessageDataFactory.createValidChatRoom();
        room.setChatId(null);
        savedRoom = chatRoomRepository.save(room);

        User user = MessageDataFactory.createValidUser();
        savedUser = userRepository.save(user);
    }

    @Test
    @DisplayName("Should successfully map composite key and relationships for a member")
    void saveAndFindMember() {
        // Arrange
        ChatRoomMemberId id = new ChatRoomMemberId(savedRoom.getChatId(), savedUser.getUserId());
        ChatRoomMember member = ChatRoomMember.builder()
                .chatRoomMemberId(id)
                .chatRoom(savedRoom)
                .user(savedUser)
                .admin(true)
                .build();

        // Act
        memberRepository.save(member);

        // Assert relationships and custom query methods
        boolean isMember = memberRepository.existsByChatRoomAndUser(savedRoom, savedUser);
        assertTrue(isMember);

        List<ChatRoomMember> roomMembers = memberRepository.findByChatRoom(savedRoom);
        assertEquals(1, roomMembers.size());
        assertTrue(roomMembers.get(0).isAdmin());
        assertNotNull(roomMembers.get(0).getChatRoomMemberId());
    }

}
