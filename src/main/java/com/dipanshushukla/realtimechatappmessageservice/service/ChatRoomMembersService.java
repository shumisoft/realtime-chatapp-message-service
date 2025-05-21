package com.dipanshushukla.realtimechatappmessageservice.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.dipanshushukla.realtimechatappmessageservice.dto.ChatRoomMembersDTO;
import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoom;
import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoomMembers;
import com.dipanshushukla.realtimechatappmessageservice.entity.User;
import com.dipanshushukla.realtimechatappmessageservice.exception.BadRequestException;
import com.dipanshushukla.realtimechatappmessageservice.exception.ResourceNotFoundException;
import com.dipanshushukla.realtimechatappmessageservice.model.ChatRoomMembersId;
import com.dipanshushukla.realtimechatappmessageservice.model.ChatRoomType;
import com.dipanshushukla.realtimechatappmessageservice.repository.ChatRoomMembersRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.ChatRoomRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.UserRepository;

@Service
public class ChatRoomMembersService {

        private final ChatRoomRepository chatRoomRepository;
        private final UserRepository userRepository;
        private final ChatRoomMembersRepository membersRepository;

        public ChatRoomMembersService(ChatRoomRepository chatRoomRepository,
                        UserRepository userRepository,
                        ChatRoomMembersRepository membersRepository) {

                this.chatRoomRepository = chatRoomRepository;
                this.userRepository = userRepository;
                this.membersRepository = membersRepository;
        }

        private void ensureMember(Long chatId, UUID requesterId) {
                ChatRoom chatRoom = chatRoomRepository.findById(chatId)
                                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));

                User requester = userRepository.findById(requesterId)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                boolean isMember = membersRepository.existsByChatRoomAndUser(chatRoom, requester);
                if (!isMember)
                        throw new BadRequestException("User is not part of this chat room.");
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

        public List<ChatRoomMembersDTO> getMembers(Long chatId, UUID requesterId) {
                ensureMember(chatId, requesterId);

                ChatRoom room = chatRoomRepository.findById(chatId)
                                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));

                return membersRepository.findByChatRoom(room)
                                .stream()
                                .map(ChatRoomMembersDTO::fromEntity)
                                .toList();
        }

        public void addMember(ChatRoomMembersDTO dto, UUID requesterId) {
                ensureMember(dto.getChatId(), requesterId);

                ChatRoom chatRoom = chatRoomRepository.findById(dto.getChatId())
                                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));

                if (chatRoom.getType() == ChatRoomType.PRIVATE)
                        ensureAdmin(dto.getChatId(), requesterId);

                User user = userRepository.findById(dto.getUserId())
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                if (membersRepository.existsByChatRoomAndUser(chatRoom, user))
                        throw new BadRequestException("User already a member.");

                ChatRoomMembers members = ChatRoomMembers.builder()
                                .chatRoom(chatRoom)
                                .user(user)
                                .chatRoomMembersId(new ChatRoomMembersId(dto.getChatId(), dto.getUserId()))
                                .build();

                membersRepository.save(members);
        }

        public void removeMember(Long chatId, UUID userId, UUID requesterId) {
                ensureAdmin(chatId, requesterId);

                ChatRoomMembersId id = new ChatRoomMembersId(chatId, userId);

                if (!membersRepository.existsById(id))
                        throw new ResourceNotFoundException("Membership not found.");

                membersRepository.deleteById(id);
        }

        public void assignAdmin(Long chatId, UUID userId, UUID requesterId) {
                ensureAdmin(chatId, requesterId);

                ChatRoom chatRoom = chatRoomRepository.findById(chatId)
                                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                ChatRoomMembers member = membersRepository.findByChatRoomAndUser(chatRoom, user);

                if (member == null)
                        throw new BadRequestException("User is not a member of this chat room.");

                member.setAdmin(true);
                membersRepository.save(member);
        }

        public void unassignAdmin(Long chatId, UUID userId, UUID requesterId) {

                if (userId == requesterId)
                        throw new BadRequestException("Cannot remove yourself as admin.");

                ensureAdmin(chatId, requesterId);

                ChatRoom chatRoom = chatRoomRepository.findById(chatId)
                                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                ChatRoomMembers member = membersRepository.findByChatRoomAndUser(chatRoom, user);

                if (member == null)
                        throw new BadRequestException("User is not a member of this chat room.");

                // Prevent removing the last admin
                List<ChatRoomMembers> admins = membersRepository.findByChatRoom(chatRoom)
                                .stream().filter(ChatRoomMembers::isAdmin).toList();

                if (admins.size() == 1 && admins.get(0).getUser().getUserId().equals(userId)) {
                        throw new BadRequestException("Cannot remove the last admin.");
                }

                member.setAdmin(false);
                membersRepository.save(member);
        }
}
