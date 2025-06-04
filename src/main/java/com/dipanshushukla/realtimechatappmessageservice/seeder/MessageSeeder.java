package com.dipanshushukla.realtimechatappmessageservice.seeder;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;

import com.dipanshushukla.realtimechatappmessageservice.dto.MessageDTO;
import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoom;
import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoomMembers;
import com.dipanshushukla.realtimechatappmessageservice.entity.User;
import com.dipanshushukla.realtimechatappmessageservice.repository.ChatRoomMembersRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.ChatRoomRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.MessageRepository;
import com.dipanshushukla.realtimechatappmessageservice.service.MessageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@Order(3)
@RequiredArgsConstructor
public class MessageSeeder implements ApplicationRunner {

  private final ChatRoomRepository chatRoomRepository;
  private final ChatRoomMembersRepository membersRepository;

  private final MessageService messageService;
  private final MessageRepository messageRepository;

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
      List<ChatRoomMembers> members = membersRepository.findByChatRoom(room);
      members.forEach(member -> {
        User user = member.getUser();
        createMessage(room.getChatId(), user.getUserId(),
            "Hello, I am " + user.getUsername());
        createMessage(room.getChatId(), user.getUserId(),
            "Nice to meet you!");
      });
    });
  }

  private void seedGroupMessages() {
    ChatRoom group = chatRoomRepository.findAll()
        .stream()
        .filter(r -> r.getName().equals("Dummy Group"))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Dummy Group chat room missing"));

    List<ChatRoomMembers> members = membersRepository.findByChatRoom(group);
    User admin = members.stream()
        .filter(ChatRoomMembers::isAdmin)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No admin found in Dummy Group"))
        .getUser();

    // Admin welcome message
    createMessage(group.getChatId(), admin.getUserId(),
        "Welcome everyone! I am " + admin.getUsername() + ". Please introduce yourselves.");

    // Other members introduction
    members.stream()
        .filter(m -> !m.isAdmin())
        .map(ChatRoomMembers::getUser)
        .forEach(user -> createMessage(group.getChatId(), user.getUserId(),
            "Hello, I am " + user.getUsername()));
  }

  private void createMessage(Long chatRoomId, java.util.UUID userId, String content) {
    MessageDTO dto = new MessageDTO();
    dto.setChatRoomId(chatRoomId);
    dto.setUserId(userId);
    dto.setContent(content);
    messageService.createMessage(dto, userId);
    log.info("[Seeder] ✔ Created message in chat {}: {}", chatRoomId, content);
  }

}
