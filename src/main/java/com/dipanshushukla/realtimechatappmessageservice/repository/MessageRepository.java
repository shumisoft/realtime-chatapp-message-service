package com.dipanshushukla.realtimechatappmessageservice.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoom;
import com.dipanshushukla.realtimechatappmessageservice.entity.Message;

import jakarta.transaction.Transactional;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {
    List<Message> findByChatRoom(ChatRoom chatRoom);

    @Query("""
                SELECT m
                FROM Message m
                WHERE m.chatRoom.chatId = :chatId
                ORDER BY m.timestamp DESC
                LIMIT 1
            """)
    Message findLatestMessage(Long chatId);

    Page<Message> findByChatRoomOrderByTimestampDesc(ChatRoom chatRoom, Pageable pageable);

    @Modifying // Tells Spring Data JPA this is an UPDATE/DELETE, not a SELECT
    @Transactional // Required for modifying queries
    @Query("UPDATE Message m SET m.status = 'READ' " +
            "WHERE m.chatRoom.id = :chatRoomId " +
            "AND m.user.userId != :requesterId " + // Don't mark my own messages as read
            "AND m.status = 'UNREAD'")
    void markMessagesAsRead(@Param("chatRoomId") Long chatRoomId, @Param("requesterId") UUID requesterId);

}
