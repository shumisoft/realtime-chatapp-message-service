package com.dipanshushukla.realtimechatappmessageservice.dto;

import com.dipanshushukla.realtimechatappmessageservice.entity.User;

import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {

    private UUID userId;

    public static UserDTO fromEntity(User entity) {
        return UserDTO.builder()
                .userId(entity.getUserId())
                .build();
    }

    public User toEntity() {
        return new User(this.userId);
    }
}
