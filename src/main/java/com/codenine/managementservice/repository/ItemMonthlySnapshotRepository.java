package com.codenine.managementservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codenine.managementservice.entity.ItemMonthlySnapshot;

public interface ItemMonthlySnapshotRepository extends JpaRepository<ItemMonthlySnapshot, Long> {}
