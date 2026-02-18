package com.dipanshushukla.realtimechatappmessageservice.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.dipanshushukla.realtimechatappmessageservice.dto.UserStatusDTO;
import com.dipanshushukla.realtimechatappmessageservice.model.OnlineStatusType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPresenceService {

    private final StringRedisTemplate redisTemplate;

    private static final long HEARTBEAT_TTL = 40; // Seconds
    private static final String ONLINE_KEY_PREFIX = "user:online:";
    private static final String LAST_SEEN_KEY_PREFIX = "user:last_seen:";

    public void markUserOnline(String userId) {
        String onlineKey = ONLINE_KEY_PREFIX + userId;
        String lastSeenKey = LAST_SEEN_KEY_PREFIX + userId;
        String timestamp = String.valueOf(System.currentTimeMillis());

        // 1. Set "Online" status with short TTL (The Jitter Buffer)
        redisTemplate.opsForValue().set(onlineKey, "true", Duration.ofSeconds(HEARTBEAT_TTL));

        // 2. Update "Last Seen" permanently
        redisTemplate.opsForValue().set(lastSeenKey, timestamp);
    }

    public void markUserOffline(String userId) {
        String onlineKey = ONLINE_KEY_PREFIX + userId;
        redisTemplate.delete(onlineKey); // Immediate offline
    }

    public boolean isUserOnline(String userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(ONLINE_KEY_PREFIX + userId));
    }

    public String getLastSeen(String userId) {
        return redisTemplate.opsForValue().get(LAST_SEEN_KEY_PREFIX + userId);
    }

    public Map<String, UserStatusDTO> getUserStatuses(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        // 1. Prepare the keys list (Interleaved)
        List<String> keys = new ArrayList<>();
        for (String userId : userIds) {
            keys.add(ONLINE_KEY_PREFIX + userId);
            keys.add(LAST_SEEN_KEY_PREFIX + userId);
        }

        // 2. MGET: Fetch all 40 values in ONE network call
        List<String> values = redisTemplate.opsForValue().multiGet(keys);

        // 3. Process the results in pairs
        Map<String, UserStatusDTO> result = new HashMap<>();

        for (int i = 0; i < userIds.size(); i++) {
            String userId = userIds.get(i);

            // Since we packed 2 keys per user, the index in 'values' list is i * 2
            String onlineValue = values.get(i * 2); // Even index: Online Status
            String lastSeenValue = values.get(i * 2 + 1); // Odd index: Last Seen Timestamp

            boolean isOnline = (onlineValue != null);

            UserStatusDTO dto = UserStatusDTO.builder()
                    .userId(userId)
                    .status(isOnline ? OnlineStatusType.ONLINE : OnlineStatusType.OFFLINE)
                    .lastSeen(lastSeenValue) // Pass the timestamp (or null)
                    .build();

            result.put(userId, dto);
        }

        log.info(result.toString());

        return result;
    }
}
