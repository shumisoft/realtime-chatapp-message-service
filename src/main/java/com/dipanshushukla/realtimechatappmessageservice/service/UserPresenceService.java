package com.dipanshushukla.realtimechatappmessageservice.service;

import java.util.List;
import java.util.Map;

import com.dipanshushukla.realtimechatappmessageservice.dto.UserStatusDTO;

public interface UserPresenceService {

  void markUserOnline(String userId);

  void markUserOffline(String userId);

  boolean isUserOnline(String userId);

  String getLastSeen(String userId);

  Map<String, UserStatusDTO> getUserStatuses(List<String> userIds);

}