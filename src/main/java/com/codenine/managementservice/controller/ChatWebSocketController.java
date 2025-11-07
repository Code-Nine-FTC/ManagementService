package com.codenine.managementservice.controller;

import com.codenine.managementservice.dto.chat.ChatMessageDTO;
import com.codenine.managementservice.dto.chat.SendMessageRequest;
import com.codenine.managementservice.service.ChatService;
import java.security.Principal;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

  private final ChatService chatService;
  private final SimpMessagingTemplate messagingTemplate;

  public ChatWebSocketController(
      ChatService chatService, SimpMessagingTemplate messagingTemplate) {
    this.chatService = chatService;
    this.messagingTemplate = messagingTemplate;
  }

  @MessageMapping("/chat.send")
  public void sendMessage(@Payload SendMessageRequest request, Principal principal) {
    try {
      System.out.println("=== WebSocket Message Received ===");
      System.out.println("ChatRoomId: " + request.getChatRoomId());
      System.out.println("Content: " + request.getContent());
      
      // Obter o ID do usuário autenticado
      Long senderId = getUserIdFromPrincipal(principal);
      System.out.println("SenderId: " + senderId);

      // Salvar mensagem no banco de dados
      ChatMessageDTO message = chatService.sendMessage(request, senderId);
      System.out.println("Message saved with ID: " + message.getId());

      // Enviar mensagem para todos os participantes da sala via WebSocket
      messagingTemplate.convertAndSend(
          "/topic/chat.room." + request.getChatRoomId(), message);

    } catch (Exception e) {
      messagingTemplate.convertAndSendToUser(
          principal.getName(), "/queue/errors", "Error sending message: " + e.getMessage());
    }
  }

  @MessageMapping("/chat.typing")
  public void userTyping(@Payload TypingNotification notification, Principal principal) {
    try {
      Long userId = getUserIdFromPrincipal(principal);
      notification.setUserId(userId);

      messagingTemplate.convertAndSend(
          "/topic/chat.room." + notification.getChatRoomId() + ".typing", notification);
    } catch (Exception e) {
      // Silenciar erros de typing notification
    }
  }

  private Long getUserIdFromPrincipal(Principal principal) {
    if (principal instanceof Authentication) {
      Authentication auth = (Authentication) principal;
      Object details = auth.getPrincipal();
      if (details instanceof com.codenine.managementservice.entity.User) {
        return ((com.codenine.managementservice.entity.User) details).getId();
      }
    }
    throw new RuntimeException("Unable to extract user ID from principal");
  }

  // Classe interna para notificação de digitação
  public static class TypingNotification {
    private Long chatRoomId;
    private Long userId;
    private String userName;
    private boolean isTyping;

    public TypingNotification() {}

    public Long getChatRoomId() {
      return chatRoomId;
    }

    public void setChatRoomId(Long chatRoomId) {
      this.chatRoomId = chatRoomId;
    }

    public Long getUserId() {
      return userId;
    }

    public void setUserId(Long userId) {
      this.userId = userId;
    }

    public String getUserName() {
      return userName;
    }

    public void setUserName(String userName) {
      this.userName = userName;
    }

    public boolean isTyping() {
      return isTyping;
    }

    public void setTyping(boolean typing) {
      isTyping = typing;
    }
  }
}
