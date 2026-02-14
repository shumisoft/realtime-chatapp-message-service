package com.dipanshushukla.realtimechatappmessageservice.entity;

import java.sql.Timestamp;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.dipanshushukla.realtimechatappmessageservice.model.MessageStatus;
import com.dipanshushukla.realtimechatappmessageservice.model.MessageType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Message {

    @Id
    @Column(name = "message_id")
    private String messageId;

    @ManyToOne
    @JoinColumn(name = "chatId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ChatRoom chatRoom;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, updatable = false)

    private Timestamp timestamp;

    @Builder.Default
    @Enumerated(value = EnumType.STRING)
    private MessageStatus status = MessageStatus.UNREAD;

    @Builder.Default
    @Enumerated(value = EnumType.STRING)
    private MessageType type = MessageType.TEXT;

    @Builder.Default
    @Column(nullable = false)
    private boolean edited = false;

}
