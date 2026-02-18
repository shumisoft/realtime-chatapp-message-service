package com.dipanshushukla.realtimechatappmessageservice.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dipanshushukla.realtimechatappmessageservice.dto.UserStatusDTO;
import com.dipanshushukla.realtimechatappmessageservice.service.UserPresenceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user-status")
@RequiredArgsConstructor
public class UserStatusController {

    private final UserPresenceService userPresenceService;

    @PostMapping("/batch")
    public ResponseEntity<Map<String, UserStatusDTO>> getUserStatuses(@RequestBody List<String> userIds) {
        return ResponseEntity.ok(userPresenceService.getUserStatuses(userIds));
    }
}
