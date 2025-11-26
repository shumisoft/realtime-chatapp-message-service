package com.dipanshushukla.realtimechatappmessageservice.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dipanshushukla.realtimechatappmessageservice.dto.MessageDTO;
import com.dipanshushukla.realtimechatappmessageservice.service.MessageService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
public class MessageControler {

    @Autowired
    private MessageService service;

    @PostMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<String> createMessage(@PathVariable Long chatRoomId,
            @Valid @RequestBody MessageDTO messageDTO) {
        messageDTO.setChatRoomId(chatRoomId);

        try {
            service.createMessage(messageDTO);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("Message created succesfully.");
    }

    @GetMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<?> getMessagesFromChatRoom(@PathVariable Long chatRoomId) {
        try {
            List<MessageDTO> messages = service.getMessagesFromChatRoom(chatRoomId);
            return ResponseEntity.ok(messages);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/messages/{messageId}")
    public ResponseEntity<?> getMessageFromMessageId(@PathVariable Long messageId) {
        try {
            MessageDTO messageDTO = service.getMessageFromMessageId(messageId);
            return ResponseEntity.ok(messageDTO);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/messages/{messageId}")
    public ResponseEntity<String> updateMessageStatu(@PathVariable Long messageId) {
        try {
            service.updateMessageStatus(messageId);
            return ResponseEntity.ok("Message status updated Successfully.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

}
