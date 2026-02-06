package com.dipanshushukla.realtimechatappmessageservice.dto;

import java.sql.Timestamp;

import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoom;
import com.dipanshushukla.realtimechatappmessageservice.model.ChatRoomType;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoomDTO {

    private Long chatId;

    @NotNull(message = "'name' is required!")
    private String name;

    @NotNull(message = "'type' is required!")
    private ChatRoomType type;

    private Timestamp createdAt;

    @NotNull(message = "'description' is required!")
    private String description;

    private MessageDTO latestMessage;

    private ChatRoomMembersDTO[] members;

    public static ChatRoomDTO fromEntity(ChatRoom entity) {
        return ChatRoomDTO.builder()
                .chatId(entity.getChatId())
                .name(entity.getName())
                .type(entity.getType())
                .createdAt(entity.getCreatedAt())
                .description(entity.getDescription())
                .members(entity.getMembers().stream().map((member) -> ChatRoomMembersDTO.fromEntity(member))
                        .toArray(ChatRoomMembersDTO[]::new))
                .build();
    }

    public ChatRoom toEntity() {
        return new ChatRoom(this.name, this.type, this.description);
    }
}
