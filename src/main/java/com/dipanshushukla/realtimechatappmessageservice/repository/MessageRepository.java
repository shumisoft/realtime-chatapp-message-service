package com.dipanshushukla.realtimechatappmessageservice.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoom;
import com.dipanshushukla.realtimechatappmessageservice.entity.Message;

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

}
