package com.codenine.managementservice.controller;

import com.codenine.managementservice.dto.chat.ChatInvitationDTO;
import com.codenine.managementservice.dto.chat.ChatMessageDTO;
import com.codenine.managementservice.dto.chat.ChatRoomDTO;
import com.codenine.managementservice.dto.chat.CreateChatRoomRequest;
import com.codenine.managementservice.dto.chat.InviteGuestRequest;
import com.codenine.managementservice.dto.chat.JoinChatResponse;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.service.ChatService;
import com.codenine.managementservice.service.GuestUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat", description = "Chat management endpoints")
public class ChatController {

  private final ChatService chatService;
  private final GuestUserService guestUserService;

  public ChatController(ChatService chatService, GuestUserService guestUserService) {
    this.chatService = chatService;
    this.guestUserService = guestUserService;
  }

  @PostMapping("/rooms")
  @Operation(summary = "Create a new chat room")
  public ResponseEntity<ChatRoomDTO> createChatRoom(
      @RequestBody CreateChatRoomRequest request, Authentication authentication) {
    User user = (User) authentication.getPrincipal();
    ChatRoomDTO chatRoom = chatService.createChatRoom(request, user.getId());
    return ResponseEntity.ok(chatRoom);
  }

  @GetMapping("/rooms")
  @Operation(summary = "Get all chat rooms for the current user")
  public ResponseEntity<List<ChatRoomDTO>> getUserChatRooms(Authentication authentication) {
    User user = (User) authentication.getPrincipal();
    List<ChatRoomDTO> chatRooms = chatService.getUserChatRooms(user.getId());
    return ResponseEntity.ok(chatRooms);
  }

  @GetMapping("/rooms/active")
  @Operation(summary = "Get all active chat rooms for the current user")
  public ResponseEntity<List<ChatRoomDTO>> getActiveUserChatRooms(Authentication authentication) {
    User user = (User) authentication.getPrincipal();
    List<ChatRoomDTO> chatRooms = chatService.getActiveUserChatRooms(user.getId());
    return ResponseEntity.ok(chatRooms);
  }

  @GetMapping("/rooms/{chatRoomId}")
  @Operation(summary = "Get chat room details")
  public ResponseEntity<ChatRoomDTO> getChatRoom(
      @PathVariable Long chatRoomId, Authentication authentication) {
    
    System.out.println("=== GET CHAT ROOM ===");
    System.out.println("chatRoomId solicitado: " + chatRoomId);
    
    Object principal = authentication.getPrincipal();
    System.out.println("Tipo de principal: " + principal.getClass().getName());
    
    // Se for guest, verificar se o chatRoomId corresponde ao chat do guest
    if (principal instanceof com.codenine.managementservice.security.GuestUserDetails) {
      System.out.println("É um GUEST USER");
      com.codenine.managementservice.security.GuestUserDetails guestDetails = 
          (com.codenine.managementservice.security.GuestUserDetails) principal;
      
      System.out.println("Email do guest: " + guestDetails.getUsername());
      
      // Buscar o guest pelo email para pegar o chat_room_id
      com.codenine.managementservice.dto.chat.GuestUserDTO guestUser = 
          guestUserService.getGuestByEmail(guestDetails.getUsername());
      
      System.out.println("Guest ID: " + guestUser.getId());
      System.out.println("Chat Room ID do guest: " + guestUser.getChatRoomId());
      
      // Verificar se o guest está tentando acessar seu próprio chat
      if (!chatRoomId.equals(guestUser.getChatRoomId())) {
        System.out.println("ACESSO NEGADO - Guest tentando acessar chat de outro usuário");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      }
      
      System.out.println("Buscando chat room do guest...");
      // Buscar o chat room
      com.codenine.managementservice.dto.chat.ChatRoomDTO chatRoom = 
          guestUserService.getGuestChatRoom(guestUser.getId());
      
      System.out.println("Chat room encontrado: " + chatRoom.getName());
      return ResponseEntity.ok(chatRoom);
    }
    
    // Se for usuário normal
    User user = (User) principal;
    List<ChatRoomDTO> chatRooms = chatService.getUserChatRooms(user.getId());
    return chatRooms.stream()
        .filter(room -> room.getId().equals(chatRoomId))
        .findFirst()
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/rooms/{chatRoomId}/messages")
  @Operation(summary = "Get all messages from a chat room")
  public ResponseEntity<List<ChatMessageDTO>> getChatRoomMessages(
      @PathVariable Long chatRoomId, Authentication authentication) {
    
    System.out.println("=== GET CHAT ROOM MESSAGES ===");
    System.out.println("chatRoomId solicitado: " + chatRoomId);
    
    Object principal = authentication.getPrincipal();
    System.out.println("Tipo de principal: " + principal.getClass().getName());
    
    // Se for guest
    if (principal instanceof com.codenine.managementservice.security.GuestUserDetails) {
      System.out.println("É um GUEST USER");
      com.codenine.managementservice.security.GuestUserDetails guestDetails = 
          (com.codenine.managementservice.security.GuestUserDetails) principal;
      
      System.out.println("Email do guest: " + guestDetails.getUsername());
      
      // Buscar o guest pelo email para pegar o chat_room_id
      com.codenine.managementservice.dto.chat.GuestUserDTO guestUser = 
          guestUserService.getGuestByEmail(guestDetails.getUsername());
      
      System.out.println("Guest ID: " + guestUser.getId());
      System.out.println("Chat Room ID do guest: " + guestUser.getChatRoomId());
      
      // Verificar se o guest está tentando acessar seu próprio chat
      if (!chatRoomId.equals(guestUser.getChatRoomId())) {
        System.out.println("ACESSO NEGADO - Guest tentando acessar mensagens de outro chat");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      }
      
      System.out.println("Buscando mensagens do chat...");
      // Buscar mensagens do chat (passar null como userId já que guest não tem ID de User)
      List<ChatMessageDTO> messages = chatService.getChatRoomMessages(chatRoomId, null);
      
      System.out.println("Mensagens encontradas: " + messages.size());
      return ResponseEntity.ok(messages);
    }
    
    // Se for usuário normal
    User user = (User) principal;
    List<ChatMessageDTO> messages = chatService.getChatRoomMessages(chatRoomId, user.getId());
    return ResponseEntity.ok(messages);
  }

  @PutMapping("/rooms/{chatRoomId}/messages/read")
  @Operation(summary = "Mark all messages in a chat room as read")
  public ResponseEntity<Void> markMessagesAsRead(
      @PathVariable Long chatRoomId, Authentication authentication) {
    
    Object principal = authentication.getPrincipal();
    
    // Se for guest
    if (principal instanceof com.codenine.managementservice.security.GuestUserDetails) {
      com.codenine.managementservice.security.GuestUserDetails guestDetails = 
          (com.codenine.managementservice.security.GuestUserDetails) principal;
      
      // Buscar o guest pelo email para pegar o chat_room_id
      com.codenine.managementservice.dto.chat.GuestUserDTO guestUser = 
          guestUserService.getGuestByEmail(guestDetails.getUsername());
      
      // Verificar se o guest está tentando acessar seu próprio chat
      if (!chatRoomId.equals(guestUser.getChatRoomId())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      }
      
      // Marcar mensagens como lidas (passar null como userId)
      chatService.markMessagesAsRead(chatRoomId, null);
      return ResponseEntity.ok().build();
    }
    
    // Se for usuário normal
    User user = (User) principal;
    chatService.markMessagesAsRead(chatRoomId, user.getId());
    return ResponseEntity.ok().build();
  }

  @GetMapping("/direct/{userId}")
  @Operation(summary = "Get or create a direct chat with another user")
  public ResponseEntity<ChatRoomDTO> getOrCreateDirectChat(
      @PathVariable Long userId, Authentication authentication) {
    User user = (User) authentication.getPrincipal();
    ChatRoomDTO chatRoom = chatService.getOrCreateDirectChat(user.getId(), userId);
    return ResponseEntity.ok(chatRoom);
  }

  @PutMapping("/rooms/{chatRoomId}/close")
  @Operation(summary = "Close a chat room (mark as inactive)")
  public ResponseEntity<Void> closeChatRoom(
      @PathVariable Long chatRoomId, Authentication authentication) {
    User user = (User) authentication.getPrincipal();
    chatService.closeChatRoom(chatRoomId, user.getId());
    return ResponseEntity.ok().build();
  }

  @PutMapping("/rooms/{chatRoomId}/reopen")
  @Operation(summary = "Reopen a chat room (mark as active)")
  public ResponseEntity<Void> reopenChatRoom(
      @PathVariable Long chatRoomId, Authentication authentication) {
    User user = (User) authentication.getPrincipal();
    chatService.reopenChatRoom(chatRoomId, user.getId());
    return ResponseEntity.ok().build();
  }

  @PostMapping("/invite")
  @Operation(summary = "Invite a guest to chat by email - Requires ADMIN or Pharmacy (SECTION_2)")
  @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('SECTION_2')")
  public ResponseEntity<ChatInvitationDTO> inviteGuest(
      @RequestBody InviteGuestRequest request, Authentication authentication) {
    
    // Log para debug
    System.out.println("=== DEBUG INVITE ===");
    System.out.println("Request received: " + request);
    System.out.println("Guest Email: " + request.getGuestEmail());
    System.out.println("Guest Name: " + request.getGuestName());
    System.out.println("==================");
    
    User user = (User) authentication.getPrincipal();
    ChatInvitationDTO invitation = chatService.inviteGuestByEmail(request, user.getId());
    return ResponseEntity.ok(invitation);
  }

  @GetMapping("/join/{token}")
  @Operation(summary = "Join chat room using invitation token (no authentication required)")
  public ResponseEntity<JoinChatResponse> joinChatByToken(@PathVariable String token) {
    JoinChatResponse response = chatService.joinChatByToken(token);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/guest/{token}")
  @Operation(summary = "Get chat room details by token (for guest access)")
  public ResponseEntity<ChatRoomDTO> getChatRoomByToken(@PathVariable String token) {
    ChatRoomDTO chatRoom = chatService.getChatRoomByToken(token);
    return ResponseEntity.ok(chatRoom);
  }

  @GetMapping("/guest/{token}/messages")
  @Operation(summary = "Get messages by token (for guest access)")
  public ResponseEntity<List<ChatMessageDTO>> getMessagesByToken(@PathVariable String token) {
    ChatRoomDTO chatRoom = chatService.getChatRoomByToken(token);
    // Guest não tem userId, então passamos null ou criamos lógica especial
    List<ChatMessageDTO> messages =
        chatService.getChatRoomMessages(chatRoom.getId(), null);
    return ResponseEntity.ok(messages);
  }
}
