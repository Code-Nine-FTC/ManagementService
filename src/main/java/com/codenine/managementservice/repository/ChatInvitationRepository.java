package com.codenine.managementservice.repository;

import com.codenine.managementservice.entity.ChatInvitation;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatInvitationRepository extends JpaRepository<ChatInvitation, Long> {

  Optional<ChatInvitation> findByToken(String token);

  @Query(
      "SELECT ci FROM ChatInvitation ci WHERE ci.token = :token AND ci.isUsed = false AND"
          + " ci.expiresAt > :now")
  Optional<ChatInvitation> findValidInvitation(
      @Param("token") String token, @Param("now") LocalDateTime now);

  List<ChatInvitation> findByChatRoomId(Long chatRoomId);

  @Query("SELECT ci FROM ChatInvitation ci WHERE ci.guestEmail = :email AND ci.isUsed = false")
  List<ChatInvitation> findPendingInvitationsByEmail(@Param("email") String email);
}
