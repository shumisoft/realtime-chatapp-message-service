package com.dipanshushukla.realtimechatappmessageservice.model;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomMembersId implements Serializable {

    private Long chatId;
    private UUID userId;

}
