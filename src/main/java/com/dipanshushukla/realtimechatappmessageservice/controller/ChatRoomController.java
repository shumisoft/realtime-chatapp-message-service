package com.dipanshushukla.realtimechatappmessageservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dipanshushukla.realtimechatappmessageservice.dto.ChatRoomDTO;
import com.dipanshushukla.realtimechatappmessageservice.dto.ResponseMessageDTO;
import com.dipanshushukla.realtimechatappmessageservice.service.ChatRoomService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService service;

    private UUID parse(String id) {
        return UUID.fromString(id);
    }

    @PostMapping
    public ResponseEntity<ChatRoomDTO> createChatRoom(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ChatRoomDTO chatRoomDTO) {

        ChatRoomDTO created = service.createChatRoom(chatRoomDTO, parse(userId));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<ChatRoomDTO> getChatRoomById(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long chatId) {

        ChatRoomDTO dto = service.getChatRoomById(chatId, parse(userId));
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{chatId}")
    public ResponseEntity<ResponseMessageDTO> updateChatRoom(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long chatId,
            @RequestBody ChatRoomDTO update) {

        service.updateChatRoom(chatId, update, parse(userId));
        return ResponseEntity.ok(new ResponseMessageDTO("Chat room updated successfully"));
    }

    @DeleteMapping("/{chatId}")
    public ResponseEntity<ResponseMessageDTO> deleteChatRoom(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long chatId) {

        service.deleteChatRoom(chatId, parse(userId));
        return ResponseEntity.ok(new ResponseMessageDTO("Chat room deleted successfully"));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ChatRoomDTO>> getMyChatRooms(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<ChatRoomDTO> list = service.getMyChatRooms(parse(userId), page, size);
        return ResponseEntity.ok(list);
    }

}
