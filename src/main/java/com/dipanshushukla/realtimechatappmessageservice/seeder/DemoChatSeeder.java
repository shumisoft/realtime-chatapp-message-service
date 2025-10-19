package com.dipanshushukla.realtimechatappmessageservice.seeder;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;

import com.dipanshushukla.realtimechatappmessageservice.dto.MessageDTO;
import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoom;
import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoomMember;
import com.dipanshushukla.realtimechatappmessageservice.entity.User;
import com.dipanshushukla.realtimechatappmessageservice.model.ChatRoomMemberId;
import com.dipanshushukla.realtimechatappmessageservice.model.ChatRoomType;
import com.dipanshushukla.realtimechatappmessageservice.repository.ChatRoomMemberRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.ChatRoomRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.UserRepository;
import com.dipanshushukla.realtimechatappmessageservice.service.MessageService;
import com.dipanshushukla.realtimechatappmessageservice.service.ULIDService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@Order(4)
@RequiredArgsConstructor
public class DemoChatSeeder implements ApplicationRunner {

	private final UserRepository userRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final ChatRoomMemberRepository memberRepository;
	private final MessageService messageService;
	private final ULIDService ulidService;

	private static final String DEMO_USER_1 = "demo-user-1";
	private static final String DEMO_USER_2 = "demo-user-2";
	private static final String DEMO_DM_ROOM = "DM_demo-user-1_demo-user-2";
	private static final String DEMO_GROUP_ROOM = "Demo Group";

	private static final List<String> DEMO_CONVERSATION = List.of(
			"Hey! Welcome to the demo chat.",
			"Thanks! This app looks really clean.",
			"Glad you like it. Real-time messaging with WebSockets.",
			"Nice. How's the latency?",
			"Pretty solid. Messages arrive instantly.",
			"What about message history? Does it persist?",
			"Yes, everything is stored and loaded on open.",
			"That's great. What tech stack is this?",
			"Spring Boot microservices on the backend.",
			"And the frontend?",
			"Angular. Connects via STOMP over WebSocket.",
			"Impressive setup for a chat app.",
			"Thanks, took a while to get right.",
			"I can imagine. Auth service too?",
			"Yes, JWT-based. Separate microservice.",
			"Clean architecture.",
			"That was the goal. Each service does one thing.",
			"Makes scaling easier too.",
			"Exactly. Anyway, feel free to explore!",
			"Will do. Thanks for the walkthrough.");

	@Override
	public void run(ApplicationArguments args) {
		log.info("[Seeder] DemoChatSeeder started!");

		User demoUser1 = getUser(DEMO_USER_1);
		User demoUser2 = getUser(DEMO_USER_2);

		seedDMChatRoom(demoUser1, demoUser2);
		seedDemoGroup(demoUser1, demoUser2);

		log.info("[Seeder] DemoChatSeeder started");
	}

	private void seedDMChatRoom(User user1, User user2) {
		if (chatRoomRepository.existsByName(DEMO_DM_ROOM)) {
			log.info("[Seeder] Demo DM room already exists. Skipping.");
			return;
		}

		ChatRoom chatRoom = chatRoomRepository.save(ChatRoom.builder()
				.name(DEMO_DM_ROOM)
				.type(ChatRoomType.DIRECT_MESSAGE)
				.build());

		saveMembership(chatRoom, user1, false);
		saveMembership(chatRoom, user2, false);

		log.info("[Seeder] ✔ Created demo DM room: {}", DEMO_DM_ROOM);

		for (int i = 0; i < DEMO_CONVERSATION.size(); i++) {
			User sender = (i % 2 == 0) ? user1 : user2;
			createMessage(chatRoom.getChatId(), sender, DEMO_CONVERSATION.get(i));
			sleep(50);
		}

		log.info("[Seeder] ✔ Seeded {} demo DM messages", DEMO_CONVERSATION.size());
	}

	private void seedDemoGroup(User user1, User user2) {
		if (chatRoomRepository.existsByName(DEMO_GROUP_ROOM)) {
			log.info("[Seeder] Demo group room already exists. Skipping.");
			return;
		}

		ChatRoom group = chatRoomRepository.save(ChatRoom.builder()
				.name(DEMO_GROUP_ROOM)
				.type(ChatRoomType.PRIVATE)
				.description("Demo group for showcasing group chat features.")
				.build());

		saveMembership(group, user1, true); // user1 is admin
		saveMembership(group, user2, false);
		log.info("[Seeder] ✔ Created demo group room: {}", DEMO_GROUP_ROOM);

		createMessage(group.getChatId(), user1,
				"Welcome to the Demo Group! I'm " + user1.getUsername() + ", the admin here.");
		createMessage(group.getChatId(), user2,
				"Hey! I'm " + user2.getUsername() + ". Happy to be here.");
		log.info("[Seeder] ✔ Seeded demo group messages");
	}

	private User getUser(String username) {
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new IllegalStateException(
						"[Seeder] Required demo user not found: " + username));
	}

	private void saveMembership(ChatRoom room, User user, boolean isAdmin) {
		memberRepository.save(ChatRoomMember.builder()
				.chatRoomMemberId(new ChatRoomMemberId(room.getChatId(), user.getUserId()))
				.chatRoom(room)
				.user(user)
				.admin(isAdmin)
				.build());

		log.info("[Seeder] ✔ Added {} to {}{}",
				user.getUsername(), room.getName(), isAdmin ? " (ADMIN)" : "");
	}

	private void createMessage(Long chatRoomId, User sender, String content) {
		MessageDTO dto = new MessageDTO();
		dto.setMessageId(ulidService.newIdString());
		dto.setChatRoomId(chatRoomId);
		dto.setUserId(sender.getUserId());
		dto.setContent(content);
		dto.setTimestamp(Timestamp.from(Instant.now()));
		messageService.createMessage(dto, sender.getUserId());
	}

	private void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

}
