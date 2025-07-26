package com.dipanshushukla.realtimechatappmessageservice.dto;

import lombok.Data;

@Data
public class TypingEventDTO {
    private String chatId;
    private String userId;
    private boolean typing;
}
