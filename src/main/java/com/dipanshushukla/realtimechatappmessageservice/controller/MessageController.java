package com.dipanshushukla.realtimechatappmessageservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dipanshushukla.realtimechatappmessageservice.dto.MessageDTO;
import com.dipanshushukla.realtimechatappmessageservice.service.MessageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageService service;

    private UUID parse(String id) {
        return UUID.fromString(id);
    }

    @PostMapping("/rooms/{chatId}/messages")
    public ResponseEntity<MessageDTO> createMessage(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long chatId,
            @Valid @RequestBody MessageDTO dto) {

        UUID requesterId = parse(userId);

        dto.setChatRoomId(chatId);
        dto.setUserId(requesterId);

        MessageDTO created = service.createMessage(dto, requesterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/rooms/{chatId}/messages")
    public ResponseEntity<List<MessageDTO>> getMessages(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long chatId) {

        List<MessageDTO> list = service.getMessagesFromChatRoom(chatId, parse(userId));
        return ResponseEntity.ok(list);
    }

    @GetMapping("/messages/{messageId}")
    public ResponseEntity<MessageDTO> getMessage(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long messageId) {

        MessageDTO dto = service.getMessage(messageId, parse(userId));
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/messages/{messageId}")
    public ResponseEntity<String> updateStatus(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long messageId) {

        service.updateMessageStatus(messageId, parse(userId));
        return ResponseEntity.ok("Message status updated.");
    }
}
