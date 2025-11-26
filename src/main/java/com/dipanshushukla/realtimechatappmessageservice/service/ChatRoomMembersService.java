package com.dipanshushukla.realtimechatappmessageservice.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dipanshushukla.realtimechatappmessageservice.dto.ChatRoomMembersDTO;
import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoom;
import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoomMembers;
import com.dipanshushukla.realtimechatappmessageservice.entity.User;
import com.dipanshushukla.realtimechatappmessageservice.exception.BadRequestException;
import com.dipanshushukla.realtimechatappmessageservice.exception.ResourceNotFoundException;
import com.dipanshushukla.realtimechatappmessageservice.model.ChatRoomMembersId;
import com.dipanshushukla.realtimechatappmessageservice.repository.ChatRoomMembersRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.ChatRoomRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.UserRepository;

@Service
public class ChatRoomMembersService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRoomMembersRepository chatRoomMembersRepository;

    public void addMember(ChatRoomMembersDTO dto) {
        ChatRoom chatRoom = chatRoomRepository.findById(dto.getChatId())
                .orElseThrow(() -> new ResourceNotFoundException("Chat Room not found with id: " + dto.getChatId()));

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getUserId()));

        if (chatRoomMembersRepository.existsByChatRoomAndUser(chatRoom, user)) {
            throw new BadRequestException("User is already a member of the chat room.");
        }

        ChatRoomMembers members = ChatRoomMembers.builder()
                .chatRoomMembersId(new ChatRoomMembersId(dto.getChatId(), dto.getUserId()))
                .chatRoom(chatRoom)
                .user(user)
                .build();

        chatRoomMembersRepository.save(members);
    }

    public void removeMember(Long chatRoomId, UUID userId) {
        chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat Room not found with id: " + chatRoomId));
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        ChatRoomMembersId id = new ChatRoomMembersId(chatRoomId, userId);
        chatRoomMembersRepository.deleteById(id);
    }

    public List<UUID> getMembers(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat Room not found with id: " + chatRoomId));

        return chatRoomMembersRepository.findByChatRoom(chatRoom)
                .stream()
                .map(x -> x.getUser().getUserId())
                .toList();
    }
}
