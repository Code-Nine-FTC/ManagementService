package com.codenine.managementservice.repository;

import com.codenine.managementservice.entity.ChatRoom;
import com.codenine.managementservice.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

  @Query(
      "SELECT cr FROM ChatRoom cr JOIN cr.participants p WHERE p.id = :userId AND cr.isActive ="
          + " true ORDER BY cr.lastMessageAt DESC NULLS LAST, cr.createdAt DESC")
  List<ChatRoom> findByParticipantId(@Param("userId") Long userId);

  @Query(
      "SELECT cr FROM ChatRoom cr JOIN cr.participants p1 JOIN cr.participants p2 WHERE p1.id ="
          + " :user1Id AND p2.id = :user2Id AND SIZE(cr.participants) = 2 AND cr.isActive = true")
  Optional<ChatRoom> findDirectChatBetweenUsers(
      @Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

  @Query("SELECT cr FROM ChatRoom cr JOIN cr.participants p WHERE p IN :users AND cr.isActive = true")
  List<ChatRoom> findByParticipantsIn(@Param("users") List<User> users);
}
