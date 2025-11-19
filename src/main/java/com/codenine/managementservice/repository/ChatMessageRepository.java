package com.codenine.managementservice.repository;

import com.codenine.managementservice.entity.ChatMessage;
import com.codenine.managementservice.entity.ChatRoom;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

  @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom = :chatRoom ORDER BY m.sentAt DESC")
  List<ChatMessage> findByChatRoomOrderBySentAtDesc(
      @Param("chatRoom") ChatRoom chatRoom, Pageable pageable);

  @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom.id = :chatRoomId ORDER BY m.sentAt ASC")
  List<ChatMessage> findByChatRoomIdOrderBySentAtAsc(@Param("chatRoomId") Long chatRoomId);

  @Query(
      "SELECT m FROM ChatMessage m WHERE m.chatRoom.id = :chatRoomId ORDER BY m.sentAt DESC"
          + " LIMIT 1")
  ChatMessage findLastMessageByChatRoomId(@Param("chatRoomId") Long chatRoomId);

  @Query(
      "SELECT COUNT(m) FROM ChatMessage m WHERE m.chatRoom.id = :chatRoomId AND m.isRead = false"
          + " AND m.sender.id != :userId")
  Long countUnreadMessagesByChatRoomAndUser(
      @Param("chatRoomId") Long chatRoomId, @Param("userId") Long userId);
}
