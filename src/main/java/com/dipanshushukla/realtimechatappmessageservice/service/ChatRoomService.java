package com.dipanshushukla.realtimechatappmessageservice.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.dipanshushukla.realtimechatappmessageservice.dto.ChatRoomDTO;
import com.dipanshushukla.realtimechatappmessageservice.dto.MessageDTO;
import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoom;
import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoomMembers;
import com.dipanshushukla.realtimechatappmessageservice.entity.Message;
import com.dipanshushukla.realtimechatappmessageservice.entity.User;
import com.dipanshushukla.realtimechatappmessageservice.exception.BadRequestException;
import com.dipanshushukla.realtimechatappmessageservice.exception.ResourceNotFoundException;
import com.dipanshushukla.realtimechatappmessageservice.model.ChatRoomMembersId;
import com.dipanshushukla.realtimechatappmessageservice.model.ChatRoomType;
import com.dipanshushukla.realtimechatappmessageservice.repository.ChatRoomMembersRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.ChatRoomRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.MessageRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final ChatRoomMembersRepository membersRepository;
    private final MessageRepository messageRepository;

    private void ensureMember(Long chatId, UUID requesterId) {
        User user = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ChatRoom chatRoom = chatRoomRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));

        boolean isMember = membersRepository.existsByChatRoomAndUser(chatRoom, user);

        if (!isMember)
            throw new BadRequestException("User is not a member of this chat room.");
    }

    private void ensureAdmin(Long chatId, UUID requesterId) {
        User user = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ChatRoom chatRoom = chatRoomRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));

        ChatRoomMembers membership = membersRepository.findByChatRoomAndUser(chatRoom, user);

        if (membership == null || !membership.isAdmin()) {
            throw new BadRequestException("Only admins can perform this action.");
        }
    }

    public ChatRoomDTO createChatRoom(ChatRoomDTO dto, UUID creatorId) {
        ChatRoom chatRoom = ChatRoom.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .type(dto.getType())
                .build();

        chatRoomRepository.save(chatRoom);

        // Add creator as first member
        User user = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Creator not found"));

        ChatRoomMembers members = ChatRoomMembers.builder()
                .chatRoom(chatRoom)
                .user(user)
                .chatRoomMembersId(new ChatRoomMembersId(chatRoom.getChatId(), creatorId))
                .build();

        if (chatRoom.getType() != ChatRoomType.DIRECT_MESSAGE)
            members.setAdmin(true);

        membersRepository.save(members);

        return ChatRoomDTO.fromEntity(chatRoom);
    }

    public ChatRoomDTO getChatRoomById(Long chatId, UUID requesterId) {
        ensureMember(chatId, requesterId);

        ChatRoom chatRoom = chatRoomRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));

        return ChatRoomDTO.fromEntity(chatRoom);
    }

    public void updateChatRoom(Long chatId, ChatRoomDTO dto, UUID requesterId) {
        ensureAdmin(chatId, requesterId);

        ChatRoom chatRoom = chatRoomRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));

        if (dto.getName() != null)
            chatRoom.setName(dto.getName());
        if (dto.getType() != null)
            chatRoom.setType(dto.getType());
        if (dto.getDescription() != null)
            chatRoom.setDescription(dto.getDescription());

        chatRoomRepository.save(chatRoom);
    }

    public void deleteChatRoom(Long chatId, UUID requesterId) {
        ensureAdmin(chatId, requesterId);

        ChatRoom c = chatRoomRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));
        if (c.getType() != ChatRoomType.DIRECT_MESSAGE)
            throw new BadRequestException("Cannot delete a direct message chatroom.");
        chatRoomRepository.delete(c);
    }

    public Page<ChatRoomDTO> getMyChatRooms(UUID userId, int page, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        PageRequest pageable = PageRequest.of(page, size);

        Page<ChatRoom> rooms = chatRoomRepository.findUserChatRoomsOrdered(userId, pageable);

        return rooms.map(room -> {
            ChatRoomDTO dto = ChatRoomDTO.fromEntity(room);

            Message latest = messageRepository.findLatestMessage(room.getChatId());
            if (latest != null) {
                dto.setLatestMessage(MessageDTO.fromEntity(latest));
            }

            return dto;
        });
    }

}
