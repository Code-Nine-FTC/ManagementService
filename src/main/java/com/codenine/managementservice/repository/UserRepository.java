package com.codenine.managementservice.repository;

import com.codenine.managementservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
