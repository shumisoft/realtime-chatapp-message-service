package com.dipanshushukla.realtimechatappmessageservice.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoom;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query(value = """
                SELECT cr.*
                FROM chat_room cr
                JOIN chat_room_members crm ON cr.chat_id = crm.chat_id
                LEFT JOIN (
                    SELECT chat_id, MAX(timestamp) AS last_message_time
                    FROM message
                    GROUP BY chat_id
                ) lm ON lm.chat_id = cr.chat_id
                WHERE crm.user_id = :userId
                ORDER BY
                    COALESCE(lm.last_message_time, cr.created_at) DESC
            """, countQuery = """
                SELECT COUNT(*)
                FROM chat_room_members
                WHERE user_id = :userId
            """, nativeQuery = true)
    Page<ChatRoom> findUserChatRoomsOrdered(UUID userId, Pageable pageable);

}
