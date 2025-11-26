package com.dipanshushukla.realtimechatappmessageservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dipanshushukla.realtimechatappmessageservice.dto.ChatRoomMembersDTO;
import com.dipanshushukla.realtimechatappmessageservice.service.ChatRoomMembersService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/rooms/{chatId}/members")
public class ChatRoomMembersController {

    @Autowired
    private ChatRoomMembersService service;

    @GetMapping
    public ResponseEntity<?> getMembers(@PathVariable Long chatId) {
        try {
            List<UUID> members = service.getMembers(chatId);
            return ResponseEntity.ok(members);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<String> addMember(@PathVariable Long chatId,
            @Valid @RequestBody ChatRoomMembersDTO chatRoomMembersDTO) {
        try {
            service.addMember(chatRoomMembersDTO);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("Member added successfully.");
    }

    @DeleteMapping
    public ResponseEntity<String> removeMember(@PathVariable Long chatId, @RequestParam UUID userId) {
        try {
            service.removeMember(chatId, userId);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

        return ResponseEntity.ok().body("Member removed successfully.");
    }

}
