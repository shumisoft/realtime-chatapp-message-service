package com.dipanshushukla.realtimechatappmessageservice.dto;

import com.dipanshushukla.realtimechatappmessageservice.model.OnlineStatusType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserStatusDTO {
    private String userId;
    private OnlineStatusType status;
    private String lastSeen; // Timestamp (Epoch millis as String) or null if never seen
}