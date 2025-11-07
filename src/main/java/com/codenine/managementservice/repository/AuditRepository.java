package com.codenine.managementservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codenine.managementservice.entity.AuditLog;

public interface AuditRepository extends JpaRepository<AuditLog, Long> {}
