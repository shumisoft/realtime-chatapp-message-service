package com.dipanshushukla.realtimechatappmessageservice.service;

import java.util.UUID;

import org.springframework.data.domain.Page;

import com.dipanshushukla.realtimechatappmessageservice.dto.ChatRoomDTO;

public interface ChatRoomService {

  ChatRoomDTO createChatRoom(ChatRoomDTO dto, UUID creatorId);

  ChatRoomDTO getChatRoomById(Long chatId, UUID requesterId);

  void updateChatRoom(Long chatId, ChatRoomDTO dto, UUID requesterId);

  void deleteChatRoom(Long chatId, UUID requesterId);

  Page<ChatRoomDTO> getMyChatRooms(UUID userId, int page, int size);

}