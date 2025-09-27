package com.dipanshushukla.realtimechatappmessageservice.service;

import java.util.List;
import java.util.UUID;

import com.dipanshushukla.realtimechatappmessageservice.dto.ChatRoomMembersDTO;

public interface ChatRoomMembersService {

  void ensureMember(Long chatId, UUID requesterId);

  List<ChatRoomMembersDTO> getMembers(Long chatId, UUID requesterId);

  void addMember(ChatRoomMembersDTO dto, UUID requesterId);

  void removeMember(Long chatId, UUID userId, UUID requesterId);

  void assignAdmin(Long chatId, UUID userId, UUID requesterId);

  void unassignAdmin(Long chatId, UUID userId, UUID requesterId);

}