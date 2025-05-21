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

    @Builder.Default
    private boolean admin = false;

    @NotNull(message = "'userId' is required!")
    private UUID userId;

    public static ChatRoomMembersDTO fromEntity(ChatRoomMembers entity) {
        return ChatRoomMembersDTO.builder()
                .chatId(entity.getChatRoomMembersId().getChatId())
                .userId(entity.getChatRoomMembersId().getUserId())
                .user(UserDTO.fromEntity(entity.getUser()))
                .admin(entity.isAdmin())
                .build();
    }

    public ChatRoomMembers toEntity() {
        return ChatRoomMembers.builder().chatRoomMembersId(new ChatRoomMembersId(this.chatId, this.userId))
                .admin(this.admin).build();
    }
}
