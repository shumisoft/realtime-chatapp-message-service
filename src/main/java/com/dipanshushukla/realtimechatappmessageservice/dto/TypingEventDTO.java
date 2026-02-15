package com.dipanshushukla.realtimechatappmessageservice.dto;

import lombok.Data;

@Data
public class TypingEventDTO {
    private String chatId;
    private String username; // or userId
    private boolean typing;
}
