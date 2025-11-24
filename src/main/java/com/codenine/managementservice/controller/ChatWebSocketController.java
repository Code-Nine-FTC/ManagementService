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
      
      // Verificar se é usuário normal ou guest
      MessageSender sender = getSenderFromPrincipal(principal);
      System.out.println("Sender Type: " + sender.type);
      System.out.println("Sender ID: " + sender.id);
      System.out.println("Sender Name: " + sender.name);

      // Salvar mensagem no banco de dados
      ChatMessageDTO message = chatService.sendMessageFromAny(request, sender);
      System.out.println("Message saved with ID: " + message.getId());

      // Enviar mensagem para todos os participantes da sala via WebSocket
      messagingTemplate.convertAndSend(
          "/topic/chat.room." + request.getChatRoomId(), message);

    } catch (Exception e) {
      e.printStackTrace();
      messagingTemplate.convertAndSendToUser(
          principal.getName(), "/queue/errors", "Error sending message: " + e.getMessage());
    }
  }

  @MessageMapping("/chat.typing")
  public void userTyping(@Payload TypingNotification notification, Principal principal) {
    try {
      MessageSender sender = getSenderFromPrincipal(principal);
      notification.setUserId(sender.id);
      notification.setUserName(sender.name);

      messagingTemplate.convertAndSend(
          "/topic/chat.room." + notification.getChatRoomId() + ".typing", notification);
    } catch (Exception e) {
      // Silenciar erros de typing notification
    }
  }

  private MessageSender getSenderFromPrincipal(Principal principal) {
    if (principal instanceof Authentication) {
      Authentication auth = (Authentication) principal;
      Object details = auth.getPrincipal();
      
      // Verificar se é um usuário normal
      if (details instanceof com.codenine.managementservice.entity.User) {
        com.codenine.managementservice.entity.User user = 
            (com.codenine.managementservice.entity.User) details;
        return new MessageSender("USER", user.getId(), user.getName(), user.getEmail());
      }
      
      // Verificar se é um guest user
      if (details instanceof com.codenine.managementservice.security.GuestUserDetails) {
        com.codenine.managementservice.security.GuestUserDetails guestDetails = 
            (com.codenine.managementservice.security.GuestUserDetails) details;
        return new MessageSender("GUEST", guestDetails.getGuestId(), 
            guestDetails.getGuestName(), guestDetails.getUsername());
      }
    }
    throw new RuntimeException("Unable to extract sender information from principal");
  }

  // Classe interna para representar o remetente (User ou Guest)
  public static class MessageSender {
    public final String type; // "USER" ou "GUEST"
    public final Long id;
    public final String name;
    public final String email;

    public MessageSender(String type, Long id, String name, String email) {
      this.type = type;
      this.id = id;
      this.name = name;
      this.email = email;
    }
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
