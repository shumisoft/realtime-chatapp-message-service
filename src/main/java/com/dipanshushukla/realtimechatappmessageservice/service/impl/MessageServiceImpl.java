package com.dipanshushukla.realtimechatappmessageservice.service.impl;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.dipanshushukla.realtimechatappmessageservice.dto.MessageDTO;
import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoom;
import com.dipanshushukla.realtimechatappmessageservice.entity.Message;
import com.dipanshushukla.realtimechatappmessageservice.entity.User;
import com.dipanshushukla.realtimechatappmessageservice.exception.BadRequestException;
import com.dipanshushukla.realtimechatappmessageservice.exception.ResourceNotFoundException;
import com.dipanshushukla.realtimechatappmessageservice.model.ChatRoomType;
import com.dipanshushukla.realtimechatappmessageservice.model.MessageStatus;
import com.dipanshushukla.realtimechatappmessageservice.repository.ChatRoomMemberRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.ChatRoomRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.MessageRepository;
import com.dipanshushukla.realtimechatappmessageservice.repository.UserRepository;
import com.dipanshushukla.realtimechatappmessageservice.service.MessageService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MessageServiceImpl implements MessageService {

        private final MessageRepository messageRepository;
        private final ChatRoomRepository chatRoomRepository;
        private final UserRepository userRepository;
        private final ChatRoomMemberRepository chatRoomMembersRepository;

        private void ensureMember(Long chatId, UUID requesterId) {
                ChatRoom room = chatRoomRepository.findById(chatId)
                                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));

                User user = userRepository.findById(requesterId)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                boolean isMember = chatRoomMembersRepository.existsByChatRoomAndUser(room, user);

                if (!isMember)
                        throw new BadRequestException("User                 if (!isMember)\n" + //
                                        "is not part of this chat room.");
        }

        @Override
        public MessageDTO createMessage(MessageDTO dto, UUID requesterId) {
                ensureMember(dto.getChatRoomId(), requesterId);

                User user = userRepository.findById(requesterId)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                ChatRoom room = chatRoomRepository.findById(dto.getChatRoomId())
                                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));

                Message message = dto.toEntity();
                message.setChatRoom(room);
                message.setUser(user);

                message = messageRepository.save(message);

                return MessageDTO.fromEntity(message);
        }

        @Override
        public Page<MessageDTO> getMessagesFromChatRoom(Long chatRoomId, UUID requesterId, int page, int size) {
                ensureMember(chatRoomId, requesterId);

                ChatRoom room = chatRoomRepository.findById(chatRoomId)
                                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));

                if (room.getType() == ChatRoomType.DIRECT_MESSAGE)
                        messageRepository.markMessagesAsRead(chatRoomId, requesterId);

                PageRequest pageable = PageRequest.of(page, size);

                Page<Message> paged = messageRepository.findByChatRoomOrderByTimestampDesc(room, pageable);

                return paged.map(MessageDTO::fromEntity);
        }

        @Override
        public MessageDTO getMessage(String messageId, UUID requesterId) {
                Message msg = messageRepository.findById(messageId)
                                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

                ensureMember(msg.getChatRoom().getChatId(), requesterId);

                return MessageDTO.fromEntity(msg);
        }

        @Override
        public void updateMessageStatus(String messageId, UUID requesterId) {
                Message msg = messageRepository.findById(messageId)
                                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

                if (!msg.getUser().getUserId().equals(requesterId))
                        throw new BadRequestException("User cannot update another user's message.");

                msg.setStatus(MessageStatus.READ);
                messageRepository.save(msg);
        }
}
