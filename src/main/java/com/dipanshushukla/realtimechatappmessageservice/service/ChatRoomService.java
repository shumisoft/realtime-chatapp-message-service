package com.dipanshushukla.realtimechatappmessageservice.service;

import com.dipanshushukla.realtimechatappmessageservice.dto.ChatRoomDTO;
import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoom;
import com.dipanshushukla.realtimechatappmessageservice.entity.User;
import com.dipanshushukla.realtimechatappmessageservice.exception.BadRequestException;
import com.dipanshushukla.realtimechatappmessageservice.exception.ResourceNotFoundException;
import com.dipanshushukla.realtimechatappmessageservice.repository.ChatRoomMembersRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.ChatRoomRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ChatRoomService {

    @Autowired
    private ChatRoomRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRoomMembersRepository chatRoomMembersRepository;

    public ChatRoomDTO createChatRoom(ChatRoomDTO dto) {
        ChatRoom chatRoom = ChatRoom.builder()
                .name(dto.getName())
                .type(dto.getType())
                .description(dto.getDescription())
                .build();

        repository.save(chatRoom);
        return ChatRoomDTO.fromEntity(chatRoom);
    }

    public ChatRoomDTO getChatRoomById(Long chatRoomId) {
        ChatRoom chatRoom = repository.findById(chatRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("No chat room found by id: " + chatRoomId));
        return ChatRoomDTO.fromEntity(chatRoom);
    }

    public void updateChatRoom(Long chatRoomId, ChatRoomDTO dto) {
        if (dto.getName() == null && dto.getType() == null && dto.getDescription() == null) {
            throw new BadRequestException("At least one field must be supplied to update the chat room.");
        }

        ChatRoom chatRoom = repository.findById(chatRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("No chat room found by id: " + chatRoomId));

        if (dto.getType() != null)
            chatRoom.setType(dto.getType());
        if (dto.getName() != null)
            chatRoom.setName(dto.getName());
        if (dto.getDescription() != null)
            chatRoom.setDescription(dto.getDescription());

        repository.save(chatRoom);
    }

    public void deleteChatRoom(Long chatRoomId) {
        ChatRoom chatRoom = repository.findById(chatRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("No chat room found by id: " + chatRoomId));
        repository.delete(chatRoom);
    }

    public List<ChatRoomDTO> getAllChatRoomsFromUserId(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No user found with id: " + userId));

        return chatRoomMembersRepository.findByUser(user)
                .stream()
                .map(x -> ChatRoomDTO.fromEntity(x.getChatRoom()))
                .toList();
    }
}
