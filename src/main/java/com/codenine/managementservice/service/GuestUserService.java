package com.codenine.managementservice.service;

import com.codenine.managementservice.dto.chat.*;
import com.codenine.managementservice.entity.ChatRoom;
import com.codenine.managementservice.entity.GuestUser;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.repository.ChatRoomRepository;
import com.codenine.managementservice.repository.GuestUserRepository;
import com.codenine.managementservice.repository.UserRepository;
import com.codenine.managementservice.security.JwtUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GuestUserService {

  private final GuestUserRepository guestUserRepository;
  private final ChatRoomRepository chatRoomRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;

  public GuestUserService(
      GuestUserRepository guestUserRepository,
      ChatRoomRepository chatRoomRepository,
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      JwtUtil jwtUtil) {
    this.guestUserRepository = guestUserRepository;
    this.chatRoomRepository = chatRoomRepository;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
  }

  @Transactional
  public ChatRoomDTO registerGuest(CreateGuestRequest request, Long createdByUserId) {
    System.out.println("=== INICIANDO REGISTRO DE GUEST ===");
    
    // Validar se já existe usuário com mesmo e-mail ou CPF
    System.out.println("Verificando email: " + request.getEmail());
    if (guestUserRepository.existsByEmail(request.getEmail())) {
      System.out.println("Email já existe!");
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Já existe um usuário cadastrado com este e-mail");
    }
    System.out.println("Email disponível");

    System.out.println("Verificando CPF: " + request.getCpf());
    if (guestUserRepository.existsByCpf(request.getCpf())) {
      System.out.println("CPF já existe!");
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Já existe um usuário cadastrado com este CPF");
    }
    System.out.println("CPF disponível");

    // Buscar o usuário que está criando o guest (ADMIN ou Farmácia)
    System.out.println("Buscando usuário criador ID: " + createdByUserId);
    User createdByUser =
        userRepository
            .findById(createdByUserId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    System.out.println("Usuário criador encontrado: " + createdByUser.getName());

    System.out.println("Usuário criador encontrado: " + createdByUser.getName());

    // Criar a sala de chat
    System.out.println("Criando sala de chat...");
    ChatRoom chatRoom = new ChatRoom();
    chatRoom.setName("Chat - " + request.getName());
    chatRoom.setCreatedAt(LocalDateTime.now());
    chatRoom.setIsActive(true);

    // Adicionar o criador como participante
    List<User> participants = new ArrayList<>();
    participants.add(createdByUser);
    chatRoom.setParticipants(participants);

    System.out.println("Salvando sala de chat...");
    chatRoom = chatRoomRepository.save(chatRoom);
    System.out.println("Sala de chat criada ID: " + chatRoom.getId());

    // Criar o usuário guest
    System.out.println("Criando guest user...");
    GuestUser guestUser =
        GuestUser.builder()
            .name(request.getName())
            .cpf(request.getCpf())
            .age(request.getAge())
            .gender(request.getGender())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .createdAt(LocalDateTime.now())
            .isActive(true)
            .chatRoom(chatRoom)
            .build();

    System.out.println("Salvando guest user...");
    guestUser = guestUserRepository.save(guestUser);
    System.out.println("Guest user criado ID: " + guestUser.getId());
    System.out.println("Verificando chat room após salvar guest: " + (guestUser.getChatRoom() != null ? guestUser.getChatRoom().getId() : "NULL"));

    // Converter para DTO e retornar
    System.out.println("Convertendo para DTO...");
    ChatRoomDTO result = convertToRoomDTO(chatRoom);
    System.out.println("=== REGISTRO CONCLUÍDO COM SUCESSO ===");
    return result;
  }

  @Transactional(readOnly = true)
  public GuestLoginResponse loginGuest(GuestLoginRequest request) {
    System.out.println("=== LOGIN GUEST ===");
    System.out.println("Email: " + request.getEmail());
    
    // Buscar o guest pelo e-mail
    GuestUser guestUser =
        guestUserRepository
            .findByEmail(request.getEmail())
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos"));

    System.out.println("Guest encontrado: " + guestUser.getName() + " (ID: " + guestUser.getId() + ")");
    System.out.println("Chat Room do guest: " + (guestUser.getChatRoom() != null ? guestUser.getChatRoom().getId() : "NULL"));
    
    // Verificar se está ativo
    if (!guestUser.getIsActive()) {
      System.out.println("Guest está INATIVO");
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário inativo");
    }

    // Verificar a senha
    if (!passwordEncoder.matches(request.getPassword(), guestUser.getPassword())) {
      System.out.println("Senha INCORRETA");
      throw new ResponseStatusException(
          HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos");
    }

    System.out.println("Senha CORRETA - gerando token...");
    // Gerar token JWT para o guest
    String token = jwtUtil.generateGuestToken(guestUser.getEmail(), guestUser.getId());

    // Converter para DTOs
    GuestUserDTO guestDTO = convertToDTO(guestUser);
    System.out.println("GuestUserDTO criado - Chat Room ID: " + guestDTO.getChatRoomId());
    
    ChatRoomDTO chatRoomDTO = null;

    if (guestUser.getChatRoom() != null) {
      System.out.println("Convertendo chat room para DTO...");
      chatRoomDTO = convertToRoomDTO(guestUser.getChatRoom());
      System.out.println("ChatRoomDTO criado - ID: " + chatRoomDTO.getId() + ", Nome: " + chatRoomDTO.getName());
    } else {
      System.out.println("AVISO: Guest não tem chat room associado!");
    }

    System.out.println("=== LOGIN CONCLUÍDO ===");
    return GuestLoginResponse.builder()
        .token(token)
        .user(guestDTO)
        .chatRoom(chatRoomDTO)
        .build();
  }

  @Transactional(readOnly = true)
  public GuestUserDTO getGuestByEmail(String email) {
    System.out.println("=== GET GUEST BY EMAIL ===");
    System.out.println("Email recebido: " + email);
    
    GuestUser guestUser =
        guestUserRepository
            .findByEmail(email)
            .orElseThrow(
                () -> {
                  System.out.println("ERRO: Guest user não encontrado com email: " + email);
                  return new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário guest não encontrado");
                });

    System.out.println("Guest encontrado: " + guestUser.getName() + " (ID: " + guestUser.getId() + ")");
    System.out.println("Chat Room ID: " + (guestUser.getChatRoom() != null ? guestUser.getChatRoom().getId() : "NULL"));
    
    GuestUserDTO dto = convertToDTO(guestUser);
    System.out.println("DTO criado - Chat Room ID no DTO: " + dto.getChatRoomId());
    
    return dto;
  }

  @Transactional(readOnly = true)
  public ChatRoomDTO getGuestChatRoom(Long guestId) {
    System.out.println("=== GET GUEST CHAT ROOM ===");
    System.out.println("Guest ID recebido: " + guestId);
    
    GuestUser guestUser =
        guestUserRepository
            .findById(guestId)
            .orElseThrow(
                () -> {
                  System.out.println("ERRO: Guest user não encontrado com ID: " + guestId);
                  return new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário guest não encontrado");
                });

    System.out.println("Guest encontrado: " + guestUser.getName());
    System.out.println("Chat Room do guest: " + (guestUser.getChatRoom() != null ? guestUser.getChatRoom().getId() : "NULL"));
    
    if (guestUser.getChatRoom() == null) {
      System.out.println("ERRO: Chat room é NULL para guest ID: " + guestId);
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Nenhuma sala de chat associada a este usuário");
    }

    System.out.println("Retornando chat room: " + guestUser.getChatRoom().getName());
    return convertToRoomDTO(guestUser.getChatRoom());
  }

  private GuestUserDTO convertToDTO(GuestUser guestUser) {
    return GuestUserDTO.builder()
        .id(guestUser.getId())
        .name(guestUser.getName())
        .cpf(guestUser.getCpf())
        .age(guestUser.getAge())
        .gender(guestUser.getGender())
        .email(guestUser.getEmail())
        .createdAt(guestUser.getCreatedAt())
        .isActive(guestUser.getIsActive())
        .chatRoomId(guestUser.getChatRoom() != null ? guestUser.getChatRoom().getId() : null)
        .build();
  }

  private ChatRoomDTO convertToRoomDTO(ChatRoom chatRoom) {
    ChatRoomDTO dto = new ChatRoomDTO();
    dto.setId(chatRoom.getId());
    dto.setName(chatRoom.getName());
    dto.setCreatedAt(chatRoom.getCreatedAt());
    dto.setLastMessageAt(chatRoom.getLastMessageAt());
    dto.setIsActive(chatRoom.getIsActive());

    if (chatRoom.getParticipants() != null) {
      List<ChatUserDTO> participantDTOs =
          chatRoom.getParticipants().stream()
              .map(
                  user -> {
                    ChatUserDTO participantDTO = new ChatUserDTO();
                    participantDTO.setId(user.getId());
                    participantDTO.setName(user.getName());
                    participantDTO.setEmail(user.getEmail());
                    return participantDTO;
                  })
              .toList();
      dto.setParticipants(participantDTOs);
    }

    return dto;
  }
}
