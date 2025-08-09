package com.dipanshushukla.realtimechatappmessageservice.dto;

import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;

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

    private String icon;

    private MessageDTO latestMessage;

    private ChatRoomMembersDTO[] members;

    private Set<UUID> memberIds;

    public static ChatRoomDTO fromEntity(ChatRoom entity) {
        return ChatRoomDTO.builder()
                .chatId(entity.getChatId())
                .name(entity.getName())
                .type(entity.getType())
                .createdAt(entity.getCreatedAt())
                .description(entity.getDescription())
                .icon(entity.getIcon())
                .members(entity.getMembers() != null
                        ? entity.getMembers().stream()
                                .map(ChatRoomMembersDTO::fromEntity)
                                .toArray(ChatRoomMembersDTO[]::new)
                        : new ChatRoomMembersDTO[0])
                .build();
    }

    public ChatRoom toEntity() {
        return ChatRoom.builder()
                .name(this.name)
                .type(this.type)
                .description(this.description)
                .icon(this.icon)
                .build();
    }
}