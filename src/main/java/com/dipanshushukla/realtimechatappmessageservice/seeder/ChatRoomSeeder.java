package com.dipanshushukla.realtimechatappmessageservice.seeder;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;

import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoom;
import com.dipanshushukla.realtimechatappmessageservice.model.ChatRoomType;
import com.dipanshushukla.realtimechatappmessageservice.repository.ChatRoomRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class ChatRoomSeeder implements ApplicationRunner {

  private final ChatRoomRepository repository;

  @Override
  public void run(ApplicationArguments args) throws Exception {

    log.info("[Seeder] ChatRoomSeeder is running...");

    if (repository.count() > 0) {
      log.info("[Seeder] ChatRoomSeeder skipped (chat rooms already exist)");
      return;
    }

    log.info("[Seeder] ChatRoomSeeder started...");

    List<ChatRoom> rooms = List.of(

        createDmRoom("user1", "user2"),
        createDmRoom("user3", "user4"),
        createDmRoom("user5", "user6"),
        createDmRoom("user7", "user8"),
        createDmRoom("user9", "user10"),
        createGroup()

    );

    rooms.forEach(room -> {
      repository.save(room);
      log.info("[Seeder] ✔ Created chat room: {} ({})",
          room.getName(), room.getType());
    });

    log.info("[Seeder] ChatRoomSeeder finished!");

  }

  private ChatRoom createDmRoom(String u1, String u2) {
    return ChatRoom.builder()
        .name("DM_" + u1 + "_" + u2)
        .type(ChatRoomType.DIRECT_MESSAGE)
        .build();
  }

  private ChatRoom createGroup() {
    return ChatRoom.builder()
        .name("Dummy Group")
        .description("Dummy group chat")
        .type(ChatRoomType.PRIVATE)
        .build();
  }

}
