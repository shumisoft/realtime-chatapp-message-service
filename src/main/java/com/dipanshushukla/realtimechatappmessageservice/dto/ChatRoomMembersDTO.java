package com.dipanshushukla.realtimechatappmessageservice.dto;

import java.util.UUID;

import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoomMembers;
import com.dipanshushukla.realtimechatappmessageservice.model.ChatRoomMembersId;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoomMembersDTO {

    private Long chatId;
    private UserDTO user;

    @NotNull(message = "'userId' is required!")
    private UUID userId;

    public static ChatRoomMembersDTO fromEntity(ChatRoomMembers entity) {
        return ChatRoomMembersDTO.builder()
                .chatId(entity.getChatRoomMembersId().getChatId())
                .userId(entity.getChatRoomMembersId().getUserId())
                .user(UserDTO.builder()
                        .userId(entity.getUser().getUserId())
                        .fullName(entity.getUser().getFullName())
                        .email(entity.getUser().getEmail())
                        .avatar(entity.getUser().getAvatar())
                        .bio(entity.getUser().getBio())
                        .build())
                .build();
    }

    public ChatRoomMembers toEntity() {
        return new ChatRoomMembers(
                new ChatRoomMembersId(this.chatId, this.userId),
                null,
                null);
    }
}
