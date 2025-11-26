package com.dipanshushukla.realtimechatappmessageservice.entity;

import com.dipanshushukla.realtimechatappmessageservice.model.ChatRoomMembersId;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoomMembers {

    @EmbeddedId
    private ChatRoomMembersId chatRoomMembersId;

    @ManyToOne
    @JoinColumn(name = "chatId", insertable = false, updatable = false)
    private ChatRoom chatRoom;

    @ManyToOne
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private User user;
}
