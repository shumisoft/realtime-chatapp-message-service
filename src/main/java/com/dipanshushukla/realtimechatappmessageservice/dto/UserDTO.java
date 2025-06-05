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
    private String username;

    private String email;

    private String fullName;
    private String avatar;
    private String bio;

    public static UserDTO fromEntity(User entity) {
        return UserDTO.builder()
                .userId(entity.getUserId())
                .fullName(entity.getFullName())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .avatar(entity.getAvatar())
                .bio(entity.getBio())
                .build();
    }

    public User toEntity() {
        return User.builder().userId(userId)
                .fullName(username)
                .email(email)
                .avatar(avatar)
                .bio(bio)
                .build();
    }
}
