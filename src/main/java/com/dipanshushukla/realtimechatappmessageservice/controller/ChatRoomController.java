package com.dipanshushukla.realtimechatappmessageservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dipanshushukla.realtimechatappmessageservice.dto.ChatRoomDTO;
import com.dipanshushukla.realtimechatappmessageservice.service.ChatRoomService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/rooms")
public class ChatRoomController {

    @Autowired
    private ChatRoomService service;

    @PostMapping
    public ResponseEntity<String> createChatRoom(@Valid @RequestBody ChatRoomDTO chatRoomDTO) {
        service.createChatRoom(chatRoomDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("Chat Room created successfully.");
    }

    @GetMapping("{chatId}")
    public ResponseEntity<?> getChatRoomById(@RequestParam Long chatRoomId) {
        try {
            return ResponseEntity.ok(service.getChatRoomById(chatRoomId));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("{chatId}")
    public ResponseEntity<String> updateChatRoom(@PathVariable Long chatId, @RequestBody ChatRoomDTO chatRoomDTO) {
        try {
            service.updateChatRoom(chatId, chatRoomDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

        return ResponseEntity.ok().body("Chat room updated successfully");
    }

    @DeleteMapping("{chatId}")
    public ResponseEntity<?> deleteChatRoom(@RequestParam Long chatRoomId) {
        try {
            service.deleteChatRoom(chatRoomId);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

        return ResponseEntity.ok().body("Chat room deleted successfully");
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getAllChatRoomsFromUserId(@RequestParam UUID userId) {
        try {
            List<ChatRoomDTO> chatRooms = service.getAllChatRoomsFromUserId(userId);
            return ResponseEntity.ok(chatRooms);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

}
