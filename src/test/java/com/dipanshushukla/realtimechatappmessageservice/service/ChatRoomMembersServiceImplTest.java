package com.dipanshushukla.realtimechatappmessageservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoom;
import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoomMember;
import com.dipanshushukla.realtimechatappmessageservice.entity.User;
import com.dipanshushukla.realtimechatappmessageservice.exception.BadRequestException;
import com.dipanshushukla.realtimechatappmessageservice.factory.MessageDataFactory;
import com.dipanshushukla.realtimechatappmessageservice.repository.ChatRoomMemberRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.ChatRoomRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.UserRepository;
import com.dipanshushukla.realtimechatappmessageservice.service.impl.ChatRoomMembersServiceImpl;

@ExtendWith(MockitoExtension.class)
class ChatRoomMembersServiceImplTest {

	@Mock
	private ChatRoomRepository chatRoomRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private ChatRoomMemberRepository membersRepository;

	@InjectMocks
	private ChatRoomMembersServiceImpl membersService;

	private ChatRoom chatRoom;
	private User requester;
	private User targetUser;
	private ChatRoomMember member;

	@BeforeEach
	void setUp() {
		chatRoom = MessageDataFactory.createValidChatRoom();
		requester = MessageDataFactory.createValidUser();
		targetUser = new User();
		targetUser.setUserId(UUID.randomUUID());
		member = ChatRoomMember.builder().chatRoom(chatRoom).user(targetUser).admin(false).build();
	}

	@Test
	@DisplayName("Cannot unassign yourself as admin")
	void unassignAdmin_Self_ThrowsException() {

		Long chatId = chatRoom.getChatId();
		UUID requesterId = requester.getUserId();

		assertThrows(BadRequestException.class,
				() -> membersService.unassignAdmin(chatId, requesterId, requesterId));
	}

	@Test
	@DisplayName("Cannot unassign the last admin in the chat room")
	void unassignAdmin_LastAdmin_ThrowsException() {
		// Setup requester as an Admin who is trying to remove another user's admin
		// rights,
		// but that target is actually the ONLY admin.
		User anotherAdminRequester = new User();
		anotherAdminRequester.setUserId(UUID.randomUUID());
		ChatRoomMember requesterMembership = ChatRoomMember.builder().admin(true).build();

		member.setAdmin(true); // Target user is currently an admin

		when(chatRoomRepository.findById(chatRoom.getChatId())).thenReturn(Optional.of(chatRoom));
		when(userRepository.findById(anotherAdminRequester.getUserId())).thenReturn(Optional.of(anotherAdminRequester));
		when(userRepository.findById(targetUser.getUserId())).thenReturn(Optional.of(targetUser));
		when(membersRepository.findByChatRoomAndUser(chatRoom, anotherAdminRequester)).thenReturn(requesterMembership); // Requester
																														// is
																														// admin
		when(membersRepository.findByChatRoomAndUser(chatRoom, targetUser)).thenReturn(member); // Target is admin

		// Return a list where ONLY the target user is an admin
		when(membersRepository.findByChatRoom(chatRoom)).thenReturn(List.of(member));

		Long chatId = chatRoom.getChatId();
		UUID targetId = targetUser.getUserId();
		UUID requesterId = anotherAdminRequester.getUserId();

		BadRequestException ex = assertThrows(BadRequestException.class,
				() -> membersService.unassignAdmin(chatId, targetId, requesterId));

		assertEquals("Cannot remove the last admin.", ex.getMessage());
	}

}
