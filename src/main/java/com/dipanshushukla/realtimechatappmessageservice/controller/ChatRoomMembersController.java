package com.dipanshushukla.realtimechatappmessageservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dipanshushukla.realtimechatappmessageservice.dto.ChatRoomMembersDTO;
import com.dipanshushukla.realtimechatappmessageservice.dto.ResponseMessageDTO;
import com.dipanshushukla.realtimechatappmessageservice.service.ChatRoomMembersService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rooms/{chatId}/members")
@RequiredArgsConstructor
public class ChatRoomMembersController {

    private final ChatRoomMembersService service;

    private UUID parse(String id) {
        return UUID.fromString(id);
    }

    @GetMapping
    public ResponseEntity<List<ChatRoomMembersDTO>> getMembers(
            @RequestHeader("X-User-Id") String requester,
            @PathVariable Long chatId) {

        List<ChatRoomMembersDTO> list = service.getMembers(chatId, parse(requester));
        return ResponseEntity.ok(list);
    }

    @PostMapping
    public ResponseEntity<ResponseMessageDTO> addMember(
            @RequestHeader("X-User-Id") String requester,
            @PathVariable Long chatId,
            @Valid @RequestBody ChatRoomMembersDTO dto) {

        dto.setChatId(chatId);
        service.addMember(dto, parse(requester));

        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseMessageDTO("Member added."));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ResponseMessageDTO> removeMember(
            @RequestHeader("X-User-Id") String requester,
            @PathVariable Long chatId,
            @PathVariable UUID userId) {

        service.removeMember(chatId, userId, parse(requester));
        return ResponseEntity.ok(new ResponseMessageDTO("Member removed."));
    }
}
