package com.codenine.managementservice.inject;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.codenine.managementservice.dto.user.Role;
import com.codenine.managementservice.entity.Section;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.repository.UserRepository;
import com.codenine.managementservice.utils.NormalizeEmail;

@Component
public class UserInitializer {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public List<User> initialize(List<Section> sections) {
    List<User> users = new ArrayList<>();
    String[] names = {
      "Capitão Silva", "Sargento Souza", "Tenente Lima", "Soldado Pereira", "Major Costa",
      "Coronel Ramos", "Sargento Oliveira", "Soldado Santos", "Tenente Braga", "Capitão Almeida"
    };

    for (int i = 0; i < names.length; i++) {
      User user = new User();
      user.setName(names[i]);
      String email = names[i].toLowerCase().replace(" ", ".") + "@exercito.mil.br";
      user.setEmail(NormalizeEmail.normalize(email));
      user.setPassword(passwordEncoder.encode("senha" + (i + 1)));
      if (names[i].contains("Capitão")) {
        user.setRole(Role.ADMIN);
      } else if (names[i].contains("Tenente") || names[i].contains("Major")) {
        user.setRole(Role.MANAGER);
      } else {
        user.setRole(Role.ASSISTANT);
      }
      List<Section> userSections = new ArrayList<>();
      userSections.add(sections.get(i % sections.size()));
      user.setSections(userSections);
      users.add(user);
    }
    return userRepository.saveAll(users);
  }
}
