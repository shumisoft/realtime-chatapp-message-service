package com.dipanshushukla.realtimechatappmessageservice.dto;

import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoomMembers;
import com.dipanshushukla.realtimechatappmessageservice.model.ChatRoomMembersId;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoomMembersDTO {

    private Long chatId;

    @NotNull(message = "'userId' is required!")
    private UUID userId;

    public static ChatRoomMembersDTO fromEntity(ChatRoomMembers entity) {
        return ChatRoomMembersDTO.builder()
                .chatId(entity.getChatRoomMembersId().getChatId())
                .userId(entity.getChatRoomMembersId().getUserId())
                .build();
    }

    public ChatRoomMembers toEntity() {
        return new ChatRoomMembers(
                new ChatRoomMembersId(this.chatId, this.userId),
                null,
                null);
    }
}
