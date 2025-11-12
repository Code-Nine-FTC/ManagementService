package com.codenine.managementservice.service;

import com.codenine.managementservice.controller.ChatWebSocketController;
import com.codenine.managementservice.dto.chat.*;
import com.codenine.managementservice.entity.ChatInvitation;
import com.codenine.managementservice.entity.ChatMessage;
import com.codenine.managementservice.entity.ChatRoom;
import com.codenine.managementservice.entity.GuestUser;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.repository.ChatInvitationRepository;
import com.codenine.managementservice.repository.ChatMessageRepository;
import com.codenine.managementservice.repository.ChatRoomRepository;
import com.codenine.managementservice.repository.GuestUserRepository;
import com.codenine.managementservice.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {

  private final ChatRoomRepository chatRoomRepository;
  private final ChatMessageRepository chatMessageRepository;
  private final UserRepository userRepository;
  private final GuestUserRepository guestUserRepository;
  private final ChatInvitationRepository chatInvitationRepository;
  private final EmailService emailService;

  @Value("${app.frontend.url:http://localhost:3000}")
  private String frontendUrl;

  public ChatService(
      ChatRoomRepository chatRoomRepository,
      ChatMessageRepository chatMessageRepository,
      UserRepository userRepository,
      GuestUserRepository guestUserRepository,
      ChatInvitationRepository chatInvitationRepository,
      EmailService emailService) {
    this.chatRoomRepository = chatRoomRepository;
    this.chatMessageRepository = chatMessageRepository;
    this.userRepository = userRepository;
    this.guestUserRepository = guestUserRepository;
    this.chatInvitationRepository = chatInvitationRepository;
    this.emailService = emailService;
  }

  @Transactional
  public ChatRoomDTO createChatRoom(CreateChatRoomRequest request, Long currentUserId) {
    ChatRoom chatRoom = new ChatRoom();
    chatRoom.setName(request.getName());
    chatRoom.setCreatedAt(LocalDateTime.now());

    List<User> participants = new ArrayList<>();
    User currentUser =
        userRepository
            .findById(currentUserId)
            .orElseThrow(() -> new RuntimeException("User not found"));
    participants.add(currentUser);

    for (Long participantId : request.getParticipantIds()) {
      if (!participantId.equals(currentUserId)) {
        User user =
            userRepository
                .findById(participantId)
                .orElseThrow(() -> new RuntimeException("Participant not found: " + participantId));
        participants.add(user);
      }
    }

    chatRoom.setParticipants(participants);
    chatRoom = chatRoomRepository.save(chatRoom);

    return convertToRoomDTO(chatRoom);
  }

  @Transactional
  public ChatMessageDTO sendMessage(SendMessageRequest request, Long senderId) {
    ChatRoom chatRoom =
        chatRoomRepository
            .findById(request.getChatRoomId())
            .orElseThrow(() -> new RuntimeException("Chat room not found"));

    User sender =
        userRepository
            .findById(senderId)
            .orElseThrow(() -> new RuntimeException("Sender not found"));

    // Verificar se o usuário é participante da sala
    boolean isParticipant =
        chatRoom.getParticipants().stream().anyMatch(p -> p.getId().equals(senderId));
    if (!isParticipant) {
      throw new RuntimeException("User is not a participant of this chat room");
    }

    ChatMessage message = new ChatMessage();
    message.setChatRoom(chatRoom);
    message.setSender(sender);
    message.setContent(request.getContent());
    message.setSentAt(LocalDateTime.now());
    message.setType(ChatMessage.MessageType.TEXT);

    System.out.println("=== Saving message to database ===");
    message = chatMessageRepository.save(message);
    System.out.println("Message saved! ID: " + message.getId());

    // Atualizar timestamp da última mensagem na sala
    chatRoom.setLastMessageAt(message.getSentAt());
    chatRoomRepository.save(chatRoom);

    return convertToMessageDTO(message);
  }

  @Transactional
  public ChatMessageDTO sendMessageFromAny(SendMessageRequest request, ChatWebSocketController.MessageSender sender) {
    ChatRoom chatRoom =
        chatRoomRepository
            .findById(request.getChatRoomId())
            .orElseThrow(() -> new RuntimeException("Chat room not found"));

    ChatMessage message = new ChatMessage();
    message.setChatRoom(chatRoom);
    message.setContent(request.getContent());
    message.setSentAt(LocalDateTime.now());
    message.setType(ChatMessage.MessageType.TEXT);

    // Definir o remetente baseado no tipo
    if ("USER".equals(sender.type)) {
      User user = userRepository
          .findById(sender.id)
          .orElseThrow(() -> new RuntimeException("User not found"));
      
      // Verificar se o usuário é participante da sala
      boolean isParticipant =
          chatRoom.getParticipants().stream().anyMatch(p -> p.getId().equals(sender.id));
      if (!isParticipant) {
        throw new RuntimeException("User is not a participant of this chat room");
      }
      
      message.setSender(user);
      message.setGuestSender(null);
    } else if ("GUEST".equals(sender.type)) {
      GuestUser guestUser = guestUserRepository
          .findById(sender.id)
          .orElseThrow(() -> new RuntimeException("Guest user not found"));
      
      // Verificar se o guest está acessando seu próprio chat
      if (!chatRoom.getId().equals(guestUser.getChatRoom().getId())) {
        throw new RuntimeException("Guest user is not authorized to send messages to this chat room");
      }
      
      message.setSender(null);
      message.setGuestSender(guestUser);
    } else {
      throw new RuntimeException("Invalid sender type: " + sender.type);
    }

    System.out.println("=== Saving message from " + sender.type + " to database ===");
    message = chatMessageRepository.save(message);
    System.out.println("Message saved! ID: " + message.getId());

    // Atualizar timestamp da última mensagem na sala
    chatRoom.setLastMessageAt(message.getSentAt());
    chatRoomRepository.save(chatRoom);

    return convertToMessageDTO(message);
  }

  public List<ChatRoomDTO> getUserChatRooms(Long userId) {
    List<ChatRoom> chatRooms = chatRoomRepository.findByParticipantId(userId);
    return chatRooms.stream().map(this::convertToRoomDTO).collect(Collectors.toList());
  }

  public List<ChatRoomDTO> getActiveUserChatRooms(Long userId) {
    List<ChatRoom> chatRooms = chatRoomRepository.findByParticipantId(userId);
    return chatRooms.stream()
        .filter(ChatRoom::getIsActive)
        .map(this::convertToRoomDTO)
        .collect(Collectors.toList());
  }

  public List<ChatMessageDTO> getChatRoomMessages(Long chatRoomId, Long userId) {
    ChatRoom chatRoom =
        chatRoomRepository
            .findById(chatRoomId)
            .orElseThrow(() -> new RuntimeException("Chat room not found"));

    // Se userId for null, é um acesso de convidado - pular verificação
    if (userId != null) {
      // Verificar se o usuário é participante
      boolean isParticipant =
          chatRoom.getParticipants().stream().anyMatch(p -> p.getId().equals(userId));
      if (!isParticipant) {
        throw new RuntimeException("User is not a participant of this chat room");
      }
    }

    List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderBySentAtAsc(chatRoomId);
    return messages.stream().map(this::convertToMessageDTO).collect(Collectors.toList());
  }

  @Transactional
  public void markMessagesAsRead(Long chatRoomId, Long userId) {
    chatRoomRepository
        .findById(chatRoomId)
        .orElseThrow(() -> new RuntimeException("Chat room not found"));

    List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderBySentAtAsc(chatRoomId);
    for (ChatMessage message : messages) {
      // Se userId for null, é um guest - marcar todas as mensagens de usuários como lidas
      if (userId == null) {
        // Guest lendo mensagens - marcar mensagens de usuários como lidas
        if (message.getSender() != null && !message.getIsRead()) {
          message.setIsRead(true);
        }
      } else {
        // Usuário normal lendo - marcar mensagens que não são dele como lidas
        if (message.getSender() != null 
            && !message.getSender().getId().equals(userId) 
            && !message.getIsRead()) {
          message.setIsRead(true);
        }
        // Também marcar mensagens de guests como lidas
        if (message.getGuestSender() != null && !message.getIsRead()) {
          message.setIsRead(true);
        }
      }
    }
    chatMessageRepository.saveAll(messages);
  }

  public ChatRoomDTO getOrCreateDirectChat(Long user1Id, Long user2Id) {
    return chatRoomRepository
        .findDirectChatBetweenUsers(user1Id, user2Id)
        .map(this::convertToRoomDTO)
        .orElseGet(
            () -> {
              CreateChatRoomRequest request = new CreateChatRoomRequest();
              userRepository
                  .findById(user2Id)
                  .orElseThrow(() -> new RuntimeException("User not found"));
              request.setName("Direct Chat");
              request.setParticipantIds(List.of(user2Id));
              return createChatRoom(request, user1Id);
            });
  }

  @Transactional
  public void closeChatRoom(Long chatRoomId, Long userId) {
    ChatRoom chatRoom =
        chatRoomRepository
            .findById(chatRoomId)
            .orElseThrow(() -> new RuntimeException("Chat room not found"));

    // Verificar se o usuário é participante da sala
    boolean isParticipant =
        chatRoom.getParticipants().stream().anyMatch(p -> p.getId().equals(userId));
    if (!isParticipant) {
      throw new RuntimeException("User is not a participant of this chat room");
    }

    chatRoom.setIsActive(false);
    chatRoomRepository.save(chatRoom);
  }

  @Transactional
  public void reopenChatRoom(Long chatRoomId, Long userId) {
    ChatRoom chatRoom =
        chatRoomRepository
            .findById(chatRoomId)
            .orElseThrow(() -> new RuntimeException("Chat room not found"));

    // Verificar se o usuário é participante da sala
    boolean isParticipant =
        chatRoom.getParticipants().stream().anyMatch(p -> p.getId().equals(userId));
    if (!isParticipant) {
      throw new RuntimeException("User is not a participant of this chat room");
    }

    chatRoom.setIsActive(true);
    chatRoomRepository.save(chatRoom);
  }

  @Transactional
  public ChatInvitationDTO inviteGuestByEmail(InviteGuestRequest request, Long inviterId) {
    // Validar email
    if (request.getGuestEmail() == null || request.getGuestEmail().trim().isEmpty()) {
      throw new RuntimeException("Email do convidado é obrigatório");
    }

    User inviter =
        userRepository
            .findById(inviterId)
            .orElseThrow(() -> new RuntimeException("Inviter not found"));

    // Criar nova sala de chat para o convidado
    ChatRoom chatRoom = new ChatRoom();
    chatRoom.setName("Chat com " + (request.getGuestName() != null ? request.getGuestName() : request.getGuestEmail()));
    chatRoom.setCreatedAt(LocalDateTime.now());
    chatRoom.setIsActive(true);

    List<User> participants = new ArrayList<>();
    participants.add(inviter);
    chatRoom.setParticipants(participants);

    chatRoom = chatRoomRepository.save(chatRoom);

    // Criar convite com token único
    ChatInvitation invitation = new ChatInvitation();
    invitation.setToken(UUID.randomUUID().toString());
    invitation.setGuestEmail(request.getGuestEmail());
    invitation.setChatRoom(chatRoom);
    invitation.setInvitedBy(inviter);
    invitation.setCreatedAt(LocalDateTime.now());
    invitation.setExpiresAt(LocalDateTime.now().plusDays(7)); // Válido por 7 dias
    invitation.setIsUsed(false);

    invitation = chatInvitationRepository.save(invitation);

    // Enviar email com link
    String chatLink = frontendUrl + "/chat/join/" + invitation.getToken();
    emailService.sendChatInvitationEmail(
        request.getGuestEmail(), request.getGuestName(), inviter.getName(), chatLink);

    return convertToInvitationDTO(invitation);
  }

  @Transactional
  public JoinChatResponse joinChatByToken(String token) {
    ChatInvitation invitation =
        chatInvitationRepository
            .findValidInvitation(token, LocalDateTime.now())
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Convite inválido, expirado ou já utilizado"));

    if (invitation.getIsUsed()) {
      throw new RuntimeException("Este convite já foi utilizado");
    }

    // Marcar convite como usado
    invitation.setIsUsed(true);
    invitation.setUsedAt(LocalDateTime.now());
    chatInvitationRepository.save(invitation);

    // Retornar informações da sala
    JoinChatResponse response = new JoinChatResponse();
    response.setToken(token);
    response.setChatRoom(convertToRoomDTO(invitation.getChatRoom()));
    response.setGuestEmail(invitation.getGuestEmail());
    response.setMessage("Bem-vindo ao chat!");

    return response;
  }

  public ChatRoomDTO getChatRoomByToken(String token) {
    ChatInvitation invitation =
        chatInvitationRepository
            .findByToken(token)
            .orElseThrow(() -> new RuntimeException("Convite não encontrado"));

    if (!invitation.getIsUsed()) {
      throw new RuntimeException("Você precisa aceitar o convite primeiro");
    }

    return convertToRoomDTO(invitation.getChatRoom());
  }

  private ChatInvitationDTO convertToInvitationDTO(ChatInvitation invitation) {
    ChatInvitationDTO dto = new ChatInvitationDTO();
    dto.setId(invitation.getId());
    dto.setToken(invitation.getToken());
    dto.setGuestEmail(invitation.getGuestEmail());
    dto.setChatRoomId(invitation.getChatRoom().getId());
    dto.setChatRoomName(invitation.getChatRoom().getName());
    dto.setInvitedByName(invitation.getInvitedBy().getName());
    dto.setCreatedAt(invitation.getCreatedAt());
    dto.setExpiresAt(invitation.getExpiresAt());
    dto.setIsUsed(invitation.getIsUsed());
    return dto;
  }

  private ChatMessageDTO convertToMessageDTO(ChatMessage message) {
    ChatMessageDTO dto = new ChatMessageDTO();
    dto.setId(message.getId());
    dto.setChatRoomId(message.getChatRoom().getId());
    
    // Verificar se é um usuário normal ou guest
    if (message.getSender() != null) {
      dto.setSenderId(message.getSender().getId());
      dto.setSenderName(message.getSender().getName());
    } else if (message.getGuestSender() != null) {
      dto.setSenderId(message.getGuestSender().getId());
      dto.setSenderName(message.getGuestSender().getName() + " (Convidado)");
    } else {
      // Caso não tenha nenhum remetente (mensagens antigas/órfãs)
      dto.setSenderId(null);
      dto.setSenderName("Usuário Removido");
    }
    
    dto.setContent(message.getContent());
    dto.setSentAt(message.getSentAt());
    dto.setIsRead(message.getIsRead());
    dto.setType(message.getType().name());
    return dto;
  }

  private ChatRoomDTO convertToRoomDTO(ChatRoom chatRoom) {
    ChatRoomDTO dto = new ChatRoomDTO();
    dto.setId(chatRoom.getId());
    dto.setName(chatRoom.getName());
    dto.setCreatedAt(chatRoom.getCreatedAt());
    dto.setLastMessageAt(chatRoom.getLastMessageAt());
    dto.setIsActive(chatRoom.getIsActive());

    List<ChatUserDTO> participantDTOs =
        chatRoom.getParticipants().stream()
            .map(
                user -> {
                  ChatUserDTO userDTO = new ChatUserDTO();
                  userDTO.setId(user.getId());
                  userDTO.setName(user.getName());
                  userDTO.setEmail(user.getEmail());
                  return userDTO;
                })
            .collect(Collectors.toList());
    dto.setParticipants(participantDTOs);

    // Buscar última mensagem
    ChatMessage lastMessage =
        chatMessageRepository.findLastMessageByChatRoomId(chatRoom.getId());
    if (lastMessage != null) {
      dto.setLastMessage(convertToMessageDTO(lastMessage));
    }

    return dto;
  }
}
