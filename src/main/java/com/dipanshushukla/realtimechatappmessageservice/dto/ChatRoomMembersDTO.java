package com.dipanshushukla.realtimechatappmessageservice.dto;

import java.util.UUID;

import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoomMember;
import com.dipanshushukla.realtimechatappmessageservice.model.ChatRoomMemberId;

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

    public static ChatRoomMembersDTO fromEntity(ChatRoomMember entity) {
        return ChatRoomMembersDTO.builder()
                .chatId(entity.getChatRoomMemberId().getChatId())
                .userId(entity.getChatRoomMemberId().getUserId())
                .user(UserDTO.fromEntity(entity.getUser()))
                .admin(entity.isAdmin())
                .build();
    }

    public ChatRoomMember toEntity() {
        return ChatRoomMember.builder().chatRoomMemberId(new ChatRoomMemberId(this.chatId, this.userId))
                .admin(this.admin).build();
    }
}
