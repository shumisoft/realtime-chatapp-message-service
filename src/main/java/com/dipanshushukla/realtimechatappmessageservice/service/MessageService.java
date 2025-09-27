package com.dipanshushukla.realtimechatappmessageservice.service;

import java.util.UUID;

import org.springframework.data.domain.Page;

import com.dipanshushukla.realtimechatappmessageservice.dto.MessageDTO;

public interface MessageService {

  MessageDTO createMessage(MessageDTO dto, UUID requesterId);

  Page<MessageDTO> getMessagesFromChatRoom(Long chatRoomId, UUID requesterId, int page, int size);

  MessageDTO getMessage(String messageId, UUID requesterId);

  void updateMessageStatus(String messageId, UUID requesterId);

}