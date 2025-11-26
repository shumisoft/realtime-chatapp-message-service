package com.dipanshushukla.realtimechatappmessageservice.dto;

import java.sql.Timestamp;
import java.util.UUID;

import com.dipanshushukla.realtimechatappmessageservice.entity.Message;
import com.dipanshushukla.realtimechatappmessageservice.model.MessageStatus;
import com.dipanshushukla.realtimechatappmessageservice.model.MessageType;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageDTO {

    private Long messageId;

    @NotNull
    private Long chatRoomId;

    @NotNull
    private UUID userId;

    @NotEmpty
    private String content;

    private Timestamp timestamp;

    private MessageStatus status = MessageStatus.UNREAD;

    private MessageType type = MessageType.TEXT;

    public static MessageDTO fromEntity(Message entity) {
        return MessageDTO.builder()
                .messageId(entity.getMessageId())
                .chatRoomId(entity.getChatRoom().getChatId())
                .userId(entity.getUser().getUserId())
                .content(entity.getContent())
                .timestamp(entity.getTimestamp())
                .status(entity.getStatus())
                .type(entity.getType())
                .build();
    }

    public Message toEntity() {
        return new Message(
                this.messageId,
                null,
                null,
                this.content,
                this.timestamp,
                this.status,
                this.type);
    }
}
