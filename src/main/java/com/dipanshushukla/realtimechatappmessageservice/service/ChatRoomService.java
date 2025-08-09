package com.dipanshushukla.realtimechatappmessageservice.service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.dipanshushukla.realtimechatappmessageservice.dto.ChatRoomDTO;
import com.dipanshushukla.realtimechatappmessageservice.dto.MessageDTO;
import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoom;
import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoomMember;
import com.dipanshushukla.realtimechatappmessageservice.entity.Message;
import com.dipanshushukla.realtimechatappmessageservice.entity.User;
import com.dipanshushukla.realtimechatappmessageservice.exception.BadRequestException;
import com.dipanshushukla.realtimechatappmessageservice.exception.ResourceNotFoundException;
import com.dipanshushukla.realtimechatappmessageservice.model.ChatRoomMemberId;
import com.dipanshushukla.realtimechatappmessageservice.model.ChatRoomType;
import com.dipanshushukla.realtimechatappmessageservice.repository.ChatRoomMemberRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.ChatRoomRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.MessageRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.UserRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final ChatRoomMemberRepository memberRepository;
    private final MessageRepository messageRepository;

    private void ensureMember(Long chatId, UUID requesterId) {
        User user = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ChatRoom chatRoom = chatRoomRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));

        boolean isMember = memberRepository.existsByChatRoomAndUser(chatRoom, user);

        if (!isMember)
            throw new BadRequestException("User is not a member of this chat room.");
    }

    private void ensureAdmin(Long chatId, UUID requesterId) {
        User user = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ChatRoom chatRoom = chatRoomRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));

        ChatRoomMember membership = memberRepository.findByChatRoomAndUser(chatRoom, user);

        if (membership == null || !membership.isAdmin()) {
            throw new BadRequestException("Only admins can perform this action.");
        }
    }

    public ChatRoomDTO createChatRoom(ChatRoomDTO dto, UUID creatorId) {
        // 1. Handle Direct Messages
        if (dto.getType() == ChatRoomType.DIRECT_MESSAGE) {
            if (dto.getMemberIds() == null || dto.getMemberIds().size() != 1) {
                throw new BadRequestException("Direct message must have exactly one recipient.");
            }

            UUID recipientId = dto.getMemberIds().iterator().next();

            // Check for existing DM to avoid duplicates
            Optional<ChatRoom> existingChat = chatRoomRepository.findExistingDirectMessage(creatorId, recipientId);
            if (existingChat.isPresent()) {
                return ChatRoomDTO.fromEntity(existingChat.get());
            }

            // Create new DM Room
            ChatRoom chatRoom = ChatRoom.builder()
                    .type(ChatRoomType.DIRECT_MESSAGE)
                    .name("DM")
                    .members(new ArrayList<>()) // FIX: Initialize list
                    .build();

            chatRoom = chatRoomRepository.save(chatRoom);

            // Add Creator & Recipient
            addMember(chatRoom, creatorId, false);
            addMember(chatRoom, recipientId, false);

            return ChatRoomDTO.fromEntity(chatRoom);
        }

        // 2. Handle Group Chats (Public/Private)
        else {
            ChatRoom chatRoom = ChatRoom.builder()
                    .name(dto.getName())
                    .description(dto.getDescription())
                    .type(dto.getType())
                    .icon(dto.getIcon())
                    .members(new ArrayList<>()) // FIX: Initialize list
                    .build();

            chatRoom = chatRoomRepository.save(chatRoom);

            // Add Creator as Admin
            addMember(chatRoom, creatorId, true);

            // Add other initial members (if any)
            if (dto.getMemberIds() != null && !dto.getMemberIds().isEmpty()) {
                for (UUID memberId : dto.getMemberIds()) {
                    if (!memberId.equals(creatorId)) {
                        addMember(chatRoom, memberId, false);
                    }
                }
            }

            return ChatRoomDTO.fromEntity(chatRoom);
        }
    }

    // Helper method: Updates BOTH Database and Memory to prevent NPE
    private void addMember(ChatRoom chatRoom, UUID userId, boolean isAdmin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        ChatRoomMember member = ChatRoomMember.builder()
                .chatRoom(chatRoom)
                .user(user)
                .chatRoomMemberId(new ChatRoomMemberId(chatRoom.getChatId(), userId))
                .admin(isAdmin)
                .build();

        // 1. Save to DB
        memberRepository.save(member);

        // 2. Update In-Memory Object (So DTO conversion works immediately)
        if (chatRoom.getMembers() == null) {
            chatRoom.setMembers(new ArrayList<>());
        }
        chatRoom.getMembers().add(member);
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
        if (dto.getIcon() != null)
            chatRoom.setIcon(dto.getIcon());

        chatRoomRepository.save(chatRoom);
    }

    public void deleteChatRoom(Long chatId, UUID requesterId) {
        ensureAdmin(chatId, requesterId);

        ChatRoom c = chatRoomRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));
        if (c.getType() == ChatRoomType.DIRECT_MESSAGE)
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