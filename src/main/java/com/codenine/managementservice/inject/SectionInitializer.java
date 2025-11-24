package com.codenine.managementservice.inject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.codenine.managementservice.dto.user.Role;
import com.codenine.managementservice.entity.Section;
import com.codenine.managementservice.entity.SectionType;
import com.codenine.managementservice.entity.User;
import com.codenine.managementservice.repository.SectionRepository;
import com.codenine.managementservice.repository.UserRepository;

@Component
public class SectionInitializer {

  private final SectionRepository sectionRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public SectionInitializer(
      SectionRepository sectionRepository,
      UserRepository userRepository,
      PasswordEncoder passwordEncoder) {
    this.sectionRepository = sectionRepository;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public Result initializeSectionsAndAdmin() {
    List<Section> sections = new ArrayList<>();

    Section almoxarifado = new Section();
    almoxarifado.setTitle("Almoxarifado");
    almoxarifado.setIsActive(true);
    almoxarifado.setSectionType(SectionType.STORAGE);
    almoxarifado.setCreatedAt(LocalDateTime.now());
    almoxarifado.setLastUpdate(LocalDateTime.now());

    Section farmacia = new Section();
    farmacia.setTitle("Farmácia");
    farmacia.setIsActive(true);
    farmacia.setSectionType(SectionType.STORAGE);
    farmacia.setCreatedAt(LocalDateTime.now());
    farmacia.setLastUpdate(LocalDateTime.now());

    sections.add(almoxarifado);
    sections.add(farmacia);
    sectionRepository.saveAll(sections);

    User adminUser = new User();
    adminUser.setName("Administrador CODE NINE");
    adminUser.setEmail("codenine@email.com");
    adminUser.setPassword(passwordEncoder.encode("codenine123"));
    adminUser.setRole(Role.ADMIN);
    adminUser.setSections(sections);
    userRepository.save(adminUser);

    almoxarifado.setLastUser(adminUser);
    almoxarifado.setLastUpdate(LocalDateTime.now());
    farmacia.setLastUser(adminUser);
    farmacia.setLastUpdate(LocalDateTime.now());
    sectionRepository.saveAll(sections);

    return new Result(sections, adminUser);
  }

  public record Result(List<Section> sections, User adminUser) {}
}
