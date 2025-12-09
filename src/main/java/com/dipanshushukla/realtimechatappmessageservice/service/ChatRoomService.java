package com.dipanshushukla.realtimechatappmessageservice.service;

import com.dipanshushukla.realtimechatappmessageservice.dto.ChatRoomDTO;
import com.dipanshushukla.realtimechatappmessageservice.dto.MessageDTO;
import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoom;
import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoomMembers;
import com.dipanshushukla.realtimechatappmessageservice.entity.Message;
import com.dipanshushukla.realtimechatappmessageservice.entity.User;
import com.dipanshushukla.realtimechatappmessageservice.exception.BadRequestException;
import com.dipanshushukla.realtimechatappmessageservice.exception.ResourceNotFoundException;
import com.dipanshushukla.realtimechatappmessageservice.model.ChatRoomMembersId;
import com.dipanshushukla.realtimechatappmessageservice.repository.ChatRoomMembersRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.ChatRoomRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.MessageRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.UserRepository;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository repository;
    private final UserRepository userRepository;
    private final ChatRoomMembersRepository membersRepository;
    private final MessageRepository messageRepository;

    private void ensureMember(Long chatId, UUID requesterId) {
        User user = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ChatRoom chatRoom = repository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));

        boolean isMember = membersRepository.existsByChatRoomAndUser(chatRoom, user);

        if (!isMember)
            throw new BadRequestException("User is not a member of this chat room.");
    }

    public ChatRoomDTO createChatRoom(ChatRoomDTO dto, UUID creatorId) {
        ChatRoom chatRoom = ChatRoom.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .type(dto.getType())
                .build();

        repository.save(chatRoom);

        // Add creator as first member
        User user = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Creator not found"));

        ChatRoomMembers members = ChatRoomMembers.builder()
                .chatRoom(chatRoom)
                .user(user)
                .chatRoomMembersId(new ChatRoomMembersId(chatRoom.getChatId(), creatorId))
                .build();

        membersRepository.save(members);

        return ChatRoomDTO.fromEntity(chatRoom);
    }

    public ChatRoomDTO getChatRoomById(Long chatId, UUID requesterId) {
        ensureMember(chatId, requesterId);

        ChatRoom chatRoom = repository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));

        return ChatRoomDTO.fromEntity(chatRoom);
    }

    public void updateChatRoom(Long chatId, ChatRoomDTO dto, UUID requesterId) {
        ensureMember(chatId, requesterId);

        ChatRoom chatRoom = repository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));

        if (dto.getName() != null)
            chatRoom.setName(dto.getName());
        if (dto.getType() != null)
            chatRoom.setType(dto.getType());
        if (dto.getDescription() != null)
            chatRoom.setDescription(dto.getDescription());

        repository.save(chatRoom);
    }

    public void deleteChatRoom(Long chatId, UUID requesterId) {
        ensureMember(chatId, requesterId);
        ChatRoom c = repository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));
        repository.delete(c);
    }

    public List<ChatRoomDTO> getMyChatRooms(UUID userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        PageRequest request = PageRequest.of(page, size);

        return membersRepository.findByUser(user, request)
                .map(m -> {
                    ChatRoom room = m.getChatRoom();
                    ChatRoomDTO dto = ChatRoomDTO.fromEntity(room);

                    Message latest = messageRepository.findLatestMessage(room.getChatId());
                    if (latest != null)
                        dto.setLatestMessage(MessageDTO.fromEntity(latest));

                    return dto;
                })
                .toList();
    }

}
