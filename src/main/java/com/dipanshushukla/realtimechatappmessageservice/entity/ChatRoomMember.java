package com.dipanshushukla.realtimechatappmessageservice.entity;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.dipanshushukla.realtimechatappmessageservice.model.ChatRoomMemberId;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
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
public class ChatRoomMember {

    @EmbeddedId
    private ChatRoomMemberId chatRoomMemberId;

    @ManyToOne
    @JoinColumn(name = "chatId", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ChatRoom chatRoom;

    @ManyToOne
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private User user;

    @Builder.Default
    @Column(nullable = false)
    private boolean admin = false;
}
