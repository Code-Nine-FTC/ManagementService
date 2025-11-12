package com.codenine.managementservice.repository;

import com.codenine.managementservice.entity.GuestUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuestUserRepository extends JpaRepository<GuestUser, Long> {

  Optional<GuestUser> findByEmail(String email);

  Optional<GuestUser> findByCpf(String cpf);

  boolean existsByEmail(String email);

  boolean existsByCpf(String cpf);
}
