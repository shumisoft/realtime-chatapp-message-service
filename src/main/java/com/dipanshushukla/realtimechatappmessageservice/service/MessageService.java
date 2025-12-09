package com.dipanshushukla.realtimechatappmessageservice.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.dipanshushukla.realtimechatappmessageservice.dto.MessageDTO;
import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoom;
import com.dipanshushukla.realtimechatappmessageservice.entity.Message;
import com.dipanshushukla.realtimechatappmessageservice.entity.User;
import com.dipanshushukla.realtimechatappmessageservice.exception.BadRequestException;
import com.dipanshushukla.realtimechatappmessageservice.exception.ResourceNotFoundException;
import com.dipanshushukla.realtimechatappmessageservice.model.MessageStatus;
import com.dipanshushukla.realtimechatappmessageservice.model.MessageType;
import com.dipanshushukla.realtimechatappmessageservice.repository.ChatRoomMembersRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.ChatRoomRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.MessageRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.UserRepository;

@Service
public class MessageService {

        private final MessageRepository msgRepo;
        private final ChatRoomRepository roomRepo;
        private final UserRepository userRepo;
        private final ChatRoomMembersRepository membersRepo;

        public MessageService(MessageRepository msgRepo,
                        ChatRoomRepository roomRepo,
                        UserRepository userRepo,
                        ChatRoomMembersRepository membersRepo) {
                this.msgRepo = msgRepo;
                this.roomRepo = roomRepo;
                this.userRepo = userRepo;
                this.membersRepo = membersRepo;
        }

        private void ensureMember(Long chatId, UUID requesterId) {
                ChatRoom room = roomRepo.findById(chatId)
                                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));

                User user = userRepo.findById(requesterId)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                boolean isMember = membersRepo.existsByChatRoomAndUser(room, user);

                if (!isMember)
                        throw new BadRequestException("User is not part of this chat room.");
        }

        public MessageDTO createMessage(MessageDTO dto, UUID requesterId) {
                ensureMember(dto.getChatRoomId(), requesterId);

                User user = userRepo.findById(requesterId)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                ChatRoom room = roomRepo.findById(dto.getChatRoomId())
                                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));

                Message message = Message.builder()
                                .chatRoom(room)
                                .user(user)
                                .content(dto.getContent())
                                .type(dto.getType() != null ? dto.getType() : MessageType.TEXT)
                                .status(MessageStatus.UNREAD)
                                .build();

                msgRepo.save(message);
                return MessageDTO.fromEntity(message);
        }

        public List<MessageDTO> getMessagesFromChatRoom(Long chatRoomId, UUID requesterId) {
                ensureMember(chatRoomId, requesterId);

                ChatRoom room = roomRepo.findById(chatRoomId)
                                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));

                return msgRepo.findByChatRoom(room)
                                .stream()
                                .map(MessageDTO::fromEntity)
                                .toList();
        }

        public MessageDTO getMessage(Long messageId, UUID requesterId) {
                Message msg = msgRepo.findById(messageId)
                                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

                ensureMember(msg.getChatRoom().getChatId(), requesterId);

                return MessageDTO.fromEntity(msg);
        }

        public void updateMessageStatus(Long messageId, UUID requesterId) {
                Message msg = msgRepo.findById(messageId)
                                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

                if (!msg.getUser().getUserId().equals(requesterId))
                        throw new BadRequestException("User cannot update another user's message.");

                msg.setStatus(MessageStatus.READ);
                msgRepo.save(msg);
        }
}
