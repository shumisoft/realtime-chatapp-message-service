package com.dipanshushukla.realtimechatappmessageservice.seeder;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;

import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoom;
import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoomMembers;
import com.dipanshushukla.realtimechatappmessageservice.entity.User;
import com.dipanshushukla.realtimechatappmessageservice.model.ChatRoomMembersId;
import com.dipanshushukla.realtimechatappmessageservice.repository.ChatRoomMembersRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.ChatRoomRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class ChatRoomMembersSeeder implements ApplicationRunner {

  private final ChatRoomRepository chatRoomRepository;
  private final UserRepository userRepository;
  private final ChatRoomMembersRepository membersRepository;

  @Override
  public void run(ApplicationArguments args) throws Exception {

    log.info("[Seeder] ChatRoomMembersSeeder is running...");

    if (membersRepository.count() > 0) {
      log.info("[Seeder] ChatRoomMembersSeeder skipped (members already exist)");
      return;
    }

    log.info("[Seeder] ChatRoomMembersSeeder started...");

    // ---------- DIRECT MESSAGE ROOMS ----------
    seedDM("DM_user1_user2", List.of("user1", "user2"));
    seedDM("DM_user3_user4", List.of("user3", "user4"));
    seedDM("DM_user5_user6", List.of("user5", "user6"));
    seedDM("DM_user7_user8", List.of("user7", "user8"));
    seedDM("DM_user9_user10", List.of("user9", "user10"));

    // ---------- COMMON PRIVATE ROOM ----------
    seedPrivate(
        "Dummy Group",
        List.of(
            "user1", "user2", "user3", "user4", "user5",
            "user6", "user7", "user8", "user9", "user10"),
        "user1" // admin
    );

    log.info("[Seeder] ChatRoomMembersSeeder finished!");

  }

  // ================= helpers =================

  private void seedDM(String roomName, List<String> usernames) {
    ChatRoom room = getRoom(roomName);

    usernames.forEach(username -> {
      User user = getUser(username);
      saveMembership(room, user, false);
    });
  }

  private void seedPrivate(String roomName,
      List<String> usernames,
      String adminUsername) {

    ChatRoom room = getRoom(roomName);

    usernames.forEach(username -> {
      User user = getUser(username);
      boolean isAdmin = username.equals(adminUsername);
      saveMembership(room, user, isAdmin);
    });
  }

  private ChatRoom getRoom(String name) {
    return chatRoomRepository
        .findAll()
        .stream()
        .filter(r -> r.getName().equals(name))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
            "Required chat room missing: " + name));
  }

  private User getUser(String username) {
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new IllegalStateException(
            "Required user missing: " + username));
  }

  private void saveMembership(ChatRoom room, User user, boolean admin) {

    ChatRoomMembers members = ChatRoomMembers.builder()
        .chatRoom(room)
        .user(user)
        .chatRoomMembersId(
            new ChatRoomMembersId(
                room.getChatId(),
                user.getUserId()))
        .admin(admin)
        .build();

    membersRepository.save(members);

    log.info(
        "[Seeder] ✔ Added {} to {}{}",
        user.getUsername(),
        room.getName(),
        admin ? " (ADMIN)" : "");
  }

}
