package com.dipanshushukla.realtimechatappmessageservice.dto;

import java.sql.Timestamp;
import java.util.UUID;

import com.dipanshushukla.realtimechatappmessageservice.entity.Message;
import com.dipanshushukla.realtimechatappmessageservice.model.MessageStatus;
import com.dipanshushukla.realtimechatappmessageservice.model.MessageType;
import com.dipanshushukla.realtimechatappmessageservice.util.UlidUtils;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageDTO {

    private String messageId;

    private Long chatRoomId;

    private UUID userId;

    @NotEmpty
    private String content;

    private Timestamp timestamp;

    @Builder.Default
    private MessageStatus status = MessageStatus.UNREAD;

    @Builder.Default
    private MessageType type = MessageType.TEXT;

    @Builder.Default
    private boolean edited = false;

    public static MessageDTO fromEntity(Message entity) {
        return MessageDTO.builder()
                .messageId(UlidUtils.toString(entity.getMessageId()))
                .chatRoomId(entity.getChatRoom().getChatId())
                .userId(entity.getUser().getUserId())
                .content(entity.getContent())
                .timestamp(entity.getTimestamp())
                .status(entity.getStatus())
                .type(entity.getType())
                .edited(entity.isEdited())
                .build();
    }

    public Message toEntity() {
        return Message.builder()
                .messageId(UlidUtils.toBytes(this.messageId))
                .content(this.content)
                .timestamp(this.timestamp)
                .status(this.status)
                .type(this.type)
                .edited(edited)
                .build();
    }

}
