package com.dipanshushukla.realtimechatappmessageservice.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoom;
import com.dipanshushukla.realtimechatappmessageservice.entity.ChatRoomMember;
import com.dipanshushukla.realtimechatappmessageservice.entity.User;
import com.dipanshushukla.realtimechatappmessageservice.model.ChatRoomMemberId;

@Repository
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, ChatRoomMemberId> {

    boolean existsByChatRoomAndUser(ChatRoom chatRoom, User user);

    List<ChatRoomMember> findByChatRoom(ChatRoom chatRoom);

    ChatRoomMember findByChatRoomAndUser(ChatRoom chatRoom, User user);

    Page<ChatRoomMember> findByUser(User user, Pageable pageable);

}
