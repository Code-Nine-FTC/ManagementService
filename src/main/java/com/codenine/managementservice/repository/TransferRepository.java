package com.codenine.managementservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codenine.managementservice.entity.Transfer;

public interface TransferRepository extends JpaRepository<Transfer, Long> {}
