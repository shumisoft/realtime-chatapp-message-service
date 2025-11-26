package com.dipanshushukla.realtimechatappmessageservice.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dipanshushukla.realtimechatappmessageservice.dto.MessageDTO;
import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoom;
import com.dipanshushukla.realtimechatappmessageservice.entity.Message;
import com.dipanshushukla.realtimechatappmessageservice.entity.User;
import com.dipanshushukla.realtimechatappmessageservice.exception.ResourceNotFoundException;
import com.dipanshushukla.realtimechatappmessageservice.model.MessageStatus;
import com.dipanshushukla.realtimechatappmessageservice.model.MessageType;
import com.dipanshushukla.realtimechatappmessageservice.repository.ChatRoomRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.MessageRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.UserRepository;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    public MessageDTO createMessage(MessageDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("No user found with id: " + dto.getUserId()));

        ChatRoom chatRoom = chatRoomRepository.findById(dto.getChatRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("No chat room found with id: " + dto.getChatRoomId()));

        Message message = Message.builder()
                .chatRoom(chatRoom)
                .user(user)
                .content(dto.getContent())
                .type(dto.getType() != null ? dto.getType() : MessageType.TEXT)
                .status(dto.getStatus() != null ? dto.getStatus() : MessageStatus.UNREAD)
                .build();

        messageRepository.save(message);
        return MessageDTO.fromEntity(message);
    }

    public List<MessageDTO> getMessagesFromChatRoom(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("No chat room found with id: " + chatRoomId));

        return messageRepository.findByChatRoom(chatRoom)
                .stream()
                .map(MessageDTO::fromEntity)
                .toList();
    }

    public MessageDTO getMessageFromMessageId(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("No message found with id: " + messageId));

        return MessageDTO.fromEntity(message);
    }

    public void updateMessageStatus(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("No message found with id: " + messageId));

        message.setStatus(MessageStatus.READ);
        messageRepository.save(message);
    }
}
