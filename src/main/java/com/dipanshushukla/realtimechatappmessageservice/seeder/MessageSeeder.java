package com.dipanshushukla.realtimechatappmessageservice.seeder;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;

import com.dipanshushukla.realtimechatappmessageservice.dto.MessageDTO;
import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoom;
import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoomMember;
import com.dipanshushukla.realtimechatappmessageservice.entity.User;
import com.dipanshushukla.realtimechatappmessageservice.repository.ChatRoomMemberRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.ChatRoomRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.MessageRepository;
import com.dipanshushukla.realtimechatappmessageservice.service.MessageService;
import com.dipanshushukla.realtimechatappmessageservice.service.ULIDService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@Order(3)
@RequiredArgsConstructor
public class MessageSeeder implements ApplicationRunner {

  private final ChatRoomRepository chatRoomRepository;
  private final ChatRoomMemberRepository membersRepository;

  private final MessageService messageService;
  private final MessageRepository messageRepository;

  private final ULIDService ulidService;

  @Override
  public void run(ApplicationArguments args) throws Exception {

    log.info("[Seeder] MessageSeeder is running...");

    if (messageRepository.count() > 0) {
      log.info("[Seeder] MessageSeeder skipped (messages already exist)");
      return;
    }

    log.info("[Seeder] MessageSeeder started...");

    seedDMMessages();
    seedGroupMessages();

    log.info("[Seeder] MessageSeeder finished!");
  }

  // ================= helpers =================

  private void seedDMMessages() {
    List<ChatRoom> dmRooms = chatRoomRepository.findAll()
        .stream()
        .filter(r -> r.getType().name().equals("DIRECT_MESSAGE"))
        .toList();

    dmRooms.forEach(room -> {
      List<ChatRoomMember> members = membersRepository.findByChatRoom(room);

      if (members.size() != 2) {
        log.warn("[Seeder] DM room {} does not have exactly 2 members. Skipping...", room.getChatId());
        return;
      }

      User user1 = members.get(0).getUser();
      User user2 = members.get(1).getUser();

      String[] conversation = {
          "Hey " + user2.getUsername() + ", how are you?",
          "I'm good! How about you?",
          "Doing well. Working on the chat app project.",
          "Nice! How's it going so far?",
          "Pretty good. Just implementing message features.",
          "That sounds interesting.",
          "Yeah, adding real-time support was tricky.",
          "WebSockets?",
          "Exactly. Took some time to get it right.",
          "But I guess it's worth it.",
          "Definitely. Feels great when it works.",
          "Are you adding notifications too?",
          "Yes, planning to.",
          "Nice. This app is going to be solid.",
          "Hope so. Still need to optimize performance.",
          "That's always the fun part.",
          "True. Debugging at midnight is not fun though.",
          "Haha, I can relate.",
          "Anyway, what are you working on?",
          "Mostly backend APIs these days.",
          "Spring Boot?",
          "Of course.",
          "Good choice.",
          "Alright, I’ll get back to coding.",
          "Sure, talk later.",
          "See you soon.",
          "Bye!"
      };

      // Generate 25–30 alternating messages
      for (int i = 0; i < conversation.length; i++) {
        User sender = (i % 2 == 0) ? user1 : user2;
        createMessage(room.getChatId(), sender.getUserId(), conversation[i]);

        try {
          Thread.sleep(50); // small delay to slightly vary timestamps
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    });
  }

  private void seedGroupMessages() {
    ChatRoom group = chatRoomRepository.findAll()
        .stream()
        .filter(r -> r.getName().equals("Dummy Group"))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Dummy Group chat room missing"));

    List<ChatRoomMember> members = membersRepository.findByChatRoom(group);
    User admin = members.stream()
        .filter(ChatRoomMember::isAdmin)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No admin found in Dummy Group"))
        .getUser();

    // Admin welcome message
    createMessage(group.getChatId(), admin.getUserId(),
        "Welcome everyone! I am " + admin.getUsername() + ". Please introduce yourselves.");

    // Other members introduction
    members.stream()
        .filter(m -> !m.isAdmin())
        .map(ChatRoomMember::getUser)
        .forEach(user -> createMessage(group.getChatId(), user.getUserId(),
            "Hello, I am " + user.getUsername()));
  }

  private void createMessage(Long chatRoomId, UUID userId, String content) {
    MessageDTO dto = new MessageDTO();
    dto.setChatRoomId(chatRoomId);
    dto.setUserId(userId);
    dto.setContent(content);
    dto.setTimestamp(Timestamp.from(Instant.now()));
    dto.setMessageId(ulidService.newIdString());
    messageService.createMessage(dto, userId);
    log.info("[Seeder] ✔ Created message in chat {}: {}", chatRoomId, content);
  }

}
